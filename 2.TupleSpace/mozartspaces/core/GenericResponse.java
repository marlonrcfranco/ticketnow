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
package org.mozartspaces.core;

import java.io.Serializable;

import net.jcip.annotations.Immutable;

/**
 * A generic implementation of <code>Response</code>.
 *
 * @author Tobias Doenz
 *
 * @param <R>
 *            the result type
 */
@Immutable
public final class GenericResponse<R extends Serializable> implements Response<R> {

    private static final long serialVersionUID = 1L;

    private final R result;
    private final Throwable error;

    /**
     * Constructs a generic response.
     *
     * @param result
     *            the result, <code>null</code> if an error occurred
     * @param error
     *            the error, <code>null</code> if no error occurred
     */
    public GenericResponse(final R result, final Throwable error) {
        this.result = result;
        this.error = error;
        assert !(this.result == null && this.error == null) : "Neither result nor error set";
        assert this.result == null || this.error == null : "Result and error set";
    }

    // for serialization
    @SuppressWarnings("unused")
    private GenericResponse() {
        this.result = null;
        this.error = null;
    }

    @Override
    public R getResult() {
        return result;
    }

    @Override
    public Throwable getError() {
        return error;
    }

    @Override
    public String toString() {
        return "GenericResponse [result=" + result + ", error=" + error + "]";
    }

}
