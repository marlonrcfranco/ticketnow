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
 * The condition of an authorization rule.
 *
 * @author Stefan Crass
 */
public final class Condition implements Serializable {

    private static final long serialVersionUID = 1L;

    // TODO support logical combination of condition queries
    private final List<ConditionQuery> conditionQueries;

    /**
     * @param conditionQueries
     *            the list of condition queries, may be {@code null} or empty
     */
    public Condition(final List<ConditionQuery> conditionQueries) {
        this.conditionQueries = (conditionQueries == null) ? null : new ArrayList<ConditionQuery>(conditionQueries);
    }

    /**
     * @param conditionQueries
     *            the condition queries
     */
    public Condition(final ConditionQuery... conditionQueries) {
        this(Arrays.asList(conditionQueries));
    }

    /**
     * @return the condition queries
     */
    public List<ConditionQuery> getConditionQueries() {
        return Collections.unmodifiableList(conditionQueries);
    }

    @Override
    public String toString() {
        return "Condition [conditionQueries=" + conditionQueries + "]";
    }

}
