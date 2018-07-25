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

import org.mozartspaces.util.AndroidHelperUtils;

/**
 * Label Coordinator.
 *
 * @author Martin Barisits
 * @author Tobias Doenz
 */
public final class LabelCoordinator implements Coordinator {

    private static final long serialVersionUID = 653449499753494064L;

    /**
     * Default Coordinator Name.
     */
    public static final String DEFAULT_NAME = "LabelCoordinator";

    private final String name;

    /**
     * Creates a new LabelCoordinator.
     *
     * @param name
     *            of the LabelCoordinator
     */
    public LabelCoordinator(final String name) {
        this.name = name;
    }

    /**
     * Creates a new LabelCoordinator.
     */
    public LabelCoordinator() {
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
        LabelCoordinator other = (LabelCoordinator) obj;
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
        return "LabelCoordinator [name=" + name + "]";
    }

    /**
     * Creates a new coordination data object.
     *
     * @param label
     *            the label of the entry
     * @param name
     *            the name of the Label Coordinator.
     * @return the created coordination data object
     */
    public static LabelData newCoordinationData(final String label, final String name) {
        return new LabelData(label, name);
    }

    /**
     * Creates a new coordination data object for the Label Coordinator with the
     * default name.
     *
     * @param label
     *            the label of the entry
     * @return the created coordination data object
     */
    public static LabelData newCoordinationData(final String label) {
        return new LabelData(label, DEFAULT_NAME);
    }

    /**
     * Returns a LabelSelector.
     *
     * @param label
     *            to select
     * @param count
     *            Entry count of this Selector
     * @param name
     *            name of the Coordinator this Selector should be associated to
     * @return LabelSelector
     */
    public static LabelSelector newSelector(final String label, final int count, final String name) {
        return new LabelSelector(label, count, name);
    }

    /**
     * Returns a LabelSelector with default name.
     *
     * @param label
     *            to select
     * @param count
     *            Entry count of this Selector
     * @return LabelSelector
     */
    public static LabelSelector newSelector(final String label, final int count) {
        return new LabelSelector(label, count, DEFAULT_NAME);
    }

    /**
     * Returns a LabelSelector with count 1 and the default name.
     *
     * @param label
     *            to select
     * @return LabelSelector
     */
    public static LabelSelector newSelector(final String label) {
        return new LabelSelector(label, 1, DEFAULT_NAME);
    }

    /**
     * @param label the label to check
     */
    public static void checkLabel(final String label) {
        if (label == null) {
            throw new NullPointerException("label");
        }
        if (AndroidHelperUtils.isEmpty(label)) {
            throw new IllegalArgumentException("label is empty");
        }
    }

    /**
     * Common properties of the Label Coordinator, used for writing and reading
     * entries.
     *
     * @author Tobias Doenz
     */
    abstract static class LabelProperties implements Serializable {

        private static final long serialVersionUID = 1L;

        private final String label;
        private final String name;

        protected LabelProperties(final String label, final String name) {
            this.label = label;
            checkLabel(this.label);
            this.name = name;
        }

        public String getLabel() {
            return this.label;
        }

        public String getName() {
            return this.name;
        }

    }

    /**
     * The coordination properties that are used for writing entries with the
     * Label Coordinator.
     *
     * @author Tobias Doenz
     */
    public static final class LabelData extends LabelProperties implements CoordinationData {

        private static final long serialVersionUID = 1L;

        private LabelData(final String label, final String name) {
            super(label, name);
        }

        // for serialization
        private LabelData() {
            super("dummy", null);
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            // from superclass
            result = prime * result + ((getLabel() == null) ? 0 : getLabel().hashCode());
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
            LabelData other = (LabelData) obj;
            // from superclass
            if (getLabel() == null) {
                if (other.getLabel() != null) {
                    return false;
                }
            } else if (!getLabel().equals(other.getLabel())) {
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
            return "LabelData [label=" + getLabel() + ", name=" + getName() + "]";
        }

    }

    /**
     * The selector to get entries from a Label Coordinator.
     *
     * @author Martin Barisits
     * @author Tobias Doenz
     */
    public static final class LabelSelector extends LabelProperties implements Selector {

        private static final long serialVersionUID = 7427943285890642076L;

        private final int count;

        private LabelSelector(final String label, final int count, final String name) {
            super(label, name);
            this.count = count;
            checkCount(this.count);
        }

        // for serialization
        private LabelSelector() {
            super("dummy", null);
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
            result = prime * result + ((getLabel() == null) ? 0 : getLabel().hashCode());
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
            LabelSelector other = (LabelSelector) obj;
            if (count != other.count) {
                return false;
            }
            // from superclass
            if (getLabel() == null) {
                if (other.getLabel() != null) {
                    return false;
                }
            } else if (!getLabel().equals(other.getLabel())) {
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
            return "LabelSelector [count=" + count + ", label=" + getLabel() + ", name=" + getName() + "]";
        }

    }
}
