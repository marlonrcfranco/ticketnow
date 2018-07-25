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
package org.mozartspaces.core.authentication;

import java.io.Serializable;

/**
 * Configuration of an IdentityProvider (IDP).
 *
 * @author Tobias Doenz
 */
public final class IdentityProviderConfiguration implements Serializable {

    private static final long serialVersionUID = 1L;

    private final String name;
    private final Serializable configuration;

    /**
     *
     * @param name
     *            the unique name (identifier) of the IDP
     * @param configuration
     *            the IDP-specific configuration, may be a map of properties or the like
     */
    public IdentityProviderConfiguration(final String name, final Serializable configuration) {
        this.name = name;
        this.configuration = configuration;
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @return the configuration
     */
    public Serializable getConfiguration() {
        return configuration;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((configuration == null) ? 0 : configuration.hashCode());
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
        IdentityProviderConfiguration other = (IdentityProviderConfiguration) obj;
        if (configuration == null) {
            if (other.configuration != null) {
                return false;
            }
        } else if (!configuration.equals(other.configuration)) {
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
        return "IdentityProviderConfiguration [name=" + name + ", configuration=" + configuration + "]";
    }

}
