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

import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import org.mozartspaces.capi3.InvalidContainerException;
import org.mozartspaces.capi3.InvalidEntryException;
import org.mozartspaces.capi3.IsolationLevel;
import org.mozartspaces.capi3.LocalContainerReference;
import org.mozartspaces.capi3.OperationType;
import org.mozartspaces.capi3.javanative.isolation.Availability.AvailabilityType;
import org.mozartspaces.capi3.javanative.operation.NativeContainer;
import org.mozartspaces.capi3.javanative.operation.NativeContainerManager;
import org.mozartspaces.capi3.javanative.operation.NativeEntry;
import org.mozartspaces.core.RequestContext;

/**
 * The IsolationManager is used to organize all Entry- and Container-Level
 * isolation and transactional behavior. It administrates all pessimistic locks
 * in the system.
 *
 * @author Martin Barisits
 * @author Tobias Doenz
 */
public final class DefaultIsolationManager implements NativeIsolationManager {

    private final ConcurrentHashMap<LocalContainerReference, NativeLock> containerLocks;
    private final ConcurrentHashMap<NativeEntry, NativeLock> entryLocks;

    /**
     * Creates a DefaultIsolationManager.
     */
    public DefaultIsolationManager() {
        this.entryLocks = new ConcurrentHashMap<NativeEntry, NativeLock>();
        this.containerLocks = new ConcurrentHashMap<LocalContainerReference, NativeLock>();
    }

    @Override
    public LockResult accquireContainerLock(final OperationType operationType, final LocalContainerReference cRef,
            final IsolationLevel isolationLevel, final NativeSubTransaction stx,
            final NativeContainerManager containerManager) throws InvalidContainerException {
        if (operationType == null) {
            throw new NullPointerException("The OperationType must not be null");
        }
        if (cRef == null) {
            throw new NullPointerException("The ContainerReference must not be null");
        }
        if (isolationLevel == null) {
            throw new NullPointerException("The IsolationLevel must not be null");
        }
        if (stx == null) {
            throw new NullPointerException("The SubTransaction must not be null");
        }

        LockResult lockResult = null;
        NativeLock containerLock = null;
        switch (operationType) {
        case CREATECONTAINER:
            NativeLock lock = new NativeLock((NativeTransaction) stx.getParent(), stx);
            if (this.containerLocks.putIfAbsent(cRef, lock) != null) {
                throw new RuntimeException("It should not be possible that two equal container keys exist");
            }
            stx.addLog(new DefaultContainerCreateLogItem(cRef, containerManager, this, stx));
            return new LockResult(true, null, null);
        case DESTROYCONTAINER:
            containerLock = this.containerLocks.get(cRef);
            if (containerLock == null) {
                throw new InvalidContainerException();
            }
            lockResult = containerLock.addLock(LockType.DELETE, (NativeTransaction) stx.getParent(), stx);
            if (lockResult.isValid()) {
                stx.addLog(new DefaultContainerDestroyLogItem(cRef, containerManager, this, stx));
            }
            return lockResult;
        case LOCKCONTAINER:
            containerLock = this.containerLocks.get(cRef);
            if (containerLock == null) {
                throw new InvalidContainerException();
            }
            lockResult = containerLock.addLock(LockType.EXCLUSIVE, (NativeTransaction) stx.getParent(), stx);
            if (lockResult.isValid()) {
                stx.addLog(new DefaultContainerLockLogItem(cRef, this, stx));
            }
            return lockResult;
        default:
            throw new IllegalArgumentException("This OperationType is not allowed for Container Operations");
        }
    }

