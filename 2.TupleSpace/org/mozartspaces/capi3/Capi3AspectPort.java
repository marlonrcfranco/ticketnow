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

import java.util.List;

import org.mozartspaces.core.Entry;
import org.mozartspaces.core.RequestContext;

/**
 * The <code>capi3AspectPort</code> is used to supply a limited Capi3-Interface to Aspects.
 *
 * @author Martin Barisits
 * @author Tobias Doenz
 */
public interface Capi3AspectPort {

    /**
     * Is used to execute a read operation for an already referenced container with a user-defined selector chain and a
     * specified isolation level.
     *
     * @param selectors
     *            the selector chain which is responsible for filtering entries
     * @param isolationLevel
     *            isolation level to be used
     * @param stx
     *            subTransaction to be used to isolate the data
     * @param context
     *            context information passed through the core, may be <code>null</code>
     * @return an EntryOperationResult containing the relevant information
     */
    EntryOperationResult executeReadOperation(List<? extends Selector> selectors, IsolationLevel isolationLevel,
            SubTransaction stx, RequestContext context);

    /**
     * Used to write an object to a already referenced container. This object is also registered to the associated
     * coordinators.
     *
     * @param entry
     *            the entry with the object that should be written
     * @param isolationLevel
     *            isolation level to be used
     * @param stx
     *            subTransaction to be used to isolate the data
     * @param context
     *            context information passed through the core, may be <code>null</code>
     * @return an OperationResult containing the relevant information
     */
    OperationResult executeWriteOperation(Entry entry, IsolationLevel isolationLevel, SubTransaction stx,
            RequestContext context);

    /**
     * Is used to execute a take operation for an already referenced container with a user-defined selector chain and a
     * specified isolation level. The returned objects are stored in the <code>OperationResult</code>
     *
     * @param selectors
     *            the selector chain which is responsible for filtering entries
     * @param isolationLevel
     *            isolation level to be used
     * @param stx
     *            subTransaction to be used to isolate the data
     * @param context
     *            context information passed through the core, may be <code>null</code>
     * @return an EntryOperationResult containing the relevant information
     */
    EntryOperationResult executeTakeOperation(List<? extends Selector> selectors, IsolationLevel isolationLevel,
            SubTransaction stx, RequestContext context);

}
