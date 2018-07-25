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
import java.util.Arrays;
import java.util.List;

/**
 * Coordinator that stores entries according to their type and allows restrictions on allowed types.
 *
 * @author Stefan Crass
 * @author Tobias Doenz
 */
public final class TypeCoordinator implements ImplicitCoordinator {

    private static final long serialVersionUID = 1L;

    /**
     * Default Coordinator Name.
     */
    public static final String DEFAULT_NAME = "TypeCoordinator";

    private final String name;
    private final List<Class<?>> allowedTypes;

    /**
     * Creates a new TypeCoordinator with type restrictions.
     *
     * @param name
     *            of the TypeCoordinator
     * @param allowedTypes
     *            types of which any managed entry must be instance of
     */
    public TypeCoordinator(final String name, final List<Class<?>> allowedTypes) {
        this.name = name;
        this.allowedTypes = allowedTypes;
    }

    /**
     * Creates a new TypeCoordinator with type restrictions.
     *
     * @param name
     *            of the TypeCoordinator
     * @param allowedTypes
     *            types of which any managed entry must be instance of, must not be {@code null}
     */
    public TypeCoordinator(final String name, final Class<?>... allowedTypes) {
        this(name, Arrays.asList(allowedTypes));
    }

    /**
     * Creates a default TypeCoordinator with type restrictions.
     *
     * @param allowedTypes
     *            types of which any managed entry must be instance of
     */
    public TypeCoordinator(final List<Class<?>> allowedTypes) {
        this(DEFAULT_NAME, allowedTypes);
    }

    /**
     * Creates a default TypeCoordinator with type restrictions.
     *
     * @param allowedTypes
     *            types of which any managed entry must be instance of, must not be {@code null}
     */
    public TypeCoordinator(final Class<?>... allowedTypes) {
        this(DEFAULT_NAME, Arrays.asList(allowedTypes));
    }

    /**
     * Creates a new TypeCoordinator without type restrictions.
     *
     * @param name
     *            of the TypeCoordinator
     */
    public TypeCoordinator(final String name) {
        this.name = name;
        this.allowedTypes = null;
    }

    /**
     * Creates a default TypeCoordinator without type restrictions.
     */
    public TypeCoordinator() {
        this(DEFAULT_NAME);
    }

    @Override
    public String getName() {
        return name;
    }

    /**
     * Gets list of allowed types. Any managed entry must be subtype of one of those.
     *
     * @return list of allowed types
     */
    public List<Class<?>> getAllowedTypes() {
        return this.allowedTypes;
    }


    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((allowedTypes == null) ? 0 : allowedTypes.hashCode());
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
        TypeCoordinator other = (TypeCoordinator) obj;
        if (allowedTypes == null) {
            if (other.allowedTypes != null) {
                return false;
            }
        } else if (!allowedTypes.equals(other.allowedTypes)) {
            return false;
        }
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
        return "TypeCoordinator [name=" + name + ", allowedTypes=" + allowedTypes + "]";
    }

    /**
     * Creates a new coordination data object for the Type Coordinator.
     *
     * @param name
     *            the name of the Type Coordinator.
     * @return the created coordination data object
     */
    public static TypeData newCoordinationData(final String name) {
        return new TypeData(name);
    }

    /**
     * Creates a new coordination data object for the Type Coordinator with the
     * default name.
     *
     * @return the created coordination data object
     */
    public static TypeData newCoordinationData() {
        return new TypeData(DEFAULT_NAME);
    }

    /**
     * Returns a TypeSelector.
     *
     * @param name
     *            name of the Coordinator this Selector should be associated to
     * @param count
     *            Entry count of this Selector
     * @param type
     *            the superclass of entries that should be selected
     * @return TypeSelector
     */
    public static TypeSelector newSelector(final Class<?> type, final int count, final String name) {
        return new TypeSelector(type, count, name);
    }

    /**
     * Returns a TypeSelector with count 1.
     *
     * @param name
     *            name of the Coordinator this Selector should be associated to
     * @param type
     *            the superclass of entries that should be selected
     * @return TypeSelector
     */
    public static TypeSelector newSelector(final Class<?> type, final String name) {
        return new TypeSelector(type, 1, name);
    }

    /**
     * Returns a TypeSelector with the default name.
     *
     * @param count
     *            Entry count of this Selector
     * @param type
     *            the superclass of entries that should be selected
     * @return TypeSelector
     */
    public static TypeSelector newSelector(final Class<?> type, final int count) {
        return new TypeSelector(type, count, DEFAULT_NAME);
    }

    /**
     * Returns a TypeSelector with count 1 and the default name.
     *
     * @param type
     *            the superclass of entries that should be selected
     * @return TypeSelector
     */
    public static TypeSelector newSelector(final Class<?> type) {
        return new TypeSelector(type, 1, DEFAULT_NAME);
    }

    /**
     * @param type the class to check
     */
    public static void checkType(final Class<?> type) {
        if (type == null) {
            throw new NullPointerException("type");
        }
    }

    /**
     * Common properties of the Type Coordinator, used for writing and reading
     * entries.
     *
     * @author Stefan Crass
     */
    abstract static class TypeProperties implements Serializable {

        private static final long serialVersionUID = 1L;

        private final String name;

        protected TypeProperties(final String name) {
            this.name = name;
        }


        public String getName() {
            return this.name;
        }


    }

    /**
     * The coordination properties that are used for writing entries with the
     * Type Coordinator.
     *
     * @author Stefan Crass
     */
    public static final class TypeData extends TypeProperties implements CoordinationData {

        private static final long serialVersionUID = 1L;

        private TypeData(final String name) {
            super(name);
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
            TypeData other = (TypeData) obj;
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
            return "TypeData [name=" + getName() + "]";
        }

    }

    /**
     * The selector to get entries from a Type Coordinator.
     *
     * @author Stefan Crass
     */
    public static final class TypeSelector extends TypeProperties implements Selector {

        private static final long serialVersionUID = -2786661820657530651L;

        private final int count;
        private final Class<?> type;

        private TypeSelector(final Class<?> type, final int count, final String name) {
            super(name);
            this.type = type;
            checkType(this.type);
            this.count = count;
            checkCount(this.count);
        }

        @Override
        public int getCount() {
            return this.count;
        }

        /**
         *
         * @return the type used for selection
         */
        public Class<?> getType() {
            return this.type;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + count;
            result = prime * result + ((getType() == null) ? 0 : getType().hashCode());
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
            TypeSelector other = (TypeSelector) obj;
            if (count != other.count) {
                return false;
            }
            if (getType() == null) {
                if (other.getType() != null) {
                    return false;
                }
            } else if (!getType().equals(other.getType())) {
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
            return "TypeSelector [count=" + count + ", type=" + type + ", name=" + getName() + "]";
        }

    }

}
