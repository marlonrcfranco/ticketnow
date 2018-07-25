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
import org.mozartspaces.capi3.CoordinatorTranslator;
import org.mozartspaces.capi3.CountNotMetException;
import org.mozartspaces.capi3.EntryLockedException;
import org.mozartspaces.capi3.InvalidEntryTypeException;
import org.mozartspaces.capi3.IsolationLevel;
import org.mozartspaces.capi3.OperationType;
import org.mozartspaces.capi3.Selector;
import org.mozartspaces.capi3.SelectorTranslator;
import org.mozartspaces.capi3.TypeCoordinator;
import org.mozartspaces.capi3.TypeCoordinator.TypeSelector;
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
 * Coordinator which stores entries according to their type.
 *
 * @author Stefan Crass
 * @author Tobias Doenz
 */
// TODO test persistence
public final class DefaultTypeCoordinator extends AbstractDefaultCoordinator implements ImplicitNativeCoordinator,
PersistentCoordinator {

    private static final long serialVersionUID = 1L;

    private final Map<Class<?>, List<NativeEntry>> types;
    private final List<Class<?>> allowedTypes;
    private final List<Class<?>> activeTypes;

    /**
     * The value is the class name and used for the persistence so that not all entries (data) need to be loaded on
     * restore.
     */
    // TODO optimize the storage of class names (redundant for entries of same type, use separate map?)
    private StoredMap<NativeEntry, String> entries;

    private static final Method GET_ENTRY_SET_METHOD;

    static {
        try {
            GET_ENTRY_SET_METHOD = DefaultTypeCoordinator.class
                    .getDeclaredMethod("getEntrySet", (Class<?>[]) null);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    /**
     * Creates a DefaultTypeCoordinator.
     *
     * @param name
     *            of the Coordinator
     * @param allowedTypes
     *            only entries which are instance of any of these types are allowed, if null no restrictions apply
     */
    public DefaultTypeCoordinator(final String name, final List<Class<?>> allowedTypes) {
        super(name);
        this.allowedTypes = allowedTypes;
        this.types = new ConcurrentHashMap<Class<?>, List<NativeEntry>>();
        this.activeTypes = new CopyOnWriteArrayList<Class<?>>();

        this.getMetaModel().put(MetaModelKeys.Containers.Container.Coordinators.ENTRIES,
                new MethodTuple(GET_ENTRY_SET_METHOD, this));
    }

    // only for meta model
    @SuppressWarnings("unused")
    private Set<NativeEntry> getEntrySet() {
        return new HashSet<NativeEntry>(entries.keySet());
    }

    @Override
    public CoordinationData createDefaultCoordinationData() {
        return TypeCoordinator.newCoordinationData();
    }

    /**
     * @return the allowed types
     */
    List<Class<?>> getAllowedTypes() {
        return this.allowedTypes;
    }

    /**
     * @return the entries
     */
    StoredMap<NativeEntry, String> getEntries() {
        return this.entries;
    }

    /**
     * @return the types
     */
    Map<Class<?>, List<NativeEntry>> getTypes() {
        return this.types;
    }

    /**
     * @return the currently active types with at least one stored entry
     */
    List<Class<?>> getActiveTypes() {
        return this.activeTypes;
    }

    @Override
    public synchronized boolean registerEntry(final NativeSubTransaction stx, final CoordinationData coordData,
            final NativeEntry entry, final RequestContext context) throws Capi3Exception {
        return registerEntry(entry, entry.getData().getClass(), stx);
    }

    private boolean registerEntry(final NativeEntry entry, final Class<?> entryClass, final NativeSubTransaction stx)
            throws InvalidEntryTypeException {
        boolean validType = false;
        if (this.getAllowedTypes() == null) {
            validType = true;
        } else {
            for (Class<?> cl : this.getAllowedTypes()) {
                if (cl.isInstance(entry.getData())) {
                    validType = true;
                    break;
                }
            }
        }
        if (!validType) {
            throw new InvalidEntryTypeException(this.getName(), entryClass.getName());
        }

        List<NativeEntry> data = this.types.get(entryClass);
        if (data == null) {
            data = new CopyOnWriteArrayList<NativeEntry>();
            this.types.put(entryClass, data);
            this.activeTypes.add(entryClass);
        }
        data.add(entry);
        this.entries.put(entry, entryClass.getName(), stx.getParent());
        return true;
    }

    @Override
    public synchronized boolean unregisterEntry(final NativeEntry entry, final RequestContext context,
            final NativeSubTransaction stx) {
        boolean entryExists = false;
        if (this.entries.containsKey(entry)) {
            this.entries.remove(entry, stx.getParent());
            entryExists = true;
            // TODO what if the entry has not been removed (which we do not know for sure because of the TX)
        } else {
            return false;
        }
        Class<?> type = entry.getData().getClass();
        List<NativeEntry> data = this.types.get(type);
        if (data.size() == 1) {
            // remove the whole entry list
            this.types.remove(type);
            this.activeTypes.remove(type);
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
     * Creates a DefaultTypeSelector.
     *
     * @param name
     *            of the Coordinator this Selector is associated to
     * @param count
     *            of entries to select
     * @param type
     *            to be selected
     * @return new DefaultTypeSelector
     */
    public static DefaultTypeSelector newSelector(final String name, final int count, final Class<?> type) {
        return new DefaultTypeSelector(name, count, type);
    }

    /**
     * Selector for the DefaultTypeCoordinator.
     *
     * @author Stefan Crass
     */
    private static final class DefaultTypeSelector extends AbstractDefaultSelector<DefaultTypeCoordinator> {

        private static final long serialVersionUID = -1L;

        private final Class<?> type;
        private Map<Class<?>, List<NativeEntry>> types;
        private StoredMap<NativeEntry, String> entries;
        private List<Class<?>> activeTypes;
        private Iterator<NativeEntry> iterator;

        /**
         * Creates a DefaultTypeSelector.
         *
         * @param name
         *            of the Coordinator this Selector is associated to
         * @param count
         *            of entries
         * @param path
         *            to be selected
         */
        private DefaultTypeSelector(final String name, final int count, final Class<?> type) {
            super(name, count);
            this.type = type;
        }

        /**
         * Returns the type.
         *
         * @return the type
         */
        Class<?> getType() {
            return this.type;
        }

        @Override
        public List<NativeEntry> getAll(final IsolationLevel isolationLevel, final AuthorizationResult auth,
                final NativeSubTransaction stx, final OperationType opType, final RequestContext context)
                        throws CountNotMetException, EntryLockedException, AccessDeniedException {

            checkCount(this.entries.size(), this.getCount(), this.getName());

            Iterator<NativeEntry> entryIterator = null;
            if (this.getPredecessor() == null) {
                // first selector in the chain
                List<NativeEntry> entryList = new ArrayList<NativeEntry>();
                for (Class<?> cls : this.activeTypes) {
                    if (this.type.isAssignableFrom(cls)) {
                        List<NativeEntry> clsEntries = this.types.get(cls);
                        if (clsEntries != null) {
                            entryList.addAll(clsEntries);
                        }
                    }
                }
                entryIterator = entryList.iterator();
                checkCount(entryList.size(), this.getCount(), this.getName());
            }

            List<NativeEntry> result = new ArrayList<NativeEntry>();
            NativeEntry nextEntry;
            // repeat until no more entries left
            do {
                if (isCountMet(result.size(), this.getCount())) {
                    // specific count is fulfilled
                    return result;
                }
                nextEntry = null;
                if (this.getPredecessor() != null) {
                    // get next entry from previous stage that is registered at the current coordinator
                    nextEntry = this.getPredecessor().getNext(isolationLevel, auth, stx, opType, context);
                    if (nextEntry != null) {
                        if (this.entries.containsKey(nextEntry) && this.type.isInstance(nextEntry.getData())) {
                            result.add(nextEntry);
                        }
                    }
                } else if (entryIterator.hasNext()) {
                    // first selector
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
                // for COUNT_MAX no iterator is used for inner selectors as streaming can be used instead
                // no count checks necessary as COUNT_MAX always returns all accessible entries
                while (true) {
                    NativeEntry nextEntry = this.getPredecessor().getNext(isolationLevel, auth, stx, opType, context);
                    if (nextEntry == null) {
                        return null;
                    }

                    if (this.entries.containsKey(nextEntry) && this.type.isInstance(nextEntry.getData())) {
                        return nextEntry;
                    }
                }
            }
        }

        @Override
        public void linkEntries(final DefaultTypeCoordinator coordinator) {
            this.types = coordinator.getTypes();
            this.entries = coordinator.getEntries();
            this.activeTypes = coordinator.getActiveTypes();
        }

        @Override
        public String toString() {
            return "Type " + this.getName() + " (type=" + this.getType().getName() + ", count=" + this.getCount() + ")";
        }
    }

    /**
     * Translates between the API and implementation class of the coordinator.
     *
     * @author Stefan Crass
     */
    public static final class TypeCoordinatorTranslator implements
    CoordinatorTranslator<TypeCoordinator, DefaultTypeCoordinator> {

        @Override
        public DefaultTypeCoordinator translateCoordinator(final TypeCoordinator coordinator) {
            return new DefaultTypeCoordinator(coordinator.getName(), coordinator.getAllowedTypes());
        }

    }

    /**
     * Translates between the API and implementation class of the selector.
     *
     * @author Stefan Crass
     */
    public static final class TypeSelectorTranslator implements SelectorTranslator<TypeSelector, DefaultTypeSelector> {
        @Override
        public DefaultTypeSelector translateSelector(final TypeSelector selector) {
            return DefaultTypeCoordinator.newSelector(selector.getName(), selector.getCount(), selector.getType());
        }
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
        // restore the entries
        for (NativeEntry entry : entries.keySet()) {
            String className = entries.get(entry);
            try {
                registerEntry(entry, Class.forName(className), stx);
            } catch (ClassNotFoundException e) {
                throw new PersistenceException("Could not load class \"" + className
                        + "\" while restoring coordinator state. Maybe your classpath changed since the last run.", e);
            } catch (Capi3Exception e) {
                throw new PersistenceException("Error while restoring coordinator state.", e);
            }
        }
    }

    @Override
    public void initPersistence(final NativeContainer nativeContainer, final PersistenceContext persistenceContext)
            throws PersistenceException {
        final String entriesStoredMapName = persistenceContext.generateStoredMapName(getClass(),
                nativeContainer.getIdAsString(), this.getName());
        entries = persistenceContext.createStoredMap(entriesStoredMapName,
                new NativeEntryPersistenceKey.NativeEntryPersistenceKeyFactory(nativeContainer));
        this.getMetaModel().put(Coordinators.ENTRYCOUNT, new MethodTuple(AbstractStoredMap.SIZE_METHOD, entries));
    }

    @Override
    public CoordinatorRestoreTask getRestoreTask() {
        return new DefaultTypeCoordinatorRestoreTask(this.getName(), allowedTypes);
    }

    /**
     * A restore task for the TypeCoordinator.
     */
    private static class DefaultTypeCoordinatorRestoreTask implements CoordinatorRestoreTask {

        private static final long serialVersionUID = 1L;

        private final String name;

        private final List<Class<?>> allowedTypes;

        public DefaultTypeCoordinatorRestoreTask(final String name, final List<Class<?>> allowedTypes) {
            this.name = name;
            this.allowedTypes = allowedTypes;
        }

        @Override
        public PersistentCoordinator restoreCoordinator() {
            return new DefaultTypeCoordinator(name, allowedTypes);
        }
    }

    @Override
    public void destroy() throws PersistenceException {
        entries.destroy();
    }

}
