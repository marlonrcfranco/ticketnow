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

import static org.mozartspaces.core.MzsConstants.Selecting.checkCount;

import java.io.Serializable;
import java.util.Arrays;

/**
 * Query Coordinator.
 *
 * @author Martin Barisits
 * @author Tobias Doenz
 */
public final class QueryCoordinator implements ImplicitCoordinator {

    private static final long serialVersionUID = -2010982037657191615L;

    /**
     * Default Coordinator Name.
     */
    public static final String DEFAULT_NAME = "QueryCoordinator";

    private final String name;

    /**
     * Creates a new QueryCoordinator.
     *
     * @param name
     *            of the QueryCoordinator
     */
    public QueryCoordinator(final String name) {
        this.name = name;
    }

    /**
     * Creates a new QueryCoordinator.
     */
    public QueryCoordinator() {
        this(DEFAULT_NAME);
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        QueryCoordinator other = (QueryCoordinator) obj;
        if (name == null) {
            if (other.name != null) {
                return false;
            }
        } else if (!name.equals(other.name)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "QueryCoordinator [name=" + name + "]";
    }

    /**
     * Creates a new coordination data object.
     *
     * @param name
     *            the name of the Query Coordinator.
     * @return the created coordination data object
     */
    public static QueryData newCoordinationData(final String name) {
        return new QueryData(name);
    }

    /**
     * Creates a new coordination data object for the Query Coordinator with the default name.
     *
     * @return the created coordination data object
     */
    public static QueryData newCoordinationData() {
        return new QueryData(DEFAULT_NAME);
    }

    /**
     * Creates a new coordination data object.
     *
     * @param name
     *            the name of the Query Coordinator.
     * @param indexData
     *            the query index data for the entry
     * @return the created coordination data object
     */
    public static QueryData newCoordinationData(final String name, final QueryIndexData... indexData) {
        return new QueryData(name, indexData);
    }

    /**
     * Creates a new coordination data object for the Query Coordinator with the default name.
     *
     * @param indexData
     *            the query index data for the entry
     *
     * @return the created coordination data object
     */
    public static QueryData newCoordinationData(final QueryIndexData... indexData) {
        return new QueryData(DEFAULT_NAME, indexData);
    }

    /**
     * Returns a QuerySelector.
     *
     * @param query
     *            to execute
     * @param count
     *            Entry count of this Selector
     * @param name
     *            name of the Coordinator this Selector should be associated to
     * @return QuerySelector
     */
    public static QuerySelector newSelector(final Query query, final int count, final String name) {
        return new QuerySelector(query, count, name);
    }

    /**
     * Returns a QuerySelector with the default name.
     *
     * @param query
     *            to execute
     * @param count
     *            Entry count of this Selector
     * @return QuerySelector
     */
    public static QuerySelector newSelector(final Query query, final int count) {
        return new QuerySelector(query, count, DEFAULT_NAME);
    }

    /**
     * Returns a QuerySelector with count 1 and the default name.
     *
     * @param query
     *            to execute
     * @return QuerySelector
     */
    public static QuerySelector newSelector(final Query query) {
        return new QuerySelector(query, 1, DEFAULT_NAME);
    }

    /**
     * @param query
     *            the query to check
     */
    public static void checkQuery(final Query query) {
        if (query == null) {
            throw new NullPointerException("query");
        }
    }

    /**
     * Common properties of the Query Coordinator, used for writing and reading entries.
     *
     * @author Tobias Doenz
     */
    abstract static class QueryProperties implements Serializable {

        private static final long serialVersionUID = 1L;

        private final String name;

        protected QueryProperties(final String name) {
            this.name = name;
        }

        public String getName() {
            return this.name;
        }

    }

    /**
     * The coordination properties that are used for writing entries with the Query Coordinator.
     *
     * @author Tobias Doenz
     */
    public static final class QueryData extends QueryProperties implements CoordinationData {

        private static final long serialVersionUID = 1L;

        private final QueryIndexData[] indexData;

        private QueryData(final String name, final QueryIndexData... indexData) {
            super(name);
            this.indexData = indexData;
        }

        // for serialization
        private QueryData() {
            super(null);
            this.indexData = null;
        }

        /**
         * @return the query index data
         */
        public QueryIndexData[] getIndexData() {
            return indexData;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            // from superclass
            result = prime * result + ((getName() == null) ? 0 : getName().hashCode());
            return result;
        }

        @Override
        public boolean equals(final Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            QueryData other = (QueryData) obj;
            // from superclass
            if (getName() == null) {
                if (other.getName() != null) {
                    return false;
                }
            } else if (!getName().equals(other.getName())) {
                return false;
            }
            return true;
        }

        @Override
        public String toString() {
            return "QueryData [name=" + getName() + ", indexData=" + Arrays.toString(indexData) + "]";
        }

    }

    /**
     * The selector to get entries from a Query Coordinator.
     *
     * @author Martin Barisits
     * @author Tobias Doenz
     */
    public static final class QuerySelector extends QueryProperties implements Selector {

        private static final long serialVersionUID = -5879017818307399013L;

        private final int count;
        private final Query query;

        private QuerySelector(final Query query, final int count, final String name) {
            super(name);
            this.query = query;
            checkQuery(query);
            this.count = count;
            checkCount(this.count);
        }

        // for serialization
        private QuerySelector() {
            super(null);
            this.query = null;
            this.count = 0;
        }

        /**
         * @return the query
         */
        public Query getQuery() {
            return this.query;
        }

        @Override
        public int getCount() {
            return this.count;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + count;
            result = prime * result + ((query == null) ? 0 : query.hashCode());
            // from superclass
            result = prime * result + ((getName() == null) ? 0 : getName().hashCode());
            return result;
        }

        @Override
        public boolean equals(final Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            QuerySelector other = (QuerySelector) obj;
            if (count != other.count) {
                return false;
            }
            if (query == null) {
                if (other.query != null) {
                    return false;
                }
            } else if (!query.equals(other.query)) {
                return false;
            }
            // from superclass
            if (getName() == null) {
                if (other.getName() != null) {
                    return false;
                }
            } else if (!getName().equals(other.getName())) {
                return false;
            }
            return true;
        }

        @Override
        public String toString() {
            return "QuerySelector [count=" + count + ", query=" + query + ", name=" + getName() + "]";
        }

    }

}
