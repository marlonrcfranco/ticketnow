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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.mozartspaces.capi3.AccessDeniedException;
import org.mozartspaces.capi3.CoordinationData;
import org.mozartspaces.capi3.CountNotMetException;
import org.mozartspaces.capi3.EntryLockedException;
import org.mozartspaces.capi3.IsolationLevel;
import org.mozartspaces.capi3.OperationType;
import org.mozartspaces.capi3.RandomCoordinator;
import org.mozartspaces.capi3.Selector;
import org.mozartspaces.capi3.javanative.authorization.AuthorizationResult;
import org.mozartspaces.capi3.javanative.isolation.NativeSubTransaction;
import org.mozartspaces.capi3.javanative.operation.NativeEntry;
import org.mozartspaces.capi3.javanative.persistence.PersistentCoordinator;
import org.mozartspaces.capi3.javanative.persistence.StoredMap;
import org.mozartspaces.core.RequestContext;

/**
 * Coordinator which enforces a pseudo-random order of the entries.
 *
 * @author Martin Barisits
 * @author Tobias Doenz
 */
public final class DefaultRandomCoordinator extends AbstractEntryMapCoordinator {

    private static final long serialVersionUID = 1L;

    /**
     * Creates a DefaultRandomCoordinator.
     *
     * @param coordinatorName
     *            name of the coordinator
     */
    public DefaultRandomCoordinator(final String coordinatorName) {
        super(coordinatorName);
    }

    @Override
    public CoordinatorRestoreTask getRestoreTask() {
        return new DefaultRandomCoordinatorRestoreTask(getName());
    }

    /**
     * Restore task for the RandomCoordinator.
     */
    private static final class DefaultRandomCoordinatorRestoreTask implements CoordinatorRestoreTask {

        private static final long serialVersionUID = 1L;

        private final String coordinatorName;

        private DefaultRandomCoordinatorRestoreTask(final String coordinatorName) {
            this.coordinatorName = coordinatorName;
        }

        @Override
        public PersistentCoordinator restoreCoordinator() {
            return new DefaultRandomCoordinator(coordinatorName);
        }
    }

    /**
     * Creates a Selector.
     *
     * @param name
     *            of the coordinator this selector is associated to
     * @param count
     *            of entries
     * @return new Selector
     */
    public static DefaultRandomSelector newSelector(final String name, final int count) {
        return new DefaultRandomSelector(name, count);
    }

    @Override
    public CoordinationData createDefaultCoordinationData() {
        return RandomCoordinator.newCoordinationData();
    }

    /**
     * Selector of the DefaultRandomCoordinator.
     *
     * @author Martin Barisits
     * @author Tobias Doenz
     * @author Stefan Crass
     */
    private static final class DefaultRandomSelector extends AbstractDefaultSelector<DefaultRandomCoordinator> {

        private static final long serialVersionUID = 1L;

        // entry iterator (used in getNext to ensure that entries are returned only once in subsequent calls)
        private Iterator<NativeEntry> iterator;
        // from the coordinator
        private StoredMap<NativeEntry, NativeEntry> entries;

        /**
         * Creates a DefaultRandomSelector.
         *
         * @param name
         *            of the Coordinator this Selector is associated to
         * @param count
         *            of entries
         */
        private DefaultRandomSelector(final String name, final int count) {
            super(name, count);
        }

        @Override
        public void linkEntries(final DefaultRandomCoordinator coordinator) {
            this.entries = coordinator.getEntries();
        }

        @Override
        public List<NativeEntry> getAll(final IsolationLevel isolationLevel, final AuthorizationResult auth,
                final NativeSubTransaction stx, final OperationType opType, final RequestContext context)
                        throws CountNotMetException, EntryLockedException, AccessDeniedException {

            checkCount(this.entries.size(), this.getCount(), this.getName());

            List<NativeEntry> entryList = null;
            if (this.getPredecessor() == null) {
                entryList = new ArrayList<NativeEntry>(this.entries.keySet());
            } else {
                entryList = this.getPredecessor().getAll(isolationLevel, auth, stx, opType, context);
                checkCount(entryList.size(), this.getCount(), this.getName());
                // check if entries are registered at the coordinator and remove them if not
                Iterator<NativeEntry> it = entryList.iterator();
                while (it.hasNext()) {
                    NativeEntry entry = it.next();
                    if (!this.entries.containsKey(entry)) {
                        it.remove();
                    }
                }
                checkCount(entryList.size(), this.getCount(), this.getName());
            }
            //shuffle all entries
            Collections.shuffle(entryList);
            Iterator<NativeEntry> entryIterator = entryList.iterator();

            List<NativeEntry> result = new ArrayList<NativeEntry>();
            NativeEntry nextEntry;
            //repeat until no more entries left
            while (entryIterator.hasNext()) {
                if (isCountMet(result.size(), this.getCount())) {
                    //specific count is fulfilled
                    return result;
                }
                nextEntry = entryIterator.next();

                if (this.getPredecessor() == null) {
                    // raise exception if entry is locked or denied when using COUNT_ALL
                    boolean isMandatory = (this.getCount() == Selector.COUNT_ALL);
                    if (this.checkAccessibility(nextEntry, isolationLevel, auth, stx, opType, isMandatory)) {
                        result.add(nextEntry);
                    }
                } else {
                    result.add(nextEntry);
                }
            }

            checkCount(result.size(), this.getCount(), this.getEntryLockedAvailability(), this.existsDeniedEntry(),
                    this.getName());
            return result;
        }

        @Override
        public NativeEntry getNext(final IsolationLevel isolationLevel, final AuthorizationResult auth,
                final NativeSubTransaction stx, final OperationType opType, final RequestContext context)
                        throws CountNotMetException, EntryLockedException, AccessDeniedException {

            if (this.iterator == null) {
                this.iterator = this.getAll(isolationLevel, auth, stx, opType, context).iterator();
            }
            if (this.iterator.hasNext()) {
                return this.iterator.next();
            } else {
                return null;
            }
        }

        @Override
        public String toString() {
            return "Random " + getName() + " (count=" + getCount() + ")";
        }

    }

}
