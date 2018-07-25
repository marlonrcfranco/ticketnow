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
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import org.mozartspaces.capi3.javanative.coordination.query.NativeProperty;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

/**
 * Property value cache with an internal Guava cache where entries expire 5 minutes after the last access.
 *
 * @author Martin Planer
 */
public final class GuavaPropertyValueCache implements PropertyValueCache {

    private final Cache<Serializable, Cache<NativeProperty, Object>> entryCache = CacheBuilder.newBuilder()
            .expireAfterAccess(5, TimeUnit.MINUTES).<Serializable, Cache<NativeProperty, Object>>build();

    /**
     *
     */
    public GuavaPropertyValueCache() {
    }

    @Override
    public void insert(final Serializable entry, final NativeProperty property, final Object value) {
        return;
        // throw new UnsupportedOperationException("Not implemented on Guava cache. SHOULD NEVER HAPPEN!");
    }

    @Override
    public Object lookup(final Serializable entry, final NativeProperty property) {

        try {
            // Try get Entry cache entry
            Cache<NativeProperty, Object> propertyCache = entryCache.get(entry,
                    new Callable<Cache<NativeProperty, Object>>() {

                        @Override
                        public Cache<NativeProperty, Object> call() throws Exception {

                            return CacheBuilder.newBuilder().expireAfterAccess(2, TimeUnit.MINUTES)
                                    .<NativeProperty, Object>build();
                        }
                    });

            // Try get property value cache entry
            Object value = propertyCache.get(property, new Callable<Object>() {

                @Override
                public Object call() throws Exception {

                    return property.getValue(entry);
                }
            });

            return value;

        } catch (ExecutionException e) {
            return CacheMiss.INSTANCE;
        }
    }

    @Override
    public void purge(final Serializable entry) {
        entryCache.invalidate(entry);
    }

}
