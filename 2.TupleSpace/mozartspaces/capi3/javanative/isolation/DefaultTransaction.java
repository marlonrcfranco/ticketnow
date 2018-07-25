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
package org.mozartspaces.capi3.javanative.isolation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import net.jcip.annotations.ThreadSafe;

import org.mozartspaces.capi3.InvalidTransactionException;
import org.mozartspaces.capi3.LocalContainerReference;
import org.mozartspaces.capi3.LogItem;
import org.mozartspaces.capi3.SubTransaction;
import org.mozartspaces.capi3.TransactionStatus;
import org.mozartspaces.core.MzsCoreRuntimeException;

/**
 * Implementation of an XVSM transaction.
 *
 * When sub-transactions are used concurrently, the method {@link #lockAndWaitForSubTransactions()} should be used to
 * wait for all sub-transactions to finish before {@link #commit()} or {@link #rollback()} is called.
 *
 * @author Martin Barisits
 * @author Tobias Doenz
 */
@ThreadSafe
public final class DefaultTransaction implements NativeTransaction {

    private final String id;
    private final List<NativeSubTransaction> subTransactions;
    private final List<LogItem> otherLog;
    private final AtomicBoolean active;
    private final AtomicLong stxCounter;

    private volatile TransactionStatus status;
    private volatile boolean locked;

    /**
     * The number of currently not finished sub transactions.
     */
    private final AtomicInteger notFinishedStxCounter;
    private CountDownLatch allSubTransactionsFinished;

    /**
     * Creates a new transaction.
     *
     * @param id
     *            the transaction id
     */
    public DefaultTransaction(final long id) {
        this.id = String.valueOf(id);
        this.subTransactions = Collections.synchronizedList(new ArrayList<NativeSubTransaction>());
        this.otherLog = new ArrayList<LogItem>();
        this.active = new AtomicBoolean(true);
        this.stxCounter = new AtomicLong();

        this.status = TransactionStatus.RUNNING;

        this.notFinishedStxCounter = new AtomicInteger();
    }

    @Override
    public synchronized void commit() throws InvalidTransactionException {

        if (!this.active.getAndSet(false)) {
            throw new InvalidTransactionException(this.getId());
        }

        List<NativeSubTransaction> stxToCommit = checkSubTransactionStatus();
        this.status = TransactionStatus.COMMITING;

        /* Commit Delete Log */
        for (NativeSubTransaction stx : stxToCommit) {
            DefaultSubTransaction dStx = (DefaultSubTransaction) stx;
            for (LogItem l : dStx.getDeleteLog()) {
                l.commitTransaction();
            }
        }

        /* Commit Insert Log */
        for (NativeSubTransaction stx : stxToCommit) {
            DefaultSubTransaction dStx = (DefaultSubTransaction) stx;
            for (LogItem l : dStx.getInsertLog()) {
                l.commitTransaction();
            }
        }

        /* Commit Read Log */
        for (NativeSubTransaction stx : stxToCommit) {
            DefaultSubTransaction dStx = (DefaultSubTransaction) stx;
            for (LogItem l : dStx.getReadLog()) {
                l.commitTransaction();
            }
        }

        /* Commit Lock Log */
        for (NativeSubTransaction stx : stxToCommit) {
            DefaultSubTransaction dStx = (DefaultSubTransaction) stx;
            for (LogItem l : dStx.getLockLog()) {
                l.commitTransaction();
            }
        }

        /* Commit Sub-Transations' Other Log */
        for (NativeSubTransaction stx : stxToCommit) {
            DefaultSubTransaction dStx = (DefaultSubTransaction) stx;
            for (LogItem l : dStx.getOtherLog()) {
                l.commitTransaction();
            }
        }

        /* Commit Transaction's Other Log */
        for (LogItem l : this.otherLog) {
            l.commitTransaction();
        }

        this.status = TransactionStatus.COMMITED;

    }

    private List<NativeSubTransaction> checkSubTransactionStatus() {
        List<NativeSubTransaction> committedStx = new ArrayList<NativeSubTransaction>();
        for (NativeSubTransaction stx : this.subTransactions) {
            switch (stx.getStatus()) {
            case COMMITED:
                committedStx.add(stx);
                break;
            case ABORTED:
                // ignore STX, it has already been rolled back
                break;
            case RUNNING:
            case COMMITING:
            case ABORTING:
                // this should not happen when lockAndWaitForSubTransaction is called before commit/rollback!
                throw new MzsCoreRuntimeException("Sub-transaction " + stx.getId() + " is " + stx.getStatus()
                        + " (notFinishedStxCounter=" + notFinishedStxCounter + ", latch=" + allSubTransactionsFinished);
                // TODO throw more specific exception?
            default:
                throw new AssertionError("STX status " + stx.getStatus());
            }
        }
        return committedStx;
    }

