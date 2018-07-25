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

import java.io.Serializable;
import java.util.Set;

import net.jcip.annotations.NotThreadSafe;

import org.mozartspaces.capi3.ContainerLockedException;
import org.mozartspaces.capi3.CoordinatorLockedException;
import org.mozartspaces.capi3.EntryLockedException;
import org.mozartspaces.capi3.InvalidTransactionException;
import org.mozartspaces.capi3.LocalContainerReference;
import org.mozartspaces.capi3.OperationResult;
import org.mozartspaces.capi3.OperationStatus;
import org.mozartspaces.capi3.Transaction;
import org.mozartspaces.core.MzsConstants.RequestTimeout;
import org.mozartspaces.core.MzsCoreException;
import org.mozartspaces.core.MzsCoreRuntimeException;
import org.mozartspaces.core.MzsTimeoutException;
import org.mozartspaces.core.Request;
import org.mozartspaces.core.RequestMessage;
import org.mozartspaces.core.RequestReference;
import org.mozartspaces.core.TransactionReference;
import org.mozartspaces.core.requests.CreateContainerRequest;
import org.mozartspaces.core.requests.DestroyContainerRequest;
import org.mozartspaces.core.requests.EntriesRequest;
import org.mozartspaces.runtime.ResponseDistributor;
import org.mozartspaces.runtime.RuntimeData;
import org.mozartspaces.runtime.blocking.ExpiringElement;
import org.mozartspaces.runtime.blocking.TimeoutProcessor;
import org.mozartspaces.runtime.blocking.WaitAndEventManager;
import org.mozartspaces.runtime.blocking.WaitForCategory;
import org.mozartspaces.runtime.blocking.WaitManagerCreateContainerLogItem;
import org.mozartspaces.runtime.blocking.WaitManagerDestroyContainerLogItem;
import org.mozartspaces.runtime.blocking.deadlock.LockedTaskHandler;
import org.mozartspaces.util.LoggerFactory;
import org.slf4j.Logger;

/**
 * The implementation of generic functionality used in all <code>Task</code>s.
 *
 * @author Tobias Doenz
 *
 * @param <R>
 *            the result type of the request that is processed by this task
 */
@NotThreadSafe
public abstract class AbstractTask<R extends Serializable> implements Task {

    private static final Logger log = LoggerFactory.get();

    private final RequestMessage requestMessage;
    // from the request message
    private final RequestReference requestRef;
    private final Request<?> request;

    // references from RuntimeData
    private final ResponseDistributor responseDistributor;
    private final TimeoutProcessor<Task> requestTimeoutProcessor;
    private final WaitAndEventManager waitEventManager;
    private final LockedTaskHandler lockedTaskHandler;

    // timeout and execution data
    private int executionCount;
    private long timestamp;
    private long timeout;
    private long expireTime;
    private long lastExecutionTime;

    // wait and event data set in transactional task or concrete task implementation
    private OperationResult capi3Result;
    private LocalContainerReference container;
    private TransactionReference txRef;
    private Transaction tx;
    private WaitForCategory waitCategory;
    private WaitForCategory[] eventCategories;
    private Set<WaitForCategory> aspectEventCategories;

    private R result;

    protected AbstractTask(final RequestMessage requestMessage, final RuntimeData runtimeData) {
        this.requestMessage = requestMessage;
        this.requestRef = requestMessage.getRequestReference();
        this.request = requestMessage.getContent();

        this.responseDistributor = runtimeData.getResponseDistributor();
        this.requestTimeoutProcessor = runtimeData.getRequestTimeoutProcessor();
        this.waitEventManager = runtimeData.getWaitEventManager();
        this.lockedTaskHandler = runtimeData.getLockedTaskHandler();
    }

