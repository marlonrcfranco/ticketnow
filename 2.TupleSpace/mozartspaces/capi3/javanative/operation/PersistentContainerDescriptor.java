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

import java.io.Serializable;
import java.util.List;

import org.mozartspaces.capi3.Coordinator;
import org.mozartspaces.capi3.javanative.coordination.NativeCoordinator;
import org.mozartspaces.capi3.javanative.persistence.PersistentCoordinator;
import org.mozartspaces.core.authorization.AuthorizationLevel;

/**
 * @author Jan Zarnikov
 */
public final class PersistentContainerDescriptor implements Serializable {

    private static final long serialVersionUID = 1L;

    private final long id;
    private final String name;

    private final List<Class<? extends NativeCoordinator>> obligatoryCoordinators;
    private final List<Class<? extends NativeCoordinator>> optionalCoordinators;

    private final List<? extends Coordinator> obligatoryCoordinatorArgs;
    private final List<? extends Coordinator> optionalCoordinatorArgs;

    private final List<PersistentCoordinator.CoordinatorRestoreTask> persistentObligatoryCoordinators;
    private final List<PersistentCoordinator.CoordinatorRestoreTask> persistentOptionalCoordinators;

    private final int size;
    private final AuthorizationLevel auth;

    /**
     * @param id
     *            the container ID
     * @param name
     *            the container name
     * @param obligatoryCoordinators
     *            the obligatory coordinators
     * @param optionalCoordinators
     *            the optional coordinators
     * @param obligatoryCoordinatorArgs
     *            the obligatory coordinator arguments
     * @param optionalCoordinatorArgs
     *            the optional coordinator arguments
     * @param persistentObligatoryCoordinators
     *            the restore tasks for the obligatory coordinators
     * @param persistentOptionalCoordinators
     *            the restore tasks for the optional coordinators
     * @param size
     *            the container size
     * @param auth
     *            the authorization level
     */
    public PersistentContainerDescriptor(final long id, final String name,
            final List<Class<? extends NativeCoordinator>> obligatoryCoordinators,
            final List<Class<? extends NativeCoordinator>> optionalCoordinators,
            final List<? extends Coordinator> obligatoryCoordinatorArgs,
            final List<? extends Coordinator> optionalCoordinatorArgs,
            final List<PersistentCoordinator.CoordinatorRestoreTask> persistentObligatoryCoordinators,
            final List<PersistentCoordinator.CoordinatorRestoreTask> persistentOptionalCoordinators, final int size,
            final AuthorizationLevel auth) {
        this.id = id;
        this.name = name;
        this.obligatoryCoordinators = obligatoryCoordinators;
        this.optionalCoordinators = optionalCoordinators;
        this.obligatoryCoordinatorArgs = obligatoryCoordinatorArgs;
        this.optionalCoordinatorArgs = optionalCoordinatorArgs;
        this.persistentObligatoryCoordinators = persistentObligatoryCoordinators;
        this.persistentOptionalCoordinators = persistentOptionalCoordinators;
        this.size = size;
        this.auth = auth;
    }

    /**
     * @return the container ID
     */
    public long getId() {
        return id;
    }

    /**
     * @return the container name
     */
    public String getName() {
        return name;
    }

    /**
     * @return the obligatory coordinators
     */
    public List<Class<? extends NativeCoordinator>> getObligatoryCoordinators() {
        return obligatoryCoordinators;
    }

    /**
     * @return the optional coordinators
     */
    public List<Class<? extends NativeCoordinator>> getOptionalCoordinators() {
        return optionalCoordinators;
    }

    /**
     * @return the obligatory coordinator arguments
     */
    public List<? extends Coordinator> getObligatoryCoordinatorArgs() {
        return obligatoryCoordinatorArgs;
    }

    /**
     * @return the optional coordinator arguments
     */
    public List<? extends Coordinator> getOptionalCoordinatorArgs() {
        return optionalCoordinatorArgs;
    }

    /**
     * @return the restore tasks for the obligatory coordinators
     */
    public List<PersistentCoordinator.CoordinatorRestoreTask> getPersistentObligatoryCoordinators() {
        return persistentObligatoryCoordinators;
    }

    /**
     * @return the restore tasks for the optional coordinators
     */
    public List<PersistentCoordinator.CoordinatorRestoreTask> getPersistentOptionalCoordinators() {
        return persistentOptionalCoordinators;
    }

    /**
     * @return the container size
     */
    public int getSize() {
        return size;
    }

    /**
     * @return the authorization level
     */
    public AuthorizationLevel getAuth() {
        return auth;
    }
}
