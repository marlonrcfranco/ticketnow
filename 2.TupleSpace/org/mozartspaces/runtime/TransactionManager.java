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
package org.mozartspaces.runtime;

import org.mozartspaces.capi3.InvalidTransactionException;
import org.mozartspaces.capi3.Transaction;
import org.mozartspaces.core.MzsTimeoutException;
import org.mozartspaces.core.TransactionReference;

/**
 * Manages the currently active explicit transactions in the core.
 *
 * @author Tobias Doenz
 *
 * @see Transaction
 */
// TODO? get list of transactions (MAPI)
public interface TransactionManager {

    /**
     * Returns the number of transactions that are currently managed. This
     * includes all created and not yet committed or rollbacked transactions,
     * except implicit transactions that span only one request.
     *
     * @return the number of currently managed transactions
     */
    int getNumberOfTransactions();

    /**
     * Adds a transaction to the Transaction Manager. If the timeout value is
     * not {@link org.mozartspaces.core.MzsConstants.RequestTimeout#INFINITE
     * INFINITE}, the expiration time is calculated based on the actual time
     * ({@link System#nanoTime()}) and the transaction is added to the
     * transaction timeout processor.
     *
     * @param txRef
     *            the transaction reference
     * @param tx
     *            the internal transaction
     * @param timeout
     *            the transaction timeout in milliseconds, as set in the request
     */
    void addTransaction(TransactionReference txRef, Transaction tx, long timeout);

    /**
     * Returns the internal transaction object for the specified transaction
     * reference.
     *
     * @param txRef
     *            the transaction reference
     * @return the internal transaction object
     * @throws MzsTimeoutException
     *             when the transaction already timed out; only thrown once for
     *             each transaction
     */
    Transaction getTransaction(TransactionReference txRef) throws MzsTimeoutException;

    /**
     * Commits a transaction, specified by its reference.
     *
     * @param txRef
     *            the transaction reference
     * @param implicitTx
     *            <code>true</code>, if the transaction is an implicit
     *            transaction, <code>false</code> otherwise
     * @throws InvalidTransactionException
     *             if the internal transaction cannot be committed
     * @throws MzsTimeoutException
     *             when the transaction already timed out; only thrown once for
     *             each transaction
     */
    void commitTransaction(TransactionReference txRef, boolean implicitTx) throws InvalidTransactionException,
            MzsTimeoutException;

    /**
     * Rollbacks a transaction, specified by its reference.
     *
     * @param txRef
     *            the transaction reference
     * @param implicitTx
     *            <code>true</code>, if the transaction is an implicit
     *            transaction, <code>false</code> otherwise
     * @throws InvalidTransactionException
     *             if the internal transaction cannot be rollbacked
     * @throws MzsTimeoutException
     *             when the transaction already timed out; only thrown once for
     *             each transaction
     */
    void rollbackTransaction(TransactionReference txRef, boolean implicitTx) throws InvalidTransactionException,
            MzsTimeoutException;

    /**
     * Shuts the Transaction Manager down. All managed transactions are
     * rollbacked.
     */
    void shutdown();

}
