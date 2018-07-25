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
 * The Status of a Transaction or SubTransaction.
 *
 * @author Martin Barisits
 */
public enum TransactionStatus {

    /**
     * Transaction is running.
     */
    RUNNING,

    /**
     * Transaction is currently commiting and not finished.
     */
    COMMITING,
    // TODO rename to COMMITTING (spelling)

    /**
     * Transaction is commited.
     */
    COMMITED,
    // TODO rename to COMMITTED (spelling)

    /**
     * Transaction is currently aborting and not finished.
     */
    ABORTING,

    /**
     * Transaction is aborted.
     */
    ABORTED
}
