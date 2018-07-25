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
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import org.mozartspaces.capi3.AccessDeniedException;
import org.mozartspaces.capi3.Capi3Exception;
import org.mozartspaces.capi3.CoordinationData;
import org.mozartspaces.capi3.CoordinatorLockedException;
import org.mozartspaces.capi3.CountNotMetException;
import org.mozartspaces.capi3.DuplicateKeyException;
import org.mozartspaces.capi3.EntryLockedException;
import org.mozartspaces.capi3.IsolationLevel;
import org.mozartspaces.capi3.KeyCoordinator.KeyData;
import org.mozartspaces.capi3.LabelCoordinator.LabelData;
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
 * Coordinator which associates labels to entries.
 *
 * @author Martin Barisits
 * @author Tobias Doenz
 * @author Stefan Crass
 */
public final class DefaultLabelCoordinator extends AbstractDefaultCoordinator implements PersistentCoordinator {

    private static final long serialVersionUID = 2943565620010655641L;

    private final boolean isKeyCoordinator;

    private final Map<String, List<NativeEntry>> labels;
    private StoredMap<NativeEntry, String> entries;

    private static final Method GET_LABEL_SET_METHOD;

    static {
        try {
            GET_LABEL_SET_METHOD = DefaultLabelCoordinator.class.getDeclaredMethod("getLabelSet", (Class<?>[]) null);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    /**
     * Creates a DefaultLabelCoordinator.
     *
     * @param name
     *            of the Coordinator
     * @param isKeyCoordinator
     *            flag that the coordinator should use the KeyCoordinator semantics, that is, allow only only one entry
     *            per label
     */
    public DefaultLabelCoordinator(final String name, final boolean isKeyCoordinator) {
        super(name);
        this.isKeyCoordinator = isKeyCoordinator;
        this.labels = new ConcurrentHashMap<String, List<NativeEntry>>();

        if (isKeyCoordinator) {
            this.getMetaModel().put(MetaModelKeys.Containers.Container.Coordinators.KeyCoordinatorMeta.KEYS,
                    new MethodTuple(GET_LABEL_SET_METHOD, this));
        } else {
            this.getMetaModel().put(MetaModelKeys.Containers.Container.Coordinators.LabelCoordinatorMeta.LABELS,
                    new MethodTuple(GET_LABEL_SET_METHOD, this));
        }
        this.getMetaModel().put(MetaModelKeys.Containers.Container.Coordinators.ENTRIES, labels);
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
    public void postRestoreContent(final PersistenceContext persistenceContext, final NativeContainer nativeContainer,
            final NativeSubTransaction stx) throws PersistenceException {
        init(nativeContainer, stx, null);
        initPersistence(nativeContainer, persistenceContext);
        for (NativeEntry entry : entries.keySet()) {
            try {
                registerEntry(stx, entries.get(entry), entry);
            } catch (Capi3Exception e) {
                throw new PersistenceException("Could not restore coordinator.", e);
            }
        }
    }

    @Override
    public void initPersistence(final NativeContainer nativeContainer, final PersistenceContext persistenceContext)
            throws PersistenceException {
        final String entriesStoredMapName =
                persistenceContext.generateStoredMapName(getClass(), nativeContainer.getIdAsString(), this.getName());
        entries = persistenceContext.createStoredMap(entriesStoredMapName,
                new NativeEntryPersistenceKey.NativeEntryPersistenceKeyFactory(nativeContainer));
        this.getMetaModel().put(Coordinators.ENTRYCOUNT,
                new MethodTuple(AbstractStoredMap.SIZE_METHOD, entries));
    }

    @Override
    public CoordinatorRestoreTask getRestoreTask() {
        return new DefaultLabelCoordinatorRestoreTask(this.getName(), isKeyCoordinator);
    }

    /**
     * Restore task for the LabelCoordinator.
     */
    private static final class DefaultLabelCoordinatorRestoreTask implements CoordinatorRestoreTask {

        private static final long serialVersionUID = 1L;

        private final String name;

        private final boolean isKeyCoordinator;

        private DefaultLabelCoordinatorRestoreTask(final String name, final boolean isKeyCoordinator) {
            this.name = name;
            this.isKeyCoordinator = isKeyCoordinator;
        }

        @Override
        public PersistentCoordinator restoreCoordinator() {
            return new DefaultLabelCoordinator(name, isKeyCoordinator);
        }
    }

    @Override
    public void destroy() throws PersistenceException {
        entries.destroy();
    }

    // only for meta model
    @SuppressWarnings("unused")
    private Set<String> getLabelSet() {
        return new HashSet<String>(labels.keySet());
    }

    /**
     * @return the internal label map
     */
    Map<String, List<NativeEntry>> getLabels() {
        return labels;
    }

    /**
     * @return the internal entry map
     */
    StoredMap<NativeEntry, String> getEntries() {
        return entries;
    }

    @Override
    public synchronized boolean registerEntry(final NativeSubTransaction stx, final CoordinationData coordData,
            final NativeEntry entry, final RequestContext context) throws Capi3Exception {
        String label = (isKeyCoordinator) ? ((KeyData) coordData).getKey() : ((LabelData) coordData).getLabel();
        return registerEntry(stx, label, entry);
    }

    private boolean registerEntry(final NativeSubTransaction stx, final String label, final NativeEntry entry)
            throws Capi3Exception {
        // store entry first in labels and then in entries
        // implication for the (not synchronized) selection: if an entry is in entries it is also in labels
        List<NativeEntry> data = this.labels.get(label);
        if (data == null) {
            data = new CopyOnWriteArrayList<NativeEntry>();
            this.labels.put(label, data);
        } else if (isKeyCoordinator) {
            if (!this.isValidEntryOverwrite(data.get(data.size() - 1), entry)) {
                throw new DuplicateKeyException(this.getName(), label);
            }
        }
        data.add(entry);
        this.entries.put(entry, label, stx.getParent());
        return true;
    }

    @Override
    public synchronized boolean unregisterEntry(final NativeEntry entry, final RequestContext context,
            final NativeSubTransaction stx) {
        String label = this.entries.get(entry);
        boolean entryExists = this.entries.containsKey(entry);
        this.entries.remove(entry, stx.getParent());
        if (label == null) {
            return entryExists;
        }
        List<NativeEntry> data = this.labels.get(label);
        if (data.size() == 1) {
            // remove the whole entry list
            this.labels.remove(label);
        } else {
            // remove the entry from the entry list
            data.remove(entry);
        }
        return entryExists;
    }

    @Override
    public void prepareEntryRemoval(final NativeSubTransaction stx, final NativeEntry entry,
            final RequestContext context) throws CoordinatorLockedException {
        return;
    }


    /**
     * Creates a DefaultLabelSelector.
     *
     * @param name
     *            of the Coordinator this Selector is associated to
     * @param count
     *            of entries to select
     * @param label
     *            to be selected
     * @return new DefaultLabelSelector
     */
    public static DefaultLabelSelector newSelector(final String name, final int count, final String label) {
        return new DefaultLabelSelector(name, count, label);
    }

    /**
     * Selector for the DefaultLabelCoordinator.
     *
     * @author Martin Barisits
     * @author Tobias Doenz
     * @author Stefan Crass
     */
    private static final class DefaultLabelSelector extends AbstractDefaultSelector<DefaultLabelCoordinator> {

        private static final long serialVersionUID = -6456339477248929002L;

        private DefaultLabelCoordinator coordinator;

        // the label to select
        private final String label;


        // entry iterator (used in getNext to ensure that entries are returned only once in subsequent calls)
        private Iterator<NativeEntry> iterator;

        // from the coordinator
        private Map<String, List<NativeEntry>> labels;
        private StoredMap<NativeEntry, String> entries;

        /**
         * Creates a DefaultLabelSelector.
         *
         * @param name
         *            of the Coordinator this Selector is associated to
         * @param count
         *            of entries
         * @param label
         *            to be selected
         */
        private DefaultLabelSelector(final String name, final int count, final String label) {
            super(name, count);
            this.label = label;
        }

        @Override
        public void linkEntries(final DefaultLabelCoordinator coordinator) {
            this.coordinator = coordinator;
            this.labels = coordinator.getLabels();
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
                List<NativeEntry> entryList = this.labels.get(this.label);
                if (entryList != null) {
                    entryIterator = entryList.iterator();
                    checkCount(entryList.size(), this.getCount(), this.getName());
                } else {
                    entryIterator = new ArrayList<NativeEntry>().iterator();
                }
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
                        String entryLabel = this.entries.get(nextEntry);
                        if (entryLabel != null && entryLabel.equals(this.label)) {
                            //access checks already done at first selector
                            result.add(nextEntry);
                        }
                    }
                } else if (entryIterator.hasNext()) {
                    //first selector
                    nextEntry = entryIterator.next();
                    // raise exception if entry is locked or denied when using COUNT_ALL
                    //RSt, SCr, 19062013: change setting of isMandatory flag to change behaviour in case of KeyCoordinator
                    boolean isMandatory;
                    if (this.coordinator.isKeyCoordinator)
                        isMandatory = !(this.getCount() == Selector.COUNT_MAX);
                    else
                        isMandatory = this.getCount() == Selector.COUNT_ALL;
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

                    String entryLabel = this.entries.get(nextEntry);
                    if (entryLabel != null && entryLabel.equals(this.label)) {
                        return nextEntry;
                    }
                }
            }
        }

        @Override
        public String toString() {
            if (coordinator.isKeyCoordinator) {
                return "Key " + getName() + " (key=" + label + ", count=" + getCount() + ")";
            } else {
                return "Label " + getName() + " (label=" + label + ", count=" + getCount() + ")";
            }
        }

    }
}
