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

import java.net.URI;

import net.jcip.annotations.Immutable;

/**
 * Identifies a container in the XVSM universe. A
 * <code>ContainerReference</code> is used in the Core to specify a container,
 * because outside of CAPI3 no direct reference to the actual container object
 * is accessible for security reasons.
 *
 * @author Tobias Doenz
 */
@Immutable
public final class ContainerReference extends Reference<String> {

    private static final long serialVersionUID = 1L;

    /**
     * The prefix for the path part of the URI representation, used by
     * {@link #toString()}.
     */
    public static final String PATH_PREFIX = "/containers/container/";

    /**
     * Dummy reference, use when {@code null} is not allowed for a container reference.
     */
    public static final ContainerReference DUMMY = new ContainerReference("", null);

    /**
     * Constructs a <code>ContainerReference</code>.
     *
     * @param id
     *            the container id, unique within a space (or core)
     * @param space
     *            the URI to identify the space
     */
    public ContainerReference(final String id, final URI space) {
        super(id, space);
    }

    @Override
    public String toString() {
        return getSpace() + PATH_PREFIX + getId();
    }

}
