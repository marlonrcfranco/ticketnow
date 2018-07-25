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

import org.mozartspaces.capi3.javanative.isolation.NativeSubTransaction;
import org.mozartspaces.capi3.javanative.operation.NativeEntry;

/**
 * Defines a search index to lookup entries quickly. Matches with this index are only based on exact equality.
 *
 * @author Martin Planer
 * @see ExtendedSearchIndex
 */
public interface SearchIndex {

    /**
     * Add a {@link NativeEntry} to the index if it is not already present.
     *
     * @param entry
     *            the entry to be indexed
     */
    void index(NativeEntry entry, NativeSubTransaction stx);

    /**
     * Returns a list of matching entries. Matches are based on equality to the given value;
     *
     * @param value
     *            the value to look for equality
     * @param restrictClass
     *            the class to restrict the results to
     * @return a list of matching entries
     */
    Set<NativeEntry> lookupEqualTo(Object value, Class<?> restrictClass);

    /**
     * Remove a {@link NativeEntry} and all corresponding index entries from the index.
     *
     * @param entry
     *            the entry to be removed from the index
     */
    void remove(NativeEntry entry, NativeSubTransaction stx);

    /**
     * Add the given class to the list of classes to be indexed by this index.
     *
     * @param clazz
     *            the class to be indexed
     */
    void addClassToIndex(Class<?> clazz);

    /**
     * Return the set of Classes that are indexed by this index.
     *
     * @return the set of Classes that are indexed by this index
     */
    Set<Class<?>> getIndexedClasses();
}
