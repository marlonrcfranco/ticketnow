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
package org.mozartspaces.capi3.javanative.coordination.query;

import java.io.Serializable;

import org.mozartspaces.capi3.javanative.coordination.query.cache.PropertyValueCache;
import org.mozartspaces.capi3.javanative.coordination.query.cache.PropertyValueCache.CacheMiss;

/**
 * This property a {@link NativeProperty} and transparently performs cache lookups and inserts through the property
 * value cache.
 *
 * @author Martin Planer
 */
public final class DefaultCacheProperty implements NativeProperty {

    private final NativeProperty property;
    private final PropertyValueCache cache;

    /**
     * Creates a new {@link DefaultCacheProperty}.
     *
     * @param defaultProperty
     *            The {@link NativeProperty} to wrap. Must not be <code>null</code>!
     * @param cache
     *            The {@link PropertyValueCache} to use. Must not be <code>null</code>!
     */
    public DefaultCacheProperty(final NativeProperty defaultProperty, final PropertyValueCache cache) {
        this.property = defaultProperty;
        this.cache = cache;
    }

    @Override
    public Object getValue(final Serializable object) {

        Object value = cache.lookup(object, property);

        if (value.getClass().equals(CacheMiss.class)) {
            value = property.getValue(object);
            cache.insert(object, property, value);
        }

        return value;
    }

    @Override
    public int hashCode() {
        return property.hashCode();
    }

    @Override
    public boolean equals(final Object obj) {
        return property.equals(obj);
    }
}
