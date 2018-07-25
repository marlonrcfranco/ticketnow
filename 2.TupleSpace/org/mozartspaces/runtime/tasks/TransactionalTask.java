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
package org.mozartspaces.runtime.tasks;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import net.jcip.annotations.NotThreadSafe;

import org.mozartspaces.capi3.Capi3;
import org.mozartspaces.capi3.InvalidTransactionException;
import org.mozartspaces.capi3.SubTransaction;
import org.mozartspaces.capi3.Transaction;
import org.mozartspaces.core.MzsTimeoutException;
import org.mozartspaces.core.RequestMessage;
import org.mozartspaces.core.TransactionReference;
import org.mozartspaces.core.MzsConstants.TransactionTimeout;
import org.mozartspaces.core.requests.TransactionalRequest;
import org.mozartspaces.runtime.RuntimeData;
import org.mozartspaces.runtime.TransactionManager;
import org.mozartspaces.runtime.util.EntryCopier;
import org.mozartspaces.runtime.util.EntryCopyingException;
import org.mozartspaces.runtime.util.RuntimeUtils;
import org.mozartspaces.util.LoggerFactory;
import org.slf4j.Logger;

/**
 * The implementation of functionality for all transactional tasks.
 *
 * @author Tobias Doenz
 *
 * @param <R>
 *            the result type of the request that is processed by this task
 */
@NotThreadSafe
public abstract class TransactionalTask<R extends Serializable> extends AbstractTask<R> {

    private static final Logger log = LoggerFactory.get();

    private final TransactionalRequest<R> request;
    private final Capi3 capi3;
    private final TransactionManager txManager;
    private final EntryCopier entryCopier;
    private final RuntimeUtils utils;

    private TransactionReference txRef;
    private boolean autocommit;

    @SuppressWarnings("unchecked")
    protected TransactionalTask(final RequestMessage requestMessage, final RuntimeData runtimeData) {
        super(requestMessage, runtimeData);
        this.request = (TransactionalRequest<R>) requestMessage.getContent();
        this.capi3 = runtimeData.getCapi3();
        this.txManager = runtimeData.getTxManager();
        this.entryCopier = runtimeData.getEntryCopier();
        this.utils = runtimeData.getRuntimeUtils();
    }

    @Override
    protected final R runSpecific() throws Throwable {
        Transaction tx = getOrCreateTransaction();
        setTransactionData(txRef, tx);
        SubTransaction stx = createSubTransaction(tx);
        // TODO improve handling of InvalidTransactionException (e.g. when prepared tx is used)

        R result = null;
        try {
            result = runInSubtransaction(tx, stx);
            // TODO catch exception directly after this method call
            // TODO try to throw original exception and not follow-up exceptions
            if (result != null) {
                // status OK (exception will be thrown for NOTOK)
                stx.commit();
                stx = null;
                if (autocommit) {
                    txManager.commitTransaction(txRef, true);
                    log.debug("Comitted implicit transaction");
                    setTransactionData(null, null);
                }
            } else {
                // status LOCKED or DELAYABLE
                // do not commit implicit tx here, only after OK/NOTOK
                rollbackSubTxAndImplicitTx(stx, tx, false);
            }
        } catch (InvalidTransactionException ex) {
            if (stx != null && result != null) {
                log.error("Could not commit sub-transaction: ", ex);
                rollbackImplicitTx(tx, autocommit);
            }
            throw ex;
        } catch (Throwable ex) {
            // logging in AbstractTask should be sufficient
            //log.debug("Exception inside sub-transaction: ", ex);
            rollbackSubTxAndImplicitTx(stx, tx, autocommit);
            throw ex;
        }

        return result;
    }

    private Transaction getOrCreateTransaction() throws MzsTimeoutException {
        if (request.getTransaction() != null) {
            // explicit transaction
            txRef = request.getTransaction();
            return txManager.getTransaction(txRef);
        }
        if (txRef != null) {
            // rescheduled task with implicit tx
            return txManager.getTransaction(txRef);
        }
        // create new implicit transaction
        Transaction tx = capi3.newTransaction();
        autocommit = true;
        txRef = utils.createTransactionReference(tx);
        log.debug("Created implicit transaction {}", txRef);
        txManager.addTransaction(txRef, tx, TransactionTimeout.INFINITE);
        return tx;
    }

    private SubTransaction createSubTransaction(final Transaction tx) throws InvalidTransactionException {
        SubTransaction stx;
        try {
            stx = tx.newSubTransaction();
        } catch (InvalidTransactionException ex) {
            log.error("Could not create sub-transaction: ", ex);
            rollbackImplicitTx(tx, autocommit);
            throw ex;
        }
        return stx;
    }

    private void rollbackSubTxAndImplicitTx(final SubTransaction stx, final Transaction tx, final boolean autocommit)
            throws InvalidTransactionException {
        try {
            stx.rollback();
            log.debug("Rollbacked sub-transaction");
        } catch (InvalidTransactionException ex1) {
            log.error("Could not rollback sub-transaction: ", ex1);
        } finally {
            rollbackImplicitTx(tx, autocommit);
        }
    }

    private void rollbackImplicitTx(final Transaction tx, final boolean autocommit) throws InvalidTransactionException {
        if (autocommit) {
            try {
                try {
                    txManager.rollbackTransaction(txRef, true);
                } catch (MzsTimeoutException ex) {
                    // should not happen at all (implicit tx have timeout INFINITE)
                    log.error("Implicit transaction timed out", ex);
                }
                log.debug("Rollbacked implicit transaction");
                setTransactionData(null, null);
            } catch (InvalidTransactionException ex1) {
                log.error("Could not rollback implicit transaction: ", ex1);
                throw ex1;
            }
        }
    }

    protected abstract R runInSubtransaction(Transaction tx, SubTransaction stx) throws Throwable;

    // used in ReadEntriesTask and TakeEntriesTask
    @SuppressWarnings("unchecked")
    protected final ArrayList<Serializable> prepareEntriesForResponse(
            final List<? extends Serializable> entries) throws EntryCopyingException {

        List<? extends Serializable> responseEntries;
        if (entries instanceof ArrayList<?>) {
            responseEntries = entries;
        } else {
            responseEntries = new ArrayList<Serializable>(entries);
        }
        if (entryCopier != null) {
            responseEntries = entryCopier.copyEntryValues(responseEntries);
        }
        return (ArrayList<Serializable>) responseEntries;
    }

}
