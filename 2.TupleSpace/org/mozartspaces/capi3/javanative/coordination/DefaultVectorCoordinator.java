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
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import org.mozartspaces.capi3.AccessDeniedException;
import org.mozartspaces.capi3.Capi3Exception;
import org.mozartspaces.capi3.CoordinationData;
import org.mozartspaces.capi3.CoordinatorLockedException;
import org.mozartspaces.capi3.CountNotMetException;
import org.mozartspaces.capi3.EntryLockedException;
import org.mozartspaces.capi3.IsolationLevel;
import org.mozartspaces.capi3.OperationType;
import org.mozartspaces.capi3.Selector;
import org.mozartspaces.capi3.VectorCoordinator;
import org.mozartspaces.capi3.VectorCoordinator.VectorData;
import org.mozartspaces.capi3.javanative.authorization.AuthorizationResult;
import org.mozartspaces.capi3.javanative.isolation.NativeSubTransaction;
import org.mozartspaces.capi3.javanative.operation.LazyNativeEntry;
import org.mozartspaces.capi3.javanative.operation.NativeContainer;
import org.mozartspaces.capi3.javanative.operation.NativeEntry;
import org.mozartspaces.capi3.javanative.persistence.OrderedLongSet;
import org.mozartspaces.capi3.javanative.persistence.PersistenceContext;
import org.mozartspaces.capi3.javanative.persistence.PersistenceException;
import org.mozartspaces.capi3.javanative.persistence.PersistentCoordinator;
import org.mozartspaces.core.MzsConstants;
import org.mozartspaces.core.MzsCoreRuntimeException;
import org.mozartspaces.core.RequestContext;
import org.mozartspaces.core.metamodel.MetaModelKeys;
import org.mozartspaces.core.metamodel.MetaModelKeys.Containers.Container.Coordinators;
import org.mozartspaces.core.metamodel.MetaModelUtils;
import org.mozartspaces.core.metamodel.MetaModelUtils.MethodTuple;

/**
 * Coordinator which organizes the entries like a Vector.
 *
 * @author Martin Barisits
 * @author Tobias Doenz
 * @author Stefan Crass
 */
public final class DefaultVectorCoordinator extends AbstractDefaultCoordinator implements PersistentCoordinator {

    private static final long serialVersionUID = 1L;

    private final Vector<List<NativeEntry>> vector;
    private final Map<NativeEntry, List<NativeEntry>> entries;
    private OrderedLongSet storedEntries;

    /**
     * Creates a DefaultVectorCoordinator.
     *
     * @param name
     *            of the coordinator
     */
    public DefaultVectorCoordinator(final String name) {
        super(name);
        this.entries = new ConcurrentHashMap<NativeEntry, List<NativeEntry>>();
        this.vector = new Vector<List<NativeEntry>>();

        this.getMetaModel().put(Coordinators.ENTRYCOUNT,
                new MethodTuple(MetaModelUtils.CONCURRENT_HASH_MAP_SIZE_METHOD, entries));
        this.getMetaModel().put(MetaModelKeys.Containers.Container.Coordinators.ENTRIES, vector);
    }

    @Override
    public void init(final NativeContainer container, final NativeSubTransaction stx, final RequestContext context)
            throws MzsCoreRuntimeException {
    }

    @Override
    public void close() {
        storedEntries.close();
    }

    @Override
    public void preRestoreContent(final PersistenceContext persistenceContext) throws PersistenceException {
    }

    @Override
    public void postRestoreContent(final PersistenceContext persistenceContext, final NativeContainer nativeContainer,
            final NativeSubTransaction stx) throws PersistenceException {
        init(nativeContainer, stx, null);
        initPersistence(nativeContainer, persistenceContext);
        for (long entryId : storedEntries) {
            // append entry at the end
            List<NativeEntry> toAdd = new CopyOnWriteArrayList<NativeEntry>();
            NativeEntry entry = new LazyNativeEntry(entryId, nativeContainer);
            toAdd.add(entry);
            // store entry first in vector and then in entries
            // implication for the (not synchronized) selection: if an entry is in entries it is also in vector
            this.vector.add(toAdd);
            this.entries.put(entry, toAdd);
        }

    }

    @Override
    public void initPersistence(final NativeContainer nativeContainer, final PersistenceContext persistenceContext)
            throws PersistenceException {
        final String entriesStoredMapName = persistenceContext.generateStoredMapName(getClass(),
                nativeContainer.getIdAsString(), this.getName());
        storedEntries = persistenceContext.createOrderedLongSet(entriesStoredMapName);
    }

    @Override
    public CoordinatorRestoreTask getRestoreTask() {
        return new DefaultVectorCoordinatorRestoreTask(this.getName());
    }

    /**
     * Restore task for the VectorCoordinator.
     */
    private static final class DefaultVectorCoordinatorRestoreTask implements CoordinatorRestoreTask {

        private static final long serialVersionUID = 1L;

        private final String name;

        private DefaultVectorCoordinatorRestoreTask(final String name) {
            this.name = name;
        }

