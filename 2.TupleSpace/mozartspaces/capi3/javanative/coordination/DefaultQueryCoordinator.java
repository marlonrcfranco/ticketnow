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
import java.util.NoSuchElementException;

import org.mozartspaces.capi3.AccessDeniedException;
import org.mozartspaces.capi3.Capi3Exception;
import org.mozartspaces.capi3.CoordinationData;
import org.mozartspaces.capi3.CountNotMetException;
import org.mozartspaces.capi3.EntryLockedException;
import org.mozartspaces.capi3.IsolationLevel;
import org.mozartspaces.capi3.OperationType;
import org.mozartspaces.capi3.Query;
import org.mozartspaces.capi3.QueryCoordinator;
import org.mozartspaces.capi3.QueryCoordinator.QueryData;
import org.mozartspaces.capi3.QueryIndexData;
import org.mozartspaces.capi3.Selector;
import org.mozartspaces.capi3.javanative.authorization.AuthorizationResult;
import org.mozartspaces.capi3.javanative.coordination.query.DefaultQuery;
import org.mozartspaces.capi3.javanative.coordination.query.cache.PropertyValueCache;
import org.mozartspaces.capi3.javanative.coordination.query.cache.PropertyValueCacheManager;
import org.mozartspaces.capi3.javanative.coordination.query.index.SearchIndexManager;
import org.mozartspaces.capi3.javanative.isolation.NativeSubTransaction;
import org.mozartspaces.capi3.javanative.operation.NativeEntry;
import org.mozartspaces.capi3.javanative.persistence.PersistentCoordinator;
import org.mozartspaces.capi3.javanative.persistence.StoredMap;
import org.mozartspaces.core.RequestContext;

/**
 * The Default Query Coordinator.
 *
 * @author Martin Barisits
 * @author Tobias Doenz
 */
public final class DefaultQueryCoordinator extends AbstractEntryMapCoordinator {

    private static final long serialVersionUID = 1L;

    private final PropertyValueCache propertyValueCache = PropertyValueCacheManager.newPropertyValueCache();
    private final SearchIndexManager searchIndexManager = new SearchIndexManager(this);

    /**
     * Creates a DefaultQueryCoordinator.
     *
     * @param coordinatorName
     *            name of the coordinator
     */
    public DefaultQueryCoordinator(final String coordinatorName) {
        super(coordinatorName);
    }

    @Override
    public CoordinatorRestoreTask getRestoreTask() {
        return new DefaultQueryCoordinatorRestoreTask(getName());
    }

    /**
     * Restore task for the QueryCoordinator.
     */
    private static final class DefaultQueryCoordinatorRestoreTask implements CoordinatorRestoreTask {

        private static final long serialVersionUID = 1L;

        private final String coordinatorName;

        private DefaultQueryCoordinatorRestoreTask(final String coordinatorName) {
            this.coordinatorName = coordinatorName;
        }

        @Override
        public PersistentCoordinator restoreCoordinator() {
            return new DefaultQueryCoordinator(coordinatorName);
        }
    }

    /**
     * Return a new QuerySelector.
     *
     * @param name
     *            of the Selector
     * @param count
     *            of the Selector
     * @param query
     *            to execute
     * @return a new QuerySelector
     */
    public static DefaultQuerySelector newSelector(final String name, final int count, final Query query) {
        return new DefaultQuerySelector(name, count, query);
    }

    @Override
    public CoordinationData createDefaultCoordinationData() {
        return QueryCoordinator.newCoordinationData();
    }

    /**
     * @return the property value cache
     */
    public PropertyValueCache getPropertyValueCache() {
        return propertyValueCache;
    }

    /**
     * @return the search index manager
     */
    public SearchIndexManager getSearchIndexManager() {
        return searchIndexManager;
    }

