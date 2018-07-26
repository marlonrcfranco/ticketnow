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

import static org.mozartspaces.core.MzsConstants.Selecting.checkCount;

import java.io.Serializable;

/**
 * Random Coordinator.
 *
 * @author Martin Barisits
 * @author Tobias Doenz
 */
public final class RandomCoordinator implements ImplicitCoordinator {

    private static final long serialVersionUID = 1998517373971901105L;

    /**
     * Default Coordinator Name.
     */
    public static final String DEFAULT_NAME = "RandomCoordinator";

    private final String name;

    /**
     * Creates a new RandomCoordinator.
     *
     * @param name
     *            of the RandomCoordinator
     */
    public RandomCoordinator(final String name) {
        this.name = name;
    }

    /**
     * Creates a new RandomCoordinator.
     */
    public RandomCoordinator() {
        this(DEFAULT_NAME);
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        RandomCoordinator other = (RandomCoordinator) obj;
        if (name == null) {
            if (other.name != null) {
                return false;
            }
        } else if (!name.equals(other.name)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "RandomCoordinator [name=" + name + "]";
    }

    /**
     * Creates a new coordination data object.
     *
     * @param name
     *            the name of the Random Coordinator.
     * @return the created coordination data object
     */
    public static RandomData newCoordinationData(final String name) {
        return new RandomData(name);
    }

    /**
     * Creates a new coordination data object for the Random Coordinator with
     * the default name.
     *
     * @return the created coordination data object
     */
    public static RandomData newCoordinationData() {
        return new RandomData(DEFAULT_NAME);
    }

    /**
     * Returns a RandomSelector.
     *
     * @param count
     *            Entry count of this Selector
     * @param name
     *            name of the Coordinator this Selector should be associated to
     * @return RandomSelector
     */
    public static RandomSelector newSelector(final int count, final String name) {
        return new RandomSelector(count, name);
    }

    /**
     * Returns a RandomSelector with default name.
     *
     * @param count
     *            Entry count of this Selector
     * @return RandomSelector
     */
    public static RandomSelector newSelector(final int count) {
        return new RandomSelector(count, DEFAULT_NAME);
    }

    /**
     * Returns a RandomSelector with count 1 and the default name.
     *
     * @return RandomSelector
     */
    public static RandomSelector newSelector() {
        return new RandomSelector(1, DEFAULT_NAME);
    }

    /**
     * Common properties of the Random Coordinator, used for writing and reading
     * entries.
     *
     * @author Tobias Doenz
     */
    abstract static class RandomProperties implements Serializable {

        private static final long serialVersionUID = 1L;

        private final String name;

        protected RandomProperties(final String name) {
            this.name = name;
        }

        public String getName() {
            return this.name;
        }

    }

    /**
     * The coordination properties that are used for writing entries with the
     * Random coordinator.
     *
     * @author Tobias Doenz
     */
    public static final class RandomData extends RandomProperties implements CoordinationData {

        private static final long serialVersionUID = 1L;

        private RandomData(final String name) {
            super(name);
        }

        // for serialization
        private RandomData() {
            super(null);
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            // from superclass
            result = prime * result + ((getName() == null) ? 0 : getName().hashCode());
            return result;
        }

        @Override
        public boolean equals(final Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            RandomData other = (RandomData) obj;
            // from superclass
            if (getName() == null) {
                if (other.getName() != null) {
                    return false;
                }
            } else if (!getName().equals(other.getName())) {
                return false;
            }
            return true;
        }

        @Override
        public String toString() {
            return "RandomData [name=" + getName() + "]";
        }

    }

    /**
     * The selector to get entries from a Random Coordinator.
     *
     * @author Martin Barisits
     * @author Tobias Doenz
     */
    public static final class RandomSelector extends RandomProperties implements Selector {

        private static final long serialVersionUID = -5331601009620489945L;

        private final int count;

        private RandomSelector(final int count, final String name) {
            super(name);
            this.count = count;
            checkCount(this.count);
        }

        // for serialization
        private RandomSelector() {
            super(null);
            this.count = 0;
        }

        @Override
        public int getCount() {
            return this.count;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + count;
            // from superclass
            result = prime * result + ((getName() == null) ? 0 : getName().hashCode());
            return result;
        }

        @Override
        public boolean equals(final Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            RandomSelector other = (RandomSelector) obj;
            if (count != other.count) {
                return false;
            }
            // from superclass
            if (getName() == null) {
                if (other.getName() != null) {
                    return false;
                }
            } else if (!getName().equals(other.getName())) {
                return false;
            }
            return true;
        }

        @Override
        public String toString() {
            return "RandomSelector [count=" + count + ", name=" + getName() + "]";
        }

    }

}
