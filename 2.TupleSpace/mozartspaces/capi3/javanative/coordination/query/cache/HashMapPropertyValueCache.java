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
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.mozartspaces.capi3.javanative.coordination.query.NativeProperty;

/**
 * Property value cache that uses a {@link ConcurrentHashMap} internally.
 *
 * @author Martin Planer
 */
public final class HashMapPropertyValueCache implements PropertyValueCache {

    private final Map<Serializable, Map<NativeProperty, Object>> entries =
            new ConcurrentHashMap<Serializable, Map<NativeProperty, Object>>();

    @Override
    public synchronized void insert(final Serializable entry, final NativeProperty property, final Object value) {

        Map<NativeProperty, Object> cacheEntry = entries.get(entry);

        if (cacheEntry == null) {
            cacheEntry = new HashMap<NativeProperty, Object>();
            entries.put(entry, cacheEntry);
        }

        cacheEntry.put(property, value);
    }

    @Override
    public synchronized Object lookup(final Serializable entry, final NativeProperty property) {

        Map<NativeProperty, Object> cacheEntry = entries.get(entry);

        if (cacheEntry == null) {
            return CacheMiss.INSTANCE;
        }

        Object value = cacheEntry.get(property);

        if (value == null && !cacheEntry.containsKey(property)) {
            return CacheMiss.INSTANCE;
        }

        return value;
    }

    @Override
    public synchronized void purge(final Serializable entry) {
        entries.remove(entry);
    }
}
