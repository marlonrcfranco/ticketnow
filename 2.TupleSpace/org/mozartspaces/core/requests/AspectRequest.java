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
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import net.jcip.annotations.ThreadSafe;

import org.mozartspaces.capi3.IsolationLevel;
import org.mozartspaces.core.RequestContext;
import org.mozartspaces.core.TransactionReference;
import org.mozartspaces.core.aspects.InterceptionPoint;

/**
 * An abstract <code>Request</code> for requests that modify aspects in a space.
 *
 * @author Tobias Doenz
 *
 * @param <R>
 *            the result type of this request
 */
@ThreadSafe
public abstract class AspectRequest<R extends Serializable> extends TransactionalRequest<R> {

    private static final long serialVersionUID = 1L;

    private final Set<InterceptionPoint> iPoints;

    protected AspectRequest(final Set<? extends InterceptionPoint> iPoints, final TransactionReference transaction,
            final IsolationLevel isolation, final RequestContext context) {

        super(transaction, isolation, context);
        if (iPoints != null) {
            this.iPoints = new HashSet<InterceptionPoint>(iPoints);
        } else {
            this.iPoints = null;
        }
    }

    /**
     * @return an unmodifiable view of the iPoint set
     */
    public final Set<InterceptionPoint> getIPoints() {
        return (iPoints == null) ? null : Collections.unmodifiableSet(iPoints);
    }

    /**
     * A class that helps to build an <code>AspectRequest</code>.
     *
     * @author Tobias Doenz
     *
     * @param <B>
     *            the type of the builder
     * @param <T>
     *            the type of the request this builder constructs
     */
    public abstract static class Builder<B, T> extends TransactionalRequest.Builder<B, T> {

        private Set<? extends InterceptionPoint> iPoints;

        /**
         * Constructs a <code>Builder</code>.
         */
        protected Builder() {
        }

        /**
         * Sets the interception points. The default value is <code>null</code>,
         * if not explicitly set. Note that <code>null</code> is only allowed
         * for removing aspects, and removes an aspect from all interception
         * points, not for adding aspects.
         *
         * @param iPoints
         *            the interception points
         * @return the builder
         */
        @SuppressWarnings("unchecked")
        public final B iPoints(final Set<? extends InterceptionPoint> iPoints) {
            this.iPoints = iPoints;
            return (B) this;
        }

        // methods used in sub-class when the request is constructed
        protected final Set<? extends InterceptionPoint> getIPoints() {
            return iPoints;
        }
    }
}
