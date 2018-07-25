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
 * Identifies a request in all XVSM Runtimes. A <code>RequestReference</code> is
 * used internally in the Runtime and also to route the response to the correct
 * entry in the virtual Answer Container.
 *
 * It is constructed when a request is sent. So, for a remote request, the space
 * URI identifies not the space where it is processed but where it was sent. A
 * <code>RequestReference</code> cannot be derived from a request, because, for
 * technical reasons, a request is a value objects without an id.
 *
 * @author Tobias Doenz
 */
@Immutable
public final class RequestReference extends Reference<String> {

    private static final long serialVersionUID = 1L;

    /**
     * The prefix for the path part of the URI representation, used by
     * {@link #toString()}.
     */
    public static final String PATH_PREFIX = "/requests/";

    /**
     * Dummy reference, use when {@code null} is not allowed for a request reference.
     */
    public static final RequestReference DUMMY = new RequestReference("", null);

    /**
     * Constructs a <code>RequestReference</code>.
     *
     * @param id
     *            the request id, unique within a space (or core)
     * @param space
     *            the URI to identify the space where the request was sent
     */
    public RequestReference(final String id, final URI space) {
        super(id, space);
    }

    @Override
    public String toString() {
        return getSpace() + PATH_PREFIX + getId();
    }

}
