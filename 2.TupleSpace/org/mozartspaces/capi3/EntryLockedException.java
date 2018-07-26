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
 * Thrown when an operation tries to access an already locked entry.
 * TODO add method to parse exception from its message (for generic serialization)
 *
 * @author Martin Barisits
 * @author Tobias Doenz
 */
public final class EntryLockedException extends Capi3Exception {

    private static final long serialVersionUID = 1L;

    private final String txId;
    private final String stxId;

    /**
     * Creates a new exception instance.
     * TODO add the selector name from where this exception is thrown
     *
     * @param txId
     *            the ID of the transaction holding the lock
     * @param stxId
     *            the ID of the sub-transaction holding the short-term lock, can be <code>null</code> if this is a
     *            long-term lock
     */
    public EntryLockedException(final String txId, final String stxId) {
        super("An entry is locked by TX " + txId + " (Sub-TX " + stxId + ")");
        this.txId = txId;
        this.stxId = stxId;
    }

    // for serialization
    @SuppressWarnings("unused")
    private EntryLockedException() {
        this.txId = null;
        this.stxId = null;
    }

    /**
     * @return the id of the transaction holding the lock
     */
    public String getTxId() {
        return txId;
    }

    /**
     * @return the id of the sub-transaction holding the short-term lock, can be <code>null</code> if this is a
     *         long-term lock
     */
    public String getSubTxId() {
        return stxId;
    }

}
