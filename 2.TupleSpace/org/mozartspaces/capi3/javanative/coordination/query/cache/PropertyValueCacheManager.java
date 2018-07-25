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

import java.lang.reflect.Constructor;

import org.mozartspaces.util.LoggerFactory;
import org.slf4j.Logger;

/**
 * Utility class to create new PropertyValueCache instances.
 *
 * @author Martin Planer
 */
public final class PropertyValueCacheManager {

    private static final Logger log = LoggerFactory.get();

    /**
     * Creates a new PropertyValueCache instance.
     *
     * @return the instance
     */
    public static PropertyValueCache newPropertyValueCache() {

        // Try to get Guava based PVC
        try {
            Class<?> guavaClass = Class
                    .forName("org.mozartspaces.capi3.javanative.coordination.query.cache.GuavaPropertyValueCache");
            Constructor<?> constructor = guavaClass.getConstructor();
            return (PropertyValueCache) constructor.newInstance();
        } catch (Exception e) {
            log.info("Could not find and instantiate Guava based property value cache. "
                    + "Falling back to HashMap based implementation.");
        }

        // Fall back to HashMap PVC as a last resort
        return new HashMapPropertyValueCache();
    }

    private PropertyValueCacheManager() {
    }
}
