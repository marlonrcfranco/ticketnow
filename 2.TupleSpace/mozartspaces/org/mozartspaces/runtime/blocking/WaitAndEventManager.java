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
import org.mozartspaces.capi3.Transaction;
import org.mozartspaces.core.TransactionReference;
import org.mozartspaces.runtime.tasks.Task;

/**
 * The <code>WaitAndEventManager</code> stores blocked tasks (Wait Container),
 * information about past events (Event Container) and processes events from
 * processed tasks (event processing logic).
 * Implementations should allow for timestamps with negative values from
 * {@link System#nanoTime()}.
 *
 * @author Tobias Doenz
 */
public interface WaitAndEventManager {

    /**
     * Adds the empty data structure for a newly created container.
     *
     * @param container
     *            the reference of the container
     */
    void addContainer(LocalContainerReference container);

    /**
     * Removes the data structure for a destroyed container and reschedules all
     * tasks in it.
     *
     * @param container
     *            the reference of the container
     */
    void removeContainer(LocalContainerReference container);

    /**
     * Adds a blocked task.
     *
     * @param task
     *            the task that is blocked and should be stored
     * @param lastExecutionTime
     *            the time when the task was executed last (before pre-aspects)
     * @param eventTime
     *            the time when the task was processed (after post-aspects)
     * @return whether the task has been added, or rescheduled otherwise
     */
    boolean addTask(Task task, long lastExecutionTime, long eventTime);

    /**
     * Removes a task from the Wait Container. Note: This method is called (only) when a task times out, so the task is
     * not in the TP anymore. The task is not rescheduled in this method!
     *
     * @param task
     *            the task that should be removed
     */
    void removeTask(Task task);

    /**
     * Processes, that a transaction has been committed. This includes
     * rescheduling of tasks that use this transaction, making changes of this
     * transaction generally visible and updating of timestamps related to that.
     *
     * @param txRef
     *            the reference of the transaction that has been committed
     * @param tx
     *            the transaction object that has been committed
     * @param eventTime
     *            the timestamp, when the transaction has been committed
     */
    void processTransactionCommit(TransactionReference txRef, Transaction tx, long eventTime);

    /**
     * Processes, that a transaction has been rolled back. This includes
     * rescheduling of tasks that use this transaction and deleting transaction
     * specific information like event timestamps.
     *
     * @param txRef
     *            the reference of the transaction that has been rolled back
     * @param tx
     *            the transaction object that has been rolled back
     * @param eventTime
     *            the timestamp, when the transaction has been rolled back
     */
    void processTransactionRollback(TransactionReference txRef, Transaction tx, long eventTime);

    /**
     * Processes events after a task has been processed.
     *
     * @param task
     *            the task that has been processed
     * @param categories
     *            the event categories, where something was changed when the
     *            task was processed
     * @param eventTime
     *            the time when the task was processed (after post-aspects)
     */
    void processEvents(Task task, WaitForCategory[] categories, long eventTime);

    /**
     * Shuts down the manager.
     */
    void shutdown();

}
