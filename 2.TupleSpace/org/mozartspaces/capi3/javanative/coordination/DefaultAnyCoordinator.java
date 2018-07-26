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
import java.util.Iterator;
import java.util.List;

import org.mozartspaces.capi3.AccessDeniedException;
import org.mozartspaces.capi3.AnyCoordinator;
import org.mozartspaces.capi3.AnyCoordinator.AnySelector;
import org.mozartspaces.capi3.CoordinationData;
import org.mozartspaces.capi3.CountNotMetException;
import org.mozartspaces.capi3.EntryLockedException;
import org.mozartspaces.capi3.IsolationLevel;
import org.mozartspaces.capi3.OperationType;
import org.mozartspaces.capi3.Selector;
import org.mozartspaces.capi3.javanative.authorization.AuthorizationResult;
import org.mozartspaces.capi3.javanative.isolation.NativeSubTransaction;
import org.mozartspaces.capi3.javanative.operation.NativeEntry;
import org.mozartspaces.capi3.javanative.persistence.PersistentCoordinator;
import org.mozartspaces.capi3.javanative.persistence.StoredMap;
import org.mozartspaces.core.RequestContext;

/**
 * Coordinator without a guaranteed entry order for operations.
 *
 * @author Martin Barisits
 * @author Tobias Doenz
 */
public final class DefaultAnyCoordinator extends AbstractEntryMapCoordinator {

    private static final long serialVersionUID = 1L;

    /**
     * Creates a DefaultAnyCoordinator.
     *
     * @param coordinatorName
     *            name of the coordinator
     */
    public DefaultAnyCoordinator(final String coordinatorName) {
        super(coordinatorName);
    }

    @Override
    public CoordinatorRestoreTask getRestoreTask() {
        return new DefaultAnyCoordinatorRestoreTask(getName());
    }

    /**
     * Restore task for the AnyCoordinator.
     */
    private static final class DefaultAnyCoordinatorRestoreTask implements CoordinatorRestoreTask {

        private static final long serialVersionUID = 1L;

        private final String coordinatorName;

        private DefaultAnyCoordinatorRestoreTask(final String coordinatorName) {
            this.coordinatorName = coordinatorName;
        }

        @Override
        public PersistentCoordinator restoreCoordinator() {
            return new DefaultAnyCoordinator(coordinatorName);
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
    public static DefaultAnySelector newSelector(final String name, final int count) {
        return new DefaultAnySelector(name, count);
    }

    @Override
    public CoordinationData createDefaultCoordinationData() {
        return AnyCoordinator.newCoordinationData();
    }

    /**
     * Selector of the DefaultAnyCoordinator.
     *
     * @author Martin Barisits
     * @author Tobias Doenz
     * @author Stefan Crass
     */
    private static final class DefaultAnySelector extends AbstractDefaultSelector<DefaultAnyCoordinator> {

        private static final long serialVersionUID = 1L;

        // entry iterator (used in getNext to ensure that entries are returned only once in subsequent calls)
        private Iterator<NativeEntry> iterator;

        // from the coordinator
        private StoredMap<NativeEntry, NativeEntry> entries;

        /**
         * Creates a DefaultAnySelector.
         *
         * @param name
         *            of the Coordinator this Selector is associated to
         * @param count
         *            of entries
         */
        private DefaultAnySelector(final String name, final int count) {
            super(name, count);
        }

        @Override
        public void linkEntries(final DefaultAnyCoordinator coordinator) {
            this.entries = coordinator.getEntries();
        }

        @Override
        public List<NativeEntry> getAll(final IsolationLevel isolationLevel, final AuthorizationResult auth,
                final NativeSubTransaction stx, final OperationType opType, final RequestContext context)
                        throws CountNotMetException, EntryLockedException, AccessDeniedException {

            checkCount(this.entries.size(), this.getCount(), this.getName());

            Iterator<NativeEntry> entryIterator = null;
            if (this.getPredecessor() == null) {
                // first selector in the chain
                entryIterator = this.entries.keySet().iterator();
            }

            List<NativeEntry> result = new ArrayList<NativeEntry>();
            NativeEntry nextEntry;
            //repeat until no more entries left
            do {
                if (isCountMet(result.size(), this.getCount())) {
                    //specific count is fulfilled
                    return result;
                }
                nextEntry = null;
                if (this.getPredecessor() != null) {
                    //get next entry from previous stage that is registered at the current coordinator
                    nextEntry = this.getPredecessor().getNext(isolationLevel, auth, stx, opType, context);
                    if (nextEntry != null) {
                        // AnySelector always includes next entry
                        if (this.entries.containsKey(nextEntry)) {
                            //access checks already done at first selector
                            result.add(nextEntry);
                        }
                    }
                } else if (entryIterator.hasNext()) {
                    //first selector
                    nextEntry = entryIterator.next();
                    // raise exception if entry is locked or denied when using COUNT_ALL
                    boolean isMandatory = (this.getCount() == Selector.COUNT_ALL);
                    if (this.checkAccessibility(nextEntry, isolationLevel, auth, stx, opType, isMandatory)) {
                        result.add(nextEntry);
                    }
                }
            } while (nextEntry != null);

            checkCount(result.size(), this.getCount(), this.getEntryLockedAvailability(), this.existsDeniedEntry(),
                    this.getName());
            return result;
        }

        @Override
        public NativeEntry getNext(final IsolationLevel isolationLevel, final AuthorizationResult auth,
                final NativeSubTransaction stx, final OperationType opType, final RequestContext context)
                        throws CountNotMetException, EntryLockedException, AccessDeniedException {

            if (this.getCount() != Selector.COUNT_MAX || this.getPredecessor() == null) {
                if (this.iterator == null) {
                    // ensure that we have an entry iterator
                    this.iterator = this.getAll(isolationLevel, auth, stx, opType, context).iterator();
                    // getAll ensures that we have enough available entries in the coordinator
                }

                if (this.iterator.hasNext()) {
                    return this.iterator.next();
                } else {
                    return null;
                }
            } else {
                //for COUNT_MAX no iterator is used for inner selectors as streaming can be used instead
                //no count checks necessary as COUNT_MAX always returns all accessible entries
                while (true) {
                    NativeEntry nextEntry = this.getPredecessor().getNext(isolationLevel, auth, stx, opType, context);
                    if (nextEntry == null) {
                        return null;
                    }
                    if (this.entries.containsKey(nextEntry)) {
                        // AnySelector always includes next entry
                        return nextEntry;
                    }
                }
            }
        }
        @Override
        public String toString() {
            return "Any " + getName() + " (count=" + getCount() + ")";
        }


    }

    /**
     * Translates between the API and implementation class of the coordinator.
     *
     * @author Tobias Doenz
     */
    public static final class AnyCoordinatorTranslator implements
    NativeCoordinatorTranslator<AnyCoordinator, DefaultAnyCoordinator> {

        @Override
        public DefaultAnyCoordinator translateCoordinator(final AnyCoordinator coordinator) {
            return new DefaultAnyCoordinator(coordinator.getName());
        }
    }

    /**
     * Translates between the API and implementation class of the selector.
     *
     * @author Tobias Doenz
     */
    public static final class AnySelectorTranslator implements
            NativeSelectorTranslator<AnySelector, DefaultAnySelector> {

        @Override
        public DefaultAnySelector translateSelector(final AnySelector selector) {
            return DefaultAnyCoordinator.newSelector(selector.getName(), selector.getCount());
        }
    }
}