    @Override
    public final void run() {
        log.debug("Executing {} for request {}", this.getClass().getSimpleName(), requestRef);

        long executionTime = System.nanoTime();
        boolean timedOut = checkRequestTimeout(executionTime);
        if (timedOut) {
            // error message already sent in checkRequestTimeout
            return;
        }

        executionCount++;
        lastExecutionTime = executionTime;

        boolean processingDone = processRequest();
        addOrRemoveContainer();
        if (txRef == null) {
            // optimization: event timestamp not necessary in this case
            // implicit tx, already committed and event processing done
            return;
        }
        long eventTime = System.nanoTime();
        if (!processingDone) {
            blockTask(eventTime);
        }
        triggerEvents(eventTime);
    }

    /**
     * Performs request specific processing.
     *
     * @return the request result, may be <code>null</code> only when the
     *         request should be blocked (status LOCKED or DELAYABLE)
     * @throws Throwable
     *             any exception that should be propagated to the user
     */
    protected abstract R runSpecific() throws Throwable;

    /**
     * Sets the expire time with the request timeout as "offset" to the initial
     * timestamp. This method is used by tasks that have a timeout and can be
     * put into the wait container. It is not used by
     * <code>CreateContainerTask</code>.
     *
     * @param timeoutInMillis
     */
    protected final void setTimeoutAndExpireTime(final long timeoutInMillis) {
        if (executionCount > 1) {
            // set timeout only at first execution (small optimization)
            return;
        }
        timeout = timeoutInMillis;
        if (timeoutInMillis >= 0) {
            expireTime = timestamp + timeoutInMillis * 1000000;
        }
    }

    /**
     * Sets a CAPI3 operation result. This method is used to set the operation
     * result for the event processing.
     *
     * @param capi3Result
     *            the result of the CAPI3 operation
     */
    protected final void setCapi3Result(final OperationResult capi3Result) {
        this.capi3Result = capi3Result;
    }

    /**
     * Sets data that is used for the event processing and blocking this task.
     *
     * @param container
     *            the container reference
     * @param waitCategory
     *            the category in the wait container where this task should be
     *            put into
     */
    protected final void setContainerAndWaitData(final LocalContainerReference container,
            final WaitForCategory waitCategory) {
        this.container = container;
        this.waitCategory = waitCategory;
    }

    /**
     * Sets the event categories.
     *
     * @param eventCategories the categories where events should be triggered
     */
    protected final void setEventCategories(final WaitForCategory[] eventCategories) {
        this.eventCategories = eventCategories;
    }

    protected final void setAspectEventCategories(final Set<WaitForCategory> eventCategories) {
        this.aspectEventCategories = eventCategories;
    }

    /**
     * Sets the reference of the active transaction of this task.
     *
     * @param txRef
     *            the transaction reference, may be <code>null</code>
     */
    protected final void setTransactionData(final TransactionReference txRef, final Transaction tx) {
        this.txRef = txRef;
        this.tx = tx;
    }

    /**
     * Gets the execution count. This method is used in several concrete tasks
     * to pass the execution count to the aspects.
     *
     * @return the execution count, that is, the number of processings of this
     *         request
     */
    protected final int getExecutionCount() {
        return executionCount;
    }

    protected final void handleSpecialResult(final OperationStatus status, final Throwable cause, final long timeout)
            throws Throwable {
        log.debug("Cause for status {}: {}", status, cause);
        switch (status) {
        case NOTOK:
            if (cause == null) {
                throw new MzsCoreException("Status NOTOK");
            } else {
                throw cause;
            }
        case LOCKED:
        case DELAYABLE:
            if (timeout == RequestTimeout.ZERO) {
                throw cause;
            }
            if (timeout == RequestTimeout.TRY_ONCE && status == OperationStatus.DELAYABLE) {
                throw cause;
            }
            if (timeout > 0) {
                setTimeoutAndExpireTime(timeout);
            }
            break;
        default:
            throw new RuntimeException("Invalid status " + status);
        }
    }

