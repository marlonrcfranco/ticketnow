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
 * This is the Visibility Return-Type to determine if an Entry or Container is
 * visible.
 * <p>
 * The Visibility Setting is depending on the Isolation Level: AVAILABLE means:
 * The Entry/Container is available for the proposed Operation. NOTAVAILABLE:
 * The Entry/Container is not available for the proposed operation. NOTVISIBLE:
 * The Entry/Container is not visible (INSERT LOCKED)
 *
 * @author Martin Barisits
 * @author Tobias Doenz
 */
public final class Availability {

    /**
     * Type of Visibility.
     *
     * @author Martin Barisits
     */
    public enum AvailabilityType {
        /**
         * The Entry is available for the given Operation.
         */
        AVAILABLE,
        /**
         * The Entry is not available for the given Operation.
         */
        NOTAVAILABLE,
        /**
         * The Entry is not visible at all.
         */
        NOTVISIBLE
    };

    private final AvailabilityType type;
    private final NativeTransaction tx;
    private final NativeSubTransaction stx;

    /**
     * Return the Transaction causing a Status May be <code>null</code>.
     *
     * @return the Transaction
     */
    public NativeTransaction getTx() {
        return tx;
    }

    /**
     * Return the SubTransaction causing a Status May be <code>null</code>.
     *
     * @return the SubTransaction
     */
    public NativeSubTransaction getSubTx() {
        return stx;
    }

    /**
     * Returns the Availability Type.
     *
     * @return the Availability Type
     */
    public AvailabilityType getType() {
        return type;
    }

    /**
     * Constructor of the Availability Entity.
     *
     * @param type
     *            the AvailabilityType to return
     * @param tx
     *            the Transaction which is responsible for the Visibility, may
     *            be <code>null</code>
     * @param stx
     *            the SubTransaction which is responsible for the Visibility,
     *            may be <code>null</code>
     */
    Availability(final AvailabilityType type, final NativeTransaction tx, final NativeSubTransaction stx) {
        this.type = type;
        if (this.type == null) {
            throw new NullPointerException("The AvailabilityType must be set");
        }
        this.tx = tx;
        this.stx = stx;
    }

    /**
     * Availability with type {@literal AVAILABLE} and no transaction or sub-transaction.
     */
    public static final Availability AVAILABLE = new Availability(AvailabilityType.AVAILABLE, null, null);

    /**
     * Availability with type {@literal NOTVISIBLE} and no transaction or sub-transaction.
     */
    public static final Availability NOTVISIBLE = new Availability(AvailabilityType.NOTVISIBLE, null, null);

    @Override
    public String toString() {
        return "Availability [type=" + type + ", tx=" + tx + ", stx=" + stx + "]";
    }

}
