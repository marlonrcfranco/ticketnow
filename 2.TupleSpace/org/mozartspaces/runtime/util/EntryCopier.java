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
package org.mozartspaces.runtime.util;

import java.io.Serializable;
import java.util.List;

import org.mozartspaces.core.Entry;
import org.mozartspaces.core.RequestContext;

/**
 * Copies entry objects with the selected method.
 *
 * @author Tobias Doenz
 */
public interface EntryCopier {

    /**
     * Copies a list of entry values.
     *
     * @param entries
     *            the entry value list
     * @return the entry value list copy
     * @throws EntryCopyingException
     *             if copying the entries failed
     */
    List<? extends Serializable> copyEntryValues(List<? extends Serializable> entries) throws EntryCopyingException;

    /**
     * Copies a list of entries.
     *
     * @param entries
     *            the entry list
     * @return the entry list copy
     * @throws EntryCopyingException
     *             if copying the entries failed
     */
    List<Entry> copyEntries(List<Entry> entries) throws EntryCopyingException;

    /**
     * Copies a request context.
     *
     * @param context
     *            the request context to copy
     * @return a deep copy of the context, if configured in the implementation,
     *         the reference of the unmodified context otherwise
     * @throws EntryCopyingException
     *             if copying the context failed
     */
    RequestContext copyContext(RequestContext context) throws EntryCopyingException;
}
