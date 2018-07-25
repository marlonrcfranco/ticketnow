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
package org.mozartspaces.core.requests;

import java.io.Serializable;

import net.jcip.annotations.ThreadSafe;

import org.mozartspaces.core.RequestContext;

/**
 * A <code>Request</code> to access the meta model.
 *
 * @author Tobias Doenz
 */
@ThreadSafe
public final class MetaModelRequest extends AbstractRequest<Serializable> {

    private static final long serialVersionUID = 1L;

    private final String path;
    private final int depth;

    /**
     * Constructs a <code>MetaModelRequest</code>. Consider to use a {@link #withPath(String) Builder}.
     *
     * @param path
     *            the path to access
     * @param depth
     *            the tree depth up to which the meta model sub-tree should be traversed
     * @param context
     *            the request context
     */
    public MetaModelRequest(final String path, final int depth,
            final RequestContext context) {
        super(context);
        this.path = path;
        if (this.path == null) {
            throw new NullPointerException("Path is null");
        }
        this.depth = depth;
        if (this.depth < 0) {
            throw new IllegalArgumentException("negative depth");
        }
    }

    // for serialization
    private MetaModelRequest() {
        super(null);
        this.path = null;
        this.depth = 0;
    }

    /**
     * @return the path
     */
    public String getPath() {
        return path;
    }

    /**
     * @return the depth
     */
    public int getDepth() {
        return depth;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((path == null) ? 0 : path.hashCode());
        result = prime * result + depth;
        // from superclass
        result = prime * result + ((getContext() == null) ? 0 : getContext().hashCode());
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof MetaModelRequest)) {
            return false;
        }
        MetaModelRequest other = (MetaModelRequest) obj;
        if (path == null) {
            if (other.path != null) {
                return false;
            }
        } else if (!path.equals(other.path)) {
            return false;
        }
        if (depth != other.depth) {
            return false;
        }
        // from superclass
        if (getContext() == null) {
            if (other.getContext() != null) {
                return false;
            }
        } else if (!getContext().equals(other.getContext())) {
            return false;
        }
        return true;
    }

    // builder stuff below
    /**
     * Constructs a new builder.
     *
     * @param path
     *            the path in the meta model that should be accessed. This parameter must not be <code>null</code>.
     * @return the builder
     */
    public static Builder withPath(final String path) {
        return new Builder(path);
    }

    /**
     * A class that helps to build a <code>MetaModelRequest</code>.
     *
     * @author Tobias Doenz
     */
    public static final class Builder extends AbstractRequest.Builder<MetaModelRequest.Builder, MetaModelRequest> {

        private final String path;

        private int depth = Integer.MAX_VALUE;

        /**
         * Protected constructor, use the static factory method {@link MetaModelRequest#withPath(String)}.
         */
        protected Builder(final String path) {
            this.path = path;
        }

        @Override
        public MetaModelRequest build() {
            return new MetaModelRequest(path, depth, getContext());
        }

        /**
         * Sets the tree depth up to which the meta model sub-tree should be traversed.
         *
         * @param depth
         *            the depth
         * @return the builder
         */
        public Builder depth(final int depth) {
            this.depth = depth;
            return this;
        }
    }
}
