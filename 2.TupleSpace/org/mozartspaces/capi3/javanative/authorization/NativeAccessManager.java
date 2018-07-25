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
package org.mozartspaces.capi3.javanative.authorization;

import org.mozartspaces.capi3.OperationType;
import org.mozartspaces.capi3.javanative.isolation.NativeSubTransaction;
import org.mozartspaces.capi3.javanative.operation.NativeContainer;
import org.mozartspaces.core.RequestContext;

/**
 * The access manager enforces access control for entries according to the XVSM Access Control Model.
 *
 * @author Stefan Crass
 */
public interface NativeAccessManager {


    /**
     * Check which entries accessible for this (sub) transaction and operation type.
     *
     * @param container
     *            the container to check
     * @param stx
     *            the SubTransaction to use
     * @param context
     *            the request context
     * @param opType
     *            type of the Operation the Entry should be available for
     * @return AuthorizationResult PERMITTED if access is permitted, DENIED if access is denied.
     */
    AuthorizationResult checkPermissions(final NativeContainer container, final OperationType opType,
            final NativeSubTransaction stx, final RequestContext context);


    /**
     * Sets the root policy container for the access manager.
     * @param policyC
     *          the root policy container
     */
    void setPolicyContainer(final NativeContainer policyC);

}
