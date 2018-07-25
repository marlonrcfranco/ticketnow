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
 * Thrown when two Coordinators of the same Name are registered at a Container.
 *
 * @author Martin Barisits
 */
public final class DuplicateCoordinatorException extends Capi3Exception {

    private static final long serialVersionUID = -978557576581479288L;

    /**
     * Creates a <code>InvalidCoordinator</code> Exception.
     *
     * @param coordinatorName
     *            name of the Coordinator
     */
    public DuplicateCoordinatorException(final String coordinatorName) {
        super("The Coordinator " + coordinatorName + " is already registered.");
    }

    // for serialization
    @SuppressWarnings("unused")
    private DuplicateCoordinatorException() {
    }
}
