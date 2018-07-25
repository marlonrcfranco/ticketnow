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
 * Vector Coordinator.
 *
 * @author Martin Barisits
 * @author Tobias Doenz
 */
public final class VectorCoordinator implements Coordinator {

    private static final long serialVersionUID = -7333282670968998036L;

    /**
     * Special index value when the entry should be appended at the end.
     */
    public static final int APPEND = -1;

    /**
     * Default Coordinator Name.
     */
    public static final String DEFAULT_NAME = "VectorCoordinator";

    private final String name;

    /**
     * Creates a new VectorCoordinator.
     *
     * @param name
     *            of the VectorCoordinator
     */
    public VectorCoordinator(final String name) {
        this.name = name;
    }

    /**
     * Creates a new VectorCoordinator.
     */
    public VectorCoordinator() {
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
        VectorCoordinator other = (VectorCoordinator) obj;
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
        return "VectorCoordinator [name=" + name + "]";
    }

    /**
     * Creates a new coordination data object.
     *
     * @param index
     *            the index where the entry should be inserted, use the constant {@code APPEND} to append it at the end
     * @param name
     *            the name of the Vector Coordinator
     * @return the created coordination data object
     */
    public static VectorData newCoordinationData(final int index, final String name) {
        return new VectorData(index, name);
    }

    /**
     * Creates a new coordination data object for the Vector Coordinator with the default name.
     *
     * @param index
     *            the index where the entry should be inserted, use the constant {@code APPEND} to append it at the end
     * @return the created coordination data object
     */
    public static VectorData newCoordinationData(final int index) {
        return new VectorData(index, DEFAULT_NAME);
    }

    /**
     * Returns a VectorSelector.
     *
     * @param index
     *            to select
     * @param count
     *            Entry count of this Selector
     * @param name
     *            name of the Coordinator this Selector should be associated to
     * @return VectorSelector
     */
    public static VectorSelector newSelector(final int index, final int count, final String name) {
        return new VectorSelector(index, count, name);
    }

    /**
     * Returns a VectorSelector with default name.
     *
     * @param index
     *            to select
     * @param count
     *            Entry count of this Selector
     * @return VectorSelector
     */
    public static VectorSelector newSelector(final int index, final int count) {
        return new VectorSelector(index, count, DEFAULT_NAME);
    }

    /**
     * Returns a VectorSelector with count 1 and the default name.
     *
     * @param index
     *            to select
     * @return VectorSelector
     */
    public static VectorSelector newSelector(final int index) {
        return new VectorSelector(index, 1, DEFAULT_NAME);
    }

    /**
     * @param index the index to check
     */
    public static void checkWriteIndex(final int index) {
        // not 0 because -1 is used for APPEND
        if (index < VectorCoordinator.APPEND) {
            throw new IllegalArgumentException("index " + index);
        }
    }

    /**
     * @param index the index to check
     */
    public static void checkSelectingIndex(final int index) {
        if (index < 0) {
            throw new IllegalArgumentException("index " + index);
        }
    }

    /**
     * Common properties of the Label Coordinator, used for writing and reading
     * entries.
     *
     * @author Tobias Doenz
     */
    abstract static class VectorProperties implements Serializable {

        private static final long serialVersionUID = 1L;

        private final int index;
        private final String name;

        protected VectorProperties(final int index, final String name) {
            this.index = index;
            this.name = name;
        }

        public int getIndex() {
            return this.index;
        }

        public String getName() {
            return this.name;
        }

    }

    /**
     * The coordination properties that are used for writing entries with the
     * Vector Coordinator.
     *
     * @author Tobias Doenz
     */
    public static final class VectorData extends VectorProperties implements CoordinationData {

        private static final long serialVersionUID = 1L;

        private VectorData(final int index, final String name) {
            super(index, name);
            checkWriteIndex(index);
        }

        // for serialization
        private VectorData() {
            super(0, null);
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            // from superclass
            result = prime * result + getIndex();
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
            VectorData other = (VectorData) obj;
            // from superclass
            if (getIndex() != other.getIndex()) {
                return false;
            }
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
            return "VectorData [index=" + getIndex() + ", name=" + getName() + "]";
        }

    }

    /**
     * The selector to get entries from a Vector Coordinator.
     *
     * @author Martin Barisits
     * @author Tobias Doenz
     */
    public static final class VectorSelector extends VectorProperties implements Selector {

        private static final long serialVersionUID = 1240229881713461057L;

        private final int count;

        private VectorSelector(final int index, final int count, final String name) {
            super(index, name);
            checkSelectingIndex(index);
            this.count = count;
            checkCount(this.count);
        }

        // for serialization
        private VectorSelector() {
            super(0, null);
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
            result = prime * result + getIndex();
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
            VectorSelector other = (VectorSelector) obj;
            if (count != other.count) {
                return false;
            }
            // from superclass
            if (getIndex() != other.getIndex()) {
                return false;
            }
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
            return "VectorSelector [count=" + count + ", index=" + getIndex() + ", name=" + getName() + "]";
        }

    }

}
