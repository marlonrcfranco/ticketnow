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

import org.mozartspaces.capi3.LogItem;
import org.mozartspaces.capi3.javanative.isolation.NativeSubTransaction;
import org.mozartspaces.capi3.javanative.operation.NativeEntry;

/**
 * A concurrent {@link ExtendedSearchIndex} implementation.
 *
 * @author Martin Planer
 */
public final class DefaultExtendedSearchIndex implements ExtendedSearchIndex {

    private final ExtendedSearchIndex index;

    public DefaultExtendedSearchIndex(final String[] path) {
        index = new TreeMapExtendedSearchIndex(path);
    }

    @Override
    public void index(final NativeEntry entry, final NativeSubTransaction stx) {

        index.index(entry, stx);

        if (stx != null) {
            stx.addLog(new LogItem() {

                @Override
                public void rollbackTransaction() {
                }

                @Override
                public void rollbackSubTransaction() {
                    remove(entry, null);
                }

                @Override
                public void commitTransaction() {
                }

                @Override
                public void commitSubTransaction() {
                }
            });
        }
    }

    @Override
    public Set<NativeEntry> lookupEqualTo(final Object value, final Class<?> restrictClass) {

        return index.lookupEqualTo(value, restrictClass);
    }

    @Override
    public void remove(final NativeEntry entry, final NativeSubTransaction stx) {

        index.remove(entry, stx);

        if (stx != null) {
            stx.addLog(new LogItem() {

                @Override
                public void rollbackTransaction() {
                }

                @Override
                public void rollbackSubTransaction() {
                    index(entry, null);
                }

                @Override
                public void commitTransaction() {
                }

                @Override
                public void commitSubTransaction() {
                }
            });
        }
    }

    @Override
    public void addClassToIndex(final Class<?> clazz) {
        index.addClassToIndex(clazz);
    }

    @Override
    public Set<Class<?>> getIndexedClasses() {
        return index.getIndexedClasses();
    }

    @Override
    public Set<NativeEntry> lookupLessThan(final Comparable<?> value, final Class<?> restrictClass) {
        return index.lookupLessThan(value, restrictClass);
    }

    @Override
    public Set<NativeEntry> lookupLessThanOrEqualTo(final Comparable<?> value, final Class<?> restrictClass) {
        return index.lookupLessThanOrEqualTo(value, restrictClass);
    }

    @Override
    public Set<NativeEntry> lookupGreaterThan(final Comparable<?> value, final Class<?> restrictClass) {
        return index.lookupGreaterThan(value, restrictClass);
    }

    @Override
    public Set<NativeEntry> lookupGreaterThanOrEqualTo(final Comparable<?> value, final Class<?> restrictClass) {
        return index.lookupGreaterThanOrEqualTo(value, restrictClass);
    }

    @Override
    public Set<NativeEntry> lookupBetween(final Comparable<?> lowerBound, final Comparable<?> upperBound,
            final Class<?> restrictClass) {
        return index.lookupBetween(lowerBound, upperBound, restrictClass);
    }
}
