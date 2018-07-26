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
package org.mozartspaces.capi3;

import java.util.Arrays;

/**
 * Some pre-defined Matchmakers.
 *
 * @author Martin Barisits
 */
public final class Matchmakers {

    private Matchmakers() {
    }

    /**
     * AND-Connect a collection of Matchmakers.
     *
     * @param matchmakers
     *            to AND-Connect
     * @return an AND-Matchmaker
     */
    public static Matchmaker and(final Matchmaker... matchmakers) {
        return new AndMatchmaker(matchmakers);
    }

    /**
     * OR-Connect a collection of Matchmakers.
     *
     * @param matchmakers
     *            to OR-Connect
     * @return an OR-Matchmaker
     */
    public static Matchmaker or(final Matchmaker... matchmakers) {
        return new OrMatchmaker(matchmakers);
    }

    /**
     * NOT-Connect a collection of Matchmakers.
     *
     * @param matchmaker
     *            to NOT-Connect
     * @return a NOT-Matchmaker
     */
    public static Matchmaker not(final Matchmaker matchmaker) {
        return new NotMatchmaker(matchmaker);
    }

    /**
     * The And Matchmaker evaluates to true if all Subsequent Matchmakers
     * evaluate true.
     *
     * @author Martin Barisits
     */
    public static final class AndMatchmaker implements Matchmaker {

        private static final long serialVersionUID = 1L;

        private final Matchmaker[] matchmakers;

        /**
         * @return the matchmakers
         */
        public Matchmaker[] getMatchmakers() {
            return matchmakers;
        }

        private AndMatchmaker(final Matchmaker... matchmakers) {
            this.matchmakers = matchmakers;
        }

        // for serialization
        private AndMatchmaker() {
            this.matchmakers = null;
        }

        @Override
        public String toString() {
            return "AndMatchmaker [matchmakers=" + Arrays.toString(matchmakers) + "]";
        }

    }

    /**
     * The Or Matchmaker evaluates to true if all Subsequent Matchmakers
     * evaluate true.
     *
     * @author Martin Barisits
     */
    public static final class OrMatchmaker implements Matchmaker {

        private static final long serialVersionUID = 1L;

        private final Matchmaker[] matchmakers;

        /**
         * @return the matchmakers
         */
        public Matchmaker[] getMatchmakers() {
            return matchmakers;
        }

        private OrMatchmaker(final Matchmaker... matchmakers) {
            this.matchmakers = matchmakers;
        }

        // for serialization
        private OrMatchmaker() {
            this.matchmakers = null;
        }

        @Override
        public String toString() {
            return "OrMatchmaker [matchmakers=" + Arrays.toString(matchmakers) + "]";
        }

    }

    /**
     * The Not Matchmaker evaluates to true if the encapsulated Matchmaker
     * evaluates to false.
     *
     * @author Martin Barisits
     */
    public static final class NotMatchmaker implements Matchmaker {

        private static final long serialVersionUID = 1L;

        private final Matchmaker matchmaker;

        /**
         * @return the matchmaker
         */
        public Matchmaker getMatchmaker() {
            return matchmaker;
        }

        private NotMatchmaker(final Matchmaker matchmaker) {
            this.matchmaker = matchmaker;
        }

        // for serialization
        private NotMatchmaker() {
            this.matchmaker = null;
        }

        @Override
        public String toString() {
            return "NotMatchmaker [matchmaker=" + matchmaker + "]";
        }

    }
}
