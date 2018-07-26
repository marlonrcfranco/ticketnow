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
 * Thrown when a Selector is supplied and no associated Coordinator can be found
 * at the Container.
 *
 * @author Martin Barisits
 */
public final class CoordinatorNotRegisteredException extends Capi3Exception {

    private static final long serialVersionUID = -4147593095372915952L;

    /**
     * Creates a <code>InvalidSelector</code> Exception.
     *
     * @param selectorName
     *            name of the invalid Selector
     */
    public CoordinatorNotRegisteredException(final String selectorName) {
        super("There is no registered Coordinator for the Selector " + selectorName + ".");
    }

    // for serialization
    @SuppressWarnings("unused")
    private CoordinatorNotRegisteredException() {
    }
}
