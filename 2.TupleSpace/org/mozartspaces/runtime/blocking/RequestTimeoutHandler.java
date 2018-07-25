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

import net.jcip.annotations.Immutable;

import org.mozartspaces.runtime.RequestHandler;
import org.mozartspaces.runtime.tasks.Task;
import org.mozartspaces.util.LoggerFactory;
import org.slf4j.Logger;

/**
 * Handles timed out requests, they are removed from the Wait and Event
 * Container and rescheduled (Request Container).
 *
 * @author Tobias Doenz
 *
 * @see WaitAndEventManager
 * @see RequestHandler
 */
@Immutable
public final class RequestTimeoutHandler implements TimeoutHandler<Task> {

    private static final Logger log = LoggerFactory.get();

    private final RequestHandler requestHandler;
    private final WaitAndEventManager waitAndEventManager;

    /**
     * Constructs a <code>RequestTimeoutHandler</code>.
     *
     * @param requestHandler the Request Container
     * @param waitAndEventManager the Wait and Event Manager
     */
    public RequestTimeoutHandler(final RequestHandler requestHandler,
            final WaitAndEventManager waitAndEventManager) {
        this.requestHandler = requestHandler;
        assert this.requestHandler != null;
        this.waitAndEventManager = waitAndEventManager;
        assert this.waitAndEventManager != null;
    }

    @Override
    public void elementTimedOut(final Task task) {
        log.info("Request {} timed out", task.getRequestReference());

        waitAndEventManager.removeTask(task);

        /*
         * Reschedule the task:
         * the timeout is checked at the beginning of the task execution, then
         * the error message is sent. It would also be possible to directly
         * create and distribute an error (would need the ResponseDistributor
         * instead of the RequestHandler), but that would create the small
         * possibility of a race condition where a task is continuously
         * rescheduled by the event processing logic even though it already
         * timed out.
         */
        requestHandler.rescheduleTask(task);

    }

}
