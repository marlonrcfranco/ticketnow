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

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.atomic.AtomicLong;

import org.mozartspaces.capi3.AccessDeniedException;
import org.mozartspaces.capi3.CoordinationData;
import org.mozartspaces.capi3.CoordinatorLockedException;
import org.mozartspaces.capi3.CountNotMetException;
import org.mozartspaces.capi3.EntryLockedException;
import org.mozartspaces.capi3.FifoCoordinator;
import org.mozartspaces.capi3.IsolationLevel;
import org.mozartspaces.capi3.LifoCoordinator;
import org.mozartspaces.capi3.OperationType;
import org.mozartspaces.capi3.Selector;
import org.mozartspaces.capi3.javanative.authorization.AuthorizationResult;
import org.mozartspaces.capi3.javanative.isolation.NativeSubTransaction;
import org.mozartspaces.capi3.javanative.operation.NativeContainer;
import org.mozartspaces.capi3.javanative.operation.NativeEntry;
import org.mozartspaces.capi3.javanative.persistence.AbstractStoredMap;
import org.mozartspaces.capi3.javanative.persistence.PersistenceContext;
import org.mozartspaces.capi3.javanative.persistence.PersistenceException;
import org.mozartspaces.capi3.javanative.persistence.PersistentCoordinator;
import org.mozartspaces.capi3.javanative.persistence.StoredMap;
import org.mozartspaces.capi3.javanative.persistence.key.NativeEntryPersistenceKey;
import org.mozartspaces.core.MzsCoreRuntimeException;
import org.mozartspaces.core.RequestContext;
import org.mozartspaces.core.metamodel.MetaModelKeys;
import org.mozartspaces.core.metamodel.MetaModelKeys.Containers.Container.Coordinators;
import org.mozartspaces.core.metamodel.MetaModelUtils.MethodTuple;

/**
 * Coordinator which stores the entries in a queue and supports FIFO (first-in-first-out) and LIFO (last-in-first-out)
 * order.
 *
 * @author Martin Barisits
 * @author Tobias Doenz
 * @author Stefan Crass
 */
