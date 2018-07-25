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
package org.mozartspaces.core.authorization;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * The scope of an authorization rule.
 *
 * @author Stefan Crass
 */
public final class Scope implements Serializable {

    private static final long serialVersionUID = 1L;

    // TODO support logical combination of scope queries
    private final List<ScopeQuery> scopeQueries;

    /**
     * @param scopeQueries
     *            the list of scope queries, may be {@code null} or empty
     */
    public Scope(final List<ScopeQuery> scopeQueries) {
        this.scopeQueries = (scopeQueries == null) ? null : new ArrayList<ScopeQuery>(scopeQueries);
    }

    /**
     * @param scopeQueries
     *            the scope queries
     */
    public Scope(final ScopeQuery... scopeQueries) {
        this(Arrays.asList(scopeQueries));
    }

    /**
     * @return the scope queries
     */
    public List<ScopeQuery> getScopeQueries() {
        return Collections.unmodifiableList(scopeQueries);
    }

    @Override
    public String toString() {
        return "Scope [scopeQueries=" + scopeQueries + "]";
    }

}
