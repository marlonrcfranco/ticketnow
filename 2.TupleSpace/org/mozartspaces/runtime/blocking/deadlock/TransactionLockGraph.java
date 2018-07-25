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
/**
 *
 */
package org.mozartspaces.runtime.blocking.deadlock;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.jcip.annotations.ThreadSafe;

import org.mozartspaces.runtime.tasks.Task;
import org.mozartspaces.util.LoggerFactory;
import org.slf4j.Logger;

/**
 * Creates a wait-for-graph of the requests (edges) and transactions (vertices).
 * Every time a task is added, the graph is searched for deadlocks (circles).
 * When a deadlock is found this is (only) logged.
 *
 * @author Tobias Doenz
 */
@ThreadSafe
public final class TransactionLockGraph implements LockedTaskHandler {

    private static final Logger log = LoggerFactory.get();

    // the graph as adjacency list
    private final AdjacencyList graph;

    // maps transaction ID to vertex for that transaction
    private final Map<String, Vertex> txToVertex;

    // maps task to transaction that blocked that task
    private final Map<Task, String> lockTransactions;

    // requests on same edge
    private final Map<Edge, List<Task>> requests;

    private List<List<Vertex>> cycles;

    /**
     * Constructs a new {@code TransactionLockGraph} instance.
     */
    public TransactionLockGraph() {
        graph = new AdjacencyList();
        txToVertex = new HashMap<String, Vertex>();
        lockTransactions = new HashMap<Task, String>();
        requests = new HashMap<Edge, List<Task>>();
    }

    @Override
    public void addTask(final Task task, final String lockTx) {
        log.debug("Adding task to graph: {}", task.getRequestReference());
        lockTransactions.put(task, lockTx);
        String requestTx = task.getTransactionReference().getId();
        Vertex source = txToVertex.get(requestTx);
        if (source == null) {
            source = new Vertex(requestTx);
            txToVertex.put(requestTx, source);
        }
        Vertex target = txToVertex.get(lockTx);
        if (target == null) {
            target = new Vertex(lockTx);
            txToVertex.put(lockTx, target);
        }

        Edge edge = graph.addEdge(source, target);
        List<Task> edgeRequests = requests.get(edge);
        if (edgeRequests == null) {
            edgeRequests = new ArrayList<Task>();
        }
        edgeRequests.add(task);
        requests.put(edge, edgeRequests);

        int numDeadLocks = findDeadlocks();
        if (numDeadLocks > 0) {
            log.warn("Found {} deadlock(s)", cycles.size());
        }
    }

    @Override
    public void removeTask(final Task task) {
        log.debug("Removing task from graph: {}", task.getRequestReference());
        String requestTx = task.getTransactionReference().getId();
        String lockTx = lockTransactions.get(task);

        Vertex source = txToVertex.get(requestTx);
        Vertex target = txToVertex.get(lockTx);
        if (source.degree() == 0) {
            txToVertex.remove(requestTx);
        }
        if (target.degree() == 0) {
            txToVertex.remove(lockTx);
        }

        Edge edge = graph.removeEdge(source, target);
        List<Task> edgeRequests = requests.get(edge);
        edgeRequests.remove(task);
        if (edgeRequests.isEmpty()) {
            requests.remove(edge);
        }
    }

    private synchronized int findDeadlocks() {
        cycles = new CycleSearcher().findCycles(graph);
        return cycles.size();
    }

}
