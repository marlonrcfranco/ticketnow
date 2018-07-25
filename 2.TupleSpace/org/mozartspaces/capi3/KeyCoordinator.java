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
 * Key Coordinator.
 *
 * @author Martin Barisits
 * @author Tobias Doenz
 */
public final class KeyCoordinator implements Coordinator {

    private static final long serialVersionUID = -7565531787950187733L;

    /**
     * Default Coordinator Name.
     */
    public static final String DEFAULT_NAME = "KeyCoordinator";

    private final String name;

    /**
     * Creates a new KeyCoordinator.
     *
     * @param name
     *            of the KeyCoordinator
     */
    public KeyCoordinator(final String name) {
        this.name = name;
    }

    /**
     * Creates a new KeyCoordinator.
     */
    public KeyCoordinator() {
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
        KeyCoordinator other = (KeyCoordinator) obj;
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
        return "KeyCoordinator [name=" + name + "]";
    }

    /**
     * Creates a new coordination data object.
     *
     * @param key
     *            the key of the entry
     * @param name
     *            the name of the Key Coordinator.
     * @return the created coordination data object
     */
    public static KeyData newCoordinationData(final String key, final String name) {
        return new KeyData(key, name);
    }

    /**
     * Creates a new coordination data object for the Key Coordinator with the
     * default name.
     *
     * @param key
     *            the key of the entry
     * @return the created coordination data object
     */
    public static KeyData newCoordinationData(final String key) {
        return new KeyData(key, DEFAULT_NAME);
    }

    /**
     * Returns a KeySelector.
     *
     * @param name
     *            name of the Coordinator this Selector should be associated to
     * @param count
     *            Entry count of this Selector
     * @param key
     *            to select
     * @return KeySelector
     */
    public static KeySelector newSelector(final String key, final int count, final String name) {
        return new KeySelector(key, count, name);
    }

    /**
     * Returns a KeySelector with count 1.
     *
     * @param name
     *            name of the Coordinator this Selector should be associated to
     * @param key
     *            to select
     * @return KeySelector
     */
    public static KeySelector newSelector(final String key, final String name) {
        return new KeySelector(key, 1, name);
    }

    /**
     * Returns a KeySelector with the default name.
     *
     * @param count
     *            Entry count of this Selector
     * @param key
     *            to select
     * @return KeySelector
     */
    public static KeySelector newSelector(final String key, final int count) {
        return new KeySelector(key, count, DEFAULT_NAME);
    }

    /**
     * Returns a KeySelector with count 1 and the default name.
     *
     * @param key
     *            to select
     * @return KeySelector
     */
    public static KeySelector newSelector(final String key) {
        return new KeySelector(key, 1, DEFAULT_NAME);
    }

  /**
    * @param key the key to check
    */
   public static void checkKey(final String key) {
       if (key == null) {
           throw new NullPointerException("key");
       }
       if (AndroidHelperUtils.isEmpty(key)) {
           throw new IllegalArgumentException("key is empty");
       }
   }

    /**
     * Common properties of the Key Coordinator, used for writing and reading
     * entries.
     *
     * @author Tobias Doenz
     */
    abstract static class KeyProperties implements Serializable {

        private static final long serialVersionUID = 1L;

        private final String key;
        private final String name;

        protected KeyProperties(final String key, final String name) {
            this.key = key;
            checkKey(this.key);
            this.name = name;
        }

        /**
         * @return the key
         */
        public String getKey() {
            return this.key;
        }

        public String getName() {
            return this.name;
        }

    }

    /**
     * The coordination properties that are used for writing entries with the
     * Key Coordinator.
     *
     * @author Tobias Doenz
     */
    public static final class KeyData extends KeyProperties implements CoordinationData {

        private static final long serialVersionUID = 1L;

        private KeyData(final String key, final String name) {
            super(key, name);
        }

        // for serialization
        private KeyData() {
            super("dummy", null);
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            // from superclass
            result = prime * result + ((getKey() == null) ? 0 : getKey().hashCode());
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
            KeyData other = (KeyData) obj;
            // from superclass
            if (getKey() == null) {
                if (other.getKey() != null) {
                    return false;
                }
            } else if (!getKey().equals(other.getKey())) {
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
            return "KeyData [key=" + getKey() + ", name=" + getName() + "]";
        }

    }

    /**
     * The selector to get entries from a Key Coordinator.
     *
     * @author Martin Barisits
     * @author Tobias Doenz
     */
    public static final class KeySelector extends KeyProperties implements Selector {

        private static final long serialVersionUID = -2786661820657530651L;

        private final int count;

        private KeySelector(final String key, final int count, final String name) {
            super(key, name);
            this.count = count;
            checkCount(this.count);
            if (this.count > 0 && this.count != 1) {
                throw new IllegalArgumentException("count " + count);
            }
        }

        // for serialization
        private KeySelector() {
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
            result = prime * result + ((getKey() == null) ? 0 : getKey().hashCode());
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
            KeySelector other = (KeySelector) obj;
            if (count != other.count) {
                return false;
            }
            // from superclass
            if (getKey() == null) {
                if (other.getKey() != null) {
                    return false;
                }
            } else if (!getKey().equals(other.getKey())) {
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
            return "KeySelector [count=" + count + ", key=" + getKey() + ", name=" + getName() + "]";
        }

    }

}
