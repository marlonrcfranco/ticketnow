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
 * Last in First out Coordinator.
 *
 * @author Martin Barisits
 * @author Tobias Doenz
 */
public final class LifoCoordinator implements ImplicitCoordinator {

    private static final long serialVersionUID = 4255217113902602842L;

    /**
     * Default Coordinator Name.
     */
    public static final String DEFAULT_NAME = "LifoCoordinator";

    private final String name;

    /**
     * Creates a new LifoCoordinator.
     *
     * @param name
     *            of the LifoCoordinator
     */
    public LifoCoordinator(final String name) {
        this.name = name;
    }

    /**
     * Creates a new LifoCoordinator.
     */
    public LifoCoordinator() {
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
        LifoCoordinator other = (LifoCoordinator) obj;
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
        return "LifoCoordinator [name=" + name + "]";
    }

    /**
     * Creates a new coordination data object.
     *
     * @param name
     *            the name of the LIFO Coordinator.
     * @return the created coordination data object
     */
    public static LifoData newCoordinationData(final String name) {
        return new LifoData(name);
    }

    /**
     * Creates a new coordination data object for the LIFO Coordinator with the default name.
     *
     * @return the created coordination data object
     */
    public static LifoData newCoordinationData() {
        return new LifoData(DEFAULT_NAME);
    }

    /**
     * Returns a LifoSelector.
     *
     * @param count
     *            Entry count of this Selector
     * @param name
     *            name of the Coordinator this Selector should be associated to
     * @return LifoSelector
     */
    public static LifoSelector newSelector(final int count, final String name) {
        return new LifoSelector(count, name);
    }

    /**
     * Returns a LifoSelector with default name.
     *
     * @param count
     *            Entry count of this Selector
     * @return LifoSelector
     */
    public static LifoSelector newSelector(final int count) {
        return new LifoSelector(count, DEFAULT_NAME);
    }

    /**
     * Returns a LifoSelector with count 1 and the default name.
     *
     * @return LifoSelector
     */
    public static LifoSelector newSelector() {
        return new LifoSelector(1, DEFAULT_NAME);
    }

    /**
     * Common properties of the LIFO Coordinator, used for writing and reading entries.
     *
     * @author Tobias Doenz
     */
    abstract static class LifoProperties implements Serializable {

        private static final long serialVersionUID = 1L;

        private final String name;

        protected LifoProperties(final String name) {
            this.name = name;
        }

        public String getName() {
            return this.name;
        }

    }

    /**
     * The coordination properties that are used for writing entries with the LIFO coordinator.
     *
     * @author Tobias Doenz
     */
    public static final class LifoData extends LifoProperties implements CoordinationData {

        private static final long serialVersionUID = 1L;

        private LifoData(final String name) {
            super(name);
        }

        // for serialization
        private LifoData() {
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
            LifoData other = (LifoData) obj;
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
            return "LifoData [name=" + getName() + "]";
        }

    }

    /**
     * The associated LifoSelector.
     *
     * @author Martin Barisits
     * @author Tobias Doenz
     */
    public static final class LifoSelector extends LifoProperties implements Selector {

        private static final long serialVersionUID = 8995735612928763446L;

        private final int count;

        private LifoSelector(final int count, final String name) {
            super(name);
            this.count = count;
            checkCount(this.count);
        }

        // for serialization
        private LifoSelector() {
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
            LifoSelector other = (LifoSelector) obj;
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
            return "LifoSelector [count=" + count + ", name=" + getName() + "]";
        }

    }

}
