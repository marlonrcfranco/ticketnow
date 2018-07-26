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

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import net.jcip.annotations.ThreadSafe;

import org.mozartspaces.capi3.LocalContainerReference;
import org.mozartspaces.capi3.Transaction;
import org.mozartspaces.core.MzsTimeoutException;
import org.mozartspaces.core.TransactionException;
import org.mozartspaces.core.TransactionReference;
import org.mozartspaces.core.util.NamedThreadFactory;
import org.mozartspaces.runtime.RequestHandler;
import org.mozartspaces.runtime.TransactionManager;
import org.mozartspaces.runtime.blocking.deadlock.LockedTaskHandler;
import org.mozartspaces.runtime.tasks.Task;
import org.mozartspaces.util.LoggerFactory;
import org.slf4j.Logger;

/**
 * Data structures and logic to store waiting tasks for each container and wait
 * category, together with the aggregated information about past events,
 * separated by transaction. The event processing logic is implemented as
 * described in the master's thesis of Stefan Craß. All non-private methods of
 * this class are <code>synchronized</code>.
 *
 * @author Tobias Doenz
 */
@ThreadSafe
public final class SynchronizedWaitAndEventManager implements WaitAndEventManager {

    private static final Logger log = LoggerFactory.get();

    private final RequestHandler requestHandler;
    private final TimeoutProcessor<Task> requestTimeoutProcessor;
    private final TransactionManager txManager;
    private final LockedTaskHandler lockedTaskHandler;

    private final ExecutorService rescheduleThreadPool;
    private final Map<LocalContainerReference, Map<WaitForCategory, WaitInfo>> containers;
    // for lookupContainer, maybe other tasks in the future
    private final WaitInfo containerlessTasks;

    /**
     * Constructs a <code>SynchronizedWaitAndEventManager</code>. There should
     * be only one instance per Runtime.
     *
     * @param requestHandler
     *            the request handler, used to reschedule tasks
     * @param requestTimeoutProcessor
     *            the timeout processor, tasks that are rescheduled are removed
     *            from it
     * @param txManager
     *            the transaction manager, used to check the validity of
     *            transaction (to prevent race conditions)
     * @param lockedTaskHandler
     *            the handler of (long-term) locked tasks, used to remove
     *            rescheduled tasks from the transaction lock graph
     * @param asyncRescheduling
     *            tasks are rescheduled asynchronously, if <code>true</code>,
     *            synchronous otherwise
     */
    public SynchronizedWaitAndEventManager(final RequestHandler requestHandler,
            final TimeoutProcessor<Task> requestTimeoutProcessor, final TransactionManager txManager,
            final LockedTaskHandler lockedTaskHandler, final boolean asyncRescheduling) {
        this.requestHandler = requestHandler;
        assert this.requestHandler != null;
        this.requestTimeoutProcessor = requestTimeoutProcessor;
        assert this.requestTimeoutProcessor != null;
        this.txManager = txManager;
        assert this.txManager != null;
        this.lockedTaskHandler = lockedTaskHandler;

        // TODO replace with configurable thread pool from DefaultMzsCoreFactory?
        rescheduleThreadPool = asyncRescheduling ? Executors.newSingleThreadExecutor(new NamedThreadFactory(
                "TaskReschedule-")) : null;

        containers = new HashMap<LocalContainerReference, Map<WaitForCategory, WaitInfo>>();
        containerlessTasks = new WaitInfo();
    }

    @Override
    public synchronized void addContainer(final LocalContainerReference container) {
        assert container != null;
        log.debug("Adding data structure for container {}", container);
        Map<WaitForCategory, WaitInfo> waitInfo = new EnumMap<WaitForCategory, WaitInfo>(WaitForCategory.class);
        for (WaitForCategory category : WaitForCategory.values()) {
            waitInfo.put(category, new WaitInfo());
        }
        if (containers.put(container, waitInfo) != null) {
            log.warn("Wait information for container {} already present", container);
        }
    }

