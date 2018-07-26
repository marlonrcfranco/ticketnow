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
package org.mozartspaces.xvsmp.util;

import java.lang.reflect.ParameterizedType;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import net.jcip.annotations.Immutable;

import org.mozartspaces.capi3.Coordinator;

/**
 * Creates coordinator instances of different types. The coordinator types are
 * identified by a type name and a corresponding creator needs to be added
 * before coordinators of that type can be created.
 *
 * @author Tobias Doenz
 */
@Immutable
public final class CoordinatorFactory {

    private final Map<String, CoordinatorCreator<?>> creators;
    private final Map<Class<? extends Coordinator>, String> typeNames;

    /**
     * Constructs a {@code CoordinatorFactory}.
     */
    public CoordinatorFactory() {
        creators = new ConcurrentHashMap<String, CoordinatorCreator<?>>();
        typeNames = new ConcurrentHashMap<Class<? extends Coordinator>, String>();
    }

    /**
     * Adds a creator for a specific coordinator type.
     *
     * @param typeName
     *            an identifying name for the coordinator type
     * @param creator
     *            the creator for this coordinator type
     */
    public void addCoordinatorCreator(final String typeName, final CoordinatorCreator<?> creator) {
        creators.put(typeName, creator);
        typeNames.put(getCoordinatorClass(creator), typeName);
    }

    @SuppressWarnings("unchecked")
    private Class<? extends Coordinator> getCoordinatorClass(final CoordinatorCreator<?> creator) {
        ParameterizedType creatorInterface = (ParameterizedType) creator.getClass().getGenericInterfaces()[0];
        return (Class<? extends Coordinator>) creatorInterface.getActualTypeArguments()[0];
    }

    /**
     * Returns the type name of a coordinator.
     *
     * @param coordinator
     *            an instance of the coordinator
     * @return the type name of the specified coordinator
     */
    // TODO move to coordinator?
    public String getTypeName(final Coordinator coordinator) {
        return typeNames.get(coordinator.getClass());
    }

    /**
     * Creates a coordinator instance of a specific type with the supplied
     * parameters.
     *
     * @param typeName
     *            the name for the coordinator type
     * @param name
     *            the name of the coordinator, to distinguish instances of the
     *            same coordinator type
     * @param params
     *            further coordinator parameters
     * @return a new coordinator instance of the specified type
     * @throws IllegalArgumentException
     *             if the type name is not known or any other argument is not
     *             valid
     */
    public Coordinator createCoordinator(final String typeName, final String name, final Object... params) {
        CoordinatorCreator<?> creator = creators.get(typeName);
        if (creator == null) {
            throw new IllegalArgumentException("No coordinator creator for type name " + typeName);
        }
        return creator.newCoordinator(name, params);
    }

}