    @Override
    public synchronized void rollback() throws InvalidTransactionException {

        if (!this.active.getAndSet(false)) {
            throw new InvalidTransactionException(this.getId());
        }

        List<NativeSubTransaction> stxToRollback = checkSubTransactionStatus();
        this.status = TransactionStatus.ABORTING;

        /* Rollback Delete Log */
        for (NativeSubTransaction stx : stxToRollback) {
            DefaultSubTransaction dStx = (DefaultSubTransaction) stx;
            for (LogItem l : dStx.getDeleteLog()) {
                l.rollbackTransaction();
            }
        }

        /* Rollback Read Log */
        for (NativeSubTransaction stx : stxToRollback) {
            DefaultSubTransaction dStx = (DefaultSubTransaction) stx;
            for (LogItem l : dStx.getReadLog()) {
                l.rollbackTransaction();
            }
        }

        /* Rollback Insert Log */
        for (NativeSubTransaction stx : stxToRollback) {
            DefaultSubTransaction dStx = (DefaultSubTransaction) stx;
            for (LogItem l : dStx.getInsertLog()) {
                l.rollbackTransaction();
            }
        }

        /* Rollback Lock Log */
        for (NativeSubTransaction stx : stxToRollback) {
            DefaultSubTransaction dStx = (DefaultSubTransaction) stx;
            for (LogItem l : dStx.getLockLog()) {
                l.rollbackTransaction();
            }
        }

        /* Rollback Sub-Transactions' Other Log */
        for (NativeSubTransaction stx : stxToRollback) {
            DefaultSubTransaction dStx = (DefaultSubTransaction) stx;
            for (LogItem l : dStx.getOtherLog()) {
                l.rollbackTransaction();
            }
        }

        /* Rollback Transaction's Other Log */
        for (LogItem l : this.otherLog) {
            l.rollbackTransaction();
        }

        this.status = TransactionStatus.ABORTED;

    }

    @Override
    public String getId() {
        return this.id;
    }

    @Override
    public synchronized SubTransaction newSubTransaction() throws InvalidTransactionException {
        if (!active.get() || locked) {
            throw new InvalidTransactionException(this.getId());
        }
        NativeSubTransaction stx = new DefaultSubTransaction(this, this.stxCounter.incrementAndGet());
        this.subTransactions.add(stx);
        notFinishedStxCounter.incrementAndGet();
        return stx;
    }

    @Override
    public boolean equals(final Object obj) {
        return (obj instanceof NativeTransaction) && (this.id.equals(((NativeTransaction) obj).getId()));
    }

    @Override
    public int hashCode() {
        int result = 17;
        result = 31 * result + id.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "DefaultTransaction [id=" + id + ", active=" + active + ", stxCounter=" + stxCounter + ", status="
                + status + ", locked=" + locked + ", notFinishedStxCounter=" + notFinishedStxCounter
                + ", subTransactions=" + subTransactions + ", otherLog=" + otherLog + ", allSubTransactionsFinished="
                + allSubTransactionsFinished + "]";
    }

    @Override
    public synchronized List<LogItem> getLog() {
        ArrayList<LogItem> log = new ArrayList<LogItem>();
        for (NativeSubTransaction stx : this.subTransactions) {
            log.addAll(((DefaultSubTransaction) stx).getInsertLog());
            log.addAll(((DefaultSubTransaction) stx).getReadLog());
            log.addAll(((DefaultSubTransaction) stx).getDeleteLog());
            log.addAll(((DefaultSubTransaction) stx).getLockLog());
            log.addAll(((DefaultSubTransaction) stx).getOtherLog());
        }
        log.addAll(this.otherLog);
        return log;
    }

    // TODO use set or list?
    @Override
    public synchronized Collection<LocalContainerReference> getAccessedContainers() {
        Collection<LocalContainerReference> containers = new HashSet<LocalContainerReference>();
        for (NativeSubTransaction stx : this.subTransactions) {
            containers.addAll(((DefaultSubTransaction) stx).getAccessedContainers());
        }
        return containers;
    }

    @Override
    public TransactionStatus getStatus() {
        return this.status;
    }

    @Override
    public synchronized void addLog(final LogItem logItem) throws InvalidTransactionException {
        if (logItem == null) {
            throw new NullPointerException("The logItem must not be null");
        }
        this.otherLog.add(logItem);
    }

    @Override
    public boolean isValid() {
        return getStatus() != TransactionStatus.COMMITED && getStatus() != TransactionStatus.ABORTED;
    }

    @Override
    public void lockAndWaitForSubTransactions() throws InterruptedException {
        this.locked = true;
        // wait for all STX to finish, create latch and wait on it only if there are open STX
        synchronized (this) {
            if (this.notFinishedStxCounter.get() > 0 && allSubTransactionsFinished == null) {
                allSubTransactionsFinished = new CountDownLatch(1);
            }
        }
        if (allSubTransactionsFinished != null) {
            // TODO use timeout to avoid infinite blocking
            allSubTransactionsFinished.await();
        }
    }

    @Override
    public boolean isLocked() {
        return this.locked;
    }

    @Override
    public synchronized void subTransactionFinished() {
        if (this.notFinishedStxCounter.decrementAndGet() == 0) {
            // the last stx to finish counts down the latch and triggers the waiting commit/rollback
            // (see lockAndWaitForSubTransactions)
            if (allSubTransactionsFinished != null) {
                allSubTransactionsFinished.countDown();
            }
        }
    }

}
