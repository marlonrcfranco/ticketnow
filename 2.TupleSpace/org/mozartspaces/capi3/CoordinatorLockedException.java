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
 * Thrown when a coordinator is locked by another transaction. TODO add method to parse exception from its message (for
 * generic serialization)
 *
 * @author Martin Barisits
 * @author Tobias Doenz
 */
public final class CoordinatorLockedException extends Capi3Exception {

    private static final long serialVersionUID = 1L;

    private final String coordinatorName;
    private final String txId;
    private final String stxId;

    /**
     * Creates a new exception instance.
     *
     * @param coordinatorName
     *            the name of the locked coordinator
     * @param txId
     *            the ID of the transaction holding the lock
     * @param stxId
     *            ID of the sub-transaction holding the lock, can be <code>null</code> if this is a long-term lock
     */
    public CoordinatorLockedException(final String coordinatorName, final String txId, final String stxId) {
        super("The coordinator " + coordinatorName + " is locked by TX " + txId + " (Sub-TX " + stxId + ")");
        this.coordinatorName = coordinatorName;
        this.txId = txId;
        this.stxId = stxId;
    }

    // for serialization
    @SuppressWarnings("unused")
    private CoordinatorLockedException() {
        this.coordinatorName = null;
        this.txId = null;
        this.stxId = null;
    }

    /**
     * @return the name of the locked coordinator
     */
    public String getCoordinatorName() {
        return coordinatorName;
    }

    /**
     * @return the ID of the transaction holding the lock
     */
    public String getTxId() {
        return txId;
    }

    /**
     * @return the ID of the sub-transaction holding the short-term lock, can be <code>null</code> if this is a
     *         long-term lock
     */
    public String getSubTxId() {
        return stxId;
    }

}