        @Override
        public PersistentCoordinator restoreCoordinator() {
            return new DefaultVectorCoordinator(name);
        }
    }

    @Override
    public void destroy() throws PersistenceException {
    }

    /**
     * @return the internal vector
     */
    Vector<List<NativeEntry>> getVector() {
        return vector;
    }

    /**
     * @return the internal entry map
     */
    Map<NativeEntry, List<NativeEntry>> getEntries() {
        return entries;
    }

    /**
     * Creates a new VectorSelector.
     *
     * @param name
     *            of the coordinator this selector should be associated to
     * @param count
     *            of entries
     * @param index
     *            to be selected
     * @return new Selector
     */
    public static DefaultVectorSelector newSelector(final String name, final int count, final int index) {
        return new DefaultVectorSelector(name, count, index);
    }

    @Override
    public synchronized boolean registerEntry(final NativeSubTransaction stx, final CoordinationData coordData,
            final NativeEntry entry, final RequestContext context) throws Capi3Exception {

        this.acquireCoordinatorLock(stx);

        int index = ((VectorData) coordData).getIndex();
        if ((index < -1) || (index > this.vector.size())) {
            // TODO Change this to a MozartSpaces Exc.
            throw new ArrayIndexOutOfBoundsException();
        }
        int vectorSize = this.vector.size();
        if ((index == VectorCoordinator.APPEND) || (index == vectorSize)) {
            // append entry at the end
            List<NativeEntry> toAdd = new CopyOnWriteArrayList<NativeEntry>();
            toAdd.add(entry);
            // store entry first in vector and then in entries
            // implication for the (not synchronized) selection: if an entry is in entries it is also in vector
            this.vector.add(toAdd);
            this.entries.put(entry, toAdd);
            this.storedEntries.append(entry.getEntryId());
            return true;
        }
        List<NativeEntry> vectorEntry = this.vector.get(index);
        NativeEntry toBeOverwritten = vectorEntry.get(vectorEntry.size() - 1);
        if (this.isValidEntryOverwrite(toBeOverwritten, entry)) {
            // overwrite the entry at vector[index]
            vectorEntry.add(entry);
            this.entries.put(entry, vectorEntry);
            this.storedEntries.remove(toBeOverwritten.getEntryId());
            // note that there must be an entry at index+1, otherwise the previous if would have been fulfilled.
            long idOfEntryAfter = this.vector.get(index + 1).get(0).getEntryId();
            this.storedEntries.insertBefore(idOfEntryAfter, entry.getEntryId());
            return true;
        } else {
            // insert the entry at vector[index]
            List<NativeEntry> newEntry = new CopyOnWriteArrayList<NativeEntry>();
            newEntry.add(entry);
            this.vector.add(index, newEntry);
            this.entries.put(entry, newEntry);
            if (this.vector.size() > index + 1) {
                long idOfEntryAfter = this.vector.get(index + 1).get(0).getEntryId();
                this.storedEntries.insertBefore(idOfEntryAfter, entry.getEntryId());
            } else {
                this.storedEntries.append(entry.getEntryId());
            }
            return true;
        }

    }

    @Override
    public synchronized boolean unregisterEntry(final NativeEntry entry, final RequestContext context,
            final NativeSubTransaction stx) {
        List<NativeEntry> data = this.entries.remove(entry);
        if (data == null) {
            return false;
        }
        if (data.size() == 1) {
            // remove the whole entry list
            // TODO optimize! this remove be slow, runtime O(n)
            this.vector.remove(data);
        } else {
            // remove the entry from the entry list in the vector
            data.remove(entry);
        }
        try {
            this.storedEntries.remove(entry.getEntryId());
        } catch (NoSuchElementException e) {
            // the deleting above will fail with NoSuchElementException if:
            // 1) transaction writes an entry X at position i
            // 2) transaction deletes an entry X at position i
            // 3) transaction writes an entry Y at position i
            // 4) transaction is committed.
            // Committing transaction will cause NativeContainer.purgeEntry(X)
            // which will lead to this.unregisterEntry(X). This will fail because the entry was already removed
            // from the stored set at step 2).
            // It is therefore safe to ignore this error.
        }
        return true;
    }

    @Override
    public synchronized void prepareEntryRemoval(final NativeSubTransaction stx, final NativeEntry entry,
            final RequestContext context) throws CoordinatorLockedException {
        if (this.entries.containsKey(entry)) {
            this.acquireCoordinatorLock(stx);
        }
        return;
    }

    /**
     * Selector of the DefaultVectorCoordinator.
     *
     * @author Martin Barisits
     * @author Tobias Doenz
     * @author Stefan Crass
     */
    private static final class DefaultVectorSelector extends AbstractDefaultSelector<DefaultVectorCoordinator> {

        private static final long serialVersionUID = -7346700820540162778L;

        // the index where the selection starts (constructor argument)
        private final int index;

        // entry iterator (used in getNext to ensure that entries are returned only once in subsequent calls)
        private Iterator<NativeEntry> iterator;
        // from the coordinator
        private Vector<List<NativeEntry>> vector;
        private Map<NativeEntry, List<NativeEntry>> entries;

