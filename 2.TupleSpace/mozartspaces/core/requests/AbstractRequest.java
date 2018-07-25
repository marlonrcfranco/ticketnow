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

import org.mozartspaces.core.Request;
import org.mozartspaces.core.RequestContext;

/**
 * An abstract <code>Request</code> with the properties that are common for all
 * predefined requests.
 *
 * @author Tobias Doenz
 *
 * @param <R>
 *            the result type of this request
 */
@ThreadSafe
public abstract class AbstractRequest<R extends Serializable> implements Request<R> {

    private static final long serialVersionUID = 1L;

    private volatile RequestContext context;

    protected AbstractRequest(final RequestContext context) {
        this.context = context;
    }

    /**
     * @return the request context
     */
    public final RequestContext getContext() {
        return context;
    }

    /**
     * Sets the request context.
     *
     * @param context
     *            the request context
     */
    public final void setContext(final RequestContext context) {
        this.context = context;
    }

    /**
     * Constructs a request from a request builder by calling its
     * <code>build</code> method. This allows a more readable creation of
     * requests with request builders, by using code of the form
     * <pre>Request req = build(CreateTransactionRequest.withTimeout(5000));</pre>
     * instead of
     * <pre>Request req = CreateTransactionRequest.withTimeout(5000).build();</pre>
     * when you statically import this method with
     * <pre>import static org.mozartspaces.core.requests.AbstractRequest.build;</pre>
     *
     * @param <T>
     *            the type of the built request
     *
     * @param requestBuilder
     *            the builder for the request
     * @return the request, created by calling the request's <code>build</code>
     *         method.
     */
    public static <T> T build(final Request.Builder<T> requestBuilder) {
        return requestBuilder.build();
    }

    /**
     * An abstract class that helps to build a request object.
     *
     * @author Tobias Doenz
     *
     * @param <B>
     *            the type of the builder
     * @param <T>
     *            the type of the request this builder constructs
     */
    public abstract static class Builder<B, T> implements Request.Builder<T> {

        private RequestContext context;

        /**
         * Constructs a <code>Builder</code>.
         */
        protected Builder() {
        }

        /**
         * Sets the context. The default value is <code>null</code>, if not
         * explicitly set.
         *
         * @param context
         *            the request context
         * @return the builder
         */
        @SuppressWarnings("unchecked")
        public final B context(final RequestContext context) {
            this.context = context;
            return (B) this;
        }

        /**
         * Constructs the request and returns it.
         *
         * @return the request
         */
        public abstract T build();

        // method used in sub-class when the request is constructed
        protected final RequestContext getContext() {
            return context;
        }

    }

}
