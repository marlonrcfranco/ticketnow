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

import org.mozartspaces.core.authorization.RequestAuthTarget;

/**
 * Authorization Target Coordinator.
 *
 * @author Stefan Crass
 */
public final class AuthTargetCoordinator implements ImplicitCoordinator {

    private static final long serialVersionUID = 1L;

    /**
     * Default Coordinator Name.
     */
    public static final String DEFAULT_NAME = "AuthTargetCoordinator";

    private final String name;

    /**
     * Creates a new AuthTargetCoordinator.
     *
     * @param name
     *            of the AuthTargetCoordinator
     */
    public AuthTargetCoordinator(final String name) {
        this.name = name;
    }

    /**
     * Creates a new AuthTargetCoordinator with default name.
     */
    public AuthTargetCoordinator() {
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
        AuthTargetCoordinator other = (AuthTargetCoordinator) obj;
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
        return "AuthTargetCoordinator [name=" + name + "]";
    }

    /**
     * Creates a new coordination data object.
     *
     * @param name
     *            the name of the AuthTargetCoordinator.
     * @return the created coordination data object
     */
    public static AuthTargetData newCoordinationData(final String name) {
        return new AuthTargetData(name);
    }

    /**
     * Creates a new coordination data object for the AuthTargetCoordinator with the
     * default name.
     *
     * @return the created coordination data object
     */
    public static AuthTargetData newCoordinationData() {
        return new AuthTargetData(DEFAULT_NAME);
    }

    /**
     * Returns an AuthTargetSelector.
     *
     * @param target
     *            authorization target to match
     * @param count
     *            Entry count of this Selector
     * @param name
     *            name of the Coordinator this Selector should be associated to
     * @return LindaSelector
     */
    public static AuthTargetSelector newSelector(final RequestAuthTarget target, final int count, final String name) {
        return new AuthTargetSelector(target, count, name);
    }

    /**
     * Returns an AuthTargetSelector with the default name.
     *
     * @param target
     *            authorization target to match
     * @param count
     *            Entry count of this Selector
     * @return AuthTargetSelector
     */
    public static AuthTargetSelector newSelector(final RequestAuthTarget target, final int count) {
        return new AuthTargetSelector(target, count, DEFAULT_NAME);
    }

    /**
     * Returns an AuthTargetSelector with count 1 and the default name.
     *
     * @param target
     *            authorization target to match
     * @return LindaSelector
     */
    public static AuthTargetSelector newSelector(final RequestAuthTarget target) {
        return new AuthTargetSelector(target, 1, DEFAULT_NAME);
    }

    /**
     * @param target the template to check
     */
    public static void checkTarget(final RequestAuthTarget target) {
        if (target == null) {
            throw new NullPointerException("target");
        }
    }

    /**
     * Common properties of the AuthTargetCoordinator, used for writing and reading
     * entries.
     *
     * @author Stefan Crass
     */
    abstract static class AuthTargetProperties implements Serializable {

        private static final long serialVersionUID = 1L;

        private final String name;

        protected AuthTargetProperties(final String name) {
            this.name = name;
        }

        public String getName() {
            return this.name;
        }

    }

    /**
     * The coordination properties that are used for writing entries with the
     * AuthTargetCoordinator.
     *
     * @author Stefan Crass
     */
    public static final class AuthTargetData extends AuthTargetProperties implements CoordinationData {

        private static final long serialVersionUID = 1L;

        private AuthTargetData(final String name) {
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
            AuthTargetData other = (AuthTargetData) obj;
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
            return "AuthTargetData [name=" + getName() + "]";
        }

    }

    /**
     * The selector to get entries from a AuthTargetCoordinator.
     *
     * @author Stefan Crass
     */
    public static final class AuthTargetSelector extends AuthTargetProperties implements Selector {

        private static final long serialVersionUID = 1L;

        private final RequestAuthTarget target;
        private final int count;

        private AuthTargetSelector(final RequestAuthTarget target, final int count, final String name) {
            super(name);
            this.target = target;
            checkTarget(this.target);
            this.count = count;
            checkCount(this.count);
        }

        @Override
        public int getCount() {
            return this.count;
        }

        /**
         * @return the target
         */
        public RequestAuthTarget getTarget() {
            return target;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + count;
            result = prime * result + ((target == null) ? 0 : target.hashCode());
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
            AuthTargetSelector other = (AuthTargetSelector) obj;
            if (count != other.count) {
                return false;
            }
            if (target == null) {
                if (other.target != null) {
                    return false;
                }
            } else if (!target.equals(other.target)) {
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
            return "AuthTargetSelector [target=" + target + ", count=" + count + ", name=" + getName() + "]";
        }

    }

}
