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
package org.mozartspaces.capi3;

import java.util.List;

import org.mozartspaces.core.Entry;
import org.mozartspaces.core.RequestContext;
import org.mozartspaces.core.authorization.AuthorizationLevel;

/**
 * Main interface of CAPI-3, part of the internal API used by the runtime to access containers and entries in them.
 *
 * @author Martin Barisits
 * @author Tobias Doenz
 * @author Stefan Crass
 */
public interface Capi3 {

    /**
     * Writes an entry to a container. The entry is also registered to the associated
     * coordinators.
     *
     * @param cRef
     *            the reference to a container
     * @param entry
     *            the entry with the object that should be written
     * @param isolationLevel
     *            isolation level to be used
     * @param stx
     *            subTransaction to be used to isolate the data
     * @param context
     *            context information passed through the core, may be <code>null</code>
     * @return an OperationResult containing the relevant information
     */
    OperationResult executeWriteOperation(LocalContainerReference cRef, Entry entry, IsolationLevel isolationLevel,
            SubTransaction stx, RequestContext context);

    /**
     * Reads entries from a container with a user-defined selector chain.
     *
     * @param cRef
     *            the reference to a container
     * @param selectors
     *            the selector chain which is responsible for filtering entries
     * @param isolationLevel
     *            isolation level to be used
     * @param stx
     *            subTransaction to be used to isolate the data
     * @param context
     *            context information passed through the core, may be <code>null</code>
     * @return an EntryOperationResult containing the relevant information
     */
    EntryOperationResult executeReadOperation(LocalContainerReference cRef, List<? extends Selector> selectors,
            IsolationLevel isolationLevel, SubTransaction stx, RequestContext context);

    /**
     * Takes entries from a container with a user-defined selector chain.
     *
     * @param cRef
     *            the reference to a container
     * @param selectors
     *            the selector chain which is responsible for filtering entries
     * @param isolationLevel
     *            isolation level to be used
     * @param stx
     *            subTransaction to be used to isolate the data
     * @param context
     *            context information passed through the core, may be <code>null</code>
     * @return an EntryOperationResult containing the relevant information
     */
    EntryOperationResult executeTakeOperation(LocalContainerReference cRef, List<? extends Selector> selectors,
            IsolationLevel isolationLevel, SubTransaction stx, RequestContext context);

    /**
     * Creates a container with the specified properties (name, coordinators etc.).
     *
     *
     * @param containerName
     *            name of the container
     * @param obligatoryCoordinators
     *            list of obligatory coordinators. Entries have to be registered at these coordinators when writing to
     *            the container
     * @param optionalCoordinators
     *            list of optional coordinators.
     * @param size
     *            number of entries the container can hold
     * @param isolationLevel
     *            isolation level to be used
     * @param stx
     *            subTransaction to be used to isolate the data
     * @param authLevel
     *            authorization level that specifies if access to the container should be secured
     * @param forceInMemory
     *            override the persistence configuration and create an in-memory-only container. Default is
     *            {@link org.mozartspaces.core.MzsConstants.Container#DEFAULT_FORCE_IN_MEMORY}.
     * @param context
     *            context information passed through the core, may be <code>null</code>
     * @return an ContainerOperationResult containing the ContainerReference of the created container
     */
    ContainerOperationResult executeContainerCreateOperation(String containerName,
            List<? extends Coordinator> obligatoryCoordinators, List<? extends Coordinator> optionalCoordinators,
            int size, IsolationLevel isolationLevel, SubTransaction stx, AuthorizationLevel authLevel,
            boolean forceInMemory, RequestContext context);

    /**
     * Destroys the container referenced by the cRef.
     *
     * @param cRef
     *            the reference to the container which should be destroyed
     * @param isolationLevel
     *            isolation level to be used
     * @param stx
     *            subTransaction to be used to isolate the data
     * @param context
     *            context information passed through the core, may be <code>null</code>
     * @return an ContainerOperationResult containing the relevant information
     */
    ContainerOperationResult executeContainerDestroyOperation(LocalContainerReference cRef,
            IsolationLevel isolationLevel, SubTransaction stx, RequestContext context);

    /**
     * Tries to find a container by name and return its reference.
     *
     * @param containerName
     *            name of the container to be looked up
     * @param isolationLevel
     *            isolation level to be used
     * @param stx
     *            subTransaction to be used to isolate the data
     * @param context
     *            context information passed through the core, may be <code>null</code>
     * @return an ContainerOperationResult containing the ContainerReference
     */
    ContainerOperationResult executeContainerLookupOperation(String containerName, IsolationLevel isolationLevel,
            SubTransaction stx, RequestContext context);

    /**
     * Locks a container for exclusive access.
     *
     * @param cRef
     *            ContainerReference of the Container to lock
     * @param isolationLevel
     *            isolation level to be used
     * @param stx
     *            subTransaction to be used to isolate the data
     * @param context
     *            context information passed through the core, may be <code>null</code>
     * @return an ContainerOperationResult containing the ContainerReference
     */
    ContainerOperationResult executeContainerLockOperation(LocalContainerReference cRef, IsolationLevel isolationLevel,
            SubTransaction stx, RequestContext context);

    /**
     * Creates a transaction and returns it to the user.
     *
     * @return Transaction
     */
    Transaction newTransaction();

    /**
     * Stops the CAPI-3 Core.
     */
    void shutDown();

}
