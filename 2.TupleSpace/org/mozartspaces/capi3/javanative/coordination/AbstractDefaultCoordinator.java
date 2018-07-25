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

import java.util.HashMap;
import java.util.Map;

import org.mozartspaces.capi3.CoordinatorLockedException;
import org.mozartspaces.capi3.TransactionStatus;
import org.mozartspaces.capi3.javanative.isolation.NativeIsolationManager;
import org.mozartspaces.capi3.javanative.isolation.NativeSubTransaction;
import org.mozartspaces.capi3.javanative.isolation.NativeTransaction;
import org.mozartspaces.capi3.javanative.operation.NativeEntry;
import org.mozartspaces.core.metamodel.MetaDataProvider;
import org.mozartspaces.core.metamodel.MetaModelKeys;
import org.mozartspaces.core.metamodel.MetaModelKeys.Containers.Container.Coordinators;
import org.mozartspaces.core.metamodel.MetaModelUtils;
import org.mozartspaces.core.metamodel.Navigable;

/**
 * General superclass for all coordinators.
 * @author Stefan Crass
 */
public abstract class AbstractDefaultCoordinator implements NativeCoordinator, Navigable, MetaDataProvider {

    private static final long serialVersionUID = 1L;

    private final String name;
    private NativeIsolationManager isolationManager;
    private NativeSubTransaction lockHolder;

    private final Map<String, Object> metaModel;

    /**
     * Creates a new coordinator.
     * @param coordinatorName
     *          the name of the coordinator
     */
    public AbstractDefaultCoordinator(final String coordinatorName) {
        this.name = coordinatorName;
        this.metaModel = new HashMap<String, Object>();
        this.metaModel.put(Coordinators.NAME, this.name);
        this.metaModel.put(MetaModelKeys.Containers.Container.Coordinators.CLASS, getClass().getName());
    }

    /**
     * Gets the meta model map of the coordinator.
     * @return the meta model map
     */
    protected final Map<String, Object> getMetaModel() {
        return this.metaModel;
    }

    /**
     * Special function that checks whether an entry can be seen as a direct replacement of another entry.
     * @param base Entry to be overwritten
     * @param overwrite new Entry
     * @return true if valid, false otherwise
     */
    protected final boolean isValidEntryOverwrite(final NativeEntry base, final NativeEntry overwrite) {
        return this.isolationManager.checkValidEntryOverwrite(base, overwrite);
    }

    /**
     * Tries to acquire the coordinator lock.
     *
     * @param stx
     *            the sub-transaction
     * @throws CoordinatorLockedException
     *            if coordinator is already locked
     */
    protected final void acquireCoordinatorLock(final NativeSubTransaction stx) throws CoordinatorLockedException {
        if (this.lockHolder == null) {
            this.lockHolder = stx;
            return;
        }
        if (this.lockHolder.getStatus() == TransactionStatus.RUNNING) {
            if (this.lockHolder.equals(stx)) {
                return;
            }
            throw new CoordinatorLockedException(this.getName(), this.lockHolder.getParent().getId(),
                    this.lockHolder.getId());
        }
        NativeTransaction tx = ((NativeTransaction) this.lockHolder.getParent());
        if (tx.getStatus() == TransactionStatus.COMMITED) {
            this.lockHolder = stx;
            return;
        }
        if (tx.getStatus() == TransactionStatus.ABORTED) {
            this.lockHolder = stx;
            return;
        }
        if (tx.equals(stx.getParent())) {
            this.lockHolder = stx;
            return;
        }
        throw new CoordinatorLockedException(this.getName(), this.lockHolder.getParent().getId(),
                this.lockHolder.getId());
    }


    @Override
    public final String getName() {
        return this.name;
    }

    @Override
    public final void setIsolationManager(final NativeIsolationManager isolationManager) {
        this.isolationManager = isolationManager;
    }

    @Override
    public final void setMetaDataProperty(final String key, final Object value) {
        throw new UnsupportedOperationException("Properties cannot be set here");
    }


    @Override
    public final Object navigate(final String path) {
        return MetaModelUtils.navigate(path, this, this.getMetaModel());
    }

    @Override
    public final Object getMetaData(final int depth) {
        return MetaModelUtils.getData(depth, this.getMetaModel());
    }
}
