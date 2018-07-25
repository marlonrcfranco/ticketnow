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

import java.io.Serializable;

/**
 * The <code>LocalContainerReference</code> provides the CAPI3-internal
 * reference to a <code>Container</code>.
 *
 * @author Martin Barisits
 */
public final class LocalContainerReference implements Serializable {

    private static final long serialVersionUID = 1L;

    private final String containerId;

    /**
     * Creates a new LocalContainerReference.
     *
     * @param containerId
     *            Id of the Container
     */
    public LocalContainerReference(final String containerId) {
        if (containerId == null) {
            throw new NullPointerException("The ContainerId must not be null");
        }
        this.containerId = containerId;
    }

    /**
     * Returns the unique ID of the container referenced by this ContainerRef.
     *
     * @return the unique container ID
     */
    public String getId() {
        return this.containerId;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(final Object obj) {
        return (obj instanceof LocalContainerReference)
                && (this.containerId.equals(((LocalContainerReference) obj)
                        .getId()));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        int result = 17;
        result = 31 * result + containerId.hashCode();
        return result;
    }

    @Override
    public String toString() {
        //"LocalContainerReference [containerId=" + containerId + "]";
        return containerId;
    }

}
