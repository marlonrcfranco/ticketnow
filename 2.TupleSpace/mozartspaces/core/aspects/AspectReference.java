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
package org.mozartspaces.core.aspects;

import java.net.URI;

import net.jcip.annotations.Immutable;

import org.mozartspaces.core.Reference;

/**
 * Identifies an aspect in the XVSM universe. An <code>AspectReference</code> is
 * used in the Core to specify an aspect, because outside of the Runtime no
 * direct reference to the actual aspect object is accessible for security
 * reasons.
 *
 * @author Tobias Doenz
 */
@Immutable
public final class AspectReference extends Reference<String> {

    private static final long serialVersionUID = 1L;

    /**
     * The prefix for the path part of the URI representation, used by
     * {@link #toString()}.
     */
    public static final String PATH_PREFIX = "/aspects/aspect/";

    /**
     * Constructs an <code>AspectReference</code>.
     *
     * @param id
     *            the aspect id, unique within a space (or core)
     * @param space
     *            the URI to identify the space
     */
    public AspectReference(final String id, final URI space) {
        super(id, space);
    }

    @Override
    public String toString() {
        return getSpace() + PATH_PREFIX + getId();
    }

}
