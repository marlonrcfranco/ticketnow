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
 * Thrown when for a Write-Operation at least one obligatory Coordinator is not
 * associated by an Selector.
 *
 * @author Martin Barisits
 */
public final class ObligatoryCoordinatorMissingException extends Capi3Exception {

    private static final long serialVersionUID = -714433640348265519L;

    /**
     * Creates a <code>ObligatoryCoordinatorMissing</code> Exception.
     *
     * @param coordinatorName
     *            the coordinatorName
     */
    public ObligatoryCoordinatorMissingException(final String coordinatorName) {
        super("The obligatory Coordinator '" + coordinatorName
                + "' is not mentioned in the selector chain.");
    }

}
