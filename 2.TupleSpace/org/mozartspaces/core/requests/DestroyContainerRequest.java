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
import org.mozartspaces.core.RequestContext;
import org.mozartspaces.core.TransactionReference;
import org.mozartspaces.core.util.Nothing;

/**
 * A <code>Request</code> to destroy a container in a space.
 *
 * @author Tobias Doenz
 */
@ThreadSafe
public final class DestroyContainerRequest extends TransactionalRequest<Nothing> {

    private static final long serialVersionUID = 1L;

    private final ContainerReference container;

    /**
     * Constructs a <code>DestroyContainerRequest</code>. Consider to use a
     * {@link #withContainer(ContainerReference) Builder}.
     *
     * @param container
     *            the reference of the container that should be destroyed.
     * @param transaction
     *            the transaction reference
     * @param isolation
     *            the transaction isolation level for this request
     * @param context
     *            the request context
     */
    public DestroyContainerRequest(final ContainerReference container, final TransactionReference transaction,
            final IsolationLevel isolation, final RequestContext context) {

        super(transaction, isolation, context);
        this.container = container;
        if (this.container == null) {
            throw new NullPointerException("Container reference is null");
        }
    }

    // for serialization
    private DestroyContainerRequest() {
        super(null, null, null);
        this.container = null;
    }

    /**
     * Gets the container reference.
     *
     * @return the container reference
     */
    public ContainerReference getContainer() {
        return container;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((container == null) ? 0 : container.hashCode());
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
        if (!(obj instanceof DestroyContainerRequest)) {
            return false;
        }
        DestroyContainerRequest other = (DestroyContainerRequest) obj;
        if (container == null) {
            if (other.container != null) {
                return false;
            }
        } else if (!container.equals(other.container)) {
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
        return "DestroyContainerRequest [container=" + container + ", isolation=" + getIsolation() + ", transaction="
                + getTransaction() + ", context=" + getContext() + "]";
    }

    // builder stuff below
    /**
     * Constructs a new builder with a container reference.
     *
     * @param container
     *            the reference of the container that should be destroyed. This
     *            parameter must not be <code>null</code>.
     * @return the builder with the container reference set
     */
    public static Builder withContainer(final ContainerReference container) {
        return new Builder(container);
    }

    /**
     * A class that helps to build a <code>DestroyContainerRequest</code>.
     *
     * @author Tobias Doenz
     */
    public static final class Builder extends
            TransactionalRequest.Builder<DestroyContainerRequest.Builder, DestroyContainerRequest> {

        private final ContainerReference container;

        /**
         * Protected constructor, use the static factory method
         * {@link DestroyContainerRequest#withContainer(ContainerReference)}.
         */
        protected Builder(final ContainerReference container) {
            this.container = container;
        }

        @Override
        public DestroyContainerRequest build() {
            return new DestroyContainerRequest(container, getTransaction(), getIsolation(), getContext());
        }
    }
}
