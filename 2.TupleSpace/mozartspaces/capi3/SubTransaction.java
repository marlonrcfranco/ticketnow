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

/**
 * A <code>SubTransaction</code> is always associated with a parent
 * <code>Transaction</code>.
 *
 * @author Martin Barisits
 *
 */
public interface SubTransaction {

    /**
     * Returns the parent <code>Transaction</code> of this SubTransaction.
     *
     * @return the parent Transaction
     */
    Transaction getParent();

    /**
     * Commits all operations executed with this <code>SubTransaction</code>.
     *
     * @throws InvalidTransactionException
     *             if the Transaction is invalid
     */
    void commit() throws InvalidTransactionException;

    /**
     * Executes a Rollback for all operations executed with this SubTransaction.
     *
     * @throws InvalidTransactionException
     *             if the Transaction is invalid
     */
    void rollback() throws InvalidTransactionException;

    /**
     * Returns the unique ID of this SubTransaction.
     *
     * @return the unique SubTransaction ID
     */
    String getId();

    /**
     * Checks the sub-transaction's validity.
     *
     * @return <code>true</code> if the transaction has been committed or
     *         aborted, <code>false</code> otherwise
     */
    boolean isValid();

}
