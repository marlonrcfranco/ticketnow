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
package org.mozartspaces.capi3.javanative.coordination;

import java.io.Closeable;

import org.mozartspaces.capi3.Capi3Exception;
import org.mozartspaces.capi3.CoordinationData;
import org.mozartspaces.capi3.Coordinator;
import org.mozartspaces.capi3.CoordinatorLockedException;
import org.mozartspaces.capi3.javanative.isolation.NativeIsolationManager;
import org.mozartspaces.capi3.javanative.isolation.NativeSubTransaction;
import org.mozartspaces.capi3.javanative.operation.NativeContainer;
import org.mozartspaces.capi3.javanative.operation.NativeEntry;
import org.mozartspaces.core.MzsCoreRuntimeException;
import org.mozartspaces.core.RequestContext;

/**
 * Interface for native coordinator implementations.
 *
 * @author Martin Barisits
 * @author Tobias Doenz
 */
public interface NativeCoordinator extends Coordinator, Closeable {

    /**
     * Initializes the coordinator. This method is called during container creation and restore.
     *
     * @param container
     *            the internal container object
     * @param stx
     *            the active sub-transaction
     * @param context
     *            the request context, may be {@code null}
     * @throws MzsCoreRuntimeException
     *             if initializing the coordinator fails
     */
    void init(NativeContainer container, NativeSubTransaction stx, RequestContext context)
            throws MzsCoreRuntimeException;

    /**
     * Closes the coordinator and all resources it uses. This method is called when the container where this coordinator
     * is used is closed, which is on shutdown and when the container is destroyed.
     *
     * @throws MzsCoreRuntimeException if closing the coordinator fails
     */
    @Override
    void close() throws MzsCoreRuntimeException;

    /**
     * Registers an entry to this coordinator.
     *
     * @param stx
     *            the sub-transaction of the operation
     * @param coordData
     *            coordination data with the coordinator-specific properties (e.g.: keys)
     * @param entry
     *            the entry
     * @param context
     *            the context of the write request
     * @return <code>true</code> if the entry was registered successfully, <code>false</code> otherwise
     * @throws Capi3Exception
     *             a Capi3Exception
     */
    boolean registerEntry(NativeSubTransaction stx, CoordinationData coordData, NativeEntry entry,
            RequestContext context) throws Capi3Exception;

    /**
     * Unregisters an entry from this Coordinator.
     *
     * @param entry
     *            the Entry to unregister
     * @param context
     *            the context of the take/delete request
     * @param stx
     *            the sub-transaction
     * @return <code>true</code> if the entry was unregistered successfully, <code>false</code> otherwise
     */
    boolean unregisterEntry(NativeEntry entry, RequestContext context, NativeSubTransaction stx);

    /**
     * Sets the isolation manager.
     *
     * @param isolationManager
     *            the isolation manager
     */
    void setIsolationManager(NativeIsolationManager isolationManager);

    /**
     * Notifies the coordinator that on commit of the sub-transaction's parent transaction an entry is removed
     * (unregistered) from his data structure. This is done to prepare any locks the coordinator could have.
     *
     * @param stx
     *            the sub-transaction
     * @param entry
     *            the entry that is removed
     * @param context
     *            the context of the take/delete request
     * @throws CoordinatorLockedException
     *             if the coordinator is already locked
     */
    void prepareEntryRemoval(NativeSubTransaction stx, NativeEntry entry, RequestContext context)
            throws CoordinatorLockedException;

}
