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
package org.mozartspaces.core.util;

/**
 * Serializes and deserializes objects. Used for remote communication and
 * copying entries.
 *
 * @author Tobias Doenz
 */
public interface Serializer {

    /**
     * Serializes an object to a byte array.
     *
     * @param <T>
     *            the type of the object
     * @param object
     *            the object to serialize
     * @return the serialized object
     * @throws SerializationException
     *             if serializing the object failed
     */
    <T> byte[] serialize(T object) throws SerializationException;

    /**
     *
     * @param <T>
     *            the type of the object
     * @param serializedObject
     *            the serialized object
     * @return the deserialized object
     * @throws SerializationException
     *             if deserializing an object failed
     */
    <T> T deserialize(final byte[] serializedObject) throws SerializationException;

    /**
     * Creates a copy of an object by serializing and deserializing it.
     *
     * @param <T>
     *            the type of the object
     * @param object
     *            the object to copy
     * @return the object copy
     * @throws SerializationException
     *             if serializing or deserializing the object failed
     */
    <T> T copyObject(T object) throws SerializationException;
}