    @Override
    public boolean registerEntry(final NativeSubTransaction stx, final CoordinationData coordData,
            final NativeEntry entry, final RequestContext context) throws Capi3Exception {

        QueryIndexData[] indexData = ((QueryData) coordData).getIndexData();
        if (indexData != null) {
            searchIndexManager.createIndexesFromCoordData(indexData, entry.getData().getClass(), stx);
        }

        searchIndexManager.updateIndexes(entry, stx);
        return super.registerEntry(stx, coordData, entry, context);
    }

    @Override
    public boolean unregisterEntry(final NativeEntry entry, final RequestContext context,
            final NativeSubTransaction stx) {

        boolean wasSuccessful = super.unregisterEntry(entry, context, stx);

        if (wasSuccessful) {
            propertyValueCache.purge(entry.getData());
            searchIndexManager.removeFromIndexes(entry, stx);
        }

        return wasSuccessful;
    }

    /**
     * The selector of the QueryCoordinator. Important for the selection is that, in contrast to other coordinators, we
     * need to evaluate the query always on all available entries.
     *
     * @author Martin Barisits
     * @author Tobias Doenz
     * @author Stefan Crass
     * @author Martin Planer
     */
    public static final class DefaultQuerySelector extends AbstractDefaultSelector<DefaultQueryCoordinator> {

        private static final long serialVersionUID = 1L;

        // the query to filter entries
        private final Query query;

        // entry iterator (used in getNext to ensure that entries are returned
        // only once in subsequent calls)
        private Iterator<NativeEntry> iterator;

        // from the coordinator
        private StoredMap<NativeEntry, NativeEntry> entries;

        /**
         * Stores how many results have been successfully returned by getNext.
         */
        private int successfulNextReturns = 0;

        DefaultQuerySelector(final String name, final int count, final Query query) {
            super(name, count);
            this.query = query;
        }

        @Override
        public void linkEntries(final DefaultQueryCoordinator coordinator) {
            this.entries = coordinator.getEntries();
        }

