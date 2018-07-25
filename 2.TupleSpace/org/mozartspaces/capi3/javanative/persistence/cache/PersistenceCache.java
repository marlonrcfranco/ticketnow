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
package org.mozartspaces.capi3.javanative.persistence.cache;

import java.io.Serializable;

import org.mozartspaces.capi3.javanative.persistence.key.PersistenceKey;

/**
 * This cache is used to hold the values of {@link org.mozartspaces.capi3.javanative.persistence.StoredMap} in memory
 * for faster access.
 * The values remain in the cache until they are explicitly deleted or evicted by implementation-specific algorithm.
 *
 * @param <V>
 *     the type of the values
 *
 * @author Jan Zarnikov
 */
public interface PersistenceCache<V extends Serializable> {

    /**
     * Put a new key-value pair into the cache (possibly rewriting an already existing value with the same key).
     * @param key the new key
     * @param value the new value
     */
    void put(PersistenceKey<?> key, V value);

    /**
     * Explicitly delete a key-value pair from the cache. Nothing happens if the given key is not present.
     * @param key the key to be removed
     */
    void delete(PersistenceKey<?> key);

    /**
     * Get the value associated with the key if present in the cache.
     * @param key the key
     * @return the value associated with the given key of {@code null} if the key is not present.
     */
    V get(PersistenceKey<?> key);

}
