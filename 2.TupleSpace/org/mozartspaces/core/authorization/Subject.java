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
package org.mozartspaces.core.authorization;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * An authenticated subject for usage in the authorization process.
 *
 * @author Stefan Crass
 * @author Tobias Doenz
 */
public final class Subject implements Serializable {

    private static final long serialVersionUID = 1L;

    private final Set<NamedValue> attributes;

    /**
     * @param attributes
     *            the identity attributes, may be {@code null}
     */
    public Subject(final Set<NamedValue> attributes) {
        this.attributes = (attributes == null) ? Collections.<NamedValue>emptySet() : Collections
                .unmodifiableSet(new HashSet<NamedValue>(attributes));
    }

    /**
     * @return the attributes
     */
    public Set<NamedValue> getAttributes() {
        return attributes;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((attributes == null) ? 0 : attributes.hashCode());
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
        Subject other = (Subject) obj;
        if (attributes == null) {
            if (other.attributes != null) {
                return false;
            }
        } else if (!attributes.equals(other.attributes)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "Subject [attributes=" + attributes + "]";
    }

}