    /**
     * Tries to processes the request and distributes the answer if the request
     * processing is completed.
     *
     * @return <code>true</code>, if the processing of the request is completed,
     *         <code>false</code> otherwise
     */
    private boolean processRequest() {
        Throwable error = null;
        boolean processingDone = false;
        try {
            result = runSpecific();
        } catch (MzsCoreException ex) {
            // probably re-thrown ex from within the tx or (handled) tx ex
            log.debug("MzsCoreException during task execution: {}", ex.toString());
            error = ex;
        } catch (MzsCoreRuntimeException ex) {
            log.info("Exception during task execution: {}", ex.toString());
            error = ex;
        } catch (Throwable ex) {
            log.error("Non-MS Exception during task execution", ex);
            error = ex;
        } finally {
            if (result != null || error != null) {
                // OK, NOTOK (or other exception)
                responseDistributor.distributeAnswer(requestMessage, result, error);
                processingDone = true;
            }
            // else: block task
        }
        return processingDone;
    }

    /**
     * Checks whether the request timed out. If this is the case, an error
     * answer is sent. On the first execution of a task the timestamp property
     * is set to the passed <code>executionTime</code>.
     *
     * @param executionTime
     *            the time when the execution of the task started (current time
     *            from {@link System#nanoTime()})
     * @return <code>true</code>, if the request timed out, <code>false</code>
     *         otherwise
     */
    private boolean checkRequestTimeout(final long executionTime) {
        if (executionCount == 0) {
            // set timestamp on first execution (not when rescheduled)
            timestamp = executionTime;
        } else {
            log.debug("Rescheduled task, already executed {}x, last {} nanos ago", executionCount, executionTime
                    - lastExecutionTime);
            // check timeout for rescheduled task (only "normal" timeout values > 0)
            if (executionTime >= expireTime && timeout > 0) {
                log.debug("Request timeout detected, setting error");
                Exception error = new MzsTimeoutException("Request timed out");
                responseDistributor.distributeAnswer(requestMessage, null, error);
                return true;
            }
        }
        return false;
    }

    /**
     * Triggers events for entry and container requests (with CAPI3 result).
     * Note: events for transaction requests (commit and rollback) are triggered
     * in the TransactionManager.
     *
     * @param eventTime
     *            the timestamp for the events
     */
    private void triggerEvents(final long eventTime) {
        if (capi3Result == null) {
            return;
        }
        OperationStatus status = capi3Result.getStatus();
        log.debug("Trigger events for request {} and status {}", requestRef, status);

        if (aspectEventCategories != null) {
            // set is either null or contains UNLOCK_ST and other categories
            if (eventCategories != null) {
                for (WaitForCategory category : eventCategories) {
                    aspectEventCategories.add(category);
                }
            }
            eventCategories = aspectEventCategories.toArray(new WaitForCategory[aspectEventCategories.size()]);
        } else if (eventCategories == null) {
            // use UNLOCK_ST, if no other categories where set in the task
            eventCategories = new WaitForCategory[] {WaitForCategory.UNLOCK_ST};
        }

        if (request instanceof EntriesRequest<?>) {
            // fast path for entry requests
            waitEventManager.processEvents(this, eventCategories, eventTime);
            return;
        }

        // standard event processing
        waitEventManager.processEvents(this, eventCategories, eventTime);
    }

    private void addOrRemoveContainer() {
        if (result == null || capi3Result == null) {
            return;
        }
        // adding/removing container information
        if (request instanceof CreateContainerRequest) {
            if (capi3Result.getStatus() == OperationStatus.OK) {
                waitEventManager.addContainer(container);
                if (tx != null) {
                    // removeContainer should be called on Tx rollback
                    // TODO check that Tx is active (inside addLog?)
                    try {
                        tx.addLog(new WaitManagerCreateContainerLogItem(waitEventManager, container));
                    } catch (InvalidTransactionException ex) {
                        log.warn("Could not add log item to clean up in WaitAndEventManager on rollback");
                    }
                }
                // set to "null" to process events on null container to wake up lookups
                container = null;
            }
        } else if (request instanceof DestroyContainerRequest) {
            if (capi3Result.getStatus() == OperationStatus.OK) {
                // garbage collection for destroy container
                if (tx == null) {
                    waitEventManager.removeContainer(container);
                } else {
                    // should be done on Tx commit
                    // TODO check that Tx is active (inside addLog?)
                    try {
                        tx.addLog(new WaitManagerDestroyContainerLogItem(waitEventManager, container));
                    } catch (InvalidTransactionException ex) {
                        log.warn("Could not add log item to clean up in WaitAndEventManager on commit");
                    }
                }
            }
        }
    }

