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
 * Thrown if a count is set for a selector and the selector has not enough entries to satisfy it.
 *
 * @author Martin Barisits
 * @author Tobias Doenz
 */
public final class CountNotMetException extends Capi3Exception {

    private static final long serialVersionUID = 1L;

    private final String selectorName;
    private final int countNeeded;
    private final int countAvailable;

    /**
     * Creates a <code>CountNotMet</code> Exception.
     *
     * @param selectorName
     *            the name of the selector/coordinator
     * @param countNeeded
     *            the number of needed entries
     * @param countAvailable
     *            the number of available entries
     */
    public CountNotMetException(final String selectorName, final int countNeeded, final int countAvailable) {
        super("Count(" + countNeeded + ") of selector '" + selectorName + "' not met. (" + countAvailable
                + " entries available)");
        this.selectorName = selectorName;
        this.countNeeded = countNeeded;
        this.countAvailable = countAvailable;
    }

    // for serialization
    @SuppressWarnings("unused")
    private CountNotMetException() {
        this.selectorName = null;
        this.countNeeded = 0;
        this.countAvailable = 0;
    }

    /**
     * @return the name of the selector/coordinator
     */
    public String getSelectorName() {
        return selectorName;
    }

    /**
     * @return the number of needed entries
     */
    public int getCountNeeded() {
        return countNeeded;
    }

    /**
     * @return the number of available entries
     */
    public int getCountAvailable() {
        return countAvailable;
    }

}
