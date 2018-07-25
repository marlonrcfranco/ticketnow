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
import java.util.Arrays;

import org.mozartspaces.capi3.QueryIndex.IndexType;

/**
 * Coordination data for creating indexes on 3rd party classes and other classes where annotations are not possible.
 *
 * @author Martin Planer
 */
public final class QueryIndexData implements Serializable {

    private static final long serialVersionUID = 1L;

    private final IndexType indexType;
    private final String[] path;

    /**
     * Creates coordination data with the default index type BASIC.
     *
     * @param path
     *            the path
     */
    public QueryIndexData(final String... path) {
        this(IndexType.BASIC, path);
    }

    /**
     * @param indexType
     *            the index type
     * @param path
     *            the path
     */
    public QueryIndexData(final IndexType indexType, final String... path) {
        this.indexType = indexType;
        this.path = path;
    }

    /**
     * @return the path
     */
    public String[] getPath() {
        return path;
    }

    /**
     * @return the index type
     */
    public IndexType getIndexType() {
        return indexType;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((indexType == null) ? 0 : indexType.hashCode());
        result = prime * result + Arrays.hashCode(path);
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
        QueryIndexData other = (QueryIndexData) obj;
        if (indexType != other.indexType) {
            return false;
        }
        if (!Arrays.equals(path, other.path)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "QueryIndexData [indexType=" + indexType + ", path=" + Arrays.toString(path) + "]";
    }

}
