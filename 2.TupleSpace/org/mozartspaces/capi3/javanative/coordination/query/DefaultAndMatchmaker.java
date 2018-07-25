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
package org.mozartspaces.capi3.javanative.coordination.query;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.mozartspaces.capi3.Matchmaker;
import org.mozartspaces.capi3.Matchmakers.AndMatchmaker;

/**
 * The And Matchmaker evaluates to true if all Subsequent Matchmakers evaluate true.
 *
 * @author Martin Barisits
 */
public final class DefaultAndMatchmaker implements NativeMatchmaker {

    private final List<NativeMatchmaker> matchmakers;

    /**
     * Create a new AndMatchmaker.
     *
     * @param matchmaker
     *            to base on
     * @param filter
     */
    public DefaultAndMatchmaker(final AndMatchmaker matchmaker, final NativeFilter filter) {
        this.matchmakers = new ArrayList<NativeMatchmaker>();
        for (Matchmaker mm : matchmaker.getMatchmakers()) {
            this.matchmakers.add(DefaultMatchmakerFilter.transposeMatchmaker(mm, filter));
        }
    }

    @Override
    public boolean evaluate(final Serializable entry) {
        for (NativeMatchmaker matchmaker : this.matchmakers) {
            if (!matchmaker.evaluate(entry)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public String toString() {
        return "AND (" + matchmakers + ")";
    }

}