        /**
         * Creates a DefaultVectorSelector.
         *
         * @param name
         *            of the Coordinator the Selector is associated to
         * @param count
         *            of entries
         * @param index
         *            to select
         */
        private DefaultVectorSelector(final String name, final int count, final int index) {
            super(name, count);
            this.index = index;
        }

        @Override
        public void linkEntries(final DefaultVectorCoordinator coordinator) {
            this.vector = coordinator.getVector();
            this.entries = coordinator.getEntries();
        }

        @Override
        public List<NativeEntry> getAll(final IsolationLevel isolationLevel, final AuthorizationResult auth,
                final NativeSubTransaction stx, final OperationType opType, final RequestContext context)
                        throws CountNotMetException, EntryLockedException, AccessDeniedException {
            // TODO check semantics

            // check that enough remaining entries remain after the specified entry
            checkCount(this.vector.size() - this.index, this.getCount(), this.getName());
            List<NativeEntry> result = new ArrayList<NativeEntry>();

            if (this.getPredecessor() == null) {
                // iterate over the specified range of the vector [index...index+count]
                for (int currentIndex = index; currentIndex < this.vector.size(); currentIndex++) {
                    if (isCountMet(result.size(), this.getCount())) {
                        return result;
                    }
                    // get the entry at the current index (list with usually only one element)
                    List<NativeEntry> data;
                    try {
                        data = this.vector.get(currentIndex);
                        // this list contains more than one element only when an entry is replaced inside a TX
                    } catch (ArrayIndexOutOfBoundsException e) {
                        // no entry with that index
                        if (this.getCount() == Selector.COUNT_ALL || this.getCount() == Selector.COUNT_MAX) {
                            return result;
                        } else {
                            throw new CountNotMetException(this.getName(), this.getCount(), currentIndex - index);
                        }
                    }
                    boolean entryAdded = false;
                    for (NativeEntry entry : data) {
                        // raise exception if entry is locked or denied when using COUNT_ALL or specific count
                        boolean isMandatory = (this.getCount() != Selector.COUNT_MAX);
                        if (this.checkAccessibility(entry, isolationLevel, auth, stx, opType, isMandatory)) {
                            result.add(entry);
                            entryAdded = true;
                        }
                    }
                    if (!entryAdded) {
                        // the entry (at the current index) is not available
                        checkCount(result.size(), this.getCount(), this.getEntryLockedAvailability(),
                                this.existsDeniedEntry(), this.getName());
                        return result;
                    }
                }
                return result;
            } else {
                if (this.getCount() == 1) {
                    // optimization when only one entry should be selected
                    while (true) {
                        if (result.size() == 1) {
                            return result;
                        }
                        NativeEntry entry = super.getPredecessor().getNext(isolationLevel, auth, stx, opType, context);
                        if (entry == null) {
                            // this should always throw an exception (because count==1)
                            checkCount(result.size(), this.getCount(), this.getName());
                            return result;
                        }
                        List<NativeEntry> data = this.entries.get(entry);
                        if (data == null) {
                            // this entry is not part of the coordinator and should be skipped
                            continue;
                        }
                        // check if the entry has the selected index in the vector
                        // TODO optimize! indexOf can be slow, runtime O(n)
                        if (this.vector.indexOf(data) == this.index) {
                            result.add(entry);
                        }
                    }
                } else {
                    // fetch all entries from the previous stage
                    List<NativeEntry> predecessorResult = super.getPredecessor().getAll(isolationLevel, auth, stx,
                            opType, context);
                    checkCount(predecessorResult.size(), this.getCount(), this.getName());
                    // find index for each entry and put it into a temporary map
                    Map<Integer, NativeEntry> entryWithIndex = new HashMap<Integer, NativeEntry>();
                    for (NativeEntry entry : predecessorResult) {
                        List<NativeEntry> data = this.entries.get(entry);
                        if (data == null) {
                            // this entry is not part of the coordinator and should be skipped
                            continue;
                        }
                        // TODO optimize! indexOf can be slow, runtime O(n)
                        int entryIndex = this.vector.indexOf(data);
                        if (entryIndex == -1) {
                            throw new IllegalStateException("entry is in 'entries' but not the vector");
                        }
                        entryWithIndex.put(entryIndex, entry);
                    }
                    // copy entries with index [index...index+count] from the map to the result
                    // we do not allow gaps, i.e., an exception is thrown if an entry at an index is not available
                    int maxIndex = 0;
                    if (MzsConstants.Selecting.isSpecialCountConstant(getCount())) {
                        maxIndex = entryWithIndex.size() - 1;
                    } else {
                        maxIndex = index + this.getCount();
                    }
                    for (int currentIndex = index; currentIndex <= maxIndex; currentIndex++) {
                        NativeEntry entry = entryWithIndex.get(currentIndex);
                        if (entry == null) {
                            checkCount(result.size(), this.getCount(), this.getName());
                            return result;
                        }
                        result.add(entry);
                    }
                    return result;
                }
            }
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
            return "Vector " + getName() + " (index=" + index + ", count=" + getCount() + ")";
        }
    }

}
