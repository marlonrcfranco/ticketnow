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
package org.mozartspaces.capi3.javanative.isolation;

import org.mozartspaces.capi3.LocalContainerReference;
import org.mozartspaces.capi3.OperationType;
import org.mozartspaces.capi3.javanative.operation.NativeContainerManager;

/**
 * Log Item for a Container Destroy Operation.
 *
 * @author Martin Barisits
 * @author Tobias Doenz
 */
public final class DefaultContainerDestroyLogItem extends DefaultLogItem {

    private final NativeContainerManager containerManager;

    DefaultContainerDestroyLogItem(final LocalContainerReference cRef, final NativeContainerManager containerManager,
            final NativeIsolationManager isolationManager, final NativeSubTransaction subTransaction) {
        super(cRef, isolationManager, subTransaction);
        this.containerManager = containerManager;
        if (this.containerManager == null) {
            throw new NullPointerException("Container manager is null");
        }
    }

    @Override
    public void commitSubTransaction() {
        getIsolationManager().releaseSubTransactionContainerLock(OperationType.DESTROYCONTAINER,
                getContainerReference(), getSubTransaction());
    }

    @Override
    public void commitTransaction() {
        this.containerManager.purgeContainer(getContainerReference());
        getIsolationManager().purgeContainerLock(getContainerReference());
    }

    @Override
    public void rollbackSubTransaction() {
        getIsolationManager().releaseContainerLock(OperationType.DESTROYCONTAINER, getContainerReference(),
                (NativeTransaction) getSubTransaction().getParent(), getSubTransaction());
    }

    @Override
    public void rollbackTransaction() {
        getIsolationManager().releaseContainerLock(OperationType.DESTROYCONTAINER, getContainerReference(),
                (NativeTransaction) getSubTransaction().getParent(), null);
    }

}
