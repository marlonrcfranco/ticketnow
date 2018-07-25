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

import org.mozartspaces.capi3.IsolationLevel;
import org.mozartspaces.core.ContainerReference;
import org.mozartspaces.core.MzsConstants;
import org.mozartspaces.core.RequestContext;
import org.mozartspaces.core.TransactionReference;

/**
 * A <code>Request</code> to lookup a named container in a space.
 *
 * @author Tobias Doenz
 */
@ThreadSafe
public final class LookupContainerRequest extends TransactionalRequest<ContainerReference> {

    private static final long serialVersionUID = 1L;

    private final String name;
    private final long timeout;

    /**
     * Constructs a <code>LookupContainerRequest</code>. Consider to use a
     * {@link #withName(String) Builder}.
     *
     * @param name
     *            the name of the container
     * @param timeoutInMilliseconds
     *            the request timeout in milliseconds
     * @param transaction
     *            the transaction reference
     * @param isolation
     *            the transaction isolation level for this request
     * @param context
     *            the request context
     */
    public LookupContainerRequest(final String name, final long timeoutInMilliseconds,
            final TransactionReference transaction, final IsolationLevel isolation, final RequestContext context) {

        super(transaction, isolation, context);
        this.name = name;
        if (this.name == null) {
            throw new IllegalArgumentException("Container name is null");
            // TODO? container name checking
        }
        this.timeout = timeoutInMilliseconds;
        if (this.timeout < MzsConstants.RequestTimeout.TRY_ONCE) {
            throw new IllegalArgumentException("timeout " + this.timeout);
        }
    }

    // for serialization
    private LookupContainerRequest() {
        super(null, null, null);
        this.name = null;
        this.timeout = 0;
    }

    /**
     * Gets the container name.
     *
     * @return the container name
     */
    public String getName() {
        return name;
    }

    /**
     * Gets the timeout.
     *
     * @return the timeout
     */
    public long getTimeout() {
        return timeout;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        result = prime * result + (int) (timeout ^ (timeout >>> 32));
        // from superclasses
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
        if (!(obj instanceof LookupContainerRequest)) {
            return false;
        }
        LookupContainerRequest other = (LookupContainerRequest) obj;
        if (name == null) {
            if (other.name != null) {
                return false;
            }
        } else if (!name.equals(other.name)) {
            return false;
        }
        if (timeout != other.timeout) {
            return false;
        }
        // from superclasses
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
        return "LookupContainerRequest [name=" + name + ", timeout=" + timeout + ", isolation=" + getIsolation()
        + ", transaction=" + getTransaction() + ", context=" + getContext() + "]";
    }

    // builder stuff below
    /**
     * Constructs a new builder with a container name.
     *
     * @param name
     *            the name of the container. This parameter must not be
     *            <code>null</code> or an empty string.
     * @return the builder with the container name set
     */
    public static Builder withName(final String name) {
        return new Builder(name);
    }

    /**
     * A class that helps to build a <code>LookupContainerRequest</code>.
     *
     * @author Tobias Doenz
     */
    public static final class Builder extends
            TransactionalRequest.Builder<LookupContainerRequest.Builder, LookupContainerRequest> {

        private final String name;
        private long timeout = MzsConstants.RequestTimeout.ZERO;

        /**
         * Protected constructor, use the static factory method
         * {@link LookupContainerRequest#withName(String)}.
         */
        protected Builder(final String name) {
            this.name = name;
        }

        @Override
        public LookupContainerRequest build() {
            return new LookupContainerRequest(name, timeout, getTransaction(), getIsolation(), getContext());
        }

        /**
         * Sets the timeout. The default value is <code>ZERO</code>, if not
         * explicitly set. The timeout value must be <code>>= 0</code>, or a
         * constant defined in {@link org.mozartspaces.core.MzsConstants.RequestTimeout
         * MzsConstants.RequestTimeout}.
         *
         * @param timeoutInMilliseconds
         *            the timeout in milliseconds
         * @return the builder
         */
        public Builder timeout(final long timeoutInMilliseconds) {
            this.timeout = timeoutInMilliseconds;
            return this;
        }

    }
}
