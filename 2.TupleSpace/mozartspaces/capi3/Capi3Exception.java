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

import org.mozartspaces.core.MzsCoreException;

/**
 * Super class of all defined and "expected" exceptions in CAPI-3.
 *
 * @author Martin Barisits, Tobias Doenz
 */
public class Capi3Exception extends MzsCoreException {

    private static final long serialVersionUID = 1L;

//    private static final StackTraceElement[] EMPTY_STACKTRACE = new StackTraceElement[] {
//        new StackTraceElement("[Stack trace truncated in Capi3Exception]", "", null, -1)
//    };

    /**
     * Constructs a new exception with null as its detail message.
     */
    public Capi3Exception() {
        super();
    }

    /**
     * Constructs a new exception with the specified detail message.
     *
     * @param message
     *            the detail message.
     */
    public Capi3Exception(final String message) {
        super(message);
    }

    /**
     * Constructs a new exception with the specified detail message and cause.
     *
     * @param message
     *            the detail message.
     * @param cause
     *            the cause.
     */
    public Capi3Exception(final String message, final Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructs a new exception with the specified cause.
     *
     * @param cause
     *            the cause.
     */
    public Capi3Exception(final Throwable cause) {
        super(cause);
    }

    /**
     * Sets an "empty" stack trace for performance optimization. The filled in
     * stack trace contains a single element as explanation.
     *
     * @return a reference to this exception
     */
//    @Override
//    public final Throwable fillInStackTrace() {
//        setStackTrace(EMPTY_STACKTRACE);
//        return this;
//    }
}
