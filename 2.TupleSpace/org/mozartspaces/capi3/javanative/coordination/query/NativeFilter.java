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
package org.mozartspaces.capi3.javanative.coordination.query;

import java.util.Iterator;

import org.mozartspaces.capi3.CountNotMetException;
import org.mozartspaces.capi3.javanative.operation.NativeEntry;

/**
 * Interface of Filters defining a Query.
 *
 * @author Martin Barisits
 */
public interface NativeFilter {

    /**
     * Return the selected entries.
     *
     * @param entries
     *            to apply filter to
     * @return selected entries
     * @throws CountNotMetException
     *             if a certain count could not be met
     */
    Iterator<NativeEntry> select(final Iterator<NativeEntry> entries) throws CountNotMetException;

    /**
     * @return the query this filter belongs to
     */
    DefaultQuery getQuery();
}
