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
 * A <code>Request</code> to shut down a specific space.
 *
 * @author Tobias Doenz
 */
@ThreadSafe
public final class ShutdownRequest extends AbstractRequest<Nothing> {

    private static final long serialVersionUID = 1L;

    /**
     * Key for the system property in the request context where the configuration that is used to create the new
     * core on reboot is stored.
     */
    public static final String CONTEXT_KEY_REBOOTCONFIG = "rebootconfig";

    /**
     * Constructs a <code>ShutdownRequest</code>. Consider to use a
     * {@link #withBuilder() Builder}.
     *
     * @param context
     *            the request context
     */
    public ShutdownRequest(final RequestContext context) {
        super(context);
    }

    // for serialization
    private ShutdownRequest() {
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
        if (!(obj instanceof ShutdownRequest)) {
            return false;
        }
        ShutdownRequest other = (ShutdownRequest) obj;
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
        return "ShutdownRequest [context=" + getContext() + "]";
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
     * A class that helps to build a <code>ShutdownRequest</code>.
     *
     * @author Tobias Doenz
     */
    public static final class Builder extends AbstractRequest.Builder<ShutdownRequest.Builder, ShutdownRequest> {

        /**
         * Protected constructor, use the static factory method
         * {@link ShutdownRequest#withBuilder()}.
         */
        protected Builder() {
        }

        @Override
        public ShutdownRequest build() {
            return new ShutdownRequest(getContext());
        }

    }
}