    @Override
    public synchronized void removeContainer(final LocalContainerReference container) {
        assert container != null;
        log.debug("Removing data structure for container {}", container);
        Map<WaitForCategory, WaitInfo> waitInfo = containers.remove(container);
        if (waitInfo == null) {
            log.warn("No wait information for container {}", container);
            return;
        }
        // reschedule all tasks for all categories of that container
        for (Map.Entry<WaitForCategory, WaitInfo> category : waitInfo.entrySet()) {
            log.trace("Reschedule tasks for category {}", category.getKey());
            WaitInfo info = category.getValue();
            for (Task task : info.waitingTasks) {
                rescheduleTask(task);
            }
        }
    }

    @Override
    public synchronized boolean addTask(final Task task, final long lastExecutionTime, final long eventTime) {

        assert task != null;
        log.debug("Adding task waiting for {} in container {}", task.getWaitCategory(), task.getContainerReference());

        WaitInfo info = getWaitInfoForTask(task);
        if (info == null) {
            // concurrent container destroy
            rescheduleTask(task);
            return false;
        }

        TransactionReference transaction = task.getTransactionReference();
        if (!isTransactionValid(transaction)) {
            // concurrent commit/rollback
            rescheduleTask(task);
            return false;
        }
        Long txTime = info.uncommittedTimes.get(transaction);
        // use Long.MIN_VALUE because timestamps can be negative (System.nanoTime)
        long lastEventTime = (txTime != null) ? txTime : Long.MIN_VALUE;
        if (info.lastCommittedTime > lastEventTime) {
            // commit after last event for this (active) tx
            lastEventTime = info.lastCommittedTime;
        }
        if (lastExecutionTime <= lastEventTime) {
            log.info("Concurrent event occurred, rescheduling task");
            rescheduleTask(task);
            return false;
        }
        info.uncommittedTimes.put(transaction, eventTime);
        info.waitingTasks.add(task);
        return true;
    }

    @Override
    public synchronized void removeTask(final Task task) {
        log.debug("Removing task from wait container");
        WaitInfo info = getWaitInfoForTask(task);
        if (info == null) {
            // concurrent container destroy
            return;
        }
        boolean removed = info.waitingTasks.remove(task);
        if (!removed) {
            log.info("Task not found in wait container");
        }
    }

    @Override
    public synchronized void processTransactionCommit(final TransactionReference txRef, final Transaction tx,
            final long eventTime) {
        assert txRef != null;
        log.debug("Processing commit of transaction {}", txRef);
        for (LocalContainerReference container : tx.getAccessedContainers()) {
            Map<WaitForCategory, WaitInfo> waitInfo = containers.get(container);
            if (waitInfo == null) {
                // for create container
                log.trace("No wait information for container {}", container);
                continue;
            }
            log.trace("Processing container {}", container);
            for (WaitForCategory category : WaitForCategory.values()) {
                log.trace("Processing category {}", category);
                WaitInfo info = waitInfo.get(category);
                finishTransaction(txRef, info, eventTime, true, true);
            }
        }
        finishTransaction(txRef, containerlessTasks, eventTime, true, true);
    }

    @Override
    public synchronized void processTransactionRollback(final TransactionReference txRef, final Transaction tx,
            final long eventTime) {
        assert txRef != null;
        log.debug("Processing rollback of transaction {}", txRef);
        for (LocalContainerReference container : tx.getAccessedContainers()) {
            Map<WaitForCategory, WaitInfo> waitInfo = containers.get(container);
            if (waitInfo == null) {
                log.trace("No wait information for container {}", container);
                continue;
            }
            log.trace("Processing container {}", container);
            finishTransaction(txRef, waitInfo.get(WaitForCategory.INSERT), eventTime, false, false);
            finishTransaction(txRef, waitInfo.get(WaitForCategory.REMOVE), eventTime, false, false);
            finishTransaction(txRef, waitInfo.get(WaitForCategory.UNLOCK_LT), eventTime, false, true);
            finishTransaction(txRef, waitInfo.get(WaitForCategory.UNLOCK_ST), eventTime, false, true);
        }
        finishTransaction(txRef, containerlessTasks, eventTime, false, true);
    }

