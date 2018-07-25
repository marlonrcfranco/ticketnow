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
package org.mozartspaces.capi3.javanative.persistence.key;

/**
 * A {@link org.mozartspaces.capi3.javanative.persistence.StoredMap} can use any type as key. In order to efficiently
 * store the keys in database they have to be converted to an indexable form. The implementations of this interface
 * wrap the keys and convert them.<br/><br/>
 *
 * <b>Persistence keys must be immutable and the values they return with {@link Object#equals(Object)} and
 * {@link Object#hashCode()} must be constant over time.</b>
 *
 * @param <K> type of the actual key
 *
 * @author Jan Zarnikov
 */
public interface PersistenceKey<K> {

    /**
     * Whether or not this key can be converted to long.
     * @return {@code true} if the actual key can be converted to a long value.
     */
    boolean isConvertibleToLong();

    /**
     * Whether or not this key can be converted to a String.
     * @return {@code true} if the actual key can be converted to a String value
     */
    boolean isConvertibleToString();

    /**
     * The key as String value. Should only be called when
     * {@link org.mozartspaces.capi3.javanative.persistence.key.PersistenceKey#isConvertibleToString()} returns
     * {@code true}.
     * @return key as String
     */
    String asString();

    /**
     * The key as String value. Should only be called when
     * {@link PersistenceKey#isConvertibleToLong()} returns  {@code true}.
     * @return key as long
     */
    long asLong();

    /**
     * The serialized form of the key as {@code byte[]}.
     * @return the serialized key.
     */
    byte[] asByteArray();

    /**
     * Returns the actual key.
     * @return the actual key object
     */
    K getKey();
}