    @Override
    public LockResult accquireEntryLock(final OperationType operationType, final NativeEntry entry,
            final LocalContainerReference container, final IsolationLevel isolationLevel,
            final NativeSubTransaction stx, final RequestContext context,
            final NativeContainer nativeContainer) throws InvalidEntryException {

        if (operationType == null) {
            throw new NullPointerException("The OperationType must not be null");
        }
        if (entry == null) {
            throw new NullPointerException("The Entry must not be null");
        }
        if (isolationLevel == null) {
            throw new NullPointerException("The IsolationLevel must not be null");
        }
        if (stx == null) {
            throw new NullPointerException("The SubTransaction must not be null");
        }

        LockResult lockResult = null;
        switch (operationType) {
        case READ:
            if (isolationLevel.equals(IsolationLevel.READ_COMMITTED)) {
                return new LockResult(true, null, null);
            } else {
                NativeLock lock = this.entryLocks.get(entry);
                if (lock == null) {
                    throw new InvalidEntryException();
                }
                lockResult = lock.addLock(LockType.READ, (NativeTransaction) stx.getParent(), stx);
            }
            if (lockResult.isValid()) {
                stx.addLog(new DefaultReadLogItem(entry, container, this, stx));
            }
            return lockResult;
        case WRITE:
            NativeLock lock = new NativeLock((NativeTransaction) stx.getParent(), stx);
            if (this.entryLocks.putIfAbsent(entry, lock) != null) {
                throw new RuntimeException("It should not be possible that two equal entry keys exist");
            }
            stx.addLog(new DefaultWriteLogItem(entry, container, this, stx, context, nativeContainer));
            return new LockResult(true, null, null);
        case TAKE:
            lock = this.entryLocks.get(entry);
            if (lock == null) {
                throw new InvalidEntryException();
            }
            lockResult = lock.addLock(LockType.DELETE, (NativeTransaction) stx.getParent(), stx);
            if (lockResult.isValid()) {
                stx.addLog(new DefaultTakeLogItem(entry, container, this, stx, context, nativeContainer));
            }
            return lockResult;
        default:
            throw new IllegalArgumentException("The OperationType is not supported for Entry Operations");
        }
    }

    @Override
    public void releaseEntryLock(final OperationType operationType, final NativeEntry entry,
            final NativeTransaction tx, final NativeSubTransaction stx) {
        if (operationType == null) {
            throw new NullPointerException("The operationType must not be null");
        }
        if (entry == null) {
            throw new NullPointerException("The entry must not be null");
        }
        if (tx == null) {
            throw new NullPointerException("The transaction must not be null");
        }

        NativeLock lock = this.entryLocks.get(entry);
        if (lock == null) {
            /* The Lock is gone due to a Delete Operation */
            return;
        }
        switch (operationType) {
        case WRITE:
            lock.releaseLock(LockType.INSERT, tx, stx);
            return;
        case READ:
            lock.releaseLock(LockType.READ, tx, stx);
            return;
        case TAKE:
            lock.releaseLock(LockType.DELETE, tx, stx);
            return;
        default:
            throw new IllegalArgumentException("The OperationType is not supported for EntryLocks");
        }
    }

    @Override
    public void releaseContainerLock(final OperationType operationType, final LocalContainerReference cRef,
            final NativeTransaction tx, final NativeSubTransaction stx) {
        if (operationType == null) {
            throw new NullPointerException("The operationType must not be null");
        }
        if (cRef == null) {
            throw new NullPointerException("The cRef must not be null");
        }
        if (tx == null) {
            throw new NullPointerException("The transaction must not be null");
        }

        NativeLock lock = this.containerLocks.get(cRef);
        if (lock == null) {
            // The Lock is gone due to an Delete Operation
            return;
        }

        switch (operationType) {
        case CREATECONTAINER:
            lock.releaseLock(LockType.INSERT, tx, stx);
            return;
        case DESTROYCONTAINER:
            lock.releaseLock(LockType.DELETE, tx, stx);
            return;
        case LOCKCONTAINER:
            lock.releaseLock(LockType.EXCLUSIVE, tx, stx);
            return;
        default:
            throw new IllegalArgumentException("The OperationType is not supported for ContainerLocks");
        }

    }

    /**
     * Internal Utility Class to represent Locks for Entries/Containers.
     */
    private static final class NativeLock {

        private final CopyOnWriteArrayList<TransactionTuple> activeReadLocks;
        private TransactionTuple activeInsertLock;
        private TransactionTuple activeDeleteLock;
        private TransactionTuple activeExclusiveLock;

        private static final LockResult VALID = new LockResult(true, null, null);

        /**
         * Constructor of the Lock.
         *
         * @param tx
         *            the Transaction holding the lock
         * @param stx
         *            the subTransaction holding the lock
         */
        protected NativeLock(final NativeTransaction tx, final NativeSubTransaction stx) {
            assert tx != null : "The Transaction must be set";
            assert stx != null : "The Subtransaction must be set";

            this.activeInsertLock = new TransactionTuple(tx, stx);
            // TODO is the CopyOnWriteArrayList a bottleneck?
            // (the only not synchronized access is in checkEntryAvailability)
            this.activeReadLocks = new CopyOnWriteArrayList<TransactionTuple>();
        }

