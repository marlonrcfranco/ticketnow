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

import java.lang.reflect.Constructor;

import org.mozartspaces.util.LoggerFactory;
import org.slf4j.Logger;

/**
 * Factory to create a caching serializer.
 *
 * @author Jan Zarnikov
 */
public final class SerializationCacheFactory {

    private static final Logger log = LoggerFactory.get();

    /**
     * Creates a caching serializer with reflection that wraps a standard MozartSpaces serializer.
     *
     * @param serializer
     *            the serializer to wrap with the caching serializer
     * @param maxSize
     *            the maximum size of the cache
     * @return the caching serializer
     */
    public static Serializer createCachingSerializer(final Serializer serializer, final int maxSize) {
        try {
            @SuppressWarnings("unchecked")
            Class<Serializer> cachingWrapperClass = (Class<Serializer>) Class
                    .forName("org.mozartspaces.core.util.CachingSerializerWrapper");
            Constructor<Serializer> constructor = cachingWrapperClass.getConstructor(Serializer.class, int.class);
            return constructor.newInstance(serializer, maxSize);
        } catch (ClassNotFoundException e) {
            log.error("Could not load the class of caching serializer. "
                    + "Make sure you've added the dependency org.mozartspaces:mozartspaces-core-cache to your project",
                    e);
            return serializer;
        } catch (Exception e) {
            log.error("Could not initialize serialization cache.", e);
            return serializer;
        }
    }

    private SerializationCacheFactory() {
    }
}