public final class DefaultQueueCoordinator extends AbstractDefaultCoordinator implements ImplicitNativeCoordinator,
PersistentCoordinator {

    /**
     * Specifies the order of entries.
     *
     * @author Tobias Doenz
     */
    public enum QueueOrder {
        /**
         * LIFO.
         */
        FIFO,
        /**
         * FIFO.
         */
        LIFO
    }

    private static final long serialVersionUID = 1L;

    private final QueueOrder order;

    private final Deque<NativeEntry> queue;
    private StoredMap<NativeEntry, Long> positions;
    private final AtomicLong counter;

    private static final Method GET_ENTRY_LIST_METHOD;

    static {
        try {
            GET_ENTRY_LIST_METHOD = DefaultQueueCoordinator.class.getDeclaredMethod("getEntryList", (Class<?>[]) null);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    /**
     * Creates a DefaultQueueCoordinator.
     *
     * @param name
     *            name of the coordinator
     * @param order
     *            specifies how the entries should be ordered
     */
    public DefaultQueueCoordinator(final String name, final QueueOrder order) {
        super(name);
        this.order = order;

        this.queue = new LinkedBlockingDeque<NativeEntry>();
        this.counter = new AtomicLong();

        this.getMetaModel().put(MetaModelKeys.Containers.Container.Coordinators.ENTRIES,
                new MethodTuple(GET_ENTRY_LIST_METHOD, this));
    }

    @Override
    public void init(final NativeContainer container, final NativeSubTransaction stx, final RequestContext context)
            throws MzsCoreRuntimeException {
    }

    @Override
    public void close() {
    }

    @Override
    public void preRestoreContent(final PersistenceContext persistenceContext) throws PersistenceException {
    }

    @Override
    public CoordinatorRestoreTask getRestoreTask() {
        return new DefaultQueueCoordinatorRestoreTask(this.getName(), order);
    }

    /**
     * A restore task for the QueueCoordinator.
     */
    private static class DefaultQueueCoordinatorRestoreTask implements CoordinatorRestoreTask {

        private static final long serialVersionUID = 1L;

        private final String name;

        private final QueueOrder order;

        public DefaultQueueCoordinatorRestoreTask(final String name, final QueueOrder order) {
            this.name = name;
            this.order = order;
        }

        @Override
        public PersistentCoordinator restoreCoordinator() {
            return new DefaultQueueCoordinator(name, order);
        }
    }

    @Override
    public void postRestoreContent(final PersistenceContext persistenceContext, final NativeContainer nativeContainer,
            final NativeSubTransaction stx) throws PersistenceException {
        init(nativeContainer, stx, null);
        initPersistence(nativeContainer, persistenceContext);
        // restore the queue:
        SortedMap<Long, NativeEntry> reversedPositions = new ConcurrentSkipListMap<Long, NativeEntry>();
        for (NativeEntry nativeEntry : positions.keySet()) {
            reversedPositions.put(positions.get(nativeEntry), nativeEntry);
        }
        for (Map.Entry<Long, NativeEntry> position : reversedPositions.entrySet()) {
            queue.add(position.getValue());
        }
    }

    @Override
    public void initPersistence(final NativeContainer nativeContainer, final PersistenceContext persistenceContext)
            throws PersistenceException {
        final String positionStoredMapName =
                persistenceContext.generateStoredMapName(getClass(), nativeContainer.getIdAsString(), this.getName());
        positions = persistenceContext.createStoredMap(positionStoredMapName,
                new NativeEntryPersistenceKey.NativeEntryPersistenceKeyFactory(nativeContainer));
        this.getMetaModel().put(Coordinators.ENTRYCOUNT,
                new MethodTuple(AbstractStoredMap.SIZE_METHOD, positions));
    }

    // only for meta model
    @SuppressWarnings("unused")
    private List<NativeEntry> getEntryList() {
        return new ArrayList<NativeEntry>(queue);
    }

    /**
     * @return the internal position map
     */
    StoredMap<NativeEntry, Long> getPositions() {
        return positions;
    }

    /**
     * @return the internal queue
     */
    private Deque<NativeEntry> getQueue() {
        return this.queue;
    }

    /**
     * @return the order of entries in the queue
     */
    private QueueOrder getOrder() {
        return this.order;
    }

    /**
     * Creates a new QueueSelector.
     *
     * @param name
     *            of the coordinator this selector should be associated to
     * @param count
     *            of entries
     * @return DefaultFifoSelector
     */
    public static DefaultQueueSelector newSelector(final String name, final int count) {
        return new DefaultQueueSelector(name, count);
    }

    @Override
    public CoordinationData createDefaultCoordinationData() {
        switch (order) {
        case FIFO:
            return FifoCoordinator.newCoordinationData();
        case LIFO:
            return LifoCoordinator.newCoordinationData();
        default:
            throw new AssertionError();
        }

    }

    @Override
    public synchronized boolean registerEntry(final NativeSubTransaction stx, final CoordinationData coordData,
            final NativeEntry entry, final RequestContext context) {
        // store entry first in queue and then in positions
        // implication for the (not synchronized) selection: if an entry is in positions it is also in queue
        this.queue.add(entry);
        this.positions.put(entry, this.counter.incrementAndGet(), stx.getParent());
        return true;
    }

    @Override
    public synchronized boolean unregisterEntry(final NativeEntry entry, final RequestContext context,
            final NativeSubTransaction stx) {
        boolean entryExists = false;
        switch (order) {
        case FIFO:
            entryExists = this.queue.removeFirstOccurrence(entry);
            break;
        case LIFO:
            /*
             * It is crucial to use "removeLastOccurence" here, and not "remove" (or the equivalent
             * "removeFirstOccurence"). A normal "remove" drastically decreases the performance.
             */
            entryExists = this.queue.removeLastOccurrence(entry);
            break;
        default:
            throw new AssertionError();
        }
        this.positions.remove(entry, stx.getParent());
        return entryExists;
    }

    @Override
    public void prepareEntryRemoval(final NativeSubTransaction stx, final NativeEntry entry,
            final RequestContext context) throws CoordinatorLockedException {
    }

    @Override
    public void destroy() throws PersistenceException {
        positions.destroy();
    }

    /**
     * Selector of the DefaultQueueCoordinator.
     *
     * @author Martin Barisits
     * @author Tobias Doenz
     */
    private static final class DefaultQueueSelector extends AbstractDefaultSelector<DefaultQueueCoordinator> {

        private static final long serialVersionUID = 1L;

        // entry iterator (used in getNext to ensure that entries are returned only once in subsequent calls)
        private Iterator<NativeEntry> iterator;

        // from the coordinator
        private QueueOrder order;
        private Deque<NativeEntry> queue;
        private StoredMap<NativeEntry, Long> positions;

        /**
         * Creates a DefaultQueueSelector.
         *
         * @param name
         *            of the Coordinator the Selector is associated to
         * @param count
         *            of entries
         */
        private DefaultQueueSelector(final String name, final int count) {
            super(name, count);
        }

        @Override
        public void linkEntries(final DefaultQueueCoordinator coordinator) {
            this.order = coordinator.getOrder();
            this.queue = coordinator.getQueue();
            this.positions = coordinator.getPositions();
        }

        @Override
        public List<NativeEntry> getAll(final IsolationLevel isolationLevel, final AuthorizationResult auth,
                final NativeSubTransaction stx, final OperationType opType, final RequestContext context)
                        throws CountNotMetException, EntryLockedException, AccessDeniedException {

            checkCount(this.queue.size(), this.getCount(), this.getName());

            Iterator<NativeEntry> entryIterator = null;
            if (this.getPredecessor() == null) {
                switch (order) {
                case FIFO:
                    entryIterator = this.queue.iterator();
                    break;
                case LIFO:
                    entryIterator = this.queue.descendingIterator();
                    break;
                default:
                    throw new AssertionError();
                }
            } else {
                // fetch all entries from previous stage
                List<NativeEntry> predecessorResult = super.getPredecessor().getAll(isolationLevel,
                        auth, stx, opType, context);
                checkCount(predecessorResult.size(), this.getCount(), this.getName());

                // temporary sorting in TreeMap
                Comparator<Long> comparator = (order.equals(QueueOrder.LIFO)) ? Collections.<Long>reverseOrder() : null;
                TreeMap<Long, NativeEntry> sorting = new TreeMap<Long, NativeEntry>(comparator);
                for (NativeEntry entry : predecessorResult) {
                    Long position = this.positions.get(entry);
                    if (position != null) {
                        // entry is registered at coordinator
                        sorting.put(position, entry);
                    }
                }
                checkCount(sorting.size(), this.getCount(), this.getName());
                entryIterator = sorting.values().iterator();
                // TODO optimization return result list or sublist immediately according to count?
            }

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
                    // raise exception if entry is locked or denied
                    // when using COUNT_ALL or specific count (queue order conflict)
                    boolean isMandatory = (this.getCount() != Selector.COUNT_MAX);
                    if (this.checkAccessibility(nextEntry, isolationLevel, auth, stx, opType, isMandatory)) {
                        result.add(nextEntry);
                    } else if (this.existsInaccessibleEntry()) {
                        // queue order conflict because entry is locked or denied (not invisible)
                        // current list should be returned, only possible if COUNT_MAX is used
                        break;
                    }
                } else {
                    result.add(nextEntry);
                }
            }

            //exceptions for inaccessible entries not possible here, so simple checkCount variant can be used
            checkCount(result.size(), this.getCount(),  this.getName());
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
            switch (order) {
            case FIFO:
                return "FIFO " + getName() + " (count=" + getCount() + ")";
            case LIFO:
                return "LIFO " + getName() + " (count=" + getCount() + ")";
            default:
                throw new AssertionError();
            }
        }

    }

}
