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

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Set;

import org.mozartspaces.capi3.CountNotMetException;
import org.mozartspaces.capi3.Matchmaker;
import org.mozartspaces.capi3.Property.AbstractValuePropertyMatchmaker;
import org.mozartspaces.capi3.Query.MatchmakerFilter;
import org.mozartspaces.capi3.javanative.coordination.DefaultQueryCoordinator;
import org.mozartspaces.capi3.javanative.coordination.query.AbstractNativeFilter;
import org.mozartspaces.capi3.javanative.coordination.query.DefaultMatchmakerFilter;
import org.mozartspaces.capi3.javanative.coordination.query.DefaultQuery;
import org.mozartspaces.capi3.javanative.coordination.query.NativeFilter;
import org.mozartspaces.capi3.javanative.coordination.query.NativeMatchmaker;
import org.mozartspaces.capi3.javanative.coordination.query.index.result.DefaultIndexResult;
import org.mozartspaces.capi3.javanative.coordination.query.index.result.IndexResult;
import org.mozartspaces.capi3.javanative.operation.NativeEntry;

/**
 * Filter which evaluates every entry against a matchmaker.
 *
 * @author Martin Barisits
 * @author Martin Planer
 */
public final class DefaultIndexMatchmakerFilter extends AbstractNativeFilter {

    private final NativeMatchmaker predicate;
    private final Set<NativeEntry> entries;
    private final Set<Class<?>> indexedClasses;

    /**
     * Creates a new DefaultMatchmakerfilter.
     *
     * @param result
     *            to base on
     * @param filter
     *            the matchmaker filter
     * @param query
     *            the query
     */
    public DefaultIndexMatchmakerFilter(final IndexResult result, final MatchmakerFilter filter,
            final DefaultQuery query) {
        super(query);

        this.entries = result.getEntries();
        this.indexedClasses = result.getIndexedClasses();
        this.predicate = DefaultMatchmakerFilter.transposeMatchmaker(filter.getPredicate(), this);
    }

    @Override
    public Iterator<NativeEntry> select(final Iterator<NativeEntry> entries) throws CountNotMetException {
        return new Iterator<NativeEntry>() {

            private NativeEntry next = null;

            @Override
            public boolean hasNext() {
                while (entries.hasNext()) {
                    next = entries.next();
                    if (isMatchingEntry(next)) {
                        return true;
                    }
                }
                next = null;
                return false;
            }

            @Override
            public NativeEntry next() {
                if (next == null) {
                    throw new NoSuchElementException();
                }
                return next;
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException("Remove not supported in this implementation!");
            }
        };
    }

    @Override
    public String toString() {
        return "INDEXMATCHMAKER (" + predicate + ")";
    }

    /**
     * Tries to get an indexed result for the given matchmaker filter.
     *
     * @param filter
     *            the matchmaker filter
     * @param query
     *            the query
     * @return the {@link DefaultIndexMatchmakerFilter} or <code>null</code> if no indexed result was found
     */
    public static NativeFilter newIndexMatchmakerFilter(final MatchmakerFilter filter, final DefaultQuery query) {

        IndexResult result = tryGetIndexResult(filter.getPredicate(), filter, query);

        if (result == null) {
            return null;
        }

        return new DefaultIndexMatchmakerFilter(result, filter, query);
    }

    private static IndexResult tryGetIndexResult(final Matchmaker matchmaker, final MatchmakerFilter filter,
            final DefaultQuery query) {

        SearchIndexManager indexManager = ((DefaultQueryCoordinator) query.getSelector().getCoordinator())
                .getSearchIndexManager();

        if (indexManager == null) {
            return null;
        }

        if (!AbstractValuePropertyMatchmaker.class.isAssignableFrom(matchmaker.getClass())) {
            return null;
        }

        IndexResult result = DefaultIndexResult.newQueryIndexResult((AbstractValuePropertyMatchmaker) matchmaker,
                indexManager);

        if (result == null) {
            return null;
        }

        return result;
    }

    private boolean isMatchingEntry(final NativeEntry entry) {

        // Check index
        if (entries.contains(entry)) {
            return true;
        }

        // Should be in index?
        if (indexedClasses.contains(entry.getData().getClass())) {
            return false;
        }

        // Fall back to non-indexed matchmaker
        return predicate.evaluate(entry.getData());
    }
}
