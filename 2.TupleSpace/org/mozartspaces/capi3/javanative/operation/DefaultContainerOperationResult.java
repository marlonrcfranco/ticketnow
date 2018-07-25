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

import org.mozartspaces.capi3.ContainerOperationResult;
import org.mozartspaces.capi3.LocalContainerReference;
import org.mozartspaces.capi3.OperationStatus;

/**
 * OperationResult for a CAPI3 ContainerCreateOperation.
 *
 * @author Martin Barisits
 */
public final class DefaultContainerOperationResult implements ContainerOperationResult {

    private final OperationStatus status;
    private final Throwable cause;
    private final LocalContainerReference containerReference;

    /**
     * Create a ContainerCreateOperationResult.
     *
     * @param status
     *            of the operation
     * @param cause
     *            of the failure
     * @param cRef
     *            container Reference
     */
    public DefaultContainerOperationResult(final OperationStatus status, final Throwable cause,
            final LocalContainerReference cRef) {
        if (status == null) {
            throw new NullPointerException("The OperationStatus must be set");
        }

        this.status = status;
        this.cause = cause;
        this.containerReference = cRef;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LocalContainerReference getContainerReference() {
        return this.containerReference;
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
