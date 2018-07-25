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

/**
 * Creates exception instances of different types. The exception types are
 * identified by a type name and a corresponding creator needs to be added
 * before exceptions of that type can be created.
 *
 * @author Tobias Doenz
 */
@Immutable
public final class ExceptionFactory {

    private final Map<String, ExceptionCreator<?>> creators;
    private final Map<Class<? extends Exception>, String> typeNames;

    /**
     * Constructs a {@code ExceptionFactory}.
     */
    public ExceptionFactory() {
        creators = new ConcurrentHashMap<String, ExceptionCreator<?>>();
        typeNames = new ConcurrentHashMap<Class<? extends Exception>, String>();
    }

    /**
     * Adds a creator for a specific exception type.
     *
     * @param typeName
     *            an identifying name for the exception type
     * @param creator
     *            the creator for this exception type
     */
    public void addExceptionCreator(final String typeName, final ExceptionCreator<?> creator) {
        creators.put(typeName, creator);
        typeNames.put(getExceptionClass(creator), typeName);
    }

    @SuppressWarnings("unchecked")
    private Class<? extends Exception> getExceptionClass(final ExceptionCreator<?> creator) {
        ParameterizedType creatorInterface = (ParameterizedType) creator.getClass().getGenericInterfaces()[0];
        return (Class<? extends Exception>) creatorInterface.getActualTypeArguments()[0];
    }

    /**
     * Returns the type name of a exception.
     *
     * @param exception
     *            an instance of the exception
     * @return the type name of the specified exception
     */
    public String getTypeName(final Exception exception) {
        return typeNames.get(exception.getClass());
    }

    /**
     * Creates a exception instance of a specific type with the supplied
     * parameters.
     *
     * @param typeName
     *            the name for the exception type
     * @param message
     *            the exception message
     * @return a new exception instance of the specified type
     * @throws IllegalArgumentException
     *             if the type name is not known
     */
    public Exception createException(final String typeName, final String message) {
        ExceptionCreator<?> creator = creators.get(typeName);
        if (creator == null) {
            throw new IllegalArgumentException("No exception creator for type name " + typeName);
        }
        return creator.newException(message);
    }

}