    private void finishTransaction(final TransactionReference transaction, final WaitInfo info,
            final long eventTime, final boolean commit, final boolean rescheduleAllTasks) {
        long newLct = eventTime;
        Long uct = info.uncommittedTimes.get(transaction);
        if (uct != null) {
            // request inside the tx affected this WaitInfo
            if (uct > newLct) {
                // event for this tx after commit
                newLct = uct;
            }
            info.uncommittedTimes.remove(transaction);
            log.trace("Removed transaction timestamp");
            if (!rescheduleAllTasks) {
                log.trace("Will reschedule tasks of this transaction");
                rescheduleAndRemoveTasksInSameTransaction(info.waitingTasks, transaction);
            }
        }
        if (rescheduleAllTasks) {
            log.trace("Will reschedule all waiting tasks");
            rescheduleAndRemoveAllTasks(info.waitingTasks);
        }
        if (commit && newLct > info.lastCommittedTime) {
            // update timestamp for commit
            info.lastCommittedTime = newLct;
            log.trace("Set lastCommittedTime to {}", newLct);
        }
    }

    private void rescheduleAndRemoveAllTasks(final List<Task> tasks) {
        for (Task task : tasks) {
            rescheduleTask(task);
        }
        tasks.clear();
    }

    private void rescheduleAndRemoveTasksInSameTransaction(final List<Task> tasks,
            final TransactionReference transaction) {
        Iterator<Task> it = tasks.iterator();
        while (it.hasNext()) {
            Task task = it.next();
            if (!transaction.equals(task.getTransactionReference())) {
                // reschedule only tasks in same transaction
                continue;
            }
            rescheduleTask(task);
            it.remove();
        }
    }

    @Override
    public synchronized void processEvents(final Task task, final WaitForCategory[] categories, final long eventTime) {

        assert task != null;
        assert categories != null;

        TransactionReference transaction = task.getTransactionReference();
        if (transaction == null) {
            // implicit, already committed/rolled back transaction
            // the event process has already been performed in processTransactionCommit/Rollback
            return;
        }
        LocalContainerReference container = task.getContainerReference();
        log.debug("Processing events {} on container {}", categories, container);
        log.debug("Processing events for transaction {}", transaction);
        if (container == null) {
            // e.g. a lookup container request
            log.trace("Processing events on general wait information");
            updateTimestamps(containerlessTasks, transaction, eventTime);
            rescheduleAndRemoveTasksForEvent(containerlessTasks.waitingTasks, transaction, task, eventTime);
            return;
        }

        Map<WaitForCategory, WaitInfo> waitInfo = containers.get(container);
        if (waitInfo == null) {
            // concurrent container destroy
            log.debug("No wait information for container {} found", container);
            return;
        }
        for (WaitForCategory category : categories) {
            WaitInfo info = waitInfo.get(category);
            // update timestamps
            updateTimestamps(info, transaction, eventTime);
            // reschedule tasks
            rescheduleAndRemoveTasksForEvent(info.waitingTasks, transaction, task, eventTime);
        }
    }

    private void rescheduleAndRemoveTasksForEvent(final List<Task> tasks, final TransactionReference transaction,
            final Task finishedTask, final long eventTime) {
        Iterator<Task> it = tasks.iterator();
        while (it.hasNext()) {
            Task task = it.next();
            if (!transaction.equals(task.getTransactionReference())) {
                // reschedule only tasks in same transaction
                continue;
            }
            if (task == finishedTask) {
                // task cannot reschedule itself
                continue;
            }
            if (task.getLastExecutionTime() > eventTime) {
                // task executed after event (but blocking it overtook event processing)
                // this is a small optimization to avoid unnecessary rescheduling
                continue;
            }
            rescheduleTask(task);
            it.remove();
        }
    }

