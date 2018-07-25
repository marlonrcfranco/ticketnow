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
import java.util.Collections;
import java.util.List;

import net.jcip.annotations.Immutable;

/**
 * Class that searches for cycles in a graph represented by an {@code
 * AdjacencyList}. Tarjan's algorithm to find strongly connected components
 * (SCC) is used for finding cycles, SCCs with more than one vertex.
 *
 * @author Tobias Doenz
 */
@Immutable
final class CycleSearcher {

    private int index = 0;

    private final List<Vertex> stack = new ArrayList<Vertex>();
    private final List<List<Vertex>> cycles = new ArrayList<List<Vertex>>();

    public List<List<Vertex>> findCycles(final AdjacencyList graph) {
        for (Vertex v : graph.getSourceNodes()) {
            if (v.number == -1) {
                tarjan(v, graph);
            }
        }
        return cycles;
    }

    private void tarjan(final Vertex v, final AdjacencyList list) {
        v.number = index;
        v.lowlink = index;
        index++;
        stack.add(v); // push stack
        v.onStack = true;
        if (list.getAdjacent(v) != null) {
            for (Edge e : list.getAdjacent(v)) {
                Vertex t = e.getTo();
                if (t.number == -1) {
                    tarjan(t, list);
                    v.lowlink = Math.min(v.lowlink, t.lowlink);
                } else if (t.onStack) {
                    v.lowlink = Math.min(v.lowlink, t.number);
                }
            }
        }
        if (v.lowlink == v.number) {
            Vertex t;
            ArrayList<Vertex> component = new ArrayList<Vertex>();
            do {
                t = stack.remove(stack.size() - 1); // pop stack
                t.onStack = false;
                component.add(t);
            } while (t != v);
            // added to original SCC algorithm
            // store only non-trivial components
            if (component.size() > 1) {
                Collections.reverse(component);
                cycles.add(component);
            }
        }
    }
}
