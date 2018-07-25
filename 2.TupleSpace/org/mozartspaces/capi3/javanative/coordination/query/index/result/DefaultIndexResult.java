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
package org.mozartspaces.capi3.javanative.coordination.query.index.result;

import java.util.Collections;
import java.util.Set;

import org.mozartspaces.capi3.ComparableProperty.GreaterThanMatchmaker;
import org.mozartspaces.capi3.ComparableProperty.GreaterThanOrEqualToMatchmaker;
import org.mozartspaces.capi3.ComparableProperty.LessThanMatchmaker;
import org.mozartspaces.capi3.ComparableProperty.LessThanOrEqualToMatchmaker;
import org.mozartspaces.capi3.Property.AbstractValuePropertyMatchmaker;
import org.mozartspaces.capi3.Property.EqualMatchmaker;
import org.mozartspaces.capi3.QueryIndex.IndexType;
import org.mozartspaces.capi3.javanative.coordination.query.index.ExtendedSearchIndex;
import org.mozartspaces.capi3.javanative.coordination.query.index.SearchIndex;
import org.mozartspaces.capi3.javanative.coordination.query.index.SearchIndexManager;
import org.mozartspaces.capi3.javanative.operation.NativeEntry;

/**
 * @author Martin Planer
 */
public final class DefaultIndexResult implements IndexResult {

    private final Set<NativeEntry> entries;
    private final Set<Class<?>> indexedClasses;

    private DefaultIndexResult(final Set<NativeEntry> entries, final Set<Class<?>> set) {
        this.indexedClasses = Collections.unmodifiableSet(set);
        this.entries = Collections.unmodifiableSet(entries);
    }

    @Override
    public Set<NativeEntry> getEntries() {
        return entries;
    }

    @Override
    public Set<Class<?>> getIndexedClasses() {
        return indexedClasses;
    }

    /**
     * Returns the index result for the given matchmaker.
     *
     * @param matchmaker
     *            the matchmaker for the index query
     * @param indexManager
     *            the index manager
     * @return the index result for the given matchmaker
     */
    public static IndexResult newQueryIndexResult(final AbstractValuePropertyMatchmaker matchmaker,
            final SearchIndexManager indexManager) {

        // Don't allow value property comparisons
        if (matchmaker.getValue() == null) {
            return null;
        }

        // Check if only basic index is needed and take the shortcut if so
        if (EqualMatchmaker.class.isAssignableFrom(matchmaker.getClass())) {
            return newBasicIndexResult((EqualMatchmaker) matchmaker, indexManager);
        } else {
            return newExtendedIndexResult(matchmaker, indexManager);
        }
    }

    private static IndexResult newBasicIndexResult(final EqualMatchmaker matchmaker,
            final SearchIndexManager indexManager) {

        SearchIndex index = indexManager.getIndex(matchmaker.getProperty().getPath(), IndexType.BASIC);

        if (index == null) {
            return null;
        }

        Set<NativeEntry> entries = index.lookupEqualTo(matchmaker.getValue(), matchmaker.getProperty().getEntryClazz());

        return new DefaultIndexResult(entries, index.getIndexedClasses());
    }

    private static IndexResult newExtendedIndexResult(final AbstractValuePropertyMatchmaker matchmaker,
            final SearchIndexManager indexManager) {

        // value must be comparable
        if (!Comparable.class.isAssignableFrom(matchmaker.getValue().getClass())) {
            return null;
        }

        Comparable<?> value = (Comparable<?>) matchmaker.getValue();

        ExtendedSearchIndex index = (ExtendedSearchIndex) indexManager.getIndex(matchmaker.getProperty().getPath(),
                IndexType.EXTENDED);

        if (index == null) {
            return null;
        }

        Set<NativeEntry> entries = Collections.emptySet();

        if (matchmaker.getClass().equals(LessThanMatchmaker.class)) {
            entries = index.lookupLessThan(value, matchmaker.getProperty().getEntryClazz());
        } else if (matchmaker.getClass().equals(LessThanOrEqualToMatchmaker.class)) {
            entries = index.lookupLessThanOrEqualTo(value, matchmaker.getProperty().getEntryClazz());
        } else if (matchmaker.getClass().equals(GreaterThanMatchmaker.class)) {
            entries = index.lookupGreaterThan(value, matchmaker.getProperty().getEntryClazz());
        } else if (matchmaker.getClass().equals(GreaterThanOrEqualToMatchmaker.class)) {
            entries = index.lookupGreaterThanOrEqualTo(value, matchmaker.getProperty().getEntryClazz());
        }

        return new DefaultIndexResult(entries, index.getIndexedClasses());
    }
}
