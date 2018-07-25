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
package org.mozartspaces.capi3.javanative.persistence.berkeley;

import com.sleepycat.je.Transaction;

/**
 * This interface defines how CAPI-transactions get mapped (1:1) to Berkeley DB transactions.
 *
 * @author Jan Zarnikov
 */
public interface TransactionMapper {

    /**
     * Get the Berkeley DB transaction that is coupled to the CAPI transaction with the given ID.
     * @param capiTransactionId ID of the CAPI transaction
     * @return a Berkeley DB transaction or null if no Berkeley DB transaction is coupled with the CAPI transaction.
     */
    Transaction getTransaction(String capiTransactionId);

    /**
     * Get the Berkeley DB transaction that is coupled to the CAPI transaction.
     * @param capiTransaction the CAPI transaction
     * @return a Berkeley DB transaction or null if no Berkeley DB transaction is coupled with the CAPI transaction.
     */
    Transaction getTransaction(org.mozartspaces.capi3.Transaction capiTransaction);

}
