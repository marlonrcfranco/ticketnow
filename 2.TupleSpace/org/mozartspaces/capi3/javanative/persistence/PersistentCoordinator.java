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
package org.mozartspaces.capi3.javanative.persistence;

import java.io.Serializable;

import org.mozartspaces.capi3.javanative.coordination.NativeCoordinator;
import org.mozartspaces.capi3.javanative.isolation.NativeSubTransaction;
import org.mozartspaces.capi3.javanative.operation.NativeContainer;

/**
 * All native coordinators that want to use the persistence have to implement this interface. For a given coordinator
 * always the same instance of {@link PersistenceContext} is used as parameter when invoking any of the methods defined
 * in this interface.
 *
 * @author Jan Zarnikov
 * @author Tobias Doenz
 */
public interface PersistentCoordinator extends NativeCoordinator {

    /**
     * Called during the restoration process after the coordinator is instantiated and before the container is restored.
     *
     * @param persistenceContext
     *            a persistence context for accessing the persistence
     * @throws PersistenceException
     *             when the pre-restore operation fails.
     */
    void preRestoreContent(PersistenceContext persistenceContext) throws PersistenceException;

    /**
     * Called during the restoration process after the container and its content are restored.
     *
     * @param persistenceContext
     *            a persistence context for accessing the persistence
     * @param nativeContainer
     *            the container to which this coordinator belongs
     * @param stx
     *            the subtransaction in which the restoration process runs
     * @throws PersistenceException
     *             post-restore operation fails
     */
    void postRestoreContent(PersistenceContext persistenceContext, NativeContainer nativeContainer,
            NativeSubTransaction stx) throws PersistenceException;

    /**
     * Called after the coordinator is created and added to the container. This method is called directly after the
     * {@link NativeCoordinator#init(NativeContainer, NativeSubTransaction, org.mozartspaces.core.RequestContext)
     * NativeCoordinator#init}.
     *
     * @param nativeContainer
     *            the container to which this coordinator belongs
     * @param persistenceContext
     *            a persistence context for accessing the persistence
     * @throws PersistenceException
     *             when the initialization fails
     */
    void initPersistence(NativeContainer nativeContainer, PersistenceContext persistenceContext)
            throws PersistenceException;

    /**
     * Called when the coordinator is deleted (because the container is deleted). The implementation should close and
     * destroy all its resources.
     *
     * @throws PersistenceException
     *             when the destruction fails.
     */
    void destroy() throws PersistenceException;

    /**
     * Get the restore task which can be used to create a new empty copy of this coordinator.
     *
     * @return a restore task
     */
    CoordinatorRestoreTask getRestoreTask();

    /**
     * The restore task is basically a serializable factory that can create a new empty instance of a given persistent
     * coordinator.
     */
    interface CoordinatorRestoreTask extends Serializable {

        /**
         * Create a new empty instance of the coordinator.
         *
         * @return a configured but empty coordinator
         */
        PersistentCoordinator restoreCoordinator();
    }

}
