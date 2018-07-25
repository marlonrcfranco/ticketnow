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
package org.mozartspaces.capi3;

import java.util.Collection;
import java.util.List;

/**
 * The <code>Transaction</code> is responsible to provide transactional safety
 * in the application.
 *
 * @author Martin Barisits
 */
public interface Transaction {

    /**
     * Creates a new <code>SubTransaction</code> associated to this Transaction
     * and returns it to the user.
     *
     * @return a SubTransaction
     * @throws InvalidTransactionException
     *             if the Transaction is invalid
     */
    SubTransaction newSubTransaction() throws InvalidTransactionException;

    /**
     * Commits all operations executed with this <code>Transaction</code> and
     * consequently all associated SubTransactions.
     *
     * @throws InvalidTransactionException
     *             if the Transaction is invalid
     */
    void commit() throws InvalidTransactionException;

    /**
     * Executes a Rollback for all operations executed with this Transaction and
     * all associated SubTransactions.
     *
     * @throws InvalidTransactionException
     *             if the Transaction is invalid
     */
    void rollback() throws InvalidTransactionException;

    /**
     * Returns the unique ID of this Transaction.
     *
     * @return the unique Transaction ID
     */
    String getId();

    /**
     * To log a user-given LogItem.
     *
     * @param logItem
     *            The item to log
     * @throws InvalidTransactionException
     *             if the Transaction is invalid
     */
    void addLog(LogItem logItem) throws InvalidTransactionException;

    /**
     * Retrieve the Transaction Log of all Operations executed by this
     * Transaction.
     * <p>
     * Note: This is a special method for use in aspects. It is
     * (currently) not used in the runtime or by CAPI-3.
     *
     * @return a List containing all LogItems
     */
    List<LogItem> getLog();

    /**
     * @return returns the containers accessed in this transaction.
     */
    Collection<LocalContainerReference> getAccessedContainers();

    /**
     * Checks the transaction's validity.
     *
     * @return <code>true</code> if the transaction has been committed or
     *         aborted, <code>false</code> otherwise
     */
    // TODO change to isActive (use according variable in implementation)
    boolean isValid();

    /**
     * Locks the transaction and blockingly wait for sub-transactions to finish.
     * This method should be called before commit/rollback.
     *
     * @throws InterruptedException
     *             if another thread interrupted the current thread before or
     *             while the current thread was waiting for the sub-transactions
     *             to finish
     */
    void lockAndWaitForSubTransactions() throws InterruptedException;

    /**
     * Returns whether the transaction is locked. A transaction is locked
     * before commit/rollback, no new sub-transactions can be created then.
     *
     * @return <code>true</code> if the transaction is locked,
     * <code>false</code> otherwise
     */
    boolean isLocked();

}
