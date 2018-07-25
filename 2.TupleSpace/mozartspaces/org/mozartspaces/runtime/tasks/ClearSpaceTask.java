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
package org.mozartspaces.runtime.tasks;

import net.jcip.annotations.NotThreadSafe;

import org.mozartspaces.core.RequestMessage;
import org.mozartspaces.core.util.Nothing;
import org.mozartspaces.runtime.RuntimeData;
import org.mozartspaces.util.LoggerFactory;
import org.slf4j.Logger;

/**
 * A <code>ClearSpaceTask</code> clears the space, that is, rollbacks all active
 * transactions and deletes all containers, entries and aspects.
 *
 * @author Tobias Doenz
 */
@NotThreadSafe
public final class ClearSpaceTask extends AbstractTask<Nothing> {

    private static final Logger log = LoggerFactory.get();

    /**
     * Constructs a <code>ClearSpaceTask</code>.
     *
     * @param requestMessage
     *            the request message
     * @param runtimeData
     *            runtime objects and components
     */
    public ClearSpaceTask(final RequestMessage requestMessage, final RuntimeData runtimeData) {
        super(requestMessage, runtimeData);
    }

    @Override
    protected Nothing runSpecific() throws Throwable {
        /**
         * TODO implement clear space task
         * lock request container
         * rollback all transactions
         * purge wait container
         * delete all containers (needs list of all containers)
         * remove all aspects
         * unlock request container
         * -> needs implementation of meta model (container list etc.)
         */
        log.error("Clear space is not implemented yet");
        return Nothing.INSTANCE;
    }

}
