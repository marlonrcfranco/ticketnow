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

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class ReflectionUtils {

    public static List<Field> getDeclaredFieldsIncludingSuperclasses(final Class<?> clazzToInspect) {
        Class<?> clazz = clazzToInspect;
        List<Field> allFields = new ArrayList<Field>();
        while (clazz != Object.class) {
            Field[] fields = clazz.getDeclaredFields();
            for (Field field : fields) {

                // Skip synthetic (compiler generated) fields like outer class references (this$0).
                if (field.isSynthetic()) {
                    continue;
                }

                // Skip private fields of superclasses
                // TODO is this behavior correct?
//                if (Modifier.isPrivate(field.getModifiers()) && clazz != clazzToInspect) {
//                    continue;
//                }

                allFields.add(field);
            }
            clazz = clazz.getSuperclass();
        }
        return allFields;
    }

    /**
     * Checks if the given Class is a valid (sub)type of the other Class (restrictClass).
     *
     * @param clazz
     *            the class to be checked
     * @param restrictClass
     *            the class the other class should be assignable to
     * @return <code>true</code> or <code>false</code>
     */
    public static boolean classIsValidTypeFor(final Class<?> clazz, final Class<?> restrictClass) {

        if (restrictClass == null) {
            return true;
        }

        return ClassAssignabilityHelper.getInstance().classIsValidTypeFor(clazz, restrictClass);
    }
}
