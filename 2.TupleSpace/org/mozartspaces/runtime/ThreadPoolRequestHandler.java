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

import java.io.Serializable;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

import net.jcip.annotations.ThreadSafe;

import org.mozartspaces.core.Request;
import org.mozartspaces.core.RequestMessage;
import org.mozartspaces.core.requests.AddAspectRequest;
import org.mozartspaces.core.requests.ClearSpaceRequest;
import org.mozartspaces.core.requests.CommitTransactionRequest;
import org.mozartspaces.core.requests.CreateContainerRequest;
import org.mozartspaces.core.requests.CreateTransactionRequest;
import org.mozartspaces.core.requests.DeleteEntriesRequest;
import org.mozartspaces.core.requests.DestroyContainerRequest;
import org.mozartspaces.core.requests.LockContainerRequest;
import org.mozartspaces.core.requests.LookupContainerRequest;
import org.mozartspaces.core.requests.MetaModelRequest;
import org.mozartspaces.core.requests.PrepareTransactionRequest;
import org.mozartspaces.core.requests.ReadEntriesRequest;
import org.mozartspaces.core.requests.RemoveAspectRequest;
import org.mozartspaces.core.requests.RollbackTransactionRequest;
import org.mozartspaces.core.requests.ShutdownRequest;
import org.mozartspaces.core.requests.TakeEntriesRequest;
import org.mozartspaces.core.requests.TestEntriesRequest;
import org.mozartspaces.core.requests.WriteEntriesRequest;
import org.mozartspaces.runtime.tasks.AddAspectTask;
import org.mozartspaces.runtime.tasks.ClearSpaceTask;
import org.mozartspaces.runtime.tasks.CommitTransactionTask;
import org.mozartspaces.runtime.tasks.CreateContainerTask;
import org.mozartspaces.runtime.tasks.CreateTransactionTask;
import org.mozartspaces.runtime.tasks.DeleteEntriesTask;
import org.mozartspaces.runtime.tasks.DestroyContainerTask;
import org.mozartspaces.runtime.tasks.LockContainerTask;
import org.mozartspaces.runtime.tasks.LookupContainerTask;
import org.mozartspaces.runtime.tasks.MetaModelTask;
import org.mozartspaces.runtime.tasks.PrepareTransactionTask;
import org.mozartspaces.runtime.tasks.ReadEntriesTask;
import org.mozartspaces.runtime.tasks.RemoveAspectTask;
import org.mozartspaces.runtime.tasks.RollbackTransactionTask;
import org.mozartspaces.runtime.tasks.ShutdownTask;
import org.mozartspaces.runtime.tasks.TakeEntriesTask;
import org.mozartspaces.runtime.tasks.Task;
import org.mozartspaces.runtime.tasks.TestEntriesTask;
import org.mozartspaces.runtime.tasks.WriteEntriesTask;
import org.mozartspaces.util.LoggerFactory;
import org.slf4j.Logger;

/**
 * A Request Container that uses a thread pool (executor service) to execute
 * requests tasks. The creation of tasks for requests and their execution is
 * handled inside this class, so the Core Processor (XP) is actually the
 * executor service and partly integrated into this Request Container.
 *
 * @author Tobias Doenz
 */
@ThreadSafe
public final class ThreadPoolRequestHandler implements RequestHandler {

    private static final Logger log = LoggerFactory.get();

    private final ExecutorService threadPool;

    private volatile RuntimeData runtimeData;

    /**
     * Constructs a <code>ThreadPoolRequestHandler</code>.
     *
     * @param threadPool
     *            the thread pool that executes the request tasks
     */
    public ThreadPoolRequestHandler(final ExecutorService threadPool) {
        this.threadPool = threadPool;
        assert this.threadPool != null;
    }

    /**
     * Sets the runtime data.
     *
     * @param runtimeData
     *            the runtime data
     */
    public void setRuntimeData(final RuntimeData runtimeData) {
        this.runtimeData = runtimeData;
        assert this.runtimeData != null;
    }

