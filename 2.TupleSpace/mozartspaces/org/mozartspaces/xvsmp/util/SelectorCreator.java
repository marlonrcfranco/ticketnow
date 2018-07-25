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

import org.mozartspaces.capi3.Selector;

/**
 * A {@code SelectorCreator} creates selectors, instances of a specific selector
 * type.
 *
 * @author Tobias Doenz
 *
 * @param <T>
 *            the type of the selector this creator creates
 */
public interface SelectorCreator<T extends Selector> {

    /**
     * Creates a new selector.
     *
     * @param name
     *            the name of the coordinator, to distinguish instances of the same
     *            coordinator type
     * @param count
     *            the entry count for this selector
     * @param params
     *            further selector parameters
     * @return the created selector
     * @throws IllegalArgumentException
     *             if the name or another argument is not allowed
     */
    T newSelector(final String name, final Integer count, final Object... params);

}
