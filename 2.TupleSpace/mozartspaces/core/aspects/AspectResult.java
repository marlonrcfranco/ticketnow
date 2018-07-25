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
package org.mozartspaces.core.aspects;

import net.jcip.annotations.Immutable;

import org.mozartspaces.capi3.OperationResult;

/**
 * The <code>AspectResult</code> encapsulates the result of one or several
 * aspect executions.
 *
 * @author Tobias Doenz
 */
@Immutable
public final class AspectResult {

    /**
     * Aspect result with status {@link AspectStatus#OK OK}.
     */
    public static final AspectResult OK = new AspectResult(AspectStatus.OK);

    /**
     * Aspect result with status {@link AspectStatus#SKIP SKIP}.
     */
    public static final AspectResult SKIP = new AspectResult(AspectStatus.SKIP);

    private final AspectStatus status;
    private final Throwable cause;

    /**
     * Constructs an <code>AspectResult</code>.
     *
     * @param status
     *            the status of the aspect(s)
     */
    public AspectResult(final AspectStatus status) {
        this(status, null);
    }

    /**
     * Constructs an <code>AspectResult</code>.
     *
     * @param status
     *            the status of the aspect(s)
     * @param cause
     *            the cause of a failed aspect execution, may be
     *            <code>null</code>
     */
    public AspectResult(final AspectStatus status, final Throwable cause) {
        this.status = status;
        if (this.status == null) {
            throw new NullPointerException("Status is null");
        }
        this.cause = cause;
    }

    /**
     * Constructs an <code>AspectResult</code> with status NOTOK.
     *
     * @param cause
     *            the cause of a failed aspect execution, may be
     *            <code>null</code>
     */
    public AspectResult(final Throwable cause) {
        this(AspectStatus.NOTOK, cause);
    }

    /**
     * Constructs an <code>AspectResult</code> from an
     * <code>OperationResult</code>.
     *
     * @param result
     *            an operation result
     */
    public AspectResult(final OperationResult result) {
        assert result != null;
        this.status = AspectStatus.fromOperationStatus(result.getStatus());
        this.cause = result.getCause();
    }

    /**
     * @return the status of the aspect(s)
     */
    public AspectStatus getStatus() {
        return status;
    }

    /**
     * @return indicates the cause, if the aspect execution failed, may be
     *         <code>null</code>
     */
    public Throwable getCause() {
        return cause;
    }
}