        /**
         * To add a specific Lock on an Entry/Container.
         *
         * @param lockType
         *            type of the Lock
         * @param tx
         *            the Transaction requesting the lock
         * @param stx
         *            the SubTransaction requesting the Lock
         * @return LockResult object
         */
        synchronized LockResult addLock(final LockType lockType, final NativeTransaction tx,
                final NativeSubTransaction stx) {
            assert lockType != null : "The lockType must be set";
            assert tx != null : "No Transaction was set";
            assert stx != null : "No SubTransaction was set";
            assert stx.getParent().equals(tx) : "The parent Stx.Transaction is unequal to the supplied Transaction";

            LockResult result = null;
            switch (lockType) {
            case INSERT:
                throw new RuntimeException("INSERT-Lock can only be set for new entries, not existing ones");
            case DELETE:
                result = checkActiveExclDelInsLocks(tx, stx);
                if (result != null) {
                    return result;
                }
                result = checkActiveReadLocks(tx, stx);
                if (result != null) {
                    return result;
                }
                this.activeDeleteLock = new TransactionTuple(tx, stx);
                return VALID;
            case READ:
                result = checkActiveExclDelInsLocks(tx, stx);
                if (result != null) {
                    return result;
                }
                this.activeReadLocks.add(new TransactionTuple(tx, stx));
                return VALID;
            case EXCLUSIVE:
                result = checkActiveExclDelInsLocks(tx, stx);
                if (result != null) {
                    return result;
                }
                result = checkActiveReadLocks(tx, stx);
                if (result != null) {
                    return result;
                }
                this.activeExclusiveLock = new TransactionTuple(tx, stx);
                return VALID;
            default:
                throw new RuntimeException("No LockType was set");
            }
        }

        private LockResult checkActiveReadLocks(final NativeTransaction tx, final NativeSubTransaction stx) {
            for (TransactionTuple t : this.activeReadLocks) {
                if (!compare(tx, stx, t)) {
                    return new LockResult(false, t.getTx(), t.getStx());
                }
            }
            return null;
        }

        private LockResult checkActiveExclDelInsLocks(final NativeTransaction tx, final NativeSubTransaction stx) {
            LockResult result = checkActiveExclusiveLock(tx, stx);
            if (result != null) {
                return result;
            }
            result = checkActiveDeleteLock(tx, stx);
            if (result != null) {
                return result;
            }
            return checkActiveInsertLock(tx, stx);
        }

        private LockResult checkActiveDeleteLock(final NativeTransaction tx, final NativeSubTransaction stx) {
            if (this.activeDeleteLock != null) {
                return new LockResult(false, this.activeDeleteLock.getTx(), this.activeDeleteLock.getStx());
            }
            return null;
        }

        private LockResult checkActiveInsertLock(final NativeTransaction tx, final NativeSubTransaction stx) {
            if (!compare(tx, stx, this.activeInsertLock)) {
                return new LockResult(false, this.activeInsertLock.getTx(), this.activeInsertLock.getStx());
            }
            return null;
        }

        private LockResult checkActiveExclusiveLock(final NativeTransaction tx, final NativeSubTransaction stx) {
            if (!compare(tx, stx, this.activeExclusiveLock)) {
                return new LockResult(false, this.activeExclusiveLock.getTx(), this.activeExclusiveLock.getStx());
            }
            return null;
        }

