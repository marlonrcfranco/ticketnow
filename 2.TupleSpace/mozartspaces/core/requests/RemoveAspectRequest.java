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

import java.util.Set;

import net.jcip.annotations.ThreadSafe;

import org.mozartspaces.capi3.IsolationLevel;
import org.mozartspaces.core.RequestContext;
import org.mozartspaces.core.TransactionReference;
import org.mozartspaces.core.aspects.AspectReference;
import org.mozartspaces.core.aspects.InterceptionPoint;
import org.mozartspaces.core.util.Nothing;

/**
 * A <code>Request</code> to remove an aspect from a container or space.
 *
 * @author Tobias Doenz
 */
@ThreadSafe
public final class RemoveAspectRequest extends AspectRequest<Nothing> {

    private static final long serialVersionUID = 1L;

    private final AspectReference aspect;

    /**
     * Constructs a <code>RemoveAspectRequest</code>. Consider to use a
     * {@link #withAspect(AspectReference) Builder}.
     *
     * @param aspect
     *            the reference of the aspect that should be removed
     * @param iPoints
     *            the interception points where this aspect should be removed
     * @param transaction
     *            the transaction reference
     * @param isolation
     *            the transaction isolation level for this request
     * @param context
     *            the request context
     */
    public RemoveAspectRequest(final AspectReference aspect, final Set<? extends InterceptionPoint> iPoints,
            final TransactionReference transaction, final IsolationLevel isolation, final RequestContext context) {

        super(iPoints, transaction, isolation, context);

        this.aspect = aspect;
        if (this.aspect == null) {
            throw new NullPointerException("Aspect reference is null");
        }
    }

    // for serialization
    private RemoveAspectRequest() {
        super(null, null, null, null);
        this.aspect = null;
    }

    /**
     * @return the aspect
     */
    public AspectReference getAspect() {
        return aspect;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((aspect == null) ? 0 : aspect.hashCode());
        // from superclasses
        result = prime * result + ((getIPoints() == null) ? 0 : getIPoints().hashCode());
        result = prime * result + ((getIsolation() == null) ? 0 : getIsolation().hashCode());
        result = prime * result + ((getTransaction() == null) ? 0 : getTransaction().hashCode());
        result = prime * result + ((getContext() == null) ? 0 : getContext().hashCode());
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof RemoveAspectRequest)) {
            return false;
        }
        RemoveAspectRequest other = (RemoveAspectRequest) obj;
        if (aspect == null) {
            if (other.aspect != null) {
                return false;
            }
        } else if (!aspect.equals(other.aspect)) {
            return false;
        }
        // from superclasses
        if (getIPoints() == null) {
            if (other.getIPoints() != null) {
                return false;
            }
        } else if (!getIPoints().equals(other.getIPoints())) {
            return false;
        }
        if (getIsolation() == null) {
            if (other.getIsolation() != null) {
                return false;
            }
        } else if (!getIsolation().equals(other.getIsolation())) {
            return false;
        }
        if (getTransaction() == null) {
            if (other.getTransaction() != null) {
                return false;
            }
        } else if (!getTransaction().equals(other.getTransaction())) {
            return false;
        }
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
        return "RemoveAspectRequest [aspect=" + aspect + ", iPoints=" + getIPoints() + ", isolation=" + getIsolation()
                + ", transaction=" + getTransaction() + ", context=" + getContext() + "]";
    }

    // builder stuff below
    /**
     * Constructs a new builder with a space aspect.
     *
     * @param aspect
     *            the reference of the aspect that should be removed. This
     *            parameter must not be <code>null</code>.
     * @return the builder with the aspect reference set
     */
    public static Builder withAspect(final AspectReference aspect) {
        return new Builder(aspect);
    }

    /**
     * A class that helps to build an <code>RemoveAspectRequest</code>.
     *
     * @author Tobias Doenz
     */
    public static final class Builder extends AspectRequest.Builder<RemoveAspectRequest.Builder, RemoveAspectRequest> {

        private final AspectReference aspect;

        /**
         * Protected constructor, use the static factory method
         * {@link RemoveAspectRequest#withAspect(AspectReference)}.
         */
        protected Builder(final AspectReference aspect) {
            this.aspect = aspect;
        }

        @Override
        public RemoveAspectRequest build() {
            return new RemoveAspectRequest(aspect, getIPoints(), getTransaction(), getIsolation(), getContext());
        }
    }
}
