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
import org.mozartspaces.capi3.LogItem;

/**
 * Abstract log item for all internal log items in capi3-javanative.
 *
 * @author Tobias Doenz
 */
public abstract class DefaultLogItem implements LogItem {

    private final LocalContainerReference containerReference;
    private final NativeIsolationManager isolationManager;
    private final NativeSubTransaction subTransaction;

    /**
     * @param containerReference
     *            the reference of the container used in the operation
     * @param isolationManager
     *            the isolation manager
     * @param subTransaction
     *            the sub-transaction
     */
    public DefaultLogItem(final LocalContainerReference containerReference,
            final NativeIsolationManager isolationManager, final NativeSubTransaction subTransaction) {
        this.containerReference = containerReference;
        if (this.containerReference == null) {
            throw new NullPointerException("container reference is null");
        }
        this.isolationManager = isolationManager;
        if (this.isolationManager == null) {
            throw new NullPointerException("isolation manager is null");
        }
        this.subTransaction = subTransaction;
        if (this.subTransaction == null) {
            throw new NullPointerException("sub-transaction is null");
        }
    }

    /**
     * @return the reference of the container used in the operation
     */
    public final LocalContainerReference getContainerReference() {
        return containerReference;
    }

    /**
     * @return the isolation manager used in the operation
     */
    public final NativeIsolationManager getIsolationManager() {
        return isolationManager;
    }

    /**
     * @return the sub-transaction used in the operation
     */
    public final NativeSubTransaction getSubTransaction() {
        return subTransaction;
    }

}
