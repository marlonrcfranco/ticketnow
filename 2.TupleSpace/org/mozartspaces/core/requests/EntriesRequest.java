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

import org.mozartspaces.capi3.IsolationLevel;
import org.mozartspaces.core.ContainerReference;
import org.mozartspaces.core.MzsConstants;
import org.mozartspaces.core.RequestContext;
import org.mozartspaces.core.TransactionReference;

/**
 * An abstract <code>Request</code> for requests that operate on entries in a
 * container.
 *
 * @author Tobias Doenz
 *
 * @param <R>
 *            the result type of this request
 */
@ThreadSafe
public abstract class EntriesRequest<R extends Serializable> extends TransactionalRequest<R> {

    private static final long serialVersionUID = 1L;

    private final ContainerReference container;
    private final long timeout;

    protected EntriesRequest(final ContainerReference container, final long timeoutInMilliseconds,
            final TransactionReference transaction, final IsolationLevel isolation, final RequestContext context) {

        super(transaction, isolation, context);

        this.container = container;
        if (this.container == null) {
            throw new NullPointerException("Container reference is null");
        }
        this.timeout = timeoutInMilliseconds;
        if (this.timeout < MzsConstants.RequestTimeout.TRY_ONCE) {
            throw new IllegalArgumentException("timeout " + this.timeout);
        }
    }

    /**
     * Gets the container reference.
     *
     * @return the container reference
     */
    public final ContainerReference getContainer() {
        return container;
    }

    /**
     * Gets the timeout.
     *
     * @return the timeout
     */
    public final long getTimeout() {
        return timeout;
    }

    /**
     * A class that helps to build an <code>EntriesRequest</code>.
     *
     * @author Tobias Doenz
     *
     * @param <B>
     *            the type of the builder
     * @param <T>
     *            the type of the request this builder constructs
     */
    public abstract static class Builder<B, T> extends TransactionalRequest.Builder<B, T> {

        private final ContainerReference container;
        private long timeout = MzsConstants.RequestTimeout.DEFAULT;

        /**
         * Constructs a <code>Builder</code>.
         *
         * @param container
         *            the reference of the container that should be used
         */
        protected Builder(final ContainerReference container) {
            this.container = container;
        }

        /**
         * Sets the timeout. The default value is <code>TRY_ONCE</code>, if not
         * explicitly set. The timeout value must be <code>>= 0</code>, or a
         * constant defined in {@link org.mozartspaces.core.MzsConstants.RequestTimeout
         * MzsConstants.RequestTimeout}.
         *
         * @param timeoutInMilliseconds
         *            the request timeout in milliseconds
         * @return the builder
         */
        @SuppressWarnings("unchecked")
        public final B timeout(final long timeoutInMilliseconds) {
            this.timeout = timeoutInMilliseconds;
            return (B) this;
        }

        // methods used in sub-class when the request is constructed
        protected final ContainerReference getContainer() {
            return container;
        }

        protected final long getTimeout() {
            return timeout;
        }
    }
}
