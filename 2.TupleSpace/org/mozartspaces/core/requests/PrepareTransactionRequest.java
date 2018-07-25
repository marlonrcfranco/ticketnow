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
import org.mozartspaces.core.TransactionReference;
import org.mozartspaces.core.util.Nothing;

/**
 * A <code>Request</code> to prepare a transaction for commit (voting phase of
 * two-phase commit) on a specific space.
 *
 * @author Tobias Doenz
 */
@ThreadSafe
public final class PrepareTransactionRequest extends AbstractRequest<Nothing> {

    private static final long serialVersionUID = 1L;

    private final TransactionReference transaction;

    /**
     * Constructs a <code>PrepareTransactionRequest</code>. Consider to use a
     * {@link #withTransaction(TransactionReference) Builder}.
     *
     * @param transaction
     *            the reference of the transaction that should be prepared for
     *            commit
     * @param context
     *            the request context
     */
    public PrepareTransactionRequest(final TransactionReference transaction, final RequestContext context) {
        super(context);
        this.transaction = transaction;
        if (this.transaction == null) {
            throw new NullPointerException("Transaction reference is null");
        }
    }

    private PrepareTransactionRequest() {
        super(null);
        this.transaction = null;
    }

    /**
     * Gets the transaction reference.
     *
     * @return the transaction reference
     */
    public TransactionReference getTransaction() {
        return transaction;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((transaction == null) ? 0 : transaction.hashCode());
        // from superclass
        result = prime * result + ((getContext() == null) ? 0 : getContext().hashCode());
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof PrepareTransactionRequest)) {
            return false;
        }
        PrepareTransactionRequest other = (PrepareTransactionRequest) obj;
        if (transaction == null) {
            if (other.transaction != null) {
                return false;
            }
        } else if (!transaction.equals(other.transaction)) {
            return false;
        }
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
        return "PrepareTransactionRequest [transaction=" + transaction + ", context=" + getContext() + "]";
    }

    // builder stuff below
    /**
     * Constructs a new builder with a transaction reference.
     *
     * @param transaction
     *            the reference of the transaction that should be prepared for
     *            commit. This parameter must not be <code>null</code>.
     * @return the builder with the transaction reference set
     */
    public static Builder withTransaction(final TransactionReference transaction) {
        return new Builder(transaction);
    }

    /**
     * A class that helps to build a <code>PrepareTransactionRequest</code>.
     *
     * @author Tobias Doenz
     */
    public static final class Builder extends
            AbstractRequest.Builder<PrepareTransactionRequest.Builder, PrepareTransactionRequest> {

        private final TransactionReference transaction;

        /**
         * Protected constructor, use the static factory method
         * {@link PrepareTransactionRequest#withTransaction(TransactionReference)}
         * .
         */
        protected Builder(final TransactionReference transaction) {
            this.transaction = transaction;
        }

        @Override
        public PrepareTransactionRequest build() {
            return new PrepareTransactionRequest(transaction, getContext());
        }
    }

}
