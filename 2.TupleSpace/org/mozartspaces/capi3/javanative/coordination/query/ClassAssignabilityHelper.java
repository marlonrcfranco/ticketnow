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

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ClassAssignabilityHelper {

    private final Map<ClassTuple, Boolean> cache =
            new ConcurrentHashMap<ClassAssignabilityHelper.ClassTuple, Boolean>();

    private static ClassAssignabilityHelper instance = new ClassAssignabilityHelper();

    protected ClassAssignabilityHelper() {
        // No direct initialisation
    }

    public static ClassAssignabilityHelper getInstance() {
        return instance;
    }

    /**
     * @return the cache
     */
    public final Map<ClassTuple, Boolean> getCache() {
        return cache;
    }

    /**
     * Returns either <code>true</code> or <code>false</code> if the given class is compatible (assignable) to the given
     * other class. e.g. f(String.class, Object.class) would be true, f(Object.class, String.class) would be false.
     *
     * @param clazz
     *            the class that should be checked
     * @param forClass
     *            the class that the other class should be assignable to
     * @return <code>true</code> or <code>false</code>
     */
    public final boolean classIsValidTypeFor(final Class<?> clazz, final Class<?> forClass) {

        ClassTuple classTuple = new ClassTuple(clazz, forClass);
        Boolean cachedResult = cache.get(classTuple);

        if (cachedResult == null) {
            boolean result = forClass.isAssignableFrom(clazz);
            cache.put(classTuple, result);
            return result;
        }

        return cachedResult;
    }

    protected static final class ClassTuple {

        private final Class<?> clazz;
        private final Class<?> forClass;

        public ClassTuple(final Class<?> clazz, final Class<?> forClass) {
            this.clazz = clazz;
            this.forClass = forClass;
        }

        @Override
        public int hashCode() {
            int hashCode = 17;
            int hashMultiplier = 59;
            hashCode = hashCode * hashMultiplier + ((clazz == null) ? 0 : clazz.hashCode());
            hashCode = hashCode * hashMultiplier + ((forClass == null) ? 0 : forClass.hashCode());
            return hashCode;
        }

        @Override
        public boolean equals(final Object obj) {
            if (obj == null) {
                return false;
            }
            if (this == obj) {
                return true;
            }
            if (obj.getClass() != this.getClass()) {
                return false;
            }
            ClassTuple tuple2 = (ClassTuple) obj;
            if (clazz.equals(tuple2.clazz) == false) {
                return false;
            }
            if (forClass.equals(tuple2.forClass) == false) {
                return false;
            }
            return true;
        }
    }
}
