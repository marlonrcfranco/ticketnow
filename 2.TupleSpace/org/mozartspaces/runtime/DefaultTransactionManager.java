/**
 * MozartSpaces - Java implementation of Extensible Virtual Shared Memory (XVSM)
 * Copyright 2009-2013 Space Based Computing Group, eva Kuehn, E185/1, TU Vienna
 * Visit http://www.mozartspaces.org for more information.
 *
 * MozartSpaces is free software: you can redistribute it and/or
 * modify it under the terms of version 3 of the GNU Affero General
 * Public License as published by the Free Software Foundation.
 *
 * MozartSpaces is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General
 * Public License along with MozartSpaces. If not, see
 * <http://www.gnu.org/licenses/>.
 */
package org.mozartspaces.runtime;

import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentHashMap;

import net.jcip.annotations.ThreadSafe;

import org.mozartspaces.capi3.InvalidTransactionException;
import org.mozartspaces.capi3.Transaction;
import org.mozartspaces.core.MzsConstants.TransactionTimeout;
import org.mozartspaces.core.MzsCore;
import org.mozartspaces.core.MzsTimeoutException;
import org.mozartspaces.core.Request;
import org.mozartspaces.core.TransactionException;
import org.mozartspaces.core.TransactionReference;
import org.mozartspaces.core.requests.RollbackTransactionRequest;
import org.mozartspaces.core.util.CoreUtils;
import org.mozartspaces.runtime.blocking.ExpiringElement;
import org.mozartspaces.runtime.blocking.NonPollingTimeoutProcessor;
import org.mozartspaces.runtime.blocking.TimeoutHandler;
import org.mozartspaces.runtime.blocking.TimeoutProcessor;
import org.mozartspaces.runtime.blocking.WaitAndEventManager;
import org.mozartspaces.util.LoggerFactory;
import org.slf4j.Logger;

/**
 * Manages the currently active explicit transactions in the core.
 *
 * This class has the mapping between transaction references from the core API
 * and transaction objects used in CAPI-3 and the Runtime. This class has an
 * internal timeout processor, which is used to detect when a transaction times
 * out.
 *
 * @author Tobias Doenz
 *
 * @see Transaction
 * @see TimeoutProcessor
 */
@ThreadSafe
public final class DefaultTransactionManager implements TransactionManager {

    private static final Logger log = LoggerFactory.get();

    private final CoreUtils coreUtils;

    private final Map<TransactionReference, Transaction> transactions;
    private final Map<TransactionReference, Transaction> timedOutTransactions;
    private final TransactionTimeoutHandler timeoutHandler;
    private final TimeoutProcessor<TransactionReference> timeoutProcessor;

    private volatile WaitAndEventManager waitEventManager;
    private volatile MzsCore core;

    /**
     * Constructs a <code>DefaultTransactionManager</code>.
     *
     * @param coreUtils
     *            core helper functions
     */
    public DefaultTransactionManager(final CoreUtils coreUtils) {
        this.coreUtils = coreUtils;
        assert this.coreUtils != null;

        transactions = new ConcurrentHashMap<TransactionReference, Transaction>();
        timedOutTransactions = Collections.synchronizedMap(new WeakHashMap<TransactionReference, Transaction>());
        timeoutHandler = new TransactionTimeoutHandler();
        // TODO make TimeoutProcessor implementation configurable
        timeoutProcessor = new NonPollingTimeoutProcessor<TransactionReference>("Transaction-TP");
        timeoutProcessor.setTimeoutHandler(timeoutHandler);
    }

    /**
     * Sets the wait and event manager (dependency injection). It is used when
     * transaction are committed or rolled back.
     *
     * @param waitEventManager
     *            the wait and event manager
     */
    public void setWaitEventManager(final WaitAndEventManager waitEventManager) {
        this.waitEventManager = waitEventManager;
        assert this.waitEventManager != null;
    }

    /**
     * Sets a reference to the core this transaction manager is part of. It is
     * used to send rollback requests when a transaction times out.
     *
     * @param core
     *            the core
     */
    public void setCore(final MzsCore core) {
        this.core = core;
        assert this.core != null;
    }

    @Override
    public int getNumberOfTransactions() {
        return transactions.size();
    }

    @Override
    public void addTransaction(final TransactionReference txRef, final Transaction tx,
            final long timeoutInMilliseconds) {
        transactions.put(txRef, tx);
        if (timeoutInMilliseconds != TransactionTimeout.INFINITE) {
            long timestamp = System.nanoTime();
            long expireTime = timestamp + timeoutInMilliseconds * 1000000;
            log.debug("Adding transaction to timeout processor");
            ExpiringElement<TransactionReference> expTx = new ExpiringElement<TransactionReference>(txRef, expireTime);
            timeoutProcessor.addElement(expTx);
        }
    }