        /**
         * Removing a Lock from an Entry/Container. The associated Lock is
         * removed completely from the Entry. If only a Transaction is provided,
         * all associated Locks are removed.
         *
         * @param lockType
         *            The Type of Lock which should be removed
         * @param tx
         *            the Transaction holding the Lock
         * @param stx
         *            the SubTransaction holding the Lock; May be
         *            <code>null</code>
         */
        synchronized void releaseLock(final LockType lockType, final NativeTransaction tx,
                final NativeSubTransaction stx) {

            assert lockType != null : "The lockType must be set";

            switch (lockType) {
            case INSERT:
                if (this.activeInsertLock == null) {
                    throw new RuntimeException(
                            "A releaseLock is required for a Lock not hold by this tx/stx, this state should "
                                    + " not be possible");
                }
                if (tx.equals(this.activeInsertLock.getTx()) && this.activeInsertLock.getStx() == null) {
                    this.activeInsertLock = null;
                    return;
                }
                if (stx != null && stx.equals(this.activeInsertLock.getStx())) {
                    this.activeInsertLock = null;
                    return;
                }
                throw new RuntimeException(
                        "A releaseLock is required for a Lock not hold by this tx/stx, this state should not"
                                + " be possible");
            case DELETE:
                if (this.activeDeleteLock == null) {
                    throw new RuntimeException(
                            "A releaseLock is required for a Lock not hold by this tx/stx, this state should "
                                    + "not be possible");
                }
                if (tx.equals(this.activeDeleteLock.getTx()) && this.activeDeleteLock.getStx() == null) {
                    this.activeDeleteLock = null;
                    return;
                }
                if (stx != null && stx.equals(this.activeDeleteLock.getStx())) {
                    this.activeDeleteLock = null;
                    return;
                }
                throw new RuntimeException(
                        "A releaseLock is required for a Lock not hold by this tx/stx, this state should not"
                                + " be possible");
            case READ:
                if (stx != null) {
                    for (Iterator<TransactionTuple> i = this.activeReadLocks.iterator(); i.hasNext();) {
                        TransactionTuple t = i.next();
                        if (tx.equals(t.getTx()) && stx.equals(t.getStx())) {
                            this.activeReadLocks.remove(t);
                            return;
                        }
                    }
                } else {
                    for (Iterator<TransactionTuple> i = this.activeReadLocks.iterator(); i.hasNext();) {
                        TransactionTuple t = i.next();
                        if (tx.equals(t.getTx())) {
                            this.activeReadLocks.remove(t);
                        }
                    }
                    return;
                }
                throw new RuntimeException(
                        "A releaseLock is required for a Lock not hold by this tx/stx, this state should not be"
                                + " possible");
            case EXCLUSIVE:
                if (this.activeExclusiveLock == null) {
                    throw new RuntimeException(
                            "A releaseLock is required for a Lock not hold by this tx/stx, this state should "
                                    + "not be possible");
                }
                if (tx.equals(this.activeExclusiveLock.getTx()) && this.activeExclusiveLock.getStx() == null) {
                    this.activeExclusiveLock = null;
                    return;
                }
                if (stx != null && stx.equals(this.activeExclusiveLock.getStx())) {
                    this.activeExclusiveLock = null;
                    return;
                }
                throw new RuntimeException(
                        "A releaseLock is required for a Lock not hold by this tx/stx, this state should not"
                                + " be possible");
            default:
                throw new RuntimeException("Unknown lockType; This state should not be possible");
            }
        }

        /**
         * Surrendering a Lock to the parent Transaction.
         *
         * @param lockType
         *            The Type of Lock which should be removed
         * @param stx
         *            the SubTransaction holding the Lock
         */
        synchronized void surrenderLock(final LockType lockType, final NativeSubTransaction stx) {
            assert lockType != null : "The lockType must be set";
            assert stx != null;

            switch (lockType) {
            case INSERT:
                if (this.activeInsertLock == null) {
                    throw new RuntimeException(
                            "A surrenderLock is required for a Lock not hold by this tx/stx, this state should not"
                                    + " be possible");
                }
                if (((NativeTransaction) stx.getParent()).equals(this.activeInsertLock.getTx())
                        && stx.equals(this.activeInsertLock.getStx())) {
                    this.activeInsertLock.subTransactionCommited();
                    return;
                }
                throw new RuntimeException(
                        "A surrenderLock is required for a Lock not hold by this tx/stx, this state should not be"
                                + " possible");
            case DELETE:
                if (this.activeDeleteLock == null) {
                    throw new RuntimeException(
                            "A surrenderLock is required for a Lock not hold by this tx/stx, this state should not be"
                                    + " possible");
                }
                if (((NativeTransaction) stx.getParent()).equals(this.activeDeleteLock.getTx())
                        && stx.equals(this.activeDeleteLock.getStx())) {
                    this.activeDeleteLock.subTransactionCommited();
                    return;
                }
                throw new RuntimeException(
                        "A surrenderLock is required for a Lock not hold by this tx/stx, this state should not be"
                                + " possible");
            case READ:
                for (Iterator<TransactionTuple> i = this.activeReadLocks.iterator(); i.hasNext();) {
                    TransactionTuple t = i.next();
                    if (((NativeTransaction) stx.getParent()).equals(t.getTx()) && stx.equals(t.getStx())) {
                        t.subTransactionCommited();
                        return;
                    }
                }
                throw new RuntimeException(
                        "A surrenderLock is required for a Lock not hold by this tx/stx, this state should not be"
                                + " possible");
            case EXCLUSIVE:
                if (this.activeExclusiveLock == null) {
                    throw new RuntimeException(
                            "A surrenderLock is required for a Lock not hold by this tx/stx, this state should not be"
                                    + " possible");
                }
                if (((NativeTransaction) stx.getParent()).equals(this.activeExclusiveLock.getTx())
                        && stx.equals(this.activeExclusiveLock.getStx())) {
                    this.activeExclusiveLock.subTransactionCommited();
                    return;
                }
                throw new RuntimeException(
                        "A surrenderLock is required for a Lock not hold by this tx/stx, this state should not be"
                                + " possible");
            default:
                throw new RuntimeException("Unknown lockType; This state should not be possible");
            }

        }

