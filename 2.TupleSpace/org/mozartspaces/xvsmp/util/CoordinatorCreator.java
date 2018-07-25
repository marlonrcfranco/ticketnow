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
package org.mozartspaces.xvsmp.util;

import org.mozartspaces.capi3.Coordinator;

/**
 * A {@code CoordinatorCreator} creates coordinators, instances of a specific
 * coordinator type.
 *
 * @author Tobias Doenz
 *
 * @param <T>
 *            the type of the coordinator this creator creates
 */
public interface CoordinatorCreator<T extends Coordinator> {

    /**
     * Creates a new coordinator.
     *
     * @param name
     *            the name of the coordinator, to distinguish instances of the
     *            same coordinator type
     * @param params
     *            further coordinator parameters
     * @return the created coordinator
     * @throws IllegalArgumentException
     *             if the name or another argument is not allowed
     */
    T newCoordinator(final String name, final Object... params);

}
