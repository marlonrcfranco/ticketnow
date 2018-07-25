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
package org.mozartspaces.notifications;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Operation types for which a notification is possible. Each of the values correspond to a request on entries in the
 * container of an XVSM space.
 *
 * @author Tobias Doenz
 *
 * @see org.mozartspaces.core.requests.EntriesRequest
 */
public enum Operation {

    /**
     * Read entries from a container, corresponds to {@link org.mozartspaces.core.requests.ReadEntriesRequest
     * ReadEntriesRequest}.
     */
    READ,

    /**
     * Test for entries in a container, corresponds to {@link org.mozartspaces.core.requests.TestEntriesRequest
     * TestEntriesRequest}.
     */
    TEST,

    /**
     * Take entries from a container (consuming read or atomic read and delete), corresponds to
     * {@link org.mozartspaces.core.requests.TakeEntriesRequest TakeEntriesRequest}.
     */
    TAKE,

    /**
     * Delete entries from a container, corresponds to {@link org.mozartspaces.core.requests.DeleteEntriesRequest
     * DeleteEntriesRequest}.
     */
    DELETE,

    /**
     * Write entries to a container, corresponds to {@link org.mozartspaces.core.requests.WriteEntriesRequest
     * WriteEntriesRequest}.
     */
    WRITE;

    /**
     * An unmodifiable set of all operation types, that is, all values of the enumeration.
     */
    public static final Set<Operation> ALL = Collections
            .unmodifiableSet(new HashSet<Operation>(Arrays.asList(values())));

}
