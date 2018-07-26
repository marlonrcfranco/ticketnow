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

import org.mozartspaces.capi3.AccessDeniedException;
import org.mozartspaces.capi3.EntryLockedException;
import org.mozartspaces.capi3.IsolationLevel;
import org.mozartspaces.capi3.OperationType;
import org.mozartspaces.capi3.javanative.LockedExceptionsHelper;
import org.mozartspaces.capi3.javanative.authorization.AuthorizationResult;
import org.mozartspaces.capi3.javanative.authorization.AuthorizationType;
import org.mozartspaces.capi3.javanative.isolation.Availability;
import org.mozartspaces.capi3.javanative.isolation.NativeSubTransaction;
import org.mozartspaces.capi3.javanative.operation.NativeEntry;

/**
 * Abstract class for the default selectors with the standard distinction of the selector position in the chain (first
 * selector or not) and the selection granularity (all entries or the next entry). Is extended in the predefined
 * internal selector implementations and can also be used for custom coordinators.
 *
 * @param <C> the coordinator type this selector belongs to
 *
 * @author Tobias Doenz
 * @author Stefan Crass
 */
public abstract class AbstractDefaultSelector<C extends NativeCoordinator> extends AbstractNativeSelector<C> {

    private static final long serialVersionUID = 1L;

    // stores information that an entry is skipped because it is locked
    private Availability entryLockedAvailability;
    private boolean existsDeniedEntry = false;

    protected AbstractDefaultSelector(final String name, final int count) {
        super(name, count);
    }

    protected final Availability getEntryLockedAvailability() {
        return this.entryLockedAvailability;
    }

    protected final boolean existsDeniedEntry() {
        return this.existsDeniedEntry;
    }

    protected final boolean existsInaccessibleEntry() {
        return (this.existsDeniedEntry || this.entryLockedAvailability != null);
    }

    /**
     * Checks the entry accessibility.
     *
     * @param entry
     *            the entry
     * @param isolationLevel
     *            the isolation level
     * @param auth
     *            the authorization result
     * @param stx
     *            the sub-transaction
     * @param opType
     *            the operation type
     * @param isMandatory
     *            whether the entry is needed for the result or can be skipped (depends on the coordinator)
     * @return whether the entry is accessible
     * @throws AccessDeniedException
     *             if access to the entry is denied
     * @throws EntryLockedException
     *             if the entry is locked
     */
    public final boolean checkAccessibility(final NativeEntry entry, final IsolationLevel isolationLevel,
            final AuthorizationResult auth, final NativeSubTransaction stx, final OperationType opType,
            final boolean isMandatory) throws AccessDeniedException, EntryLockedException {

        // check whether the entry is permitted
        boolean permitted = false;
        if (auth == null || auth.checkEntryAuthorization(entry) == AuthorizationType.PERMITTED) {
            permitted = true;
        }

        // check whether the entry is available
        Availability availability = this.getIsolationManager().checkEntryAvailability(entry, isolationLevel, stx,
                opType);

        switch (availability.getType()) {
        case AVAILABLE:
            if (permitted) {
                return true;
            } else {
                this.existsDeniedEntry = true;
                if (isMandatory) {
                    throw new AccessDeniedException();
                } else {
                    return false;
                }
            }
        case NOTVISIBLE:
            // insert lock exists on the entry
            return false;
        case NOTAVAILABLE:
            this.entryLockedAvailability = availability;
            if (isMandatory) {
                throw LockedExceptionsHelper.newEntryLockedException(this.entryLockedAvailability);
            } else {
                return false;
            }
        default:
            throw new IllegalStateException("This state should not be reachable");
        }
    }
}