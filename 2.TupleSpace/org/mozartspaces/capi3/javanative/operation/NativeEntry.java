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
package org.mozartspaces.capi3.javanative.operation;

import java.io.Serializable;

/**
 * A {@code NativeEntry} encapsulates the user-provided entry object and has an ID that is unique within a container and
 * used in the methods {@code hashCode} and {@code equals}.
 *
 * @author Martin Barisits
 * @author Tobias Doenz
 */
public interface NativeEntry extends Serializable {

    /**
     * Returns the data object encapsulated in this Entry.
     *
     * @return the data object
     */
    Serializable getData();

    /**
     * Returns the entry ID which has to be unique within the space.
     *
     * @return the EntryId
     */
    long getEntryId();

    @Override
    boolean equals(final Object obj);

    @Override
    int hashCode();

    /**
     * Get the container in which this entry is stored.
     * @return a container containing this entry
     */
    NativeContainer getContainer();
}
