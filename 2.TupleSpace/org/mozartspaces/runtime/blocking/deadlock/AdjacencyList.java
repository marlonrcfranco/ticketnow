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
package org.mozartspaces.runtime.blocking.deadlock;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.jcip.annotations.Immutable;

/**
 * Stores a graph as adjacency list.
 *
 * @author Tobias Doenz
 */
@Immutable
final class AdjacencyList {

    private final Map<Vertex, List<Edge>> adjacencies = new HashMap<Vertex, List<Edge>>();

    public Edge addEdge(final Vertex source, final Vertex target) {
        source.outdegree++;
        target.indegree++;
        List<Edge> list;
        if (!adjacencies.containsKey(source)) {
            list = new ArrayList<Edge>();
            adjacencies.put(source, list);
        } else {
            list = adjacencies.get(source);
        }
        Edge edge = new Edge(source, target);
        int index = list.indexOf(edge);
        if (index == -1) {
            list.add(edge);
        } else {
            list.get(index).incrementMultiplicity();
        }
        return edge;
    }

    public Edge removeEdge(final Vertex source, final Vertex target) {
        Edge edge = null;
        source.outdegree--;
        target.indegree--;
        if (adjacencies.containsKey(source)) {
            List<Edge> list = adjacencies.get(source);
            edge = new Edge(source, target);
            int index = list.indexOf(edge);
            edge = list.get(index);
            edge.decrementMultiplicity();
            if (edge.getMultiplicity() == 0) {
                list.remove(index);
            }
            if (list.size() == 0) {
                adjacencies.remove(source);
            }
        }
        return edge;
    }

    public List<Edge> getAdjacent(final Vertex source) {
        return adjacencies.get(source);
    }

    public Set<Vertex> getSourceNodes() {
        return adjacencies.keySet();
    }
}