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
 * Linda Coordinator.
 *
 * @author Martin Barisits
 * @author Tobias Doenz
 */
public final class LindaCoordinator implements ImplicitCoordinator {

    private static final long serialVersionUID = -7802783192849470713L;

    /**
     * Default Coordinator Name.
     */
    public static final String DEFAULT_NAME = "LindaCoordinator";

    /**
     * Default value for the flag {@code onlyAnnotatedEntries}.
     */
    public static final boolean DEFAULT_ONLY_ANNOTATED_ENTRIES = true;

    private final String name;
    private final boolean onlyAnnotatedEntries;

    /**
     * Creates a new LindaCoordinator.
     *
     * @param name
     *            of the LindaCoordinator
     * @param onlyAnnotatedEntries
     *            {@code true} if only annotated entries may be written to the coordinator, {@code false} otherwise
     */
    public LindaCoordinator(final String name, final boolean onlyAnnotatedEntries) {
        this.name = name;
        this.onlyAnnotatedEntries = onlyAnnotatedEntries;
    }

    /**
     * Creates a new LindaCoordinator.
     *
     * @param name
     *            of the LindaCoordinator
     */
    public LindaCoordinator(final String name) {
        this(name, DEFAULT_ONLY_ANNOTATED_ENTRIES);
    }

    /**
     * Creates a new LindaCoordinator.
     *
     * @param onlyAnnotatedEntries
     *            {@code true} if only annotated entries may be written to the coordinator, {@code false} otherwise
     */
    public LindaCoordinator(final boolean onlyAnnotatedEntries) {
        this(DEFAULT_NAME, onlyAnnotatedEntries);
    }

    /**
     * Creates a new LindaCoordinator.
     */
    public LindaCoordinator() {
        this(DEFAULT_NAME);
    }

    @Override
    public String getName() {
        return name;
    }

    /**
     * @return whether only annotated entries may be written to the coordinator
     */
    public boolean isOnlyAnnotatedEntries() {
        return onlyAnnotatedEntries;
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
        LindaCoordinator other = (LindaCoordinator) obj;
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
        return "LindaCoordinator [name=" + name + ", onlyAnnotatedEntries=" + onlyAnnotatedEntries + "]";
    }

    /**
     * Creates a new coordination data object.
     *
     * @param name
     *            the name of the Linda Coordinator.
     * @return the created coordination data object
     */
    public static LindaData newCoordinationData(final String name) {
        return new LindaData(name);
    }

    /**
     * Creates a new coordination data object for the Linda Coordinator with the
     * default name.
     *
     * @return the created coordination data object
     */
    public static LindaData newCoordinationData() {
        return new LindaData(DEFAULT_NAME);
    }

    /**
     * Returns a LindaSelector.
     *
     * @param template
     *            to match
     * @param count
     *            Entry count of this Selector
     * @param name
     *            name of the Coordinator this Selector should be associated to
     * @return LindaSelector
     */
    public static LindaSelector newSelector(final Serializable template, final int count, final String name) {
        return new LindaSelector(template, count, name);
    }

    /**
     * Returns a LindaSelector with the default name.
     *
     * @param template
     *            to match
     * @param count
     *            Entry count of this Selector
     * @return LindaSelector
     */
    public static LindaSelector newSelector(final Serializable template, final int count) {
        return new LindaSelector(template, count, DEFAULT_NAME);
    }

    /**
     * Returns a LindaSelector with count 1 and the default name.
     *
     * @param template
     *            to match
     * @return LindaSelector
     */
    public static LindaSelector newSelector(final Serializable template) {
        return new LindaSelector(template, 1, DEFAULT_NAME);
    }

    /**
     * @param template the template to check
     */
    public static void checkTemplate(final Serializable template) {
        // TODO allow null template? (select all entries)
        if (template == null) {
            throw new NullPointerException("template");
        }
    }

    /**
     * Common properties of the Linda Coordinator, used for writing and reading
     * entries.
     *
     * @author Tobias Doenz
     */
    abstract static class LindaProperties implements Serializable {

        private static final long serialVersionUID = 1L;

        private final String name;

        protected LindaProperties(final String name) {
            this.name = name;
        }

        public String getName() {
            return this.name;
        }

    }

    /**
     * The coordination properties that are used for writing entries with the
     * Linda Coordinator.
     *
     * @author Tobias Doenz
     */
    public static final class LindaData extends LindaProperties implements CoordinationData {

        private static final long serialVersionUID = 1L;

        private LindaData(final String name) {
            super(name);
        }

        // for serialization
        private LindaData() {
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
            LindaData other = (LindaData) obj;
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
            return "LindaData [name=" + getName() + "]";
        }

    }

    /**
     * The selector to get entries from a Linda Coordinator.
     *
     * @author Martin Barisits
     * @author Tobias Doenz
     */
    public static final class LindaSelector extends LindaProperties implements Selector {

        private static final long serialVersionUID = 2782872010768573000L;

        private final Serializable template;
        private final int count;

        private LindaSelector(final Serializable template, final int count, final String name) {
            super(name);
            this.template = template;
            checkTemplate(this.template);
            this.count = count;
            checkCount(this.count);
        }

        // for serialization
        private LindaSelector() {
            super(null);
            this.template = null;
            this.count = 0;
        }

        @Override
        public int getCount() {
            return this.count;
        }

        /**
         * @return the template
         */
        public Serializable getTemplate() {
            return template;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + count;
            result = prime * result + ((template == null) ? 0 : template.hashCode());
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
            LindaSelector other = (LindaSelector) obj;
            if (count != other.count) {
                return false;
            }
            if (template == null) {
                if (other.template != null) {
                    return false;
                }
            } else if (!template.equals(other.template)) {
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
            return "LindaSelector [template=" + template + ", count=" + count + ", name=" + getName() + "]";
        }

    }

}
