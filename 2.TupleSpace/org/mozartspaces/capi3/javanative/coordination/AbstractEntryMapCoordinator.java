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
import java.util.HashSet;
import java.util.Set;

import org.mozartspaces.capi3.Capi3Exception;
import org.mozartspaces.capi3.CoordinationData;
import org.mozartspaces.capi3.CoordinatorLockedException;
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

/**
 * @author Tobias Doenz
 * @author Stefan Crass
 */
public abstract class AbstractEntryMapCoordinator extends AbstractDefaultCoordinator implements
ImplicitNativeCoordinator, PersistentCoordinator {

    private static final long serialVersionUID = 1L;

    private StoredMap<NativeEntry, NativeEntry> entries;

    private static final Method GET_ENTRY_SET_METHOD;

    static {
        try {
            GET_ENTRY_SET_METHOD = AbstractEntryMapCoordinator.class
                    .getDeclaredMethod("getEntrySet", (Class<?>[]) null);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    /**
     * Creates a DefaultAnyCoordinator.
     *
     * @param coordinatorName
     *            name of the coordinator
     */
    public AbstractEntryMapCoordinator(final String coordinatorName) {
        super(coordinatorName);

        this.getMetaModel().put(Coordinators.ENTRIES, new MethodTuple(GET_ENTRY_SET_METHOD, this));
    }

    @Override
    public void preRestoreContent(final PersistenceContext persistenceContext) throws PersistenceException {
    }

    @Override
    public final void postRestoreContent(final PersistenceContext persistenceContext,
            final NativeContainer nativeContainer, final NativeSubTransaction stx) throws PersistenceException {
        init(nativeContainer, stx, null);
        initPersistence(nativeContainer, persistenceContext);
    }

    @Override
    public final void init(final NativeContainer container, final NativeSubTransaction stx,
            final RequestContext context) throws MzsCoreRuntimeException {
    }

    @Override
    public final void initPersistence(final NativeContainer nativeContainer,
            final PersistenceContext persistenceContext) throws PersistenceException {
        final String entriesStoredMapName = persistenceContext.generateStoredMapName(getClass(),
                nativeContainer.getIdAsString(), this.getName());
        entries = persistenceContext.createStoredMap(entriesStoredMapName,
                new NativeEntryPersistenceKey.NativeEntryPersistenceKeyFactory(nativeContainer));
        this.getMetaModel().put(Coordinators.ENTRYCOUNT, new MethodTuple(AbstractStoredMap.SIZE_METHOD, entries));
    }

    @Override
    public void close() {
    }

    // only for meta model
    @SuppressWarnings("unused")
    private Set<NativeEntry> getEntrySet() {
        return new HashSet<NativeEntry>(entries.keySet());
    }

    /**
     * @return the entries map
     */
    public final StoredMap<NativeEntry, NativeEntry> getEntries() {
        return entries;
    }

    @Override
    public boolean registerEntry(final NativeSubTransaction stx, final CoordinationData coordData,
            final NativeEntry entry, final RequestContext context) throws Capi3Exception {
        this.entries.put(entry, entry, stx.getParent());
        return true;
    }

    @Override
    public boolean unregisterEntry(final NativeEntry entry, final RequestContext context,
            final NativeSubTransaction stx) {
        boolean entryExists = this.entries.containsKey(entry);
        this.entries.remove(entry, stx.getParent());
        return entryExists;
    }

    @Override
    public final void prepareEntryRemoval(final NativeSubTransaction stx, final NativeEntry entry,
            final RequestContext context) throws CoordinatorLockedException {
        return;
    }

    @Override
    public final void destroy() throws PersistenceException {
        entries.destroy();
    }
}
