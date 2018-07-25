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
import org.mozartspaces.core.MzsConstants;
import org.mozartspaces.core.RequestContext;
import org.mozartspaces.core.TransactionReference;

/**
 * An abstract <code>Request</code> for requests that can be processed within a
 * transaction.
 *
 * @author Tobias Doenz
 *
 * @param <R>
 *            the result type of this request
 */
@ThreadSafe
public abstract class TransactionalRequest<R extends Serializable> extends AbstractRequest<R> {

    private static final long serialVersionUID = 1L;

    private final TransactionReference transaction;
    private final IsolationLevel isolation;

    protected TransactionalRequest(final TransactionReference transaction, final IsolationLevel isolation,
            final RequestContext context) {

        super(context);
        this.transaction = transaction;
        if (isolation == null) {
            this.isolation = MzsConstants.DEFAULT_ISOLATION;
        } else {
            this.isolation = isolation;
        }
    }

    /**
     * Gets the transaction reference.
     *
     * @return the transaction reference
     */
    public final TransactionReference getTransaction() {
        return transaction;
    }

    /**
     * Gets the isolation level.
     *
     * @return the isolation level
     */
    public final IsolationLevel getIsolation() {
        return isolation;
    }

    /**
     * A class that helps to build a <code>TransactionalRequest</code>.
     *
     * @author Tobias Doenz
     *
     * @param <B>
     *            the type of the builder
     * @param <T>
     *            the type of the request this builder constructs
     */
    public abstract static class Builder<B, T> extends AbstractRequest.Builder<B, T> {

        private TransactionReference transaction;
        private IsolationLevel isolation = MzsConstants.DEFAULT_ISOLATION;

        /**
         * Constructs a <code>Builder</code>.
         */
        protected Builder() {
        }

        /**
         * Sets the transaction reference. The default value is
         * <code>null</code> (implicit transaction), if not explicitly set.
         *
         * @param transaction
         *            the transaction reference
         * @return the builder
         */
        @SuppressWarnings("unchecked")
        public final B transaction(final TransactionReference transaction) {
            this.transaction = transaction;
            return (B) this;
        }

        /**
         * Sets the isolation level. The default value is {@value
         * org.mozartspaces.core.MzsConstants#DEFAULT_ISOLATION MzsConstants#DEFAULT_ISOLATION}, if not explicitly set.
         *
         * @param isolation
         *            the isolation level.
         * @return the builder
         */
        @SuppressWarnings("unchecked")
        public final B isolation(final IsolationLevel isolation) {
            this.isolation = isolation;
            return (B) this;
        }

        // methods used in sub-class when the request is constructed
        protected final TransactionReference getTransaction() {
            return transaction;
        }

        protected final IsolationLevel getIsolation() {
            return isolation;
        }
    }
}
