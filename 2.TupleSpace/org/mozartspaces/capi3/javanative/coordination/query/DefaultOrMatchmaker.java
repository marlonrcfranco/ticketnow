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

import org.mozartspaces.capi3.Matchmaker;
import org.mozartspaces.capi3.Matchmakers.OrMatchmaker;

/**
 * The Or Matchmaker evaluates to true if all Subsequent Matchmakers evaluate true.
 *
 * @author Martin Barisits
 */
public final class DefaultOrMatchmaker implements NativeMatchmaker {

    private final ArrayList<NativeMatchmaker> matchmakers;

    /**
     * Create a new OrMatchmaker.
     *
     * @param matchmaker
     *            to base on
     * @param filter
     */
    public DefaultOrMatchmaker(final OrMatchmaker matchmaker, final NativeFilter filter) {
        this.matchmakers = new ArrayList<NativeMatchmaker>();
        for (Matchmaker mm : matchmaker.getMatchmakers()) {
            this.matchmakers.add(DefaultMatchmakerFilter.transposeMatchmaker(mm, filter));
        }
    }

    @Override
    public boolean evaluate(final Serializable entry) {
        for (NativeMatchmaker matchmaker : matchmakers) {
            if (matchmaker.evaluate(entry)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String toString() {
        return "OR (" + matchmakers + ")";
    }

}