    @Override
    public void processRequest(final RequestMessage requestMessage) {
        assert requestMessage != null;
        // log.debug("Getting task for request {}",
        // requestMessage.getRequestReference());

        Task task = getTaskForRequestMessage(requestMessage);
        threadPool.execute(task);
    }

    @Override
    public void rescheduleTask(final Task task) {
        assert task != null;
        log.debug("Rescheduling task for request {}", task.getRequestReference());
        threadPool.execute(task);
    }

    @Override
    public void shutdown(final boolean wait) {
        log.debug("Shutting down the Core Processor");
        // TODO purge wait container etc.
        if (runtimeData != null) {
            runtimeData.shutdown();
        }
        threadPool.shutdown();
        if (wait) {
            try {
                threadPool.awaitTermination(120, TimeUnit.SECONDS);
                // TODO improve (see JCiP, TcpSocketReceiver)
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            List<Runnable> queuedTasks = threadPool.shutdownNow();
            if (queuedTasks != null && !queuedTasks.isEmpty()) {
                log.error("Stopped XP thread pool, {} tasks were still waiting", queuedTasks.size());
            }
        }
        if (runtimeData != null) {
            runtimeData.getCapi3().shutDown();
        }
    }

    @SuppressWarnings("rawtypes")
    private <R extends Serializable> Task getTaskForRequestMessage(final RequestMessage requestMessage) {
        Request<?> request = requestMessage.getContent();

        // entries requests
        if (request instanceof ReadEntriesRequest<?>) {
            return new ReadEntriesTask(requestMessage, runtimeData);
        }
        if (request instanceof TakeEntriesRequest<?>) {
            return new TakeEntriesTask(requestMessage, runtimeData);
        }
        if (request instanceof WriteEntriesRequest) {
            return new WriteEntriesTask(requestMessage, runtimeData);
        }
        if (request instanceof TestEntriesRequest) {
            return new TestEntriesTask(requestMessage, runtimeData);
        }
        if (request instanceof DeleteEntriesRequest) {
            return new DeleteEntriesTask(requestMessage, runtimeData);
        }

        // transaction requests
        if (request instanceof CreateTransactionRequest) {
            return new CreateTransactionTask(requestMessage, runtimeData);
        }
        if (request instanceof CommitTransactionRequest) {
            return new CommitTransactionTask(requestMessage, runtimeData);
        }
        if (request instanceof RollbackTransactionRequest) {
            return new RollbackTransactionTask(requestMessage, runtimeData);
        }
        if (request instanceof PrepareTransactionRequest) {
            return new PrepareTransactionTask(requestMessage, runtimeData);
        }

        // container requests
        if (request instanceof CreateContainerRequest) {
            return new CreateContainerTask(requestMessage, runtimeData);
        }
        if (request instanceof LookupContainerRequest) {
            return new LookupContainerTask(requestMessage, runtimeData);
        }
        if (request instanceof DestroyContainerRequest) {
            return new DestroyContainerTask(requestMessage, runtimeData);
        }
        if (request instanceof LockContainerRequest) {
            return new LockContainerTask(requestMessage, runtimeData);
        }

        // aspect requests
        if (request instanceof AddAspectRequest) {
            return new AddAspectTask(requestMessage, runtimeData);
        }
        if (request instanceof RemoveAspectRequest) {
            return new RemoveAspectTask(requestMessage, runtimeData);
        }

        // other requests
        if (request instanceof MetaModelRequest) {
            return new MetaModelTask(requestMessage, runtimeData);
        }
        if (request instanceof ClearSpaceRequest) {
            return new ClearSpaceTask(requestMessage, runtimeData);
        }
        if (request instanceof ShutdownRequest) {
            return new ShutdownTask(requestMessage, runtimeData);
        }

        throw new IllegalArgumentException("Unknown request type " + request.getClass().getName());

    }

}