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
 * Thrown when access to an entry is denied by the authorization framework.
 *
 * @author Stefan Crass
 */
public final class AccessDeniedException extends Capi3Exception {

    private static final long serialVersionUID = -1233759244637601716L;

    /**
     * Constructs a new exception.
     *
     * @see Exception#Exception()
     */
    public AccessDeniedException() {
        super("Access denied.");
        //TODO include reason (entry and policy/rule)
    }

    /**
     * Constructs a new exception with the specified detail message and cause.
     *
     * @param message
     *            the detail message
     * @param cause
     *            the cause
     * @see Exception#Exception(String, Throwable)
     */
    public AccessDeniedException(final String message, final Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructs a new exception with the specified detail message.
     *
     * @param message
     *            the detail message
     * @see Exception#Exception(String)
     */
    public AccessDeniedException(final String message) {
        super(message);
    }

    /**
     * Constructs a new exception with the specified cause.
     *
     * @param cause
     *            the cause
     * @see Exception#Exception(Throwable)
     */
    public AccessDeniedException(final Throwable cause) {
        super(cause);
    }

}
