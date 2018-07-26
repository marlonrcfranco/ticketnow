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
package org.mozartspaces.capi3.javanative.persistence;

import java.io.Serializable;
import java.util.List;
import java.util.Properties;
import java.util.Vector;

import org.mozartspaces.capi3.LogItem;
import org.mozartspaces.capi3.Transaction;
import org.mozartspaces.capi3.javanative.operation.NativeEntry;
import org.mozartspaces.capi3.javanative.persistence.cache.PersistenceCache;
import org.mozartspaces.capi3.javanative.persistence.key.PersistenceKeyFactory;
import org.mozartspaces.core.util.Serializer;

/**
 * An in-memory persistence backend. All data is held in Java Collections and will be lost once the backend is closed
 * or when the JVM exits.
 *
 * @author Jan Zarnikov
 */
public final class InMemoryDB implements PersistenceBackend {

    private final DeferredDB deferredDB = new DeferredDB();

    private final List<StoredMap<?, ?>> storedMaps = new Vector<StoredMap<?, ?>>();

    private final EmptyBatchMode emptyBatchMode = new EmptyBatchMode();

    @Override
    public void close() {
        for (StoredMap<?, ?> storedMap : storedMaps) {
            try {
                storedMap.close();
            } catch (PersistenceException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void init(final Properties properties) {
    }

    @Override
    public <K, V extends Serializable> StoredMap<K, V> createNewStoredMap(final String name,
                                                                          final PersistenceKeyFactory<K>
                                                                                  persistenceKeyFactory,
                                                                          final Serializer serializer,
                                                                          final PersistenceCache<V> cache)
            throws PersistenceException {
        InMemoryStoredMap<K, V> storedMap = new InMemoryStoredMap<K, V>(name, deferredDB);
        storedMaps.add(storedMap);
        return storedMap;
    }

    @Override
    public OrderedLongSet createOrderedLongSet(final StoredMap<Long, long[]> data) {
        return new DummyOrderedLongSet();
    }

    @Override
    public LogItem createPersistentTransaction(final Transaction tx) {
        return deferredDB.createPersistentTransaction(tx, emptyBatchMode);
    }

    /**
     * An implementation of batch mode that does not do anything.
     */
    private static class EmptyBatchMode implements DeferredDBBatchMode {
        @Override
        public void startBatchMode() throws PersistenceException {
        }

        @Override
        public void endBatchMode(final boolean successful) throws PersistenceException {
        }
    }

    @Override
    public NativeEntry makeEntryLazy(final NativeEntry nativeEntry) {
        return nativeEntry;
    }
}