        /**
         * This function compares the requesting Transactions with one of the
         * currently active Lock's TransactionTuple It returns true, if the
         * requesting Transactions 'outrank' the active Lock.
         * <p>
         * Example:
         * </p>
         *
         * @param tx
         *            the requesting Transaction
         * @param stx
         *            the requesting SubTransaction
         * @param txTuple
         *            the active Lock's TransactionTuple
         * @return true if the requesting transactions may take this lock, false
         *         otherwise
         */
        private boolean compare(final NativeTransaction tx, final NativeSubTransaction stx,
                final TransactionTuple txTuple) {
            if (txTuple == null) {
                return true;
            }
            if (tx.equals(txTuple.getTx())) {
                if (txTuple.getStx() == null) {
                    return true;
                }
                if ((stx == null) && (txTuple.getStx() != null)) {
                    return false;
                }
                if (!stx.equals(txTuple.getStx())) {
                    return false;
                }
                return true;
            } else {
                // Failed, because another Transaction holds this Lock
                return false;
            }
        }

        @Override
        public String toString() {
            return "NativeLock [activeReadLocks=" + activeReadLocks + ", activeInsertLock=" + activeInsertLock
                    + ", activeDeleteLock=" + activeDeleteLock + ", activeExclusiveLock=" + activeExclusiveLock + "]";
        }

    }

    /**
     * Internal Utility Class for the Representation of Tx<->sTx Tuples holding
     * Locks.
     */
    private static final class TransactionTuple {

        private final NativeTransaction tx;
        private NativeSubTransaction stx;

        /**
         * Constructor creating the TransactionTuple.
         *
         * @param tx
         *            the Tx
         * @param stx
         *            the SubTx
         */
        TransactionTuple(final NativeTransaction tx, final NativeSubTransaction stx) {
            assert tx != null : "The Transaction has to be set";
            this.tx = tx;
            this.stx = stx;
        }

        /**
         * Returns the Transaction of the Tuple.
         *
         * @return Transaction
         */
        public NativeTransaction getTx() {
            return tx;
        }

        /**
         * Returns the SubTransaction of the Tuple.
         *
         * @return SubTransaction
         */
        public NativeSubTransaction getStx() {
            return stx;
        }

        /**
         * When the SubTransaction gets committed it is removed from the Tuple.
         */
        void subTransactionCommited() {
            this.stx = null;
        }

        Availability newAvailability(final AvailabilityType type) {
            return new Availability(type, tx, stx);
        }

        @Override
        public String toString() {
            return "TransactionTuple [tx=" + tx + ", stx=" + stx + "]";
        }

    }

    @Override
    public void purgeContainerLock(final LocalContainerReference cRef) {
        if (cRef == null) {
            throw new NullPointerException("The ContainerReference must not be null");
        }
        this.containerLocks.remove(cRef);
    }

    @Override
    public void purgeEntryLock(final NativeEntry entry) {
        if (entry == null) {
            throw new NullPointerException("The entry must not be null");
        }
        this.entryLocks.remove(entry);
    }

