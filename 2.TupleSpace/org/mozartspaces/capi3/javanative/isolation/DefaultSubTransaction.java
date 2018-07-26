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
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import net.jcip.annotations.NotThreadSafe;

import org.mozartspaces.capi3.InvalidTransactionException;
import org.mozartspaces.capi3.LocalContainerReference;
import org.mozartspaces.capi3.LogItem;
import org.mozartspaces.capi3.TransactionStatus;

/**
 * A <code>DefaultSubTransaction</code> is always associated with a parent
 * <code>Transaction</code>.
 *
 * @author Martin Barisits
 * @author Tobias Doenz
 */
@NotThreadSafe
public final class DefaultSubTransaction implements NativeSubTransaction {

    private final String id;
    private final NativeTransaction parentTransaction;
    private final AtomicBoolean active;

    private final List<LogItem> insertLog;
    private final List<LogItem> readLog;
    private final List<LogItem> deleteLog;
    private final List<LogItem> lockLog;
    private final List<LogItem> otherLog;

    private final Collection<LocalContainerReference> containers;

    private volatile TransactionStatus status;

    /**
     * Creates a DefaultSubTransaction.
     *
     * @param tx
     *            the parent Transaction
     * @param stxId
     *            the SubTransaction unique Id
     */
    DefaultSubTransaction(final NativeTransaction tx, final long stxId) {
        if (tx == null) {
            throw new NullPointerException("The Transaction must be supplied");
        }
        if (stxId < 0) {
            throw new IllegalArgumentException("The SubTransactionId must be bigger than 0");
        }
        this.id = tx.getId() + stxId;
        this.parentTransaction = tx;
        this.active = new AtomicBoolean(true);
        this.status = TransactionStatus.RUNNING;

        this.insertLog = new ArrayList<LogItem>();
        this.readLog = new ArrayList<LogItem>();
        this.deleteLog = new ArrayList<LogItem>();
        this.lockLog = new ArrayList<LogItem>();
        this.otherLog = new ArrayList<LogItem>();

        this.containers = new ArrayList<LocalContainerReference>();
    }

    @Override
    public String getId() {
        return this.id;
    }

    @Override
    public NativeTransaction getParent() {
        return this.parentTransaction;
    }

    @Override
    public void commit() throws InvalidTransactionException {
        if (this.active.getAndSet(false)) {
            this.status = TransactionStatus.COMMITING;
            for (LogItem logItem : this.insertLog) {
                logItem.commitSubTransaction();
            }
            for (LogItem logItem : this.readLog) {
                logItem.commitSubTransaction();
            }
            for (LogItem logItem : this.deleteLog) {
                logItem.commitSubTransaction();
            }
            for (LogItem logItem : this.lockLog) {
                logItem.commitSubTransaction();
            }
            for (LogItem logItem : this.otherLog) {
                logItem.commitSubTransaction();
            }
            this.status = TransactionStatus.COMMITED;
            this.parentTransaction.subTransactionFinished();
            return;
        }
        throw new InvalidTransactionException(this.getId());
    }

    @Override
    public void rollback() throws InvalidTransactionException {
        if (this.active.getAndSet(false)) {
            this.status = TransactionStatus.ABORTING;
            for (LogItem logItem : this.deleteLog) {
                logItem.rollbackSubTransaction();
            }
            for (LogItem logItem : this.readLog) {
                logItem.rollbackSubTransaction();
            }
            for (LogItem logItem : this.insertLog) {
                logItem.rollbackSubTransaction();
            }
            for (LogItem logItem : this.lockLog) {
                logItem.rollbackSubTransaction();
            }
            for (LogItem logItem : this.otherLog) {
                logItem.rollbackSubTransaction();
            }
            this.status = TransactionStatus.ABORTED;
            this.parentTransaction.subTransactionFinished();
            return;
        }
        throw new InvalidTransactionException(this.getId());
    }

    @Override
    public boolean equals(final Object obj) {
        return (obj instanceof NativeSubTransaction) && (this.id.equals(((NativeSubTransaction) obj).getId()));
    }

    @Override
    public int hashCode() {
        int result = 17;
        result = 31 * result + id.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "DefaultSubTransaction [id=" + id + ", parentTransactionId=" + parentTransaction.getId() + ", active="
                + active + ", status=" + status + ", insertLog=" + insertLog + ", readLog=" + readLog + ", deleteLog="
                + deleteLog + ", lockLog=" + lockLog + ", otherLog=" + otherLog + ", containers=" + containers + "]";
    }

    @Override
    public TransactionStatus getStatus() {
        return this.status;
    }

    /**
     * Return the log of all DeleteLogItems.
     *
     * @return Log
     */
    List<LogItem> getDeleteLog() {
        return this.deleteLog;
    }

    /**
     * Return the log of all InsertLogItems.
     *
     * @return Log
     */
    List<LogItem> getInsertLog() {
        return this.insertLog;
    }

    /**
     * Return the log of all ReadLogItems.
     *
     * @return Log
     */
    List<LogItem> getReadLog() {
        return this.readLog;
    }

    /**
     * Return the log of all LockLogItems.
     *
     * @return Log
     */
    List<LogItem> getLockLog() {
        return this.lockLog;
    }

    /**
     * Return the log of all other LogItems.
     *
     * @return Log
     */
    List<LogItem> getOtherLog() {
        return this.otherLog;
    }

    Collection<LocalContainerReference> getAccessedContainers() {
        return containers;
    }

    @Override
    public void addLog(final LogItem logItem) {
        if (logItem == null) {
            throw new NullPointerException("The logItem must not be null");
        }
        if (logItem instanceof DefaultLogItem) {
            containers.add(((DefaultLogItem) logItem).getContainerReference());
        }
        if (logItem instanceof DefaultWriteLogItem) {
            this.insertLog.add(logItem);
            return;
        }
        if (logItem instanceof DefaultContainerCreateLogItem) {
            // creating containers must go to the otherLog because:
            // when you create a container and write some entries and then rollback the transaction
            // then the removing of the entries from the insertLog must happen first and
            // the destroying of the container must happen last
            // if it was the other way around you would destroy the container (with all its StoredMaps)
            // and then try to remove the entries from it (from the already destroyed StoredMap)
            this.otherLog.add(logItem);
            return;
        }
        if (logItem instanceof DefaultReadLogItem) {
            this.readLog.add(logItem);
            return;
        }
        if (logItem instanceof DefaultTakeLogItem) {
            this.deleteLog.add(logItem);
            return;
        }
        if (logItem instanceof DefaultContainerDestroyLogItem) {
            this.deleteLog.add(logItem);
            return;
        }
        if (logItem instanceof DefaultContainerLockLogItem) {
            this.lockLog.add(logItem);
            return;
        }
        this.otherLog.add(logItem);
    }

    @Override
    public boolean isValid() {
        return getStatus() != TransactionStatus.COMMITED && getStatus() != TransactionStatus.ABORTED;
    }
}
