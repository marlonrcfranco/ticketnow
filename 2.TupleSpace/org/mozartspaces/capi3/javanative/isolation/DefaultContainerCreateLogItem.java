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
 * Log Item for a Container Create Operation.
 *
 * @author Martin Barisits
 * @author Tobias Doenz
 */
public final class DefaultContainerCreateLogItem extends DefaultLogItem {

    private final NativeContainerManager containerManager;

    DefaultContainerCreateLogItem(final LocalContainerReference cRef, final NativeContainerManager containerManager,
            final NativeIsolationManager isolationManager, final NativeSubTransaction subTransaction) {
        super(cRef, isolationManager, subTransaction);
        this.containerManager = containerManager;
        if (this.containerManager == null) {
            throw new NullPointerException("Container manager is null");
        }
    }

    @Override
    public void commitSubTransaction() {
        getIsolationManager().releaseSubTransactionContainerLock(OperationType.CREATECONTAINER,
                getContainerReference(), getSubTransaction());
    }

    @Override
    public void commitTransaction() {
        getIsolationManager().releaseContainerLock(OperationType.CREATECONTAINER, getContainerReference(),
                (NativeTransaction) getSubTransaction().getParent(), null);
    }

    @Override
    public void rollbackSubTransaction() {
        this.containerManager.purgeContainer(getContainerReference());
        getIsolationManager().purgeContainerLock(getContainerReference());
    }

    @Override
    public void rollbackTransaction() {
        this.containerManager.purgeContainer(getContainerReference());
        getIsolationManager().purgeContainerLock(getContainerReference());
    }

}
