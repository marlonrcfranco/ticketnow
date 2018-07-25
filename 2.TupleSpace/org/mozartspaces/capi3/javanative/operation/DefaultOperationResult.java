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
package org.mozartspaces.capi3.javanative.operation;

import org.mozartspaces.capi3.OperationResult;
import org.mozartspaces.capi3.OperationStatus;

/**
 * OperationResult of the CAPI3 Write Operation.
 *
 * @author Martin Barisits
 */
public final class DefaultOperationResult implements OperationResult {

    private final OperationStatus status;
    private final Throwable cause;

    /**
     * Create a CAPI3 WriteOperationResult.
     *
     * @param status
     *            of the operation
     * @param cause
     *            of the failure
     */
    public DefaultOperationResult(final OperationStatus status, final Throwable cause) {
        if (status == null) {
            throw new NullPointerException("The OperationStatus must be set");
        }
        this.status = status;
        this.cause = cause;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Throwable getCause() {
        return this.cause;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public OperationStatus getStatus() {
        return this.status;
    }

}
