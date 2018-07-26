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

import org.mozartspaces.core.MzsConstants.TransactionTimeout;
import org.mozartspaces.core.RequestContext;
import org.mozartspaces.core.TransactionReference;

/**
 * A <code>Request</code> to create a transaction on a specific space.
 *
 * @author Tobias Doenz
 */
@ThreadSafe
public final class CreateTransactionRequest extends AbstractRequest<TransactionReference> {

    private static final long serialVersionUID = 1L;

    private final long timeout;

    /**
     * Constructs a <code>CreateTransactionRequest</code>. Consider to use a
     * {@link #withTimeout(long) Builder}.
     *
     * @param timeoutInMilliseconds
     *            the transaction timeout in milliseconds
     * @param context
     *            the request context
     */
    public CreateTransactionRequest(final long timeoutInMilliseconds, final RequestContext context) {
        super(context);
        this.timeout = timeoutInMilliseconds;
        if (this.timeout <= 0 && this.timeout != TransactionTimeout.INFINITE) {
            throw new IllegalArgumentException("timeout " + this.timeout);
        }
    }

    // for serialization
    private CreateTransactionRequest() {
        super(null);
        this.timeout = 0;
    }

    /**
     * Gets the transaction timeout.
     *
     * @return the transaction timeout
     */
    public long getTimeout() {
        return timeout;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (int) (timeout ^ (timeout >>> 32));
        // from superclass
        result = prime * result + ((getContext() == null) ? 0 : getContext().hashCode());
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof CreateTransactionRequest)) {
            return false;
        }
        CreateTransactionRequest other = (CreateTransactionRequest) obj;
        if (timeout != other.timeout) {
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
        return "CreateTransactionRequest [timeout=" + timeout + ", context=" + getContext() + "]";
    }

    // builder stuff below
    // static building method with obligatory request parameter
    /**
     * Constructs a new builder with a timeout. The timeout value must be
     * must be <code>> 0</code> or
     * {@link org.mozartspaces.core.MzsConstants.TransactionTimeout#INFINITE INFINITE}.
     *
     * @param timeoutInMilliseconds
     *            the transaction timeout in milliseconds.
     * @return the builder with the timeout set
     */
    public static Builder withTimeout(final long timeoutInMilliseconds) {
        return new Builder(timeoutInMilliseconds);
    }

    /**
     * A class that helps to build a <code>CreateTransactionRequest</code>.
     *
     * @author tobias Doenz
     */
    public static final class Builder extends
            AbstractRequest.Builder<CreateTransactionRequest.Builder, CreateTransactionRequest> {

        private final long timeout;

        /**
         * Protected constructor, use the static factory method
         * {@link CreateTransactionRequest#withTimeout(long)}.
         */
        protected Builder(final long timeoutInMilliseconds) {
            this.timeout = timeoutInMilliseconds;
        }

        @Override
        public CreateTransactionRequest build() {
            return new CreateTransactionRequest(timeout, getContext());
        }

    }
}
