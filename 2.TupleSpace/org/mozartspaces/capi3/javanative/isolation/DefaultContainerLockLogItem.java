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

/**
 * Log Item for Container Lock Operation.
 *
 * @author Martin Barisits
 * @author Tobias Doenz
 */
public final class DefaultContainerLockLogItem extends DefaultLogItem {

    DefaultContainerLockLogItem(final LocalContainerReference cRef, final NativeIsolationManager isolationManager,
            final NativeSubTransaction subTransaction) {
        super(cRef, isolationManager, subTransaction);
    }

    @Override
    public void commitSubTransaction() {
        getIsolationManager().releaseSubTransactionContainerLock(OperationType.LOCKCONTAINER, getContainerReference(),
                getSubTransaction());
    }

    @Override
    public void commitTransaction() {
        getIsolationManager().releaseContainerLock(OperationType.LOCKCONTAINER, getContainerReference(),
                (NativeTransaction) getSubTransaction().getParent(), null);
    }

    @Override
    public void rollbackSubTransaction() {
        getIsolationManager().releaseContainerLock(OperationType.LOCKCONTAINER, getContainerReference(),
                (NativeTransaction) getSubTransaction().getParent(), getSubTransaction());
    }

    @Override
    public void rollbackTransaction() {
        getIsolationManager().releaseContainerLock(OperationType.LOCKCONTAINER, getContainerReference(),
                (NativeTransaction) getSubTransaction().getParent(), null);
    }

}
