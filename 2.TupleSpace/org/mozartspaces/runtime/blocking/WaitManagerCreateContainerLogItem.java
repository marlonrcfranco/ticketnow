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
package org.mozartspaces.runtime.blocking;

import org.mozartspaces.capi3.LocalContainerReference;
import org.mozartspaces.capi3.LogItem;

/**
 * Removes the container information from the WaitAndEventManager on transaction
 * rollback. Added to the transaction log when a container is created in an
 * explicit transaction.
 *
 * @author Tobias Doenz
 */
public final class WaitManagerCreateContainerLogItem implements LogItem {

    private final WaitAndEventManager waitEventManager;
    private final LocalContainerReference container;

    /**
     * Constructs a {@code WaitManagerCreateContainerLogItem}.
     *
     * @param waitEventManager
     *            the Wait and Event Manager
     * @param container
     *            the container reference
     */
    public WaitManagerCreateContainerLogItem(final WaitAndEventManager waitEventManager,
            final LocalContainerReference container) {
        this.waitEventManager = waitEventManager;
        this.container = container;
    }

    @Override
    public void commitSubTransaction() {
    }

    @Override
    public void commitTransaction() {
    }

    @Override
    public void rollbackSubTransaction() {
    }

    @Override
    public void rollbackTransaction() {
        waitEventManager.removeContainer(container);
    }

}
