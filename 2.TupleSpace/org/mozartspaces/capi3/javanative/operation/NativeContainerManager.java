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
package org.mozartspaces.capi3.javanative.operation;

import java.io.Closeable;
import java.util.List;

import org.mozartspaces.capi3.ContainerOperationResult;
import org.mozartspaces.capi3.Coordinator;
import org.mozartspaces.capi3.DuplicateCoordinatorException;
import org.mozartspaces.capi3.InvalidContainerException;
import org.mozartspaces.capi3.InvalidContainerNameException;
import org.mozartspaces.capi3.IsolationLevel;
import org.mozartspaces.capi3.LocalContainerReference;
import org.mozartspaces.capi3.javanative.coordination.NativeCoordinator;
import org.mozartspaces.capi3.javanative.isolation.NativeSubTransaction;
import org.mozartspaces.capi3.javanative.persistence.PersistenceException;
import org.mozartspaces.core.MzsCoreRuntimeException;
import org.mozartspaces.core.RequestContext;
import org.mozartspaces.core.authorization.AuthorizationLevel;
import org.mozartspaces.core.metamodel.MetaDataProvider;
import org.mozartspaces.core.metamodel.Navigable;

/**
 * The container manager administrates the containers and executes the direct container operations.
 *
 * @author Martin Barisits
 * @author Tobias Doenz
 */
public interface NativeContainerManager extends Navigable, MetaDataProvider, Closeable {

    @Override
    void close() throws MzsCoreRuntimeException;

    /**
     * Creates a container and associates the provided coordinators with it.
     *
     * @param containerName
     *            name of the container
     * @param obligatoryCoordinatorArgs
     *            list of obligatory coordinators as specified in the request (for use in the meta model)
     * @param obligatoryCoordinators
     *            list of obligatory coordinators. Entries have to be registered at these coordinators when writing to
     *            the container
     * @param optionalCoordinatorArgs
     *            list of optional coordinators as specified in the request (for use in the meta model)
     * @param optionalCoordinators
     *            list of optional coordinators.
     * @param size
     *            number of entries the container can hold
     * @param isolationLevel
     *            isolation level to be used
     * @param stx
     *            sub-transaction to be used to isolate the data
     * @param auth
     *            authorization level to be used
     * @param forceInMemory
     *            whether the container should be in-memory (regardless of the persistence configuration)
     * @param context
     *            context information passed through the core, may be <code>null</code>
     *
     * @return a result object containing the reference of the created container
     */
    ContainerOperationResult createContainer(String containerName,
            List<? extends Coordinator> obligatoryCoordinatorArgs, List<NativeCoordinator> obligatoryCoordinators,
            List<? extends Coordinator> optionalCoordinatorArgs, List<NativeCoordinator> optionalCoordinators,
            int size, IsolationLevel isolationLevel, NativeSubTransaction stx, AuthorizationLevel auth,
            boolean forceInMemory, RequestContext context);

    /**
     * Destroys a container.
     *
     * @param cRef
     *            reference of the container to destroy
     * @param isolationLevel
     *            isolation level to be used
     * @param stx
     *            sub-transaction to be used to isolate the data
     * @param context
     *            context information passed through the core, may be <code>null</code>
     * @return a result object containing the reference of the destroyed container
     */
    ContainerOperationResult destroyContainer(LocalContainerReference cRef, IsolationLevel isolationLevel,
            NativeSubTransaction stx, RequestContext context);

    /**
     * Looks up a container by name.
     *
     * @param containerName
     *            name of the container to be looked up
     * @param isolationLevel
     *            isolation level to be used
     * @param stx
     *            sub-transaction to be used to isolate the data
     * @param context
     *            context information passed through the core, may be <code>null</code>
     * @return a result object containing the reference of the looked up container
     */
    ContainerOperationResult lookupContainer(String containerName, IsolationLevel isolationLevel,
            NativeSubTransaction stx, RequestContext context);

    /**
     * Lock a Container for exclusive access.
     *
     * @param cRef
     *            reference of the container to lock
     * @param isolationLevel
     *            isolation level to be used
     * @param stx
     *            sub-transaction to be used to isolate the data
     * @param context
     *            context information passed through the core, may be <code>null</code>
     * @return a result object containing the reference of the locked container
     */
    ContainerOperationResult lockContainer(LocalContainerReference cRef, IsolationLevel isolationLevel,
            NativeSubTransaction stx, RequestContext context);

    /**
     * Removes a container permanently from the container manager.
     *
     * @param cRef
     *            The container to remove
     */
    void purgeContainer(LocalContainerReference cRef);

    /**
     * Returns the container associated to a specified reference.
     *
     * @param cRef
     *            reference of the container
     * @return the associated container
     * @throws InvalidContainerException
     *             if the reference is associated to an invalid container
     */
    NativeContainer getContainer(LocalContainerReference cRef) throws InvalidContainerException;

    /**
     * Restore all containers, their coordinators and the content. Called during the restoration process before the
     * space starts accepting requests.
     *
     * @param stx
     *            sub-transaction of the restoration process
     * @throws PersistenceException
     *             when the underlying persistence layer fails
     * @throws InvalidContainerNameException
     *             when an invalid container name is encountered during the restoration
     * @throws DuplicateCoordinatorException
     *             when duplicate coordinators are encountered during the restoration
     */
    void restoreContainers(NativeSubTransaction stx) throws PersistenceException, InvalidContainerNameException,
            DuplicateCoordinatorException;

}
