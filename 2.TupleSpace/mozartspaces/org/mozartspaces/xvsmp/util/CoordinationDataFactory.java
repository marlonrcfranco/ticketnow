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

import org.mozartspaces.capi3.CoordinationData;

/**
 * Creates coordination data instances of different types. The coordination data
 * types are identified by a type name and a corresponding creator needs to be
 * added before coordination data objects of that type can be created.
 *
 * @author Tobias Doenz
 */
@Immutable
public final class CoordinationDataFactory {

    private final Map<String, CoordinationDataCreator<?>> creators;
    private final Map<Class<? extends CoordinationData>, String> typeNames;

    /**
     * Constructs a {@code CoordinationDataFactory}.
     */
    public CoordinationDataFactory() {
        creators = new ConcurrentHashMap<String, CoordinationDataCreator<?>>();
        typeNames = new ConcurrentHashMap<Class<? extends CoordinationData>, String>();
    }

    /**
     * Adds a creator for a specific coordination data type.
     *
     * @param typeName
     *            an identifying name for the coordination data type
     * @param creator
     *            the creator for this coordination data type
     */
    public void addCoordinationDataCreator(final String typeName, final CoordinationDataCreator<?> creator) {
        creators.put(typeName, creator);
        typeNames.put(getCoordinationDataClass(creator), typeName);
    }

    @SuppressWarnings("unchecked")
    private Class<? extends CoordinationData> getCoordinationDataClass(final CoordinationDataCreator<?> creator) {
        ParameterizedType creatorInterface = (ParameterizedType) creator.getClass().getGenericInterfaces()[0];
        return (Class<? extends CoordinationData>) creatorInterface.getActualTypeArguments()[0];
    }

    /**
     * Returns the type name of a coordination data object.
     *
     * @param coordData
     *            a coordination data object
     * @return the type name of the specified coordination data object
     */
    public String getTypeName(final CoordinationData coordData) {
        return typeNames.get(coordData.getClass());
    }

    /**
     * Creates a coordination data instance of a specific type with the supplied
     * parameters.
     *
     * @param typeName
     *            the name for the coordination data type
     * @param name
     *            the name of the coordinator, to distinguish instances of the
     *            same coordinator type
     * @param params
     *            further coordination data parameters
     * @return a new coordination data instance of the specified type
     * @throws IllegalArgumentException
     *             if the type name is not known or any other argument is not
     *             valid
     */
    public CoordinationData createCoordinationData(final String typeName, final String name, final Object... params) {
        CoordinationDataCreator<?> creator = creators.get(typeName);
        if (creator == null) {
            throw new IllegalArgumentException("No selector creator for type name " + typeName);
        }
        return creator.newCoordinationData(name, params);
    }

}
