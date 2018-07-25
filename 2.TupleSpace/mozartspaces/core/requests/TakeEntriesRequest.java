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
import java.util.ArrayList;
import java.util.List;

import net.jcip.annotations.ThreadSafe;

import org.mozartspaces.capi3.IsolationLevel;
import org.mozartspaces.capi3.Selector;
import org.mozartspaces.core.ContainerReference;
import org.mozartspaces.core.RequestContext;
import org.mozartspaces.core.TransactionReference;

/**
 * A <code>Request</code> to take entries from a container.
 *
 * @author Tobias Doenz
 *
 * @param <T>
 *            the type of the entries
 */
@ThreadSafe
public final class TakeEntriesRequest<T extends Serializable> extends SelectingEntriesRequest<ArrayList<T>> {

    private static final long serialVersionUID = 1L;

    /**
     * Constructs a <code>TakeEntriesRequest</code>. Consider to use a
     * {@link #withContainer(ContainerReference) Builder}.
     *
     * @param container
     *            the reference of the container where entries should be taken
     * @param selectors
     *            the entry selector list
     * @param timeoutInMilliseconds
     *            the request timeout in milliseconds
     * @param transaction
     *            the transaction reference
     * @param isolation
     *            the transaction isolation level for this request
     * @param context
     *            the request context
     */
    public TakeEntriesRequest(final ContainerReference container, final List<? extends Selector> selectors,
            final long timeoutInMilliseconds, final TransactionReference transaction, final IsolationLevel isolation,
            final RequestContext context) {

        super(container, selectors, timeoutInMilliseconds, transaction, isolation, context);
    }

    // for serialization
    private TakeEntriesRequest() {
        super(ContainerReference.DUMMY, DUMMY_SELECTORS, 0, null, null, null);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        // from superclasses
        result = prime * result + ((getContainer() == null) ? 0 : getContainer().hashCode());
        result = prime * result + ((getSelectors() == null) ? 0 : getSelectors().hashCode());
        result = prime * result + (int) (getTimeout() ^ (getTimeout() >>> 32));
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
        if (!(obj instanceof TakeEntriesRequest<?>)) {
            return false;
        }
        TakeEntriesRequest<?> other = (TakeEntriesRequest<?>) obj;
        // from superclasses
        if (getContainer() == null) {
            if (other.getContainer() != null) {
                return false;
            }
        } else if (!getContainer().equals(other.getContainer())) {
            return false;
        }
        if (getSelectors() == null) {
            if (other.getSelectors() != null) {
                return false;
            }
        } else if (!getSelectors().equals(other.getSelectors())) {
            return false;
        }
        if (getTimeout() != other.getTimeout()) {
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
        return "TakeEntriesRequest [container=" + getContainer() + ", selectors=" + getSelectors() + ", timeout="
                + getTimeout() + ", isolation=" + getIsolation() + ", transaction=" + getTransaction()
                + ", context=" + getContext() + "]";
    }

    // builder stuff below
    /**
     * Constructs a new builder with a container reference.
     *
     * @param <T>
     *            the type of the entries
     * @param container
     *            the reference of the container where entries should be taken.
     *            This parameter must not be <code>null</code>.
     * @return the builder with the container reference set
     */
    public static <T extends Serializable> Builder<T> withContainer(final ContainerReference container) {
        return new Builder<T>(container);
    }

    /**
     * A class that helps to build a <code>TakeEntriesRequest</code>.
     *
     * @author Tobias Doenz
     *
     * @param <T>
     *            the type of the entries
     */
    public static final class Builder<T extends Serializable> extends
            SelectingEntriesRequest.Builder<TakeEntriesRequest.Builder<T>, TakeEntriesRequest<T>> {

        /**
         * Protected constructor, use the static factory method
         * {@link TakeEntriesRequest#withContainer(ContainerReference)}.
         */
        protected Builder(final ContainerReference container) {
            super(container);
        }

        @Override
        public TakeEntriesRequest<T> build() {
            return new TakeEntriesRequest<T>(getContainer(), getSelectors(), getTimeout(), getTransaction(),
                    getIsolation(), getContext());
        }
    }
}
