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
package org.mozartspaces.capi3;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Defines a search index for the field it is set on.
 *
 * @author Martin Planer
 */
@Target({ ElementType.FIELD, ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
public @interface QueryIndex {

    /**
     * The path to the element that should be indexed. This can be used to define indexes for nested properties. If not
     * specified, the path for the field that is annotated is used.
     */
    String[] path() default { };

    /**
     * The type of the index for this property. Default is {@link IndexType.BASIC}.
     */
    IndexType type() default IndexType.BASIC;

    /**
     * Determines the index type for a query index.
     *
     * @author Martin Planer
     */
    public enum IndexType {
        /**
         * No index. Reserved for future use.
         */
        NONE,
        /**
         * Basic index. Can only be used for exact equality matches.
         */
        BASIC,
        /**
         * Extended index. Enables queries based on order (<, <=, >, >=, <= x <=).
         */
        EXTENDED
    }
}
