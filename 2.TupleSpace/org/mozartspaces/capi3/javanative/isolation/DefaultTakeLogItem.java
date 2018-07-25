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
import org.mozartspaces.capi3.javanative.operation.NativeContainer;
import org.mozartspaces.capi3.javanative.operation.NativeEntry;
import org.mozartspaces.core.RequestContext;

/**
 * Log Item for a TAKE Operation.
 *
 * @author Martin Barisits
 * @author Tobias Doenz
 */
public final class DefaultTakeLogItem extends DefaultLogItem {

    private final NativeEntry entry;
    private final RequestContext context;
    private final NativeContainer nativeContainer;

    /**
     * @param entry
     *            the entry, may be {@code null}
     * @param cRef
     *            the reference of the container
     * @param isolationManager
     *            the isolation manager
     * @param subTransaction
     *            the sub-transaction
     * @param context
     *            the context
     * @param nativeContainer
     *            the internal container object
     */
    public DefaultTakeLogItem(final NativeEntry entry, final LocalContainerReference cRef,
            final NativeIsolationManager isolationManager, final NativeSubTransaction subTransaction,
            final RequestContext context, final NativeContainer nativeContainer) {
        super(cRef, isolationManager, subTransaction);
        this.entry = entry;
        this.context = context;
        this.nativeContainer = nativeContainer;
    }

    @Override
    public void commitSubTransaction() {
        if (entry != null) {
            getIsolationManager().releaseSubTransactionEntryLock(OperationType.TAKE, this.entry, getSubTransaction());
        }
    }

    @Override
    public void commitTransaction() {
        if (entry != null) {
            this.nativeContainer.purgeEntry(entry, context, getSubTransaction());
            getIsolationManager().purgeEntryLock(this.entry);
        }
    }

    @Override
    public void rollbackSubTransaction() {
        if (entry != null) {
            getIsolationManager().releaseEntryLock(OperationType.TAKE, this.entry,
                    (NativeTransaction) getSubTransaction().getParent(), getSubTransaction());
        }
    }

    @Override
    public void rollbackTransaction() {
        if (entry != null) {
            getIsolationManager().releaseEntryLock(OperationType.TAKE, this.entry,
                    (NativeTransaction) getSubTransaction().getParent(), null);
        }
    }

}