    @Override
    public Availability checkContainerAvailability(final LocalContainerReference cRef,
            final IsolationLevel isolationLevel, final NativeSubTransaction stx, final OperationType opType) {
        if (cRef == null) {
            throw new NullPointerException("The ContainerReference must not be null");
        }
        if (isolationLevel == null) {
            throw new NullPointerException("The IsolationLevel must not be null");
        }
        if (stx == null) {
            throw new NullPointerException("The SubTransaction must not be null");
        }
        if (opType == null) {
            throw new NullPointerException("The Operationtype must not be null");
        }

        NativeLock lock = this.containerLocks.get(cRef);
        if (lock == null) {
            // The Container got already deleted
            return Availability.NOTVISIBLE;
        }
        synchronized (lock) {
            if (lock.activeDeleteLock != null) {
                // The Container is Delete-locked
                return lock.activeDeleteLock.newAvailability(AvailabilityType.NOTAVAILABLE);
            }
            if (lock.activeInsertLock != null) {
                // The Container is Insert-Locked
                if (((NativeTransaction) stx.getParent()).equals(lock.activeInsertLock.getTx())) {
                    if (lock.activeInsertLock.getStx() == null) {
                        return Availability.AVAILABLE;
                    }
                    if (!lock.activeInsertLock.getStx().equals(stx)) {
                        return lock.activeInsertLock.newAvailability(AvailabilityType.NOTVISIBLE);
                    }
                } else {
                    return lock.activeInsertLock.newAvailability(AvailabilityType.NOTVISIBLE);
                }
            }
            if (lock.activeExclusiveLock != null) {
                if (((NativeTransaction) stx.getParent()).equals(lock.activeExclusiveLock.getTx())) {
                    if (lock.activeExclusiveLock.getStx() == null) {
                        // allow the access for sub-transactions in the same transaction
                        return Availability.AVAILABLE;
                    }
                    if (!lock.activeExclusiveLock.getStx().equals(stx)) {
                        return lock.activeExclusiveLock.newAvailability(AvailabilityType.NOTAVAILABLE);
                    }
                } else {
                    return lock.activeExclusiveLock.newAvailability(AvailabilityType.NOTAVAILABLE);
                }
            }
        }
        return Availability.AVAILABLE;
    }

