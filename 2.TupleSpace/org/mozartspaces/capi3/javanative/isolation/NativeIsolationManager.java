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

import org.mozartspaces.capi3.InvalidContainerException;
import org.mozartspaces.capi3.InvalidEntryException;
import org.mozartspaces.capi3.IsolationLevel;
import org.mozartspaces.capi3.LocalContainerReference;
import org.mozartspaces.capi3.OperationType;
import org.mozartspaces.capi3.javanative.operation.NativeContainer;
import org.mozartspaces.capi3.javanative.operation.NativeContainerManager;
import org.mozartspaces.capi3.javanative.operation.NativeEntry;
import org.mozartspaces.core.RequestContext;

/**
 * The isolation manager is used to organize all entry- and container-level isolation for the transactional behavior. It
 * adminstrates all pessimistic locks in the system.
 *
 * @author Martin Barisits
 */
public interface NativeIsolationManager {

    /**
     * Used to acquire a specific lock for a transaction and/or sub-transaction on an entry.
     *
     * @param operationType
     *            Either WRITE, READ, TAKE or DELETE
     * @param entry
     *            the NativeEntry
     * @param container
     *            the container
     * @param isolationLevel
     *            the isolation level to acquire the lock for
     * @param stx
     *            The sub-transaction
     * @param context
     *            the context of the take/delete request
     * @param nativeContainer
     *            the internal container object
     * @return a LockResult object
     * @throws InvalidEntryException
     *             thrown when an Entry is invalid (has been deleted)
     */
    LockResult accquireEntryLock(OperationType operationType, NativeEntry entry, LocalContainerReference container,
            IsolationLevel isolationLevel, NativeSubTransaction stx, RequestContext context,
            NativeContainer nativeContainer) throws InvalidEntryException;

    /**
     * Used to completely release a lock for a specific entry. The stx may be <code>null</code>
     *
     * @param operationType
     *            OperationType to release
     * @param entry
     *            Entry to release the lock on
     * @param tx
     *            Transaction
     * @param stx
     *            SubTransaction
     */
    void releaseEntryLock(OperationType operationType, NativeEntry entry, NativeTransaction tx,
            NativeSubTransaction stx);

    /**
     * Used to surrender an specific Lock for an Entry. This means that the Lock-Holder is moved from the SubTransaction
     * to the Transaction Level
     *
     * @param operationType
     *            OperationType to release
     * @param entry
     *            Entry to release the lock on
     * @param stx
     *            SubTransaction
     */
    void releaseSubTransactionEntryLock(OperationType operationType, NativeEntry entry, NativeSubTransaction stx);

    /**
     * Used to accquire a specific Lock for a Transaction and/or SubTransaction on an Container.
     *
     * @param operationType
     *            Either CREATECONTAINER, DESTROYCONTAINER or LOCKCONTAINER
     * @param cRef
     *            the ContainerReference
     * @param isolationLevel
     *            the isolationLevel to accquire the lock for
     * @param stx
     *            The SubTransaction
     * @param containerManager
     *            the CAPI3 ContainerManager
     * @return a LockResult object
     * @throws InvalidContainerException
     *             thrown when a Container is invalid (Has been deleted)
     */
    LockResult accquireContainerLock(OperationType operationType, LocalContainerReference cRef,
            IsolationLevel isolationLevel, NativeSubTransaction stx, NativeContainerManager containerManager)
            throws InvalidContainerException;

    /**
     * Used to completely release a lock for a specific Container.
     *
     * @param operationType
     *            OperationType to release
     * @param cRef
     *            Container to release the lock on
     * @param tx
     *            Transaction
     * @param stx
     *            SubTransaction
     */
    void releaseContainerLock(OperationType operationType, LocalContainerReference cRef, NativeTransaction tx,
            NativeSubTransaction stx);

    /**
     * Used to surrender a Lock for a Container This means that the Lock Holder is moved from SubTransaction Level to
     * Transaction Level.
     *
     * @param operationType
     *            OperationType to surrender
     * @param cRef
     *            Container to surrender the Lock on
     * @param stx
     *            SubTransaction
     */
    void releaseSubTransactionContainerLock(OperationType operationType, LocalContainerReference cRef,
            NativeSubTransaction stx);

    /**
     * Purge the associated Lock completely from the isolationManager.
     *
     * @param cRef
     *            the ContainerReference associated to the Lock
     */
    void purgeContainerLock(LocalContainerReference cRef);

    /**
     * Purge the associated Lock completely from the isolationmanager.
     *
     * @param entry
     *            the Entry associated to the Lock
     */
    void purgeEntryLock(NativeEntry entry);

    /**
     * Check if an Entry is available for this Transaction, SubTransaction and OperationType.
     *
     * @param entry
     *            the Entry to check on
     * @param isolationLevel
     *            the isolationLevel to use
     * @param stx
     *            the SubTransaction to use
     * @param opType
     *            Type of the Operation the Entry should be available for
     * @return AvailabilityType AVAILABLE if avialable, NOTVISIBLE if the Entry is not visible (INSERT-locked) or
     *         NOTAVIALABLE if Locks would block this Operation
     */
    Availability checkEntryAvailability(NativeEntry entry, IsolationLevel isolationLevel, NativeSubTransaction stx,
            OperationType opType);

    /**
     * Check if a Container is visible for this Transaction, SubTransaction and OperationType.
     *
     * @param cRef
     *            The Container to check on
     * @param isolationLevel
     *            the IsolationLevel to use
     * @param stx
     *            the Subtransaction to use
     * @param opType
     *            Type of the Operation the Container should be available for
     * @return AvailabilityType AVAILABLE if avialable, NOTVISIBLE if the Continaer is not visible (INSERT-locked) or
     *         NOTAVIALABLE if Locks would block this Operation
     */
    Availability checkContainerAvailability(LocalContainerReference cRef, IsolationLevel isolationLevel,
            NativeSubTransaction stx, OperationType opType);

    /**
     * This is a special Function for Coordinators like Key or Vector. The function has to determine if an Entry is
     * actually replacing another entry. This is the case if the Base Entry is DELETE-LOCKED by the same (or superior)
     * Transaction as the INSERT-LOCK of the overwriting Entry
     *
     * @param base
     *            Entry to be overwritten
     * @param overwrite
     *            new Entry
     * @return true if valid, false otherwise
     */
    boolean checkValidEntryOverwrite(NativeEntry base, NativeEntry overwrite);
}
