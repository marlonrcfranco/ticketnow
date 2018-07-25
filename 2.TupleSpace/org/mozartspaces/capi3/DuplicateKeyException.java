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
 * If a duplicate Key exists in a Key coordinator.
 *
 * @author Florian
 */
public class DuplicateKeyException extends Capi3Exception {

    private static final long serialVersionUID = -7331251183843685754L;

    /**
     * Creates a <code>DuplicateKey</code> Exception.
     *
     * @param coordinator
     *            name of the Coordinator the exception is thrown
     * @param key
     *            duplicate key
     */
    public DuplicateKeyException(final String coordinator, final String key) {
        super("The key '" + key + "' is already existing in Coordinator '"
                + coordinator + "'");
    }

    // for serialization
    @SuppressWarnings("unused")
    private DuplicateKeyException() {
    }
}
