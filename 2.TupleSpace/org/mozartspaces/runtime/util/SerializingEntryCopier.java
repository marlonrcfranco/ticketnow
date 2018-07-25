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
import org.mozartspaces.core.util.SerializationException;
import org.mozartspaces.core.util.Serializer;
import org.mozartspaces.util.LoggerFactory;
import org.slf4j.Logger;

/**
 * Copies entries by serializing and deserializing them.
 *
 * @author Tobias Doenz
 */
@Immutable
public final class SerializingEntryCopier implements EntryCopier {

    private static final Logger log = LoggerFactory.get();

    private final Serializer serializer;

    /**
     * Constructs a <code>SerializingEntryCopier</code>.
     *
     * @param serializer
     *            the serializer to use
     */
    public SerializingEntryCopier(final Serializer serializer) {
        this.serializer = serializer;
        assert this.serializer != null;
    }

    @Override
    public List<? extends Serializable> copyEntryValues(final List<? extends Serializable> entries)
            throws EntryCopyingException {
        log.debug("Copying entry values with serialization");
        List<Serializable> copiedEntryValues = new ArrayList<Serializable>(entries.size());
        try {
            for (Serializable entry : entries) {
                copiedEntryValues.add(serializer.copyObject(entry));
            }
        } catch (SerializationException ex) {
            throw new EntryCopyingException("Could not copy entry values", ex);
        }
        return copiedEntryValues;
    }

    @Override
    public List<Entry> copyEntries(final List<Entry> entries) throws EntryCopyingException {
        assert entries != null;
        log.debug("Copying entries with serialization");
        List<Entry> copiedEntries = new ArrayList<Entry>(entries.size());
        try {
            for (Entry entry : entries) {
                copiedEntries.add(new Entry(serializer.copyObject(entry.getValue()), entry.getCoordinationData()));
            }
        } catch (SerializationException ex) {
            throw new EntryCopyingException("Could not copy entries", ex);
        }
        return copiedEntries;
    }

    @Override
    public RequestContext copyContext(final RequestContext context) throws EntryCopyingException {
        if (context != null) {
            log.debug("Copying request context with serialization");
            try {
                return serializer.copyObject(context);
            } catch (SerializationException ex) {
                throw new EntryCopyingException("Could not copy context", ex);
            }
        }
        return context;
    }

}