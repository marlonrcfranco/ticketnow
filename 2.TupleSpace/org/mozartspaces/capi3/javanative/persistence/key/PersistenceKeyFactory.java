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
 * A persistence key factory creates {@link PersistenceKey}s from their serialized representation. <br/>
 * <br/>
 * Implementations of this class must be thread-safe.
 *
 * @param <K>
 *            the type of keys
 *
 * @author Jan Zarnikov
 */
public interface PersistenceKeyFactory<K> {

    /**
     * Creates a new persistence key from an actual key object. This is the inverse of
     * {@link org.mozartspaces.capi3.javanative.persistence.key.PersistenceKey#getKey()}
     *
     * @param key
     *            the key object.
     * @return a new persistence key representing the key object
     */
    PersistenceKey<K> createPersistenceKey(K key);

    /**
     * Creates a new persistence key from a serialized key. This is the inverse of
     * {@link org.mozartspaces.capi3.javanative.persistence.key.PersistenceKey#asByteArray()}
     *
     * @param data
     *            the serialized key
     * @return a new persistence key representing the key object
     */
    PersistenceKey<K> createPersistenceKeyFromByteArray(byte[] data);

    /**
     * Creates a new persistence key from a long value. This method should only be called if
     * {@link org.mozartspaces.capi3.javanative.persistence.key.PersistenceKeyFactory#canConvertFromLong()} returns
     * {@code true}. This is the inverse of {@link PersistenceKey#asLong()} ()}
     *
     * @param key
     *            the key as long
     * @return a new persistence key representing the key object
     */
    PersistenceKey<K> createPersistenceKeyFromLong(long key);

    /**
     * Creates a new persistence key from a long value. This method should only be called if
     * {@link PersistenceKeyFactory#canConvertFromString()} ()} returns {@code true}. This is the inverse of
     * {@link org.mozartspaces.capi3.javanative.persistence.key.PersistenceKey#asString()}.
     *
     * @param key
     *            the key as String
     * @return a new persistence key representing the key object
     */
    PersistenceKey<K> createPersistenceKeyFromString(String key);

    /**
     * Whether or not this factory can convert long values.
     *
     * @return {@code true} if this factory can create persistence keys from long values.
     */
    boolean canConvertFromLong();

    /**
     * Whether or not this factory can convert String values.
     *
     * @return {@code true} if this factory can create persistence keys from String values.
     */
    boolean canConvertFromString();
}
