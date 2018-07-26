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
package org.mozartspaces.core;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.net.URI;

import net.jcip.annotations.Immutable;

/**
 * A <code>Reference</code> identifies an entity in the XVSM universe. It
 * consists of an id that is unique within a space (or core), and a URI that
 * identifies the space. References are currently used for <code>Requests</code>,
 * {@link ContainerReference Containers}, {@link TransactionReference
 * Transactions}, and {@link org.mozartspaces.core.aspects.AspectReference
 * Aspects}
 *
 * @author Tobias Doenz
 *
 * @param <T>
 *            the type of the id
 */
@Immutable
public abstract class Reference<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    private static final String PATH_DELIMITER = "/";

    private final T id;
    private final String space;
    private transient URI spaceURI;

    /**
     * Constructs a <code>Reference</code>.
     *
     * @param id
     *            the id, unique within a core (or core)
     * @param space
     *            the URI to identify the space
     */
    public Reference(final T id, final URI space) {
        this.id = id;
        assert this.id != null;
        this.space = (space == null) ? null : space.toString();
        this.spaceURI = space;
    }

    /**
     * Gets the reference id.
     *
     * @return the id
     */
    public final T getId() {
        return id;
    }

    /**
     * Gets the space URI.
     *
     * @return the space URI
     */
    public final URI getSpace() {
        return spaceURI;
    }

    /**
     * Returns a short string representation of the reference. It is intended to serialize the reference. When the type
     * of the reference is known, it can be easily deserialized. In contrast, the {@code toString} method of a reference
     * returns a string representation that contains the meta-model path of the reference, which allows to determine the
     * type of the reference from the string alone.
     *
     * @return a string representation of the reference
     */
    public final String getStringRepresentation() {
        return space + PATH_DELIMITER + getId();
    }

    private void readObject(final ObjectInputStream ois) throws IOException, ClassNotFoundException {
        ois.defaultReadObject();
        spaceURI = URI.create(space);
    }

    @Override
    public final int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        result = prime * result + ((space == null) ? 0 : space.hashCode());
        return result;
    }

    @Override
    public final boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof Reference<?>)) {
            return false;
        }
        Reference<?> other = (Reference<?>) obj;
        if (id == null) {
            if (other.id != null) {
                return false;
            }
        } else if (!id.equals(other.id)) {
            return false;
        }
        if (space == null) {
            if (other.space != null) {
                return false;
            }
        } else if (!space.equals(other.space)) {
            return false;
        }
        return true;
    }

}