    @Override
    public Transaction getTransaction(final TransactionReference txRef) throws MzsTimeoutException {
        Transaction tx = transactions.get(txRef);
        checkTransaction(txRef, tx);
        return tx;
    }

    @Override
    public void commitTransaction(final TransactionReference txRef, final boolean implicitTx)
            throws InvalidTransactionException, MzsTimeoutException {
        assert txRef != null;
        log.debug("Comitting transaction {}", txRef);
        Transaction tx = removeTransaction(txRef, implicitTx);
        commitTx(txRef, tx, implicitTx);
    }

    @Override
    public void rollbackTransaction(final TransactionReference txRef, final boolean implicitTx)
            throws InvalidTransactionException, MzsTimeoutException {
        assert txRef != null;
        log.debug("Rollbacking transaction {}", txRef);
        Transaction tx = removeTransaction(txRef, implicitTx);
        rollbackTx(txRef, tx, implicitTx);
    }

    private Transaction removeTransaction(final TransactionReference txRef, final boolean implicitTx)
            throws MzsTimeoutException {
        Transaction tx = transactions.remove(txRef);
        checkTransaction(txRef, tx);
        if (!implicitTx) {
            // dirty hack below (could be "fixed" with extra map)
            ExpiringElement<TransactionReference> expTx = new ExpiringElement<TransactionReference>(txRef, 0);
            timeoutProcessor.removeElement(expTx);
        }
        return tx;
    }

    @Override
    public synchronized void shutdown() {
        log.debug("Shutting down the Transaction Manager");
        timeoutProcessor.shutdown();
        for (TransactionReference txRef : transactions.keySet()) {
            try {
                rollbackTx(txRef, transactions.get(txRef), false);
            } catch (InvalidTransactionException ex) {
                log.error("Could not rollback transaction: ", ex);
            }
        }
        transactions.clear();
    }

    private void checkTransaction(final TransactionReference txRef, final Transaction tx) throws MzsTimeoutException {

        assert txRef != null;
        if (tx == null) {
            Transaction timedOutTx = timedOutTransactions.get(txRef);
            if (timedOutTx != null) {
                // not reliable, because timedOutTransactions is a WeakHashMap
                throw new MzsTimeoutException("Transaction " + txRef + " timed out");
            }
            if (!coreUtils.isEmbeddedSpace(txRef.getSpace())) {
                throw new TransactionException("Transaction " + txRef + " is not from this core");
            }
            throw new TransactionException("No transaction for reference " + txRef);
        }
    }

    private void commitTx(final TransactionReference txRef, final Transaction tx, final boolean implicitTx)
            throws InvalidTransactionException {

        assert txRef != null;
        assert tx != null;
        try {
            lockTxAndWaitForSubTransactions(tx);
            tx.commit();
            log.debug("Committed transaction {}", txRef);
        } catch (InvalidTransactionException ex) {
            log.error("Could not commit transaction: {}", ex.toString());
            throw ex;
        } finally {
            long eventTime = System.nanoTime();
            waitEventManager.processTransactionCommit(txRef, tx, eventTime);
        }
    }

    private void rollbackTx(final TransactionReference txRef, final Transaction tx, final boolean implicitTx)
            throws InvalidTransactionException {

        assert txRef != null;
        assert tx != null;
        try {
            lockTxAndWaitForSubTransactions(tx);
            tx.rollback();
            log.debug("Rollbacked transaction {}", txRef);
        } catch (InvalidTransactionException ex) {
            log.error("Could not rollback transaction: {}", ex.toString());
            throw ex;
        } finally {
            long eventTime = System.nanoTime();
            waitEventManager.processTransactionRollback(txRef, tx, eventTime);
        }
    }

    private void lockTxAndWaitForSubTransactions(final Transaction tx) {
        try {
            tx.lockAndWaitForSubTransactions();
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new TransactionException("Interrupted while waiting for sub-transactions: " + ex);
        }
    }

    /**
     * The timeout handler for transactions.
     *
     * @author Tobias Doenz
     */
    private class TransactionTimeoutHandler implements TimeoutHandler<TransactionReference> {

        @Override
        public void elementTimedOut(final TransactionReference txRef) {
            assert txRef != null;
            log.info("Transaction {} timed out", txRef);
            Transaction tx = transactions.get(txRef);
            if (tx == null) {
                log.info("Transaction object not found. Concurrent commit/rollback?");
                return;
            }

            timedOutTransactions.put(txRef, tx);

            // send the rollback request via the core API so that aspects are
            // also called
            Request<?> rollbackRequest = new RollbackTransactionRequest(txRef, null);
            core.send(rollbackRequest, null);
        }

    }

}
