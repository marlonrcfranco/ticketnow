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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.mozartspaces.core.MzsConstants;
import org.mozartspaces.util.parser.sql.SQLParser;
import org.mozartspaces.util.parser.sql.javacc.ParseException;

/**
 * XVSM Query.
 *
 * @author Martin Barisits
 * @author Martin Planer
 */
public final class Query implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * Constant used in conjunction with cnt(n) or cnt(min,max) for unbounded counts.
     */
    public static final int ALL = MzsConstants.Selecting.COUNT_ALL;

    private final List<Filter> filters;

    /**
     * @return the filters
     */
    public List<Filter> getFilters() {
        return filters;
    }

    /**
     * Creates a new Query.
     */
    public Query() {
        this.filters = new ArrayList<Filter>();
    }

    /**
     * Add additional Matchmaker to the Query.
     *
     * @param matchmaker
     *            a Matchmaker
     * @return Query including the Matchmakers
     */
    public Query filter(final Matchmaker matchmaker) {
        this.filters.add(new MatchmakerFilter(matchmaker));
        return this;
    }

    /**
     * Add a Query Stage with CNT.
     *
     * Selects specified amount of entries.
     *
     * @param count
     *            number of Entries (>= 0) to select or {@link Query#ALL ALL} for all committed entries
     * @return Query with the count set
     */
    public Query cnt(final int count) {
        this.filters.add(new CountSequencerFilter(count));
        return this;
    }

    /**
     * Add a Query Stage with CNT (with minimum and maximum count).
     *
     * cnt(0, ALL) equals CNT_MAX. cnt(ALL, ALL) equals CNT_ALL.
     *
     * @param minCount
     *            the minimum count (>= 0) of entries returned or {@link Query#ALL ALL} for all committed entries
     * @param maxCount
     *            the maximum count (>= 0) of entries returned or {@link Query#ALL ALL} for all committed entries
     * @return Query with the count set
     */
    public Query cnt(final int minCount, final int maxCount) {
        this.filters.add(new CountSequencerFilter(minCount, maxCount));
        return this;
    }

    /**
     * Add a Query Stage with SORTUP.
     *
     * @param property
     *            to base the Sort on
     * @return Query with the sort defined.
     */
    public Query sortup(final Property property) {
        this.filters.add(new SortUpSequencerFilter(property));
        return this;
    }

    /**
     * Add a Query Stage with SORTDOWN.
     *
     * @param property
     *            to base the Sort on
     * @return Query with the sort defined.
     */
    public Query sortdown(final Property property) {
        this.filters.add(new SortDownSequencerFilter(property));
        return this;
    }

    /**
     * Add a Query Stage with REVERSE.
     *
     * @return Query with the sort defined.
     */
    public Query reverse() {
        this.filters.add(new ReverseSequencerFilter());
        return this;
    }

    /**
     * Add a Query Stage with DISTINCT.
     *
     * @param property
     *            to base the distinct on
     * @return Query with the sort defined.
     */
    public Query distinct(final Property property) {
        this.filters.add(new DistinctSequencerFilter(property));
        return this;
    }

    /**
     * Add a Query Stage with an SQL-like query.
     *
     * @param query
     *            the query string
     * @return Query with the sort defined.
     * @throws ParseException if parsing the query string fails
     */
    public Query sql(final String query) throws ParseException {

        Query sqlQuery = SQLParser.parse(query);
        List<Filter> sqlFilters = sqlQuery.getFilters();

        for (Filter filter : sqlFilters) {
            this.filters.add(filter);
        }

        return this;
    }

    @Override
    public String toString() {
        return "Query [filters=" + filters + "]";
    }

    /**
     * Filter which evaluates every entry against a matchmaker.
     *
     * @author Martin Barisits
     */
    public static final class MatchmakerFilter implements Filter {

        private static final long serialVersionUID = 1L;

        private final Matchmaker predicate;

        /**
         * @return the predicate
         */
        public Matchmaker getPredicate() {
            return predicate;
        }

        private MatchmakerFilter(final Matchmaker matchmaker) {
            this.predicate = matchmaker;
        }

        @Override
        public String toString() {
            return "MatchmakerFilter [predicate=" + predicate + "]";
        }

    }

    /**
     * Sequencer which selects only a certain count of entries.
     *
     * @author Martin Barisits
     * @author Martin Planer
     */
    public static final class CountSequencerFilter implements Filter {

        private static final long serialVersionUID = 1L;

        private final int minCount;
        private final int maxCount;

        /**
         * @return the minimum count
         */
        public int getMinCount() {
            return minCount;
        }

        /**
         * @return the maximum count
         */
        public int getMaxCount() {
            return maxCount;
        }

        private CountSequencerFilter(final int count) {
            this.minCount = count;
            this.maxCount = count;
        }

        private CountSequencerFilter(final int minCount, final int maxCount) {
            this.minCount = minCount;
            this.maxCount = maxCount;
        }

        @Override
        public String toString() {
            return "CountSequencerFilter [minCount=" + minCount + ", maxCount=" + maxCount + "]";
        }
    }

    /**
     * Sequencer which orders the entries by a given property.
     *
     * @author Martin Barisits
     */
    public static final class SortUpSequencerFilter implements Filter {

        private static final long serialVersionUID = 1L;

        private final Property sortUpProperty;

        /**
         * @return the sortUpProperty
         */
        public Property getSortUpProperty() {
            return sortUpProperty;
        }

        private SortUpSequencerFilter(final Property property) {
            this.sortUpProperty = property;
        }

        @Override
        public String toString() {
            return "SortUpSequencerFilter [sortUpProperty=" + sortUpProperty + "]";
        }

    }

    /**
     * Sequencer which orders the entries by a given property down.
     *
     * @author Martin Barisits
     */
    public static final class SortDownSequencerFilter implements Filter {

        private static final long serialVersionUID = 1L;

        private final Property sortDownProperty;

        /**
         * @return the sortDownProperty
         */
        public Property getSortDownProperty() {
            return sortDownProperty;
        }

        private SortDownSequencerFilter(final Property property) {
            this.sortDownProperty = property;
        }

        @Override
        public String toString() {
            return "SortDownSequencerFilter [sortDownProperty=" + sortDownProperty + "]";
        }

    }

    /**
     * Sequencer which reverses the order.
     *
     * @author Martin Barisits
     */
    public static final class ReverseSequencerFilter implements Filter {

        private static final long serialVersionUID = 1L;

        private ReverseSequencerFilter() {
        }

        @Override
        public String toString() {
            return "ReverseSequencerFilter";
        }
    }

    /**
     * Sequencer which only returns entries with pairwise distinct values for the given property.
     *
     * @author Martin Planer
     */
    public static final class DistinctSequencerFilter implements Filter {

        private static final long serialVersionUID = 1L;

        private final Property distinctProperty;

        private DistinctSequencerFilter(final Property property) {
            this.distinctProperty = property;
        }

        /**
         * The {@link Property} on which the distinction is based on.
         *
         * @return the distinctProperty
         */
        public Property getDistinctProperty() {
            return distinctProperty;
        }

        @Override
        public String toString() {
            return "DistinctSequencerFilter [distinctProperty=" + distinctProperty + "]";
        }
    }
}
