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
package org.mozartspaces.capi3.javanative.persistence;

import org.mozartspaces.capi3.Capi3Exception;

/**
 * Thrown when the initialization of persistent storage fails (e.g. because of bad configuration).
 *
 * @author Jan Zarnikov
 */
public class PersistenceInitializationException extends Capi3Exception {

    private static final long serialVersionUID = 1L;

    /**
     * Create a new exception with the given message.
     * @param message a descriptive message of what went wrong.
     */
    public PersistenceInitializationException(final String message) {
        super(message);
    }

    /**
     * Create new exception with the given message and a cause.
     * @param message a descriptive message of what went wrong.
     * @param cause a throwable of the underlying problem
     */
    public PersistenceInitializationException(final String message, final Throwable cause) {
        super(message, cause);
    }

}
