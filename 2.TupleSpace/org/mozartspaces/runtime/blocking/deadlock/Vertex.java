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

import net.jcip.annotations.NotThreadSafe;

/**
 * Represents a vertex with an indegree and an outdegree. This class also has
 * fields for Tarjan's algorithm to find strongly connected components.
 *
 * @author Tobias Doenz
 */
@NotThreadSafe
final class Vertex {

    // CHECKSTYLE:OFF (deactivate warnings that fields are not private)

    private final Object id;
    int indegree;
    int outdegree;

    // TODO? move to CycleSearcher, use unique integer id for tarjan
    // for Tarjan's algorithm
    int lowlink = -1;
    int number = -1;
    boolean onStack = false;

    public Vertex(final Object id) {
        this.id = id;
    }

    public Object getId() {
        return id;
    }

    public int degree() {
        return indegree + outdegree;
    }

    @Override
    public String toString() {
        return id.toString();
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Vertex other = (Vertex) obj;
        if ((this.id == null) ? (other.id != null) : !this.id.equals(other.id)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 97 * hash + (this.id != null ? this.id.hashCode() : 0);
        return hash;
    }

}
