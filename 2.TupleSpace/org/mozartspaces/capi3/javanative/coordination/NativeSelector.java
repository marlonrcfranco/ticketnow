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
package org.mozartspaces.capi3.javanative.coordination;

import java.util.List;

import org.mozartspaces.capi3.AccessDeniedException;
import org.mozartspaces.capi3.CountNotMetException;
import org.mozartspaces.capi3.EntryLockedException;
import org.mozartspaces.capi3.IsolationLevel;
import org.mozartspaces.capi3.OperationType;
import org.mozartspaces.capi3.Selector;
import org.mozartspaces.capi3.javanative.authorization.AuthorizationResult;
import org.mozartspaces.capi3.javanative.isolation.NativeIsolationManager;
import org.mozartspaces.capi3.javanative.isolation.NativeSubTransaction;
import org.mozartspaces.capi3.javanative.operation.NativeEntry;
import org.mozartspaces.core.RequestContext;

/**
 * Interface for all CAPI-3 JavaNative selector implementations.
 *
 * @param <C> the coordinator type this selector belongs to
 *
 * @author Martin Barisits
 * @author Tobias Doenz
 * @author Stefan Crass
 */
public interface NativeSelector<C extends NativeCoordinator> extends Selector {

    /**
     * Retrieves all entries in the stream. For every selecting operation, this method is called on the last selector
     * in the chain. This method is also called by a selector inside the chain, when all matching entries are needed,
     * e.g., because they need to be reordered or sorted to ensure a property of the coordinator like FIFO order.
     *
     * @param isolationLevel
     *            the isolation level of the selecting operation
     * @param auth
     *            the authorization result for the container entries
     * @param stx
     *            the sub-transaction of the selecting operation
     * @param opType
     *            the type of the operation for which the data is fetched
     * @param context
     *            the request context
     * @return all entries
     * @throws CountNotMetException
     *             if the count of at least one selector is not reached
     * @throws EntryLockedException
     *             if an entry which should be processed is locked
     * @throws AccessDeniedException
     *             if an entry which should be processed is denied by the authorization process
     */
    List<NativeEntry> getAll(IsolationLevel isolationLevel, AuthorizationResult auth, NativeSubTransaction stx,
            OperationType opType, RequestContext context)
                    throws CountNotMetException, EntryLockedException, AccessDeniedException;

    /**
     * Retrieves the next entry in the stream. This method is called by a selector inside the chain, when the selector
     * does not need all entries to ensure the order.
     *
     * @param isolationLevel
     *            the isolation level of the selecting operation
     * @param auth
     *            the authorization result for the container entries
     * @param stx
     *            the sub-transaction of the selecting operation
     * @param opType
     *            the type of the operation for which the data is fetched
     * @param context
     *            the request context
     * @return the next entry
     * @throws CountNotMetException
     *             if the count of at least one selector is not reached
     * @throws EntryLockedException
     *             if an entry which should be processed is locked
     * @throws AccessDeniedException
     *             if an entry which should be processed is denied by the authorization process
     */
    NativeEntry getNext(IsolationLevel isolationLevel, AuthorizationResult auth, NativeSubTransaction stx,
            OperationType opType, RequestContext context)
                    throws CountNotMetException, EntryLockedException, AccessDeniedException;

    /**
     * Sets the predecessing selector.
     *
     * @param next
     *            the previous selector in the stream, {@code null} if this is the first selector
     */
    void setPredecessor(NativeSelector<NativeCoordinator> next);

    /**
     * Links this selector to the specific coordinator registered at the target container and sets the isolation
     * manager.
     *
     * @param coordinator
     *            the coordinator to link this selector to
     * @param isolationManager
     *            the isolation manager
     */
    void link(C coordinator, NativeIsolationManager isolationManager);

    /**
     * @see java.lang.Object#equals
     * @param obj
     *            the object to compare
     * @return boolean
     */
    @Override
    boolean equals(final Object obj);

    /**
     * @see java.lang.Object#hashCode
     * @return the HashCode
     */
    @Override
    int hashCode();

}