    /**
     * Blocks a task, that is, adds it to the WaitAndEventManager
     * ("wait container"). If the task has a timeout, it is also added to the
     * timeout processor for requests.
     *
     * @param eventTime
     *            the timestamp for adding the task
     */
    private void blockTask(final long eventTime) {
        log.debug("Blocking task for request {}", requestRef);

        if (capi3Result == null) {
            String message = "Cannot block task without CAPI-3 result";
            log.error(message);
            throw new IllegalStateException(message);
        }

        // prepare short/long-term lock status
        if (capi3Result.getStatus() == OperationStatus.LOCKED) {
            String lockTx = null;
            String lockSubTx = null;
            Throwable ex = capi3Result.getCause();
            if (ex instanceof EntryLockedException) {
                lockTx = ((EntryLockedException) capi3Result.getCause()).getTxId();
                lockSubTx = ((EntryLockedException) capi3Result.getCause()).getSubTxId();
            } else if (ex instanceof ContainerLockedException) {
                lockTx = ((ContainerLockedException) capi3Result.getCause()).getTxId();
                lockSubTx = ((ContainerLockedException) capi3Result.getCause()).getSubTxId();
            } else if (ex instanceof CoordinatorLockedException) {
                lockTx = ((CoordinatorLockedException) capi3Result.getCause()).getTxId();
                lockSubTx = ((CoordinatorLockedException) capi3Result.getCause()).getSubTxId();
                // TODO refactor locked exceptions (common super class)
            } else {
                responseDistributor.distributeAnswer(requestMessage, null,
                        new MzsCoreRuntimeException("Invalid exception for locked task", ex));
                return;
            }
            if (lockSubTx == null) {
                waitCategory = WaitForCategory.UNLOCK_LT;
                if (lockedTaskHandler != null) {
                    // for deadlock detection
                    lockedTaskHandler.addTask(this, lockTx);
                }
            } else {
                waitCategory = WaitForCategory.UNLOCK_ST;
            }
        }

        synchronized (this) {
            // add task to wait "container"
            boolean taskAdded = waitEventManager.addTask(this, lastExecutionTime, eventTime);
            // add task to timeout processor
            if (taskAdded && canTimeOut()) {
                // add only tasks with "normal" timeout to timeout processor
                log.debug("Request {} expiring in {} ms", requestRef, (long) ((expireTime - System.nanoTime()) / 1e6));
                ExpiringElement<Task> expTask = new ExpiringElement<Task>(this, expireTime);
                requestTimeoutProcessor.addElement(expTask);
            }
        }
        /**
         * Note: It can happen in situations with high concurrency that a task is added to the wait container (see
         * above) and rescheduled there before it can be added to the Request-TP. When a task is rescheduled, it is
         * tried to remove a task from the Request-TP before it is rescheduled, which is not possible in this case. Thus
         * it can happen that a task is executed and in the Request-TP at the same time, which should not occur in
         * general. This causes "only" a warning in the log when the task times out and no wrong behavior. Therefore
         * this situation is not prevented. The simple approach to solve this by synchronizing on
         * requestTimeoutProcessor above and in SynchronizedWaitAndEventManager (method rescheduleTask) causes sometimes
         * deadlocks, locking the task as above is better but still not guaranteed deadlock-free!
         */

    }

    @Override
    public final RequestReference getRequestReference() {
        return requestRef;
    }

    @Override
    public final long getLastExecutionTime() {
        return lastExecutionTime;
    }

    // methods below used in the SynchronizedWaitAndEventManager
    @Override
    public final TransactionReference getTransactionReference() {
        return txRef;
    }

    @Override
    public final LocalContainerReference getContainerReference() {
        return container;
    }

    @Override
    public final WaitForCategory getWaitCategory() {
        return waitCategory;
    }

    @Override
    public final boolean canTimeOut() {
        return timeout > 0;
    }

}
