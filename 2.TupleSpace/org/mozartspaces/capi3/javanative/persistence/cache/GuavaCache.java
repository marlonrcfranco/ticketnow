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

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

/**
 * Persistence cache based on the Guava library. Both keys and value are wrapped with soft references so they can be
 * garbage collected.
 *
 * @param <V> type of values that should be cached
 *
 * @author Jan Zarnikov
 */
public final class GuavaCache<V extends Serializable> implements PersistenceCache<V> {

    private final Cache<PersistenceKey<?>, V> wrapped;

    /**
     * Create a new persistence cache.
     * @param size maximum number of values that should be cached. After this limit is reached values will be evicted
     *             based on LRU strategy
     */
    public GuavaCache(final long size) {
        this.wrapped = CacheBuilder.newBuilder()
                .concurrencyLevel(1)
                .maximumSize(size)
                .build();
    }

    @Override
    public void delete(final PersistenceKey<?> key) {
        wrapped.invalidate(key);
    }

    @Override
    public void put(final PersistenceKey<?> key, final V value) {
        wrapped.put(key, value);
    }

    @Override
    public V get(final PersistenceKey<?> key) {
        return wrapped.getIfPresent(key);
    }
}
