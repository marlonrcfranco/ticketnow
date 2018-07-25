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

import static org.mozartspaces.core.MzsConstants.Selecting.DEFAULT_COUNT;
import static org.mozartspaces.core.MzsConstants.Selecting.checkCount;

import java.io.Serializable;

/**
 * Any Coordinator, the selector returns entries in arbitrary order. In contrast
 * to the {@link RandomCoordinator} they are not shuffled and guaranteed to be
 * in random order.
 *
 * @author Tobias Doenz
 */
public final class AnyCoordinator implements ImplicitCoordinator {

    private static final long serialVersionUID = 1L;

    /**
     * Default Coordinator Name.
     */
    public static final String DEFAULT_NAME = "AnyCoordinator";

    private final String name;

    /**
     * Creates a new AnyCoordinator.
     *
     * @param name
     *            of the AnyCoordinator
     */
    public AnyCoordinator(final String name) {
        this.name = name;
    }

    /**
     * Creates a new AnyCoordinator.
     */
    public AnyCoordinator() {
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
        AnyCoordinator other = (AnyCoordinator) obj;
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
        return "AnyCoordinator [name=" + name + "]";
    }

    /**
     * Creates a new coordination data object.
     *
     * @param name
     *            the name of the Any Coordinator.
     * @return the created coordination data object
     */
    public static AnyData newCoordinationData(final String name) {
        return new AnyData(name);
    }

    /**
     * Creates a new coordination data object for the any coordinator with the
     * default name.
     *
     * @return the created coordination data object
     */
    public static AnyData newCoordinationData() {
        return new AnyData(DEFAULT_NAME);
    }

    /**
     * Returns an AnySelector.
     *
     * @param count
     *            Entry count of this Selector
     * @param name
     *            name of the Coordinator this Selector should be associated to
     * @return AnySelector
     */
    public static AnySelector newSelector(final int count, final String name) {
        return new AnySelector(count, name);
    }

    /**
     * Returns an AnySelector with the default name.
     *
     * @param count
     *            Entry count of this Selector
     * @return RandomSelector
     */
    public static AnySelector newSelector(final int count) {
        return new AnySelector(count, DEFAULT_NAME);
    }

    /**
     * Returns an AnySelector with the
     * {@link org.mozartspaces.core.MzsConstants.Selecting#DEFAULT_COUNT DEFAULT_COUNT}
     * and the default name.
     *
     * @return AnySelector
     */
    public static AnySelector newSelector() {
        return new AnySelector(DEFAULT_COUNT, DEFAULT_NAME);
    }

    /**
     * Common properties of the any coordinator, used for writing and reading
     * entries.
     *
     * @author Tobias Doenz
     */
    abstract static class AnyProperties implements Serializable {

        private static final long serialVersionUID = 1L;

        private final String name;

        protected AnyProperties(final String name) {
            this.name = name;
        }

        public String getName() {
            return this.name;
        }

    }

    /**
     * The coordination properties that are used for writing entries with the
     * any coordinator.
     *
     * @author Tobias Doenz
     */
    public static final class AnyData extends AnyProperties implements CoordinationData {

        private static final long serialVersionUID = 1L;

        private AnyData(final String name) {
            super(name);
        }

        // for serialization
        private AnyData() {
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
            AnyData other = (AnyData) obj;
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
            return "AnyData [name=" + getName() + "]";
        }

    }

    /**
     * The selector to get entries from an any coordinator.
     *
     * @author Tobias Doenz
     */
    public static final class AnySelector extends AnyProperties implements Selector {

        private static final long serialVersionUID = 1L;

        private final int count;

        private AnySelector(final int count, final String name) {
            super(name);
            this.count = count;
            checkCount(this.count);
        }

        // for serialization
        private AnySelector() {
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
            AnySelector other = (AnySelector) obj;
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
            return "AnySelector [count=" + count + ", name=" + getName() + "]";
        }

    }

}
