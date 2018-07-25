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

import org.mozartspaces.capi3.Selector;

/**
 * Creates selector instances of different types. The selector types are
 * identified by a type name and a corresponding creator needs to be added
 * before selector of that type can be created.
 *
 * @author Tobias Doenz
 */
@Immutable
public final class SelectorFactory {

    private final Map<String, SelectorCreator<?>> creators;
    private final Map<Class<? extends Selector>, String> typeNames;

    /**
     * Constructs a {@code SelectorFactory}.
     */
    public SelectorFactory() {
        creators = new ConcurrentHashMap<String, SelectorCreator<?>>();
        typeNames = new ConcurrentHashMap<Class<? extends Selector>, String>();
    }

    /**
     * Adds a creator for a specific selector type.
     *
     * @param typeName
     *            an identifying name for the selector type
     * @param creator
     *            the creator for this selector type
     */
    public void addSelectorCreator(final String typeName, final SelectorCreator<?> creator) {
        creators.put(typeName, creator);
        typeNames.put(getSelectorClass(creator), typeName);
    }

    @SuppressWarnings("unchecked")
    private Class<? extends Selector> getSelectorClass(final SelectorCreator<?> creator) {
        ParameterizedType creatorInterface = (ParameterizedType) creator.getClass().getGenericInterfaces()[0];
        return (Class<? extends Selector>) creatorInterface.getActualTypeArguments()[0];
    }

    /**
     * Returns the type name of a selector.
     *
     * @param selector
     *            an instance of the selector
     * @return the type name of the specified selector
     */
    public String getTypeName(final Selector selector) {
        return typeNames.get(selector.getClass());
    }

    /**
     * Creates a selector instance of a specific type with the supplied
     * parameters.
     *
     * @param typeName
     *            the name for the selector type
     * @param name
     *            the name of the coordinator, to distinguish instances of the same
     *            coordinator type
     * @param count
     *            the entry count for this selector
     * @param params
     *            further selector parameters
     * @return a new selector instance of the specified type
     * @throws IllegalArgumentException
     *             if the type name is not known or any other argument is not
     *             valid
     */
    public Selector createSelector(final String typeName, final Integer count, final String name,
            final Object... params) {
        SelectorCreator<?> creator = creators.get(typeName);
        if (creator == null) {
            throw new IllegalArgumentException("No selector creator for type name " + typeName);
        }
        return creator.newSelector(name, count, params);
    }

}