    // Updates the event timestamp for a category for the specified transaction.
    private void updateTimestamps(final WaitInfo info, final TransactionReference transaction, final long eventTime) {
        Long uct = info.uncommittedTimes.get(transaction);
        if (uct == null || eventTime > uct) {
            info.uncommittedTimes.put(transaction, eventTime);
        }
    }

    /**
     * Gets the {@link WaitInfo} for a specified task.
     *
     * @param task
     *            the task
     * @return the wait info for the specified task, <code>null</code> if none
     *         is found for the task's container (probably because of a
     *         concurrent container destroy)
     */
    private WaitInfo getWaitInfoForTask(final Task task) {
        assert task != null;

        LocalContainerReference container = task.getContainerReference();
        if (container == null) {
            // e.g. a lookup container task
            log.trace("No container specified, using general wait information");
            return containerlessTasks;
        } else {
            Map<WaitForCategory, WaitInfo> waitInfo = containers.get(container);
            if (waitInfo == null) {
                log.info("No wait information for container {} found", container);
                return null;
            }
            WaitForCategory category = task.getWaitCategory();
            return waitInfo.get(category);
        }
    }

    /**
     * Checks whether a transaction is valid.
     *
     * @param transaction
     *            the reference of the transaction
     * @return <code>true</code> if the transaction is valid, <code>false</code>
     *         otherwise (transaction was rollbacked/committed or timed out in
     *         the mean time)
     */
    private boolean isTransactionValid(final TransactionReference transaction) {
        log.trace("Checking validity of transaction " + transaction);
        try {
            if (txManager.getTransaction(transaction).isValid()) {
                // TODO use other method, something like Transaction.isActive
                return true;
            } else {
                log.debug("Transaction {} is invalid.", transaction);
                return false;
            }
        } catch (TransactionException ex) {
            log.debug("There is something wrong with transaction {}: {}", transaction, ex.toString());
            return false;
        } catch (MzsTimeoutException ex) {
            log.debug("Transaction {} timed out.", transaction);
            return false;
        }
    }

    /**
     * Reschedules a task.
     *
     * @param task
     *            the task to reschedule
     */
    private void rescheduleTask(final Task task) {
        if (task.canTimeOut()) {
            // dirty hack below (similar to the one in TransactionManager)
            ExpiringElement<Task> expTask = new ExpiringElement<Task>(task, 0);
            synchronized (task) {
                boolean elementRemoved = requestTimeoutProcessor.removeElement(expTask);
                if (!elementRemoved) {
                    log.info("Race-condition that may causes warning because of missing request future for {}",
                            task.getRequestReference());
                }
            }
        }
        if (lockedTaskHandler != null && task.getWaitCategory() == WaitForCategory.UNLOCK_LT) {
            lockedTaskHandler.removeTask(task);
        }
        if (rescheduleThreadPool != null) {
            // do not reschedule in same thread to avoid possible ConcurrentModificationExceptions
            rescheduleThreadPool.execute(new Runnable() {
                @Override
                public void run() {
                    requestHandler.rescheduleTask(task);
                }
            });
        } else {
            requestHandler.rescheduleTask(task);
        }
    }

    @Override
    public void shutdown() {
        if (rescheduleThreadPool != null) {
            rescheduleThreadPool.shutdown();
        }
    }

    /**
     * Data structure for storing the waiting tasks and event timestamps (for a
     * specific container and category).
     *
     * @author Tobias Doenz
     */
    private static class WaitInfo {

        // lastCommittedTime not set to 0 because timestamps can be negative (System.nanoTime)
        private long lastCommittedTime = Long.MIN_VALUE;
        private final Map<TransactionReference, Long> uncommittedTimes;
        private final List<Task> waitingTasks;

        public WaitInfo() {
            uncommittedTimes = new HashMap<TransactionReference, Long>();
            waitingTasks = new ArrayList<Task>();
        }

        @Override
        public String toString() {
            return "WaitInfo [lastCommittedTime=" + lastCommittedTime + ", uncommitted times for "
                    + uncommittedTimes.size() + " transactions, " + waitingTasks.size() + " waiting tasks";
        }

    }

}