    @Override
    public Availability checkEntryAvailability(final NativeEntry entry, final IsolationLevel isolationLevel,
            final NativeSubTransaction stx, final OperationType opType) {
        if (entry == null) {
            throw new NullPointerException("The Entry must not be null");
        }
        if (isolationLevel == null) {
            throw new NullPointerException("The IsolationLevel must not be null");
        }
        if (stx == null) {
            throw new NullPointerException("The SubTransaction must not be null");
        }
        if (opType == null) {
            throw new NullPointerException("The OperationType must not be null");
        }

        NativeLock lock = this.entryLocks.get(entry);
        if (lock == null) {
            return Availability.NOTVISIBLE;
        }
        synchronized (lock) {
            switch (isolationLevel) {
            case READ_COMMITTED:
                switch (opType) {
                case READ:
                    if (lock.activeDeleteLock != null) {
                        if (((NativeTransaction) stx.getParent()).equals(lock.activeDeleteLock.getTx())) {
                            if (lock.activeDeleteLock.getStx() == null) {
                                return lock.activeDeleteLock.newAvailability(AvailabilityType.NOTVISIBLE);
                            }
                            if (lock.activeDeleteLock.getStx().equals(stx)) {
                                return lock.activeDeleteLock.newAvailability(AvailabilityType.NOTVISIBLE);
                            }
                            if (!lock.activeDeleteLock.getStx().equals(stx)) {
                                return Availability.AVAILABLE;
                            }
                        }
                    }
                    if (lock.activeInsertLock != null) {
                        if (((NativeTransaction) stx.getParent()).equals(lock.activeInsertLock.getTx())) {
                            if (lock.activeInsertLock.getStx() == null) {
                                return Availability.AVAILABLE;
                            }
                            if (!lock.activeInsertLock.getStx().equals(stx)) {
                                return lock.activeInsertLock.newAvailability(AvailabilityType.NOTVISIBLE);
                            }
                        } else {
                            return lock.activeInsertLock.newAvailability(AvailabilityType.NOTVISIBLE);
                        }
                    }
                    return Availability.AVAILABLE;
                case TAKE:
                    if (lock.activeInsertLock != null) {
                        if (((NativeTransaction) stx.getParent()).equals(lock.activeInsertLock.getTx())) {
                            if (lock.activeInsertLock.getStx() != null) {
                                if (!lock.activeInsertLock.getStx().equals(stx)) {
                                    return lock.activeInsertLock.newAvailability(AvailabilityType.NOTVISIBLE);
                                }
                            }
                        } else {
                            return lock.activeInsertLock.newAvailability(AvailabilityType.NOTVISIBLE);
                        }
                    }
                    if (lock.activeDeleteLock != null) {
                        if (((NativeTransaction) stx.getParent()).equals(lock.activeDeleteLock.getTx())) {
                            if (lock.activeDeleteLock.getStx() == null) {
                                return lock.activeDeleteLock.newAvailability(AvailabilityType.NOTVISIBLE);
                            }
                            if (lock.activeDeleteLock.getStx().equals(stx)) {
                                return lock.activeDeleteLock.newAvailability(AvailabilityType.NOTVISIBLE);
                            }
                        }
                        return lock.activeDeleteLock.newAvailability(AvailabilityType.NOTAVAILABLE);
                    }
                    if (!lock.activeReadLocks.isEmpty()) {
                        for (TransactionTuple tuple : lock.activeReadLocks) {
                            if (tuple.getTx().equals(stx.getParent())) {
                                if (tuple.getStx() != null) {
                                    if (!tuple.getStx().equals(stx)) {
                                        return tuple.newAvailability(AvailabilityType.NOTAVAILABLE);
                                    }
                                }
                            } else {
                                return tuple.newAvailability(AvailabilityType.NOTAVAILABLE);
                            }
                        }
                    }
                    return Availability.AVAILABLE;
                default:
                    throw new IllegalArgumentException("This is not a valid OperationType");
                }
            case REPEATABLE_READ:
                switch (opType) {
                case READ:
                    if (lock.activeDeleteLock != null) {
                        if (((NativeTransaction) stx.getParent()).equals(lock.activeDeleteLock.getTx())) {
                            if (lock.activeDeleteLock.getStx() == null) {
                                return lock.activeDeleteLock.newAvailability(AvailabilityType.NOTVISIBLE);
                            }
                            if (lock.activeDeleteLock.getStx().equals(stx)) {
                                return lock.activeDeleteLock.newAvailability(AvailabilityType.NOTVISIBLE);
                            }
                        }
                        if (lock.activeInsertLock != null) {
                            if (((NativeTransaction) stx.getParent()).equals(lock.activeInsertLock.getTx())) {
                                if (lock.activeInsertLock.getStx() != null
                                        && !lock.activeInsertLock.getStx().equals(stx)) {
                                    lock.activeInsertLock.newAvailability(AvailabilityType.NOTVISIBLE);
                                }
                            } else {
                                return lock.activeInsertLock.newAvailability(AvailabilityType.NOTVISIBLE);
                            }
                        }
                        return lock.activeDeleteLock.newAvailability(AvailabilityType.NOTAVAILABLE);
                    }
                    if (lock.activeInsertLock != null) {
                        if (((NativeTransaction) stx.getParent()).equals(lock.activeInsertLock.getTx())) {
                            if (lock.activeInsertLock.getStx() != null && !lock.activeInsertLock.getStx().equals(stx)) {
                                return lock.activeInsertLock.newAvailability(AvailabilityType.NOTVISIBLE);
                            }
                        } else {
                            return lock.activeInsertLock.newAvailability(AvailabilityType.NOTVISIBLE);
                        }
                    }
                    return Availability.AVAILABLE;
                case TAKE:
                    if (lock.activeInsertLock != null) {
                        if (((NativeTransaction) stx.getParent()).equals(lock.activeInsertLock.getTx())) {
                            if (lock.activeInsertLock.getStx() != null) {
                                if (!lock.activeInsertLock.getStx().equals(stx)) {
                                    return lock.activeInsertLock.newAvailability(AvailabilityType.NOTVISIBLE);
                                }
                            }
                        } else {
                            return lock.activeInsertLock.newAvailability(AvailabilityType.NOTVISIBLE);
                        }
                    }
                    if (lock.activeDeleteLock != null) {
                        if (((NativeTransaction) stx.getParent()).equals(lock.activeDeleteLock.getTx())) {
                            if (lock.activeDeleteLock.getStx() == null) {
                                return lock.activeDeleteLock.newAvailability(AvailabilityType.NOTVISIBLE);
                            }
                            if (lock.activeDeleteLock.getStx().equals(stx)) {
                                return lock.activeDeleteLock.newAvailability(AvailabilityType.NOTVISIBLE);
                            }
                        }
                        return lock.activeDeleteLock.newAvailability(AvailabilityType.NOTAVAILABLE);
                    }
                    if (!lock.activeReadLocks.isEmpty()) {
                        for (TransactionTuple tuple : lock.activeReadLocks) {
                            if (tuple.getTx().equals(stx.getParent())) {
                                if (tuple.getStx() != null) {
                                    if (!tuple.getStx().equals(stx)) {
                                        return tuple.newAvailability(AvailabilityType.NOTAVAILABLE);
                                    }
                                }
                            } else {
                                return tuple.newAvailability(AvailabilityType.NOTAVAILABLE);
                            }
                        }
                    }
                    return Availability.AVAILABLE;
                default:
                }

            default:
                throw new IllegalArgumentException("Unknown IsolationLevel used");
            }
        }
    }

