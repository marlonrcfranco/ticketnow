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
package org.mozartspaces.capi3.javanative.coordination.query.index;

import java.util.Set;

import org.mozartspaces.capi3.javanative.operation.NativeEntry;

/**
 * An extended search index that enables queries based on order (<, <=, >, >=, <= x <=).
 *
 * @author Martin Planer
 * @see SearchIndex
 */
public interface ExtendedSearchIndex extends SearchIndex {

    /**
     * Returns a list of matching entries. Matches are based on an order less than the given value;
     *
     * @param value
     *            the upper bound value (exclusive)
     * @param restrictClass
     *            the class to restrict the results to
     * @return a list of matching entries
     */
    Set<NativeEntry> lookupLessThan(final Comparable<?> value, final Class<?> restrictClass);

    /**
     * Returns a list of matching entries. Matches are based on an order less than or equal to the given value;
     *
     * @param value
     *            the upper bound value (inclusive)
     * @param restrictClass
     *            the class to restrict the results to
     * @return a list of matching entries
     */
    Set<NativeEntry> lookupLessThanOrEqualTo(final Comparable<?> value, final Class<?> restrictClass);

    /**
     * Returns a list of matching entries. Matches are based on an order greater than the given value;
     *
     * @param value
     *            the lower bound value (exclusive)
     * @param restrictClass
     *            the class to restrict the results to
     * @return a list of matching entries
     */
    Set<NativeEntry> lookupGreaterThan(final Comparable<?> value, final Class<?> restrictClass);

    /**
     * Returns a list of matching entries. Matches are based on an order greater than or equal to the given value;
     *
     * @param value
     *            the lower bound value (inclusive)
     * @param restrictClass
     *            the class to restrict the results to
     * @return a list of matching entries
     */
    Set<NativeEntry> lookupGreaterThanOrEqualTo(final Comparable<?> value, final Class<?> restrictClass);

    /**
     * Returns a list of matching entries. Matches are based on an order greater than or equal to the given lower bound
     * and less than or equal to the given upper bound;
     *
     * @param lowerBound
     *            the lower bound value (inclusive)
     * @param upperBound
     *            the upper bound value (inclusive)
     * @param restrictClass
     *            the class to restrict the results to
     * @return a list of matching entries
     */
    Set<NativeEntry> lookupBetween(final Comparable<?> lowerBound, final Comparable<?> upperBound,
            final Class<?> restrictClass);
}
