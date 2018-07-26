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

import net.jcip.annotations.ThreadSafe;

import org.mozartspaces.core.RequestContext;
import org.mozartspaces.core.util.Nothing;

/**
 * A <code>Request</code> to clear a specified space.
 *
 * @author Tobias Doenz
 */
@ThreadSafe
public final class ClearSpaceRequest extends AbstractRequest<Nothing> {

    private static final long serialVersionUID = 1L;

    /**
     * Constructs a <code>ClearSpaceRequest</code>. Consider to use a
     * {@link #withBuilder() Builder}.
     *
     * @param context
     *            the request context
     */
    public ClearSpaceRequest(final RequestContext context) {
        super(context);
    }

    // for serialization
    private ClearSpaceRequest() {
        super(null);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        // from superclass
        result = prime * result + ((getContext() == null) ? 0 : getContext().hashCode());
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof ClearSpaceRequest)) {
            return false;
        }
        ClearSpaceRequest other = (ClearSpaceRequest) obj;
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

    @Override
    public String toString() {
        return "ClearSpaceRequest [context=" + getContext() + "]";
    }

    // builder stuff below
    /**
     * Constructs a new builder.
     *
     * @return the builder
     */
    public static Builder withBuilder() {
        return new Builder();
    }

    /**
     * A class that helps to build a <code>ClearSpaceRequest</code>.
     *
     * @author Tobias Doenz
     */
    public static final class Builder extends AbstractRequest.Builder<ClearSpaceRequest.Builder, ClearSpaceRequest> {

        /**
         * Protected constructor, use the static factory method
         * {@link ClearSpaceRequest#withBuilder()}.
         */
        protected Builder() {
        }

        @Override
        public ClearSpaceRequest build() {
            return new ClearSpaceRequest(getContext());
        }

    }
}
