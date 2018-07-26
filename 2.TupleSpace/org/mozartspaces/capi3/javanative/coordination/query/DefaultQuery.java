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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.mozartspaces.capi3.AccessDeniedException;
import org.mozartspaces.capi3.CountNotMetException;
import org.mozartspaces.capi3.EntryLockedException;
import org.mozartspaces.capi3.Filter;
import org.mozartspaces.capi3.IsolationLevel;
import org.mozartspaces.capi3.OperationType;
import org.mozartspaces.capi3.Query;
import org.mozartspaces.capi3.Query.CountSequencerFilter;
import org.mozartspaces.capi3.Query.DistinctSequencerFilter;
import org.mozartspaces.capi3.Query.MatchmakerFilter;
import org.mozartspaces.capi3.Query.ReverseSequencerFilter;
import org.mozartspaces.capi3.Query.SortDownSequencerFilter;
import org.mozartspaces.capi3.Query.SortUpSequencerFilter;
import org.mozartspaces.capi3.javanative.authorization.AuthorizationResult;
import org.mozartspaces.capi3.javanative.coordination.DefaultQueryCoordinator.DefaultQuerySelector;
import org.mozartspaces.capi3.javanative.coordination.query.index.DefaultIndexMatchmakerFilter;
import org.mozartspaces.capi3.javanative.isolation.NativeSubTransaction;
import org.mozartspaces.capi3.javanative.operation.NativeEntry;

/**
 * Defines an XVSM Query.
 *
 * @author Martin Barisits
 * @author Martin Planer
 */
public final class DefaultQuery {

    private final List<NativeFilter> filters;
    private final DefaultQuerySelector selector;
    private final IsolationLevel isolationLevel;
    private final AuthorizationResult auth;
    private final NativeSubTransaction stx;
    private final OperationType opType;

    /**
     * Creates a new DefaultQuery.
     *
     * @param query
     *            to base the DefaultQuery on
     * @param selector
     *            the selector this query belongs to
     * @param opType
     *            the operation type
     * @param stx
     *            the sub-transaction
     * @param auth
     *            the authorization result
     * @param isolationLevel
     *            the isolation level
     */
    public DefaultQuery(final Query query, final DefaultQuerySelector selector, final IsolationLevel isolationLevel,
            final AuthorizationResult auth, final NativeSubTransaction stx, final OperationType opType) {

        this.selector = selector;
        this.isolationLevel = isolationLevel;
        this.auth = auth;
        this.stx = stx;
        this.opType = opType;

        this.filters = new ArrayList<NativeFilter>();

        if (query == null) {
            return;
        }
        for (Filter filter : query.getFilters()) {
            if (filter.getClass().equals(MatchmakerFilter.class)) {

                // Try to get existing query index, else fall back to normal matchmaker filter
                NativeFilter indexMatchmakerFilter = DefaultIndexMatchmakerFilter.newIndexMatchmakerFilter(
                        (MatchmakerFilter) filter, this);

                if (indexMatchmakerFilter != null) {
                    this.filters.add(indexMatchmakerFilter);
                } else {
                    this.filters.add(new DefaultMatchmakerFilter((MatchmakerFilter) filter, this));
                }
            } else if (filter.getClass().equals(ReverseSequencerFilter.class)) {
                this.filters.add(new DefaultReverseSequencerFilter(this));
            } else if (filter.getClass().equals(SortDownSequencerFilter.class)) {
                this.filters.add(new DefaultSortDownSequencerFilter((SortDownSequencerFilter) filter, this));
            } else if (filter.getClass().equals(SortUpSequencerFilter.class)) {
                this.filters.add(new DefaultSortUpSequencerFilter((SortUpSequencerFilter) filter, this));
            } else if (filter.getClass().equals(CountSequencerFilter.class)) {
                this.filters.add(new DefaultCountSequencerFilter((CountSequencerFilter) filter, this));
            } else if (filter.getClass().equals(DistinctSequencerFilter.class)) {
                this.filters.add(new DefaultDistinctSequencerFilter((DistinctSequencerFilter) filter, this));
            } else {
                throw new IllegalArgumentException("Unsupported filter " + filter);
            }
        }
    }

    /**
     * Execute the Query. This version uses streaming and semi-lazy evaluation with iterators to minimize all internal
     * query related calls to a minimum.
     *
     * @param entries
     *            to execute the query on
     * @return selected entries
     * @throws CountNotMetException
     *             if a count was not met
     */
    public Iterator<NativeEntry> execute(final Iterator<NativeEntry> entries) throws CountNotMetException {

        Iterator<NativeEntry> data = entries;

        for (NativeFilter filter : this.filters) {
            data = filter.select(data);
        }

        return data;
    }

    /**
     * Checks if the given entry is accessible from the current query.
     *
     * @param entry
     *            the entry to check
     * @return <code>true</code> if the entry is accessible, <code>false</code> otherwise
     * @throws AccessDeniedException
     * @throws EntryLockedException
     */
    public boolean isAccessible(final NativeEntry entry) {
        try {
            return selector.checkAccessibility(entry, isolationLevel, auth, stx, opType, false);
        } catch (AccessDeniedException e) {
            return false;
        } catch (EntryLockedException e) {
            return false;
        }
    }

    /**
     * @return the selector this query belongs to
     */
    public DefaultQuerySelector getSelector() {
        return selector;
    }

    @Override
    public String toString() {
        return "QUERY (" + filters + ")";
    }

}
