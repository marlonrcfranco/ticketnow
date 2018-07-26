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

import org.mozartspaces.capi3.LocalContainerReference;
import org.mozartspaces.core.RequestReference;
import org.mozartspaces.core.TransactionReference;
import org.mozartspaces.runtime.blocking.WaitForCategory;

/**
 * A <code>Task</code> is used to process a request in the Runtime. There is a
 * specific task implementation for each request type and a task instance for
 * each request.
 *
 * @author Tobias Doenz
 */
public interface Task extends Runnable {

    /**
     * Gets the reference of the request that is processed in this task.
     *
     * @return the request reference
     */
    RequestReference getRequestReference();

    /**
     * Gets the timestamp (from {@code System.nanoTime}) when the task was
     * executed last. The time is measured at the beginning of the task
     * execution before the request is processed.
     *
     * @return timestamp when the task was executed last
     */
    long getLastExecutionTime();

    // methods below are for WaitAndEventManager and return meaningful values
    // only for tasks that use CAPI-3, i.e., entry oder container tasks
    /**
     * Gets the reference of the transaction in whose context this task was
     * processed. This can be an explicit transaction or an implicit transaction
     * that has not been committed or rolled back yet.
     *
     * @return the transaction reference
     */
    TransactionReference getTransactionReference();

    /**
     * Gets the reference of the container that is used in the task, i.e., accessed
     * or created/destroyed.
     *
     * @return the container reference
     */
    LocalContainerReference getContainerReference();

    /**
     * Gets the event category this task is waiting for.
     *
     * @return the event category
     */
    WaitForCategory getWaitCategory();

    /**
     * @return {@code true} if the task can timeout, that is, the request has a
     *         timeout in milliseconds set
     */
    boolean canTimeOut();

}
