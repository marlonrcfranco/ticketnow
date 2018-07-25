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
package org.mozartspaces.runtime;

import org.mozartspaces.core.RequestMessage;
import org.mozartspaces.runtime.tasks.Task;

/**
 * Basic interface for enqueuing new and rescheduled requests for processing.
 * Implementations of this class implement the Request Container of the XVSM
 * formal model, maybe also the Core Processor (XP) itself.
 *
 * @author Tobias Doenz
 */
public interface RequestHandler {

    /**
     * Processes a request.
     *
     * @param requestMessage
     *            the request message that should be processed
     */
    void processRequest(RequestMessage requestMessage);

    /**
     * Reschedules a task, that is, executes it again.
     *
     * @param task
     *            the task to reschedule
     */
    void rescheduleTask(Task task);

    /**
     * Shuts down the request handler.
     *
     * @param wait
     *            determines if the shutdown should be made synchronous (
     *            <code>true</code>, wait for shutdown to complete) or
     *            asynchronous in its own thread (<code>false</code>)
     */
    void shutdown(boolean wait);
}
