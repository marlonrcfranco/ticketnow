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
import java.util.Set;

import org.mozartspaces.capi3.InvalidTransactionException;
import org.mozartspaces.capi3.LogItem;
import org.mozartspaces.capi3.Transaction;
import org.mozartspaces.capi3.javanative.persistence.cache.PersistenceCache;
import org.mozartspaces.capi3.javanative.persistence.db.TransactionalDBAdapter;
import org.mozartspaces.capi3.javanative.persistence.key.PersistenceKey;
import org.mozartspaces.capi3.javanative.persistence.key.PersistenceKeyFactory;
import org.mozartspaces.core.util.SerializationException;
import org.mozartspaces.core.util.Serializer;

/**
 * An implementation of stored map for backends that support concurrent long running transactions.
 *
 * @param <K>
 *     the type of keys
 * @param <V>
 *     the type of values
 *
 * @author Jan Zarnikov
 */
public final class TransactionalStoredMap<K, V extends Serializable> extends AbstractStoredMap<K, V> {

    private final TransactionalDBAdapter<K> dbAdapter;

    private final Serializer serializer;

    private final PersistenceCache<V> cache;

    /**
     * Create a new transactional stored map with the given database adapter for the backend.
     *
     * @param name
     *            the name of the map
     * @param persistenceKeyFactory
     *            a key factory for converting stored keys to actual key objects
     * @param cache
     *            a value cache.
     * @param dbAdapter
     *            a database adapter for accessing the database container.
     * @param serializer
     *            a thread-safe serializer for converting the values from/to {@code byte[]}
     */
    public TransactionalStoredMap(final String name, final PersistenceKeyFactory<K> persistenceKeyFactory,
                                  final PersistenceCache<V> cache, final TransactionalDBAdapter<K> dbAdapter,
                                  final Serializer serializer) {
        super(name, persistenceKeyFactory);
        this.cache = cache;
        this.dbAdapter = dbAdapter;
        this.serializer = serializer;
    }

    @Override
    public void clear() throws PersistenceException {
        dbAdapter.clear();

    }

    @Override
    public void close() throws PersistenceException {
        dbAdapter.close();
    }

    @Override
    public synchronized V get(final PersistenceKey<K> key) throws PersistenceException {
        V value = cache.get(key);
        if (value != null) {
            return value;
        }
        try {
            byte[] data = dbAdapter.get(key);
            if (data != null) {
                return serializer.deserialize(data);
            } else {
                return null;
            }
        } catch (SerializationException e) {
            throw new PersistenceException("Could not deserialize data from storage.", e);
        }
    }

    @Override
    public synchronized void put(final PersistenceKey<K> key, final V value, final Transaction tx)
            throws PersistenceException {
        try {
            cache.put(key, value);
            byte[] data = serializer.serialize(value);
            dbAdapter.put(key, data, tx);
            if (tx != null) {
                try {
                    tx.addLog(new LogItem() {
                        @Override
                        public void commitSubTransaction() {
                        }

                        @Override
                        public void commitTransaction() {
                        }

                        @Override
                        public void rollbackTransaction() {
                            cache.delete(key);
                        }

                        @Override
                        public void rollbackSubTransaction() {
                    }
                    });
                } catch (InvalidTransactionException e) {
                    cache.delete(key);
                }
            }
        } catch (SerializationException e) {
            throw new PersistenceException("Cound not serialize data for storage.", e);
        }
    }

    @Override
    public synchronized void remove(final PersistenceKey<K> key, final Transaction tx) throws PersistenceException {
        dbAdapter.delete(key, tx);
        cache.delete(key);
    }

    @Override
    public int size() throws PersistenceException {
        return dbAdapter.count();
    }

    @Override
    public synchronized Set<K> keySet() throws PersistenceException {
        Set<K> keySet = new HashSet<K>();
        for (PersistenceKey<K> key : dbAdapter.keySet()) {
            keySet.add(key.getKey());
        }
        return keySet;
    }

    @Override
    public void destroy() throws PersistenceException {
        dbAdapter.destroy();
    }
}
