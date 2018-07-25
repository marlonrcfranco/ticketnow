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
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.mozartspaces.capi3.Transaction;
import org.mozartspaces.capi3.javanative.persistence.cache.PersistenceCache;
import org.mozartspaces.capi3.javanative.persistence.db.DBAdapter;
import org.mozartspaces.capi3.javanative.persistence.key.PersistenceKey;
import org.mozartspaces.capi3.javanative.persistence.key.PersistenceKeyFactory;
import org.mozartspaces.core.util.SerializationException;
import org.mozartspaces.core.util.Serializer;

/**
 * An implementation of {@link StoredMap} for backends that do not support concurrent long running transactions.
 * All write operations are buffered and flushed when the transaction is committed.
 *
 * @param <K>
 *     type of the keys
 * @param <V>
 *     type of values.
 *
 *
 * @author Jan Zarnikov
 */
//@SuppressWarnings("unused") // currently only used in Android version
public final class BufferedStoredMap<K, V extends Serializable> extends AbstractStoredMap<K, V> {

    private final DBAdapter<K> dbAdapter;

    private final DeferredDB deferredDB;

    private final Serializer serializer;

    private final Map<PersistenceKey<K>, byte[]> writeBuffer = new ConcurrentHashMap<PersistenceKey<K>, byte[]>();
    private final Map<PersistenceKey<K>, PersistenceKey<K>> deleteBuffer =
            new ConcurrentHashMap<PersistenceKey<K>, PersistenceKey<K>>();
    private final PersistenceCache<V> cache;

    /**
     * Creates a new buffered stored map.
     *
     * @param name
     *            the name of the map
     * @param persistenceKeyFactory
     *            a key factory for converting stored keys to actual key objects
     * @param cache
     *            a value cache.
     * @param dbAdapter
     *            a database adapter for accessing the database container.
     * @param deferredDB
     *            a container for buffered transactions.
     * @param serializer
     *            a thread-safe serializer for converting the values from/to {@code byte[]}
     */
    public BufferedStoredMap(final String name, final PersistenceKeyFactory<K> persistenceKeyFactory,
                             final PersistenceCache<V> cache, final DBAdapter<K> dbAdapter, final DeferredDB deferredDB,
                             final Serializer serializer) {
        super(name, persistenceKeyFactory);
        this.cache = cache;
        this.dbAdapter = dbAdapter;
        this.deferredDB = deferredDB;
        this.serializer = serializer;
    }

    @Override
    public synchronized void clear() throws PersistenceException {
        if (writeBuffer.isEmpty() && deleteBuffer.isEmpty()) {
            dbAdapter.clear();
        } else {
            throw new PersistenceException("This map cannot be cleared because there are still opened transactions");
        }
    }

    @Override
    public void close() throws PersistenceException {
        dbAdapter.close();
    }


    @Override
    public synchronized void remove(final PersistenceKey<K> key, final Transaction tx) throws PersistenceException {
        deleteBuffer.put(key, key);
        writeBuffer.remove(key);
        if (tx == null) {
            dbAdapter.delete(key);
        } else {
            deferredDB.addPersistenceOperation(tx, new PersistenceOperation() {
                @Override
                public void commit() throws PersistenceException {
                    deleteBuffer.remove(key);
                    dbAdapter.delete(key);
                }

                @Override
                public void rollback() throws PersistenceException {
                    deleteBuffer.remove(key);
                }
            });
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public synchronized V get(final PersistenceKey<K> key) throws PersistenceException {
        byte[] data;
        if (deleteBuffer.containsKey(key)) {
            return null;
        }
        if (writeBuffer.containsKey(key)) {
            data = writeBuffer.get(key);
        } else {
            V value = cache.get(key);
            if (value != null) {
                return value;
            }
            data = dbAdapter.get(key);
        }
        if (data != null) {
            try {
                return (V) serializer.deserialize(data);
            } catch (SerializationException e) {
                throw new PersistenceException("Could nor restore object from DB.", e);
            }
        } else {
            return null;
        }
    }

    @Override
    public synchronized void put(final PersistenceKey<K> key, final V value, final Transaction tx)
            throws PersistenceException {
        final byte[] data;
        try {
            data = serializer.serialize(value);
        } catch (SerializationException e) {
            throw new PersistenceException("Could not store object in DB.", e);
        }
        if (tx == null) {
            dbAdapter.put(key, data);
        } else {
            writeBuffer.put(key, data);
            deleteBuffer.remove(key);
            deferredDB.addPersistenceOperation(tx, new PersistenceOperation() {
                @Override
                public void commit() throws PersistenceException {
                    dbAdapter.put(key, data);
                    writeBuffer.remove(key);
                }

                @Override
                public void rollback() throws PersistenceException {
                    writeBuffer.remove(key);
                }
            });
        }
    }

    @Override
    public int size() throws PersistenceException {
        return dbAdapter.count() + writeBuffer.size() - deleteBuffer.size();
    }


    @Override
    public synchronized Set<K> keySet() throws PersistenceException {
        Set<K> keySet = new HashSet<K>();
        Set<PersistenceKey<K>> persistenceKeys = dbAdapter.keySet();
        persistenceKeys.addAll(writeBuffer.keySet());
        persistenceKeys.removeAll(deleteBuffer.keySet());
        for (PersistenceKey<K> key : persistenceKeys) {
            keySet.add(key.getKey());
        }
        return keySet;
    }

    @Override
    public void destroy() throws PersistenceException {
        dbAdapter.destroy();
    }
}
