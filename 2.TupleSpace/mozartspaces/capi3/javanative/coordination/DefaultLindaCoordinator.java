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

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.mozartspaces.capi3.AccessDeniedException;
import org.mozartspaces.capi3.Capi3Exception;
import org.mozartspaces.capi3.CoordinationData;
import org.mozartspaces.capi3.CoordinatorLockedException;
import org.mozartspaces.capi3.CountNotMetException;
import org.mozartspaces.capi3.EntryLockedException;
import org.mozartspaces.capi3.EntryNotAnnotatedException;
import org.mozartspaces.capi3.Index;
import org.mozartspaces.capi3.IsolationLevel;
import org.mozartspaces.capi3.LindaCoordinator;
import org.mozartspaces.capi3.OperationType;
import org.mozartspaces.capi3.Queryable;
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
import org.mozartspaces.core.metamodel.MetaModelKeys.Containers.Container.Coordinators;
import org.mozartspaces.core.metamodel.MetaModelUtils.MethodTuple;
import org.mozartspaces.util.LoggerFactory;
import org.slf4j.Logger;

/**
 * Coordinator that supports Linda template matching.
 *
 * @author Martin Barisits
 * @author Tobias Doenz
 * @author Stefan Crass
 */
public final class DefaultLindaCoordinator extends AbstractDefaultCoordinator implements ImplicitNativeCoordinator,
PersistentCoordinator {

    private static final long serialVersionUID = 1L;

    private static final Logger log = LoggerFactory.get();

    private final boolean onlyAnnotatedEntries;
    private final Map<Class<?>, Map<NativeEntry, NativeEntry>> entries;
    private final Map<Class<?>, LindaMatcher> matchers;

    /**
     * The value is the class name and used for the persistence so that not all entries (data) need to be loaded on
     * restore.
     */
    // TODO optimize the storage of class names (redundant for entries of same type, use separate map?)
    private StoredMap<NativeEntry, String> entryClasses;

    private static final Method GET_ENTRY_SET_METHOD;

    static {
        try {
            GET_ENTRY_SET_METHOD = DefaultLindaCoordinator.class
                    .getDeclaredMethod("getEntrySet", (Class<?>[]) null);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    /**
     * Creates a DefaultLindaCoordinator.
     *
     * @param name
     *            of the Coordinator
     * @param onlyAnnotatedEntries
     *            {@code true} if only annotated entries may be written to the coordinator, {@code false} otherwise
     */
    public DefaultLindaCoordinator(final String name, final boolean onlyAnnotatedEntries) {
        super(name);
        this.onlyAnnotatedEntries = onlyAnnotatedEntries;
        this.entries = new ConcurrentHashMap<Class<?>, Map<NativeEntry, NativeEntry>>();
        this.matchers = new ConcurrentHashMap<Class<?>, LindaMatcher>();

        this.getMetaModel().put(Coordinators.ENTRIES, new MethodTuple(GET_ENTRY_SET_METHOD, this));
    }

    // only for meta model
    @SuppressWarnings("unused")
    private Set<NativeEntry> getEntrySet() {
        return new HashSet<NativeEntry>(entryClasses.keySet());
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
        for (NativeEntry entry : entryClasses.keySet()) {
            String className = entryClasses.get(entry);
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
        final String entryClassesStoredMapName = persistenceContext.generateStoredMapName(getClass(),
                nativeContainer.getIdAsString(), this.getName());
        entryClasses = persistenceContext.createStoredMap(entryClassesStoredMapName,
                new NativeEntryPersistenceKey.NativeEntryPersistenceKeyFactory(nativeContainer));
        this.getMetaModel().put(Coordinators.ENTRYCOUNT,
                new MethodTuple(AbstractStoredMap.SIZE_METHOD, entryClasses));
    }

    @Override
    public CoordinatorRestoreTask getRestoreTask() {
        return new DefaultLindaCoordinatorRestoreTask(this.getName(), onlyAnnotatedEntries);
    }

    /**
     * Restore task for the LindaCoordinator.
     */
    private static final class DefaultLindaCoordinatorRestoreTask implements CoordinatorRestoreTask {

        private static final long serialVersionUID = 1L;

        private final String name;

        private final boolean onlyAnnotatedEntries;

        private DefaultLindaCoordinatorRestoreTask(final String name, final boolean onlyAnnotatedEntries) {
            this.name = name;
            this.onlyAnnotatedEntries = onlyAnnotatedEntries;
        }

        @Override
        public PersistentCoordinator restoreCoordinator() {
            return new DefaultLindaCoordinator(name, onlyAnnotatedEntries);
        }
    }

    @Override
    public void destroy() throws PersistenceException {
        entryClasses.destroy();
    }

    /**
     * @return the internal linda matcher map
     */
    Map<Class<?>, LindaMatcher> getMatchers() {
        return matchers;
    }

    /**
     * @return the internal entry map
     */
    Map<Class<?>, Map<NativeEntry, NativeEntry>> getEntries() {
        return entries;
    }

    /**
     * Creates a DefaultLindaSelector.
     *
     * @param name
     *            of the coordinator this selector is associated to
     * @param count
     *            of entries
     * @param template
     *            to be matched
     * @return new DefaultLindaSelector
     */
    public static DefaultLindaSelector newSelector(final String name, final int count, final Object template) {
        return new DefaultLindaSelector(name, count, template);
    }

    @Override
    public CoordinationData createDefaultCoordinationData() {
        return LindaCoordinator.newCoordinationData();
    }

    @Override
    public void prepareEntryRemoval(final NativeSubTransaction stx, final NativeEntry entry,
            final RequestContext context) throws CoordinatorLockedException {
    }

    @Override
    public synchronized boolean registerEntry(final NativeSubTransaction stx, final CoordinationData coordData,
            final NativeEntry entry, final RequestContext context) throws Capi3Exception {
        return registerEntry(entry, entry.getData().getClass(), stx);
    }

    private boolean registerEntry(final NativeEntry entry, final Class<?> entryClass, final NativeSubTransaction stx)
            throws Capi3Exception {
        Map<NativeEntry, NativeEntry> entriesOfSameClass = this.entries.get(entryClass);
        if (entriesOfSameClass == null) {
            this.storeLindaMatcher(entryClass);
            entriesOfSameClass = new ConcurrentHashMap<NativeEntry, NativeEntry>();
            this.entries.put(entryClass, entriesOfSameClass);
        }
        entriesOfSameClass.put(entry, entry);
        entryClasses.put(entry, entryClass.getName(), stx.getParent());
        return true;
    }

    @Override
    public synchronized boolean unregisterEntry(final NativeEntry entry, final RequestContext context,
            final NativeSubTransaction stx) {
        boolean entryExists;
        Class<?> entryClass = entry.getData().getClass();
        Map<NativeEntry, NativeEntry> entriesOfSameClass = this.entries.get(entryClass);
        if (entriesOfSameClass != null) {
            if (entriesOfSameClass.size() == 1 && entriesOfSameClass.containsKey(entry)) {
                this.entries.remove(entryClass);
                entryExists = true;
            } else {
                entryExists = entriesOfSameClass.containsKey(entry);
                entriesOfSameClass.remove(entry);
            }
        } else {
            entryExists = false;
        }
        entryClasses.remove(entry, stx.getParent());
        return entryExists;
    }

    /**
     * Stores a LindaMatcher for a class.
     *
     * @param clazz
     *            the class to be evaluated
     * @param visited
     *            the list of visited objects to avoid cycles
     * @throws EntryNotAnnotatedException
     *             if the class is not annotated as required
     */
    private void storeLindaMatcher(final Class<?> clazz, final Set<Class<?>> visited, final boolean requireAnnotations)
            throws EntryNotAnnotatedException {

        log.debug("Storing Linda matcher for class {}", clazz.getName());

        // TODO check if there is an annotated superclass
        List<Field> fields = new ArrayList<Field>();
        if (!requireAnnotations || clazz.isAnnotationPresent(Queryable.class)) {
            Queryable queryable = clazz.getAnnotation(Queryable.class);
            // class has to be investigated
            if (visited.contains(clazz)) {
                return;
            }
            visited.add(clazz);
            for (Field field : clazz.getDeclaredFields()) {
                if (!requireAnnotations || queryable.autoindex() || field.isAnnotationPresent(Index.class)) {
                    // field has to be included
                    if (!field.isAccessible()) {
                        field.setAccessible(true);
                    }
                    fields.add(field);
                    // recursively store matcher for annotated classes
                    if (field.getType().isAnnotationPresent(Queryable.class)) {
                        this.storeLindaMatcher(field.getType(), visited, true);
                    }
                    // analyze collections
                    if (Collection.class.isAssignableFrom(field.getType())) {
                        this.analyzeCollection(field.getGenericType(), visited);
                    }
                }
            }
            this.matchers.put(clazz, new LindaMatcher(fields, this.matchers));
        } else {
            throw new EntryNotAnnotatedException();
        }
    }

    /**
     * Analyze a collection to decide if new LindaMatcher have to be build.
     *
     * @param type
     *            the type to be analyzed
     * @param visited
     *            the list of visited objects to avoid cycles
     * @throws EntryNotAnnotatedException
     *             thrown the evaluated class is not annotated
     */
    private void analyzeCollection(final Type type, final Set<Class<?>> visited) throws EntryNotAnnotatedException {

        log.debug("Analyzing collection of type {}", type);

        if (type instanceof ParameterizedType) {
            Type parameterType = ((ParameterizedType) type).getActualTypeArguments()[0];
            if (((Class<?>) parameterType).isAnnotationPresent(Queryable.class)) {
                // store matcher for annotated parameter class
                this.storeLindaMatcher(((Class<?>) parameterType), visited, true);
            }
            if (Collection.class.isAssignableFrom(((Class<?>) parameterType))) {
                // recursively analyze collection
                this.analyzeCollection(parameterType, visited);
            }
        }

    }

    /**
     * Stores a new {@link LindaMatcher} for a given class.
     *
     * @param clazz
     *            to be evaluated
     * @throws EntryNotAnnotatedException
     *             if the class is not annotated as required
     */
    public void storeLindaMatcher(final Class<?> clazz) throws EntryNotAnnotatedException {
        if (this.matchers.containsKey(clazz)) {
            return;
        }
        Set<Class<?>> visited = new HashSet<Class<?>>();
        this.storeLindaMatcher(clazz, visited, onlyAnnotatedEntries);
    }

    /**
     * Special Linda Matching Object built for every Class which is stored in the Coordinator.
     *
     * @author Martin Barisits
     * @author Tobias Doenz
     */
    public static final class LindaMatcher {

        private final List<Field> fields;
        private final Map<Class<?>, LindaMatcher> matchers;

        /**
         * Creates a new LindaMatcher Object.
         *
         * @param fields
         *            Fields to evaluate by the match function
         * @param matchers
         *            complete Map of all LindaMatchers stored by the Coordinator
         */
        private LindaMatcher(final List<Field> fields, final Map<Class<?>, LindaMatcher> matchers) {
            this.fields = fields;
            this.matchers = matchers;
        }

        /**
         * Matches the template against the check Object.
         *
         * @param template
         *            to base the check on
         * @param check
         *            object to be matched
         * @return true if the Template equals the Check Object
         */
        public boolean match(final Object template, final Object check) {
            Set<Object> visited = new HashSet<Object>();
            return this.match(template, check, visited);
        }

        /**
         * Matches the template against the check Object.
         *
         * @param template
         *            to base the check on
         * @param check
         *            object to be matched
         * @param visited
         *            map of visited objects to avoid cycles
         * @return true if the Template equals the Check Object
         */
        private boolean match(final Object template, final Object check, final Set<Object> visited) {

            try {
                for (Field field : this.fields) {
                    // wildcard
                    if (field.get(template) == null) {
                        continue;
                    }
                    // TODO re-add correct code to avoid traversing object cycles
                    // this does not work!! (see LindaBooleanMatchingTest)
                    // if (visited.contains(field.get(template))) {
                    // continue;
                    // }
                    // visited.add(field.get(template));

                    // template matching when matcher exists
                    LindaMatcher matcher = matchers.get(field.getType());
                    if (matcher != null) {
                        if (!matcher.match(field.get(template), field.get(check), visited)) {
                            return false;
                        }
                        continue;
                    }
                    // special handling of collections
                    if (Collection.class.isAssignableFrom(field.getType())) {
                        @SuppressWarnings("unchecked")
                        Collection<Object> templateCollection = (Collection<Object>) field.get(template);
                        @SuppressWarnings("unchecked")
                        Collection<Object> checkCollection = (Collection<Object>) field.get(check);
                        if (checkCollection == null) {
                            return false;
                        }
                        if (templateCollection.size() != checkCollection.size()) {
                            return false;
                        }
                        Iterator<Object> checkIt = checkCollection.iterator();
                        for (Object templateObject : templateCollection) {
                            // checked that collections have same size, call to next must succeed
                            Object checkObject = checkIt.next();
                            if (templateObject == null) {
                                continue;
                            }
                            Class<?> templateClass = templateObject.getClass();
                            matcher = matchers.get(templateClass);
                            if (matcher != null && templateClass.isAnnotationPresent(Queryable.class)) {
                                /*
                                 * Check for Queryable annotation to avoid indeterminism. Otherwise the existence of a
                                 * matcher would depend on the entries previously registered at the coordinator, that
                                 * is, whether an instance of this class was used as entry (an not only as field inside
                                 * an entry)
                                 */
                                if (!matcher.match(templateObject, checkObject, visited)) {
                                    return false;
                                }
                            } else {
                                if (!templateObject.equals(checkObject)) {
                                    return false;
                                }
                            }
                        }
                        continue;
                    }
                    // check for exact match
                    if (field.get(template).equals(field.get(check))) {
                        continue;
                    }
                    return false;
                }
            } catch (IllegalArgumentException e) {
                // TODO throw exception
                return false;
            } catch (IllegalAccessException e) {
                // TODO throw exception
                return false;
            }
            return true;

        }
    }

    /**
     * Selector of the DefaultLindaCoordinator.
     *
     * @author Martin Barisits
     * @author Tobias Doenz
     * @author Stefan Crass
     */
    private static final class DefaultLindaSelector extends AbstractDefaultSelector<DefaultLindaCoordinator> {

        private static final long serialVersionUID = -3887415361982845224L;

        // the template to match (constructor argument)
        private final Object template;

        // entry iterator (used in getNext to ensure that entries are returned only once in subsequent calls)
        private Iterator<NativeEntry> iterator;
        // from the coordinator
        private Map<Class<?>, Map<NativeEntry, NativeEntry>> entries;
        private Map<Class<?>, LindaMatcher> matchers;

        /**
         * Creates a DefaultLindaSelector.
         *
         * @param name
         *            of the coordinator this selector is associated to
         * @param count
         *            of entries
         * @param template
         *            to be matched
         */
        DefaultLindaSelector(final String name, final int count, final Object template) {
            super(name, count);
            this.template = template;
        }

        @Override
        public void linkEntries(final DefaultLindaCoordinator coordinator) {
            this.entries = coordinator.getEntries();
            this.matchers = coordinator.getMatchers();
        }

        @Override
        public List<NativeEntry> getAll(final IsolationLevel isolationLevel, final AuthorizationResult auth,
                final NativeSubTransaction stx, final OperationType opType, final RequestContext context)
                        throws CountNotMetException, EntryLockedException, AccessDeniedException {

            Map<NativeEntry, NativeEntry> data = this.entries.get(this.template.getClass());
            if (data == null) {
                // no entries of that class
                checkCount(0, this.getCount(), this.getName());
                return new ArrayList<NativeEntry>();
            }
            checkCount(data.size(), this.getCount(), this.getName());

            Iterator<NativeEntry> entryIterator = null;
            if (this.getPredecessor() == null) {
                entryIterator = data.keySet().iterator();
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
                        // check that entry is registered at this coordinator and matches the template
                        if (this.template.getClass().equals(nextEntry.getData().getClass())) {
                            if (this.entries.get(nextEntry.getData().getClass()).containsKey(nextEntry.getData())) {
                                // entry must be registered
                                LindaMatcher matcher = this.matchers.get(nextEntry.getData().getClass());
                                if (matcher.match(this.template, nextEntry.getData())) {
                                    result.add(nextEntry);
                                }
                            }
                        }
                    }
                } else if (entryIterator.hasNext()) {
                    // first selector
                    nextEntry = entryIterator.next();

                    LindaMatcher matcher = this.matchers.get(nextEntry.getData().getClass());
                    if (matcher.match(this.template, nextEntry.getData())) {
                        // raise exception if entry is locked or denied when using COUNT_ALL
                        boolean isMandatory = (this.getCount() == Selector.COUNT_ALL);
                        if (this.checkAccessibility(nextEntry, isolationLevel, auth, stx, opType, isMandatory)) {
                            result.add(nextEntry);
                        }
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
                    // check that entry is registered at this coordinator and matches the template
                    if (this.template.getClass().equals(nextEntry.getData().getClass())) {
                        if (this.entries.get(nextEntry.getData().getClass()).containsKey(nextEntry.getData())) {
                            LindaMatcher matcher = this.matchers.get(nextEntry.getData().getClass());
                            if (matcher.match(this.template, nextEntry.getData())) {
                                return nextEntry;
                            }
                        }
                    }
                }
            }
        }

        @Override
        public String toString() {
            return "Linda " + getName() + " (template=" + template + ", count=" + getCount() + ")";
        }

    }
}
