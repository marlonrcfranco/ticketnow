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
package org.mozartspaces.capi3.javanative.isolation;

import org.mozartspaces.capi3.LogItem;
import org.mozartspaces.capi3.SubTransaction;
import org.mozartspaces.capi3.TransactionStatus;

/**
 * A <code>NativeSubTransaction</code> is always associated with a parent
 * <code>Transaction</code>.
 *
 * @author Martin Barisits
 */
public interface NativeSubTransaction extends SubTransaction {

    /**
     * Adds a logItem for later rollback/commit.
     *
     * @param logItem
     *            to be added
     */
    void addLog(LogItem logItem);

    /**
     * {@inheritDoc}
     */
    boolean equals(final Object obj);

    /**
     * {@inheritDoc}
     */
    int hashCode();

    /**
     * Returns the Status of the SubTransaction.
     *
     * @return Status
     */
    TransactionStatus getStatus();

}
