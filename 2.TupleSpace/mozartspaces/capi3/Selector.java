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
 * The <code>Selector</code> Interface has to be implemented by every provided
 * Selector in the system. Every selector is always associated with a specific
 * Coordinator and is used to select the required data of a container.
 *
 * @author Martin Barisits
 */
public interface Selector extends Serializable {

    /**
     * To select all available Entries matching the specific Selector.
     */
    int COUNT_MAX = -1;

    /**
     * To sellect all Entries matching the specific Selector If at least one
     * Entry is not available, <code>EntryLockedException</code> is returned.
     */
    int COUNT_ALL = -2;

    /**
     * Return the Name of the Selector.
     *
     * @return name of the selector
     */
    String getName();

    /**
     * Returns the Count of the Selector.
     *
     * @return the Count of the Selector
     */
    int getCount();

}
