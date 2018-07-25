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
package org.mozartspaces.capi3.javanative.coordination.query.cache;

import java.io.Serializable;

import org.mozartspaces.capi3.javanative.coordination.query.NativeProperty;

/**
 * Caches the lookup for property values in objects to decrease reflective traversal through the object tree.
 *
 * @author Martin Planer
 */
public interface PropertyValueCache {

    /**
     * Insert the given entry and corresponding property value into the cache. If a given entry already exists in the
     * cache, the old value gets overwritten by the new one.
     *
     * @param entry
     *            the entry
     * @param property
     *            the property
     * @param value
     *            the property value
     */
    void insert(final Serializable entry, final NativeProperty property, final Object value);

    /**
     * Performs a lookup for the given entry and property on the cache.
     *
     * @param entry
     *            the entry
     * @param property
     *            the property
     * @return the cached property value or
     *         {@link org.mozartspaces.capi3.javanative.coordination.query.cache.PropertyValueCache.CacheMiss.INSTANCE
     *         CacheMiss.INSTANCE} if no cache entry could be found
     */
    Object lookup(final Serializable entry, final NativeProperty property);

    /**
     * Purge the given entry and all corresponding property values from the cache.
     *
     * @param entry
     *            the entry to be purged
     */
    void purge(final Serializable entry);

    /**
     * Value for cache miss.
     */
    public static enum CacheMiss {
        /**
         * Enum singleton instance.
         */
        INSTANCE
    }
}
