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

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.mozartspaces.capi3.AccessDeniedException;
import org.mozartspaces.capi3.AuthTargetCoordinator;
import org.mozartspaces.capi3.CoordinationData;
import org.mozartspaces.capi3.CoordinatorLockedException;
import org.mozartspaces.capi3.CountNotMetException;
import org.mozartspaces.capi3.EntryLockedException;
import org.mozartspaces.capi3.InvalidTypeException;
import org.mozartspaces.capi3.IsolationLevel;
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
import org.mozartspaces.core.authorization.AuthorizationRule;
import org.mozartspaces.core.authorization.RequestAuthTarget;
import org.mozartspaces.core.metamodel.MetaModelKeys;
import org.mozartspaces.core.metamodel.MetaModelKeys.Containers.Container.Coordinators;
import org.mozartspaces.core.metamodel.MetaModelUtils.MethodTuple;

/**
 * Coordinator that stores authorization rules according to their targets (subjects, containers, actions).
 *
 * @author Stefan Crass
 * @author Tobias Doenz
 */
// TODO test persistence
public final class DefaultAuthTargetCoordinator extends AbstractDefaultCoordinator
implements ImplicitNativeCoordinator, PersistentCoordinator {

    private static final long serialVersionUID = 1L;

    // TODO use optimized data structure
    // (e.g. Map<LocalContainerReference, Map<Subject, Map<ContainerAction, NativeEntry>>>)
    private StoredMap<NativeEntry, NativeEntry> entries;

    private static final Method GET_ENTRY_LIST_METHOD;

    static {
        try {
            GET_ENTRY_LIST_METHOD = DefaultAuthTargetCoordinator.class.getDeclaredMethod("getEntryList",
                    (Class<?>[]) null);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    /**
     * Creates a DefaultAuthTargetCoordinator.
     *
     * @param name
     *            name of the coordinator
     */
    public DefaultAuthTargetCoordinator(final String name) {
        super(name);

        this.getMetaModel().put(MetaModelKeys.Containers.Container.Coordinators.ENTRIES,
                new MethodTuple(GET_ENTRY_LIST_METHOD, this));
    }

    // only for meta model
    @SuppressWarnings("unused")
    private List<NativeEntry> getEntryList() {
        return new ArrayList<NativeEntry>(entries.keySet());
    }

    /**
     * Creates a new DefaultAuthTargetSelector.
     *
     * @param name
     *            of the coordinator this selector should be associated to
     * @param count
     *            of entries
     * @param reqAuthTarget
     *            authorization target to match
     * @return DefaultAuthTargetSelector
     */
    public static DefaultAuthTargetSelector newSelector(final String name, final int count,
            final RequestAuthTarget reqAuthTarget) {
        return new DefaultAuthTargetSelector(name, count, reqAuthTarget);
    }

    @Override
    public CoordinationData createDefaultCoordinationData() {
        return AuthTargetCoordinator.newCoordinationData();

    }

    @Override
    public void init(final NativeContainer container, final NativeSubTransaction stx, final RequestContext context)
            throws MzsCoreRuntimeException {
    }

    @Override
    public void close() {
    }

    @Override
    public synchronized boolean registerEntry(final NativeSubTransaction stx, final CoordinationData coordData,
            final NativeEntry entry, final RequestContext context) throws InvalidTypeException {
        Serializable data = entry.getData();
        if (data instanceof AuthorizationRule) {
            this.entries.put(entry, entry, stx.getParent());
        } else {
            throw new InvalidTypeException(this.getName(), data.getClass());
        }
        return true;
    }

    @Override
    public synchronized boolean unregisterEntry(final NativeEntry entry, final RequestContext context,
            final NativeSubTransaction stx) {
        boolean entryExists = this.entries.containsKey(entry);
        this.entries.remove(entry, stx.getParent());
        return entryExists;
    }

    @Override
    public void prepareEntryRemoval(final NativeSubTransaction stx, final NativeEntry entry,
            final RequestContext context) throws CoordinatorLockedException {
    }

    /**
     * @return the internal entry map
     */
    StoredMap<NativeEntry, NativeEntry> getEntries() {
        return this.entries;
    }

    /**
     * Selector of the DefaultAuthTargetCoordinator.
     *
     * @author Stefan Crass
     */
    private static final class DefaultAuthTargetSelector extends AbstractDefaultSelector<DefaultAuthTargetCoordinator> {

        private static final long serialVersionUID = 1L;

        // entry iterator (used in getNext to ensure that entries are returned only once in subsequent calls)
        private Iterator<NativeEntry> iterator;

        private StoredMap<NativeEntry, NativeEntry> entries;

        private final RequestAuthTarget reqAuthTarget;

        /**
         * Creates a DefaultQueueSelector.
         *
         * @param name
         *            of the Coordinator the Selector is associated to
         * @param count
         *            of entries
         */
        private DefaultAuthTargetSelector(final String name, final int count, final RequestAuthTarget authTarget) {
            super(name, count);
            this.reqAuthTarget = authTarget;
        }

        @Override
        public void linkEntries(final DefaultAuthTargetCoordinator coordinator) {
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
                List<NativeEntry> matchingRules = new ArrayList<NativeEntry>();
                for (NativeEntry ruleEntry : entries.keySet()) {
                    AuthorizationRule rule = (AuthorizationRule) ruleEntry.getData();

                    if (rule.getTarget().matchesRequest(this.reqAuthTarget)) {
                        matchingRules.add(ruleEntry);
                    }
                }
                entryIterator = matchingRules.iterator();
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
                        if (this.entries.containsKey(nextEntry)) {
                            // access checks already done at first selector

                            AuthorizationRule rule = (AuthorizationRule) nextEntry.getData();
                            if (rule.getTarget().matchesRequest(this.reqAuthTarget)) {
                                result.add(nextEntry);
                            }
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
                    if (this.entries.containsKey(nextEntry)) {
                        AuthorizationRule rule = (AuthorizationRule) nextEntry.getData();
                        if (rule.getTarget().matchesRequest(this.reqAuthTarget)) {
                            return nextEntry;
                        }
                    }
                }
            }
        }

        @Override
        public String toString() {
            return "AuthTarget " + getName() + " (target=" + reqAuthTarget + ", count=" + getCount() + ")";
        }
    }

    @Override
    public void preRestoreContent(final PersistenceContext persistenceContext) throws PersistenceException {
    }

    @Override
    public void postRestoreContent(final PersistenceContext persistenceContext, final NativeContainer nativeContainer,
            final NativeSubTransaction stx) throws PersistenceException {
        init(nativeContainer, stx, null);
        initPersistence(nativeContainer, persistenceContext);
    }

    @Override
    public void initPersistence(final NativeContainer nativeContainer, final PersistenceContext persistenceContext)
            throws PersistenceException {
        final String entriesStoredMapName = persistenceContext.generateStoredMapName(getClass(),
                nativeContainer.getIdAsString(), this.getName());
        entries = persistenceContext.createStoredMap(entriesStoredMapName,
                new NativeEntryPersistenceKey.NativeEntryPersistenceKeyFactory(nativeContainer));
        this.getMetaModel().put(Coordinators.ENTRYCOUNT,
                new MethodTuple(AbstractStoredMap.SIZE_METHOD, entries));
    }

    @Override
    public CoordinatorRestoreTask getRestoreTask() {
        return new AuthTargetCoordinatorRestoreTask(getName());
    }

    /**
     * Restore task for the AuthTargetCoordinator.
     */
    private static final class AuthTargetCoordinatorRestoreTask implements CoordinatorRestoreTask {

        private static final long serialVersionUID = 1L;

        private final String coordinatorName;

        private AuthTargetCoordinatorRestoreTask(final String coordinatorName) {
            this.coordinatorName = coordinatorName;
        }

        @Override
        public PersistentCoordinator restoreCoordinator() {
            return new DefaultAuthTargetCoordinator(coordinatorName);
        }
    }

    @Override
    public void destroy() throws PersistenceException {
        entries.destroy();
    }

}
