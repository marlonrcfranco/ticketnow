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

import static org.mozartspaces.core.MzsConstants.Selecting.isSpecialCountConstant;

import org.mozartspaces.capi3.AccessDeniedException;
import org.mozartspaces.capi3.CountNotMetException;
import org.mozartspaces.capi3.EntryLockedException;
import org.mozartspaces.capi3.javanative.LockedExceptionsHelper;
import org.mozartspaces.capi3.javanative.isolation.Availability;
import org.mozartspaces.capi3.javanative.isolation.NativeIsolationManager;
import org.mozartspaces.core.MzsConstants;

/**
 * Abstract class for native selectors.
 *
 * @param <C> the coordinator type this selector belongs to
 *
 * @author Martin Barisits
 * @author Tobias Doenz
 * @author Stefan Crass
 */
public abstract class AbstractNativeSelector<C extends NativeCoordinator> implements NativeSelector<C> {

    private static final long serialVersionUID = 1L;

    private final String name;
    private final int count;

    private NativeSelector<NativeCoordinator> predecessor;
    private NativeIsolationManager isolationManager;
    private C coordinator;

    /**
     * Constructs an <code>AbstractNativeSelector</code>.
     *
     * @param name
     *            the name of the selector
     * @param count
     *            the count of entries the selector should select
     */
    protected AbstractNativeSelector(final String name, final int count) {
        this.name = name;
        this.count = count;
        MzsConstants.Selecting.checkCount(this.count);

        this.predecessor = null;
    }

    /**
     * Checks whether an expected count is met.
     *
     * @param actualCount
     *            the current actual count
     * @param expectedCount
     *            the expected count
     * @return {@code true} if the actual count is {@code >=} than the expected count and the expected count is not one
     *         of the special count constants.
     *
     * @see org.mozartspaces.core.MzsConstants.Selecting#isSpecialCountConstant(int)
     */
    protected static boolean isCountMet(final int actualCount, final int expectedCount) {
        if (actualCount >= expectedCount && !isSpecialCountConstant(expectedCount)) {
            return true;
        }
        return false;
    }

    /**
     * Checks the current actual count against an expected count.
     *
     * @param actualCount
     *            the current actual count
     * @param expectedCount
     *            the expected count
     * @param message
     *            the message used for the {@code CountNotMetException}, if one is thrown inside this method.
     * @throws CountNotMetException
     *             if the actual count is {@code <} than the expected count and the expected count is not one of the
     *             special count constants.
     *
     * @see org.mozartspaces.core.MzsConstants.Selecting#isSpecialCountConstant(int)
     */
    protected static void checkCount(final int actualCount, final int expectedCount, final String message)
            throws CountNotMetException {
        if (actualCount < expectedCount && !isSpecialCountConstant(expectedCount)) {
            throw new CountNotMetException(message, expectedCount, actualCount);
        }
    }

    /**
     * Checks the current actual count against an expected count.
     *
     * @param actualCount
     *            the current actual count
     * @param expectedCount
     *            the expected count
     * @param entryLockedAvailability
     *            the availability information for an entry, may be {@code null}
     * @param message
     *            the message used for the {@code CountNotMetException}, if one is thrown inside this method.
     * @throws CountNotMetException
     *             if the actual count is {@code <} than the expected count, the expected count is not one of the
     *             special count constants and the argument {@code entryLockedAvailability} is {@code null}
     * @throws EntryLockedException
     *             if the actual count is {@code <} than the expected count, the expected count is not one of the
     *             special count constants and the argument {@code entryLockedAvailability} is not {@code null}
     *
     * @see org.mozartspaces.core.MzsConstants.Selecting#isSpecialCountConstant(int)
     */
    protected static void checkCount(final int actualCount, final int expectedCount,
            final Availability entryLockedAvailability, final boolean existsDeniedEntry, final String message)
            throws CountNotMetException, EntryLockedException, AccessDeniedException {
        if (actualCount < expectedCount && !isSpecialCountConstant(expectedCount)) {
            if (entryLockedAvailability != null) {
                throw LockedExceptionsHelper.newEntryLockedException(entryLockedAvailability);
            } else if (existsDeniedEntry) {
                throw new AccessDeniedException();
            } else {
                throw new CountNotMetException(message, expectedCount, actualCount);
            }
        }
    }

    @Override
    public final String getName() {
        return name;
    }

    @Override
    public final int getCount() {
        return count;
    }

    /**
     * @return the preceding selector in the selector chain
     */
    protected final NativeSelector<NativeCoordinator> getPredecessor() {
        return predecessor;
    }

    @Override
    public final void setPredecessor(final NativeSelector<NativeCoordinator> next) {
        this.predecessor = next;
    }

    /**
     * Returns the isolation manager of this selector.
     *
     * @return the IsolationManager
     */
    protected final NativeIsolationManager getIsolationManager() {
        return isolationManager;
    }

    /**
     * @return the coordinator
     */
    public final C getCoordinator() {
        return coordinator;
    }

    @Override
    public final void link(final C coordinator, final NativeIsolationManager isolationManager) {
        this.isolationManager = isolationManager;
        this.coordinator = coordinator;
        this.linkEntries(coordinator);
    }

    /**
     * Synchronizes the current coordinator state with the selector's view.
     *
     * @param coordinator
     *            the coordinator
     */
    protected abstract void linkEntries(C coordinator);

    @Override
    public final int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        return result;
    }

    @Override
    public final boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        AbstractNativeSelector<?> other = (AbstractNativeSelector<?>) obj;
        if (name == null) {
            if (other.name != null) {
                return false;
            }
        } else if (!name.equals(other.name)) {
            return false;
        }
        return true;
    }

}
