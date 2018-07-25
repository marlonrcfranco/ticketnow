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

/**
 * The result of a Lock Operation.
 *
 * @author Martin Barisits
 */
public final class LockResult {

    private final NativeTransaction tx;
    private final NativeSubTransaction stx;
    private final boolean valid;

    /**
     * Create a new LockResult.
     *
     * @param valid
     *            if the Lock could be acquired
     * @param tx
     *            the Transaction causing the failure. Can be <code>null</code>
     * @param stx
     *            the SubTransaction causing the failued. Can be
     *            <code>null</code>
     */
    public LockResult(final boolean valid, final NativeTransaction tx, final NativeSubTransaction stx) {
        this.valid = valid;
        this.tx = tx;
        this.stx = stx;
    }

    /**
     * The Transaction causing the failure.
     *
     * @return the tx
     */
    public NativeTransaction getTx() {
        return tx;
    }

    /**
     * The SubTransaction causing the failure.
     *
     * @return the stx
     */
    public NativeSubTransaction getStx() {
        return stx;
    }

    /**
     * Is the Lock valid.
     *
     * @return the valid
     */
    public boolean isValid() {
        return valid;
    }
}
