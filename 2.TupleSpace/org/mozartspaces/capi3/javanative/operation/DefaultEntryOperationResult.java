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

import java.io.Serializable;
import java.util.List;

import org.mozartspaces.capi3.EntryOperationResult;
import org.mozartspaces.capi3.OperationStatus;

/**
 * OperationResult for the CAPI3 Take Operation.
 *
 * @author Martin Barisits
 */
public final class DefaultEntryOperationResult implements EntryOperationResult {

    private final OperationStatus status;
    private final Throwable cause;
    private final List<? extends Serializable> result;

    /**
     * Creates a CAPI3 Take Operation Result.
     *
     * @param status
     *            of the Operation
     * @param cause
     *            of the failure
     * @param result
     *            holding the entries
     */
    public DefaultEntryOperationResult(final OperationStatus status, final Throwable cause,
            final List<? extends Serializable> result) {
        if (status == null) {
            throw new NullPointerException("The OperationStatus must be set");
        }
        this.status = status;
        this.cause = cause;
        this.result = result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<? extends Serializable> getResult() {
        return this.result;
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
