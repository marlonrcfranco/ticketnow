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
 * First in First Out Coordinator.
 *
 * @author Martin Barisits
 * @author Tobias Doenz
 */
public final class FifoCoordinator implements ImplicitCoordinator {

    private static final long serialVersionUID = 4112012757052386681L;

    /**
     * Default Coordinator Name.
     */
    public static final String DEFAULT_NAME = "FifoCoordinator";

    private final String name;

    /**
     * Creates a new FifoCoordinator.
     *
     * @param name
     *            of the FifoCoordinator
     */
    public FifoCoordinator(final String name) {
        this.name = name;
    }

    /**
     * Creates a new FifoCoordinator.
     */
    public FifoCoordinator() {
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
        FifoCoordinator other = (FifoCoordinator) obj;
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
        return "FifoCoordinator [name=" + name + "]";
    }

    /**
     * Creates a new coordination data object.
     *
     * @param name
     *            the name of the FIFO Coordinator.
     * @return the created coordination data object
     */
    public static FifoData newCoordinationData(final String name) {
        return new FifoData(name);
    }

    /**
     * Creates a new coordination data object for the FIFO Coordinator with the
     * default name.
     *
     * @return the created coordination data object
     */
    public static FifoData newCoordinationData() {
        return new FifoData(DEFAULT_NAME);
    }

    /**
     * Returns a new FifoSelector.
     *
     * @param count
     *            Entry count of this Selector
     * @param name
     *            name of the Coordinator this Selector should be associated to
     * @return FifoSelector
     */
    public static FifoSelector newSelector(final int count, final String name) {
        return new FifoSelector(count, name);
    }

    /**
     * Returns a FifoSelector associated to a FifoCoordinator with the default name.
     *
     * @param count
     *            Entry count of this Selector
     * @return FifoSelector
     */
    public static FifoSelector newSelector(final int count) {
        return new FifoSelector(count, DEFAULT_NAME);
    }

    /**
     * Returns a FifoSelector with count 1 and the default coordinator name.
     *
     * @return FifoSelector
     */
    public static FifoSelector newSelector() {
        return new FifoSelector(1, DEFAULT_NAME);
    }

    /**
     * Common properties of the FIFO Coordinator, used for writing and reading
     * entries.
     *
     * @author Tobias Doenz
     */
    abstract static class FifoProperties implements Serializable {

        private static final long serialVersionUID = 1L;

        private final String name;

        protected FifoProperties(final String name) {
            this.name = name;
        }

        public String getName() {
            return this.name;
        }

    }

    /**
     * The coordination properties that are used for writing entries with the
     * FIFO coordinator.
     *
     * @author Tobias Doenz
     */
    public static final class FifoData extends FifoProperties implements CoordinationData {

        private static final long serialVersionUID = 1L;

        private FifoData(final String name) {
            super(name);
        }

        // for serialization
        private FifoData() {
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
            FifoData other = (FifoData) obj;
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
            return "FifoData [name()=" + getName() + "]";
        }

    }

    /**
     * The selector to get entries from a FIFO Coordinator.
     *
     * @author Martin Barisits
     * @author Tobias Doenz
     */
    public static final class FifoSelector extends FifoProperties implements Selector {

        private static final long serialVersionUID = -5077364621975549531L;

        private final int count;

        private FifoSelector(final int count, final String name) {
            super(name);
            this.count = count;
            checkCount(this.count);
        }

        // for serialization
        private FifoSelector() {
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
            FifoSelector other = (FifoSelector) obj;
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
            return "FifoSelector [count=" + count + ", name()=" + getName() + "]";
        }

    }

}