        @Override
        public List<NativeEntry> getAll(final IsolationLevel isolationLevel, final AuthorizationResult auth,
                final NativeSubTransaction stx, final OperationType opType, final RequestContext context)
                throws CountNotMetException, EntryLockedException, AccessDeniedException {

            checkCount(this.entries.size(), this.getCount(), this.getName());

            // GET ENTRIES

            List<NativeEntry> entryList = null;
            if (this.getPredecessor() == null) {
                entryList = new ArrayList<NativeEntry>(this.entries.keySet());
            } else {
                entryList = this.getPredecessor().getAll(isolationLevel, auth, stx, opType, context);
                checkCount(entryList.size(), this.getCount(), this.getName());
                // check if entries are registered at the coordinator and remove
                // them if not
                Iterator<NativeEntry> it = entryList.iterator();
                while (it.hasNext()) {
                    NativeEntry entry = it.next();
                    if (!this.entries.containsKey(entry)) {
                        it.remove();
                    }
                }
                checkCount(entryList.size(), this.getCount(), this.getName());
            }

            // EXECUTE QUERY

            DefaultQuery theQuery = new DefaultQuery(this.query, this, isolationLevel, auth, stx, opType);

            // Iterator<NativeEntry> entryIterator = query.execute(entryList).iterator();
            Iterator<NativeEntry> entryIterator = theQuery.execute(entryList.iterator());

            // currently query does not regard inaccessible entry
            // TODO improve locking behavior for indeterministic matchmakers
            // TODO define exact locking semantics for QuerySelector
            // (regard/ignore locked entries?)

            List<NativeEntry> result = new ArrayList<NativeEntry>();
            NativeEntry nextEntry;
            // repeat until no more entries left
            while (!isCountMet(result.size(), this.getCount()) && entryIterator.hasNext()) {
                nextEntry = entryIterator.next();

                if (this.getPredecessor() == null) {
                    // raise exception if entry is locked or denied when using
                    // COUNT_ALL
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

            checkCount(this.entries.size(), this.getCount(), this.getName());

            // Due to the Iterator interface we have to wrap, catch and re-throw all MzsExceptions :(
            try {

                if (this.iterator == null) {

                    // GET ENTRIES
                    Iterator<NativeEntry> inputEntries = (this.getPredecessor() == null) ? entries.keySet().iterator()
                            : new PredecessorIterator(isolationLevel, auth, stx, opType, context);

                    // EXECUTE QUERY
                    DefaultQuery theQuery = new DefaultQuery(this.query, this, isolationLevel, auth, stx, opType);
                    this.iterator = theQuery.execute(inputEntries);
                }

                if (isCountMet(successfulNextReturns, this.getCount())) {
                    return null;
                }

                while (this.iterator.hasNext()) {
                    NativeEntry nextEntry = this.iterator.next();
                    boolean isMandatory = (this.getCount() == Selector.COUNT_ALL);
                    if (this.checkAccessibility(nextEntry, isolationLevel, auth, stx, opType, isMandatory)) {
                        successfulNextReturns++;
                        return nextEntry;
                    }
                }

                checkCount(successfulNextReturns, this.getCount(), this.getEntryLockedAvailability(),
                        this.existsDeniedEntry(), this.getName());

                // No more elements
                return null;

            } catch (RuntimeCountNotMetException e) {
                throw e.getCause();
            } catch (RuntimeEntryLockedException e) {
                throw e.getCause();
            } catch (RuntimeAccessDeniedException e) {
                throw e.getCause();
            }
        }

        @Override
        public String toString() {
            return "Query " + getName() + " (query=" + query + ", count=" + getCount() + ")";
        }

        /**
         * Iterates over entries of the predecessor of this coordinator.
         *
         * @author Martin Planer
         */
        private final class PredecessorIterator implements Iterator<NativeEntry> {

            private final IsolationLevel isolationLevel;
            private final AuthorizationResult auth;
            private final NativeSubTransaction stx;
            private final OperationType opType;
            private final RequestContext context;

            private NativeEntry next;

            public PredecessorIterator(final IsolationLevel isolationLevel, final AuthorizationResult auth,
                    final NativeSubTransaction stx, final OperationType opType, final RequestContext context) {
                this.isolationLevel = isolationLevel;
                this.auth = auth;
                this.stx = stx;
                this.opType = opType;
                this.context = context;
            }

            @Override
            public boolean hasNext() {
                try {
                    while ((next = getPredecessor().getNext(isolationLevel, auth, stx, opType, context)) != null) {
                        if (entries.containsKey(next)) {
                            return true;
                        }
                    }
                } catch (CountNotMetException e) {
                    throw new RuntimeCountNotMetException(e);
                } catch (EntryLockedException e) {
                    throw new RuntimeEntryLockedException(e);
                } catch (AccessDeniedException e) {
                    throw new RuntimeAccessDeniedException(e);
                }

                next = null;
                return false;
            }

            @Override
            public NativeEntry next() {
                if (next == null) {
                    throw new NoSuchElementException();
                }
                return next;
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException("remove() not implemented");
            }
        }

        /**
         *
         */
        private static class RuntimeCountNotMetException extends RuntimeException {
            private static final long serialVersionUID = 1L;
            private final CountNotMetException e;

            public RuntimeCountNotMetException(final CountNotMetException e) {
                this.e = e;
            }

            @Override
            public CountNotMetException getCause() {
                return e;
            }
        }

        /**
         *
         */
        private static class RuntimeEntryLockedException extends RuntimeException {
            private static final long serialVersionUID = 1L;
            private final EntryLockedException e;

            public RuntimeEntryLockedException(final EntryLockedException e) {
                this.e = e;
            }

            @Override
            public EntryLockedException getCause() {
                return e;
            }
        }

        /**
         *
         */
        private static class RuntimeAccessDeniedException extends RuntimeException {
            private static final long serialVersionUID = 1L;
            private final AccessDeniedException e;

            public RuntimeAccessDeniedException(final AccessDeniedException e) {
                this.e = e;
            }

            @Override
            public AccessDeniedException getCause() {
                return e;
            }
        }

    }

}
