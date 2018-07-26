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

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

/**
 * A wrapper for the {@link Serializer} interface that uses Guava library to cache byte[] values of objects.
 *
 * The values a wrapped by weak references so this cache will not prevent them from being garbage collected.
 *
 * @author Jan Zarnikov
 */
public final class CachingSerializerWrapper implements Serializer {

    private final Serializer wrapped;

    private final Cache<Object, byte[]> cache;

    /**
     * Create a new caching serializer wrapper.
     * @param wrapped an initialized Serializer that you want to wrap
     * @param maxSize maximum number of objects that should be cached together with their byte[]-value
     */
    public CachingSerializerWrapper(final Serializer wrapped, final int maxSize) {
        if (wrapped == null) {
            throw new IllegalArgumentException("The wrapped Serializer must not be null.");
        }
        if (maxSize <= 0) {
            throw new IllegalArgumentException("The cache size must be greater or equal zero. If you don't want "
                    + "caching then don't use this wrapper.");
        }
        this.wrapped = wrapped;
        cache = CacheBuilder.newBuilder()
                .concurrencyLevel(10)
                .weakValues()
                .maximumSize(maxSize)
                .build();
    }

    @Override
    public <T> byte[] serialize(final T object) throws SerializationException {
        try {
            return cache.get(object, new Callable<byte[]>() {
                @Override
                public byte[] call() throws Exception {
                    return wrapped.serialize(object);
                }
            });
        } catch (ExecutionException e) {
            throw new SerializationException(e);
        }
    }

    @Override
    public <T> T deserialize(final byte[] serializedObject) throws SerializationException {
        return wrapped.deserialize(serializedObject);
    }

    @Override
    public <T> T copyObject(final T object) throws SerializationException {
        return wrapped.copyObject(object);
    }
}
