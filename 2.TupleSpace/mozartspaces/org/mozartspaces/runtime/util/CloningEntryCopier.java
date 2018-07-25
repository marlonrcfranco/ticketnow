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
import java.util.ArrayList;
import java.util.List;

import net.jcip.annotations.Immutable;

import org.mozartspaces.core.Entry;
import org.mozartspaces.core.RequestContext;
import org.mozartspaces.util.LoggerFactory;
import org.mozartspaces.util.MzsCloneable;
import org.slf4j.Logger;

/**
 * Copies entries by calling the {@link Object#clone() clone} method with
 * reflection.
 *
 * @author Tobias Doenz
 */
@Immutable
public final class CloningEntryCopier implements EntryCopier {

    private static final Logger log = LoggerFactory.get();

    @Override
    public List<? extends Serializable> copyEntryValues(final List<? extends Serializable> entries)
            throws EntryCopyingException {

        assert entries != null;

        log.debug("Copying entry values with cloning");
        List<Serializable> clonedEntries = new ArrayList<Serializable>(entries.size());
        for (Serializable entry : entries) {
            try {
                Serializable clonedEntry = null;
                try {
                    clonedEntry = (Serializable) ((MzsCloneable) entry).clone();
                } catch (ClassCastException ex) {
                    throw new CloneNotSupportedException(ex.toString());
                }
                clonedEntries.add(clonedEntry);
            } catch (CloneNotSupportedException ex) {
                throw new EntryCopyingException("Could not clone entry value", ex);
            }
        }
        return clonedEntries;
    }

    @Override
    public List<Entry> copyEntries(final List<Entry> entries) throws EntryCopyingException {

        assert entries != null;

        log.debug("Copying entries with cloning");
        List<Entry> clonedEntries = new ArrayList<Entry>(entries.size());
        for (Entry entry : entries) {
            try {
                clonedEntries.add(entry.clone());
            } catch (CloneNotSupportedException ex) {
                throw new EntryCopyingException("Could not clone entry", ex);
            }
        }
        return clonedEntries;
    }

    @Override
    public RequestContext copyContext(final RequestContext context) throws EntryCopyingException {

        if (context != null) {
            log.debug("Copying request context with cloning");
            try {
                return context.clone();
            } catch (CloneNotSupportedException ex) {
                throw new EntryCopyingException("Could not clone request context", ex);
            }
        }
        return context;
    }
}