    @Override
    public void releaseSubTransactionContainerLock(final OperationType operationType,
            final LocalContainerReference cRef, final NativeSubTransaction stx) {
        if (operationType == null) {
            throw new NullPointerException("The OperationType must not be null");
        }
        if (cRef == null) {
            throw new NullPointerException("The ContainerReference must not be null");
        }
        if (stx == null) {
            throw new NullPointerException("The SubTransaction must not be null");
        }

        NativeLock lock = this.containerLocks.get(cRef);
        if (lock == null) {
            throw new RuntimeException("Lock not found; Should not be possible when surrendering Container Locks");
        }
        switch (operationType) {
        case CREATECONTAINER:
            lock.surrenderLock(LockType.INSERT, stx);
            return;
        case DESTROYCONTAINER:
            lock.surrenderLock(LockType.DELETE, stx);
            return;
        case LOCKCONTAINER:
            lock.surrenderLock(LockType.EXCLUSIVE, stx);
            return;
        default:
            throw new IllegalArgumentException("The OperationType is not supported for ContainerLocks");
        }
    }

    @Override
    public void releaseSubTransactionEntryLock(final OperationType operationType, final NativeEntry entry,
            final NativeSubTransaction stx) {
        if (operationType == null) {
            throw new NullPointerException("The OperationType must not be null");
        }
        if (entry == null) {
            throw new NullPointerException("The Entry must not be null");
        }
        if (stx == null) {
            throw new NullPointerException("The SubTransaction must not be null");
        }

        NativeLock lock = this.entryLocks.get(entry);
        if (lock == null) {
            throw new RuntimeException("Lock not found, should not be possible when surrendering Entry Lock");
        }
        switch (operationType) {
        case WRITE:
            lock.surrenderLock(LockType.INSERT, stx);
            return;
        case READ:
            lock.surrenderLock(LockType.READ, stx);
            return;
        case TAKE:
            lock.surrenderLock(LockType.DELETE, stx);
            return;
        default:
            throw new IllegalArgumentException("The OperationType is not supported for EntryLocks");
        }
    }

    @Override
    public boolean checkValidEntryOverwrite(final NativeEntry base, final NativeEntry overwrite) {
        NativeLock baseLock = this.entryLocks.get(base);
        if (baseLock == null) {
            return false;
        }
        synchronized (baseLock) {
            NativeLock checkLock = this.entryLocks.get(overwrite);
            if (checkLock == null) {
                throw new IllegalStateException();
            }
            synchronized (checkLock) {
                if (checkLock.activeInsertLock == null) {
                    throw new IllegalStateException();
                }

                if (baseLock.activeDeleteLock != null) {
                    if (baseLock.activeDeleteLock.getTx().equals(checkLock.activeInsertLock.getTx())) {
                        if (baseLock.activeDeleteLock.getStx() != null) {
                            if (baseLock.activeDeleteLock.getStx().equals(checkLock.activeInsertLock.getStx())) {
                                return true;
                            }
                            return false;
                        } else {
                            return true;
                        }
                    } else {
                        return false;
                    }
                } else {
                    return false;
                }
            }
        }
    }
}
