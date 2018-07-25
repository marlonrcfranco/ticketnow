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
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.mozartspaces.capi3.Transaction;

/**
 * An implementation of the {@link StoredMap} that holds its data in memory only. Nothing is stored persistently.
 *
 * @param <K>
 *     type of keys
 * @param <V>
 *     type of values
 *
 * @author Jan Zarnikov
 */
public final class InMemoryStoredMap<K, V extends Serializable> implements StoredMap<K, V> {

    private final ConcurrentHashMap<K, V> data = new ConcurrentHashMap<K, V>();

    private final String name;
    private final DeferredDB deferredDB;

    private volatile boolean destroyed = false;

    /**
     * Creates a new in-memory map.
     *
     * @param name
     *            the name of the map
     * @param deferredDB
     *            a container for buffered transactions.
     */
    public InMemoryStoredMap(final String name, final DeferredDB deferredDB) {
        this.name = name;
        this.deferredDB = deferredDB;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void clear() throws PersistenceException {
        data.clear();
    }

    @Override
    public V get(final K key) throws PersistenceException {
        if (destroyed) {
            throw new IllegalStateException("This stored map was destroyed.");
        }
        return data.get(key);
    }

    @Override
    public void put(final K key, final V value, final Transaction tx) throws PersistenceException {
        if (destroyed) {
            throw new IllegalStateException("This stored map was destroyed.");
        }
        data.put(key, value);
        if (tx != null) {
            deferredDB.addPersistenceOperation(tx, new PersistenceOperation() {
                @Override
                public void commit() throws PersistenceException {
                }

                @Override
                public void rollback() throws PersistenceException {
                    data.remove(key);
                }
            });
        }
    }

    @Override
    public void put(final K key, final V value) throws PersistenceException {
        if (destroyed) {
            throw new IllegalStateException("This stored map was destroyed.");
        }
        data.put(key, value);
    }

    @Override
    public void remove(final K key, final Transaction tx) throws PersistenceException {
        if (destroyed) {
            throw new IllegalStateException("This stored map was destroyed.");
        }
        final V value = data.remove(key);
        if (tx != null && value != null) {
            deferredDB.addPersistenceOperation(tx, new PersistenceOperation() {
                @Override
                public void commit() throws PersistenceException {
                }

                @Override
                public void rollback() throws PersistenceException {
                    data.put(key, value);
                }
            });
        }
    }

    @Override
    public void remove(final K key) throws PersistenceException {
        if (destroyed) {
            throw new IllegalStateException("This stored map was destroyed.");
        }
        data.remove(key);
    }

    @Override
    public boolean containsKey(final K key) throws PersistenceException {
        if (destroyed) {
            throw new IllegalStateException("This stored map was destroyed.");
        }
        return data.containsKey(key);
    }

    @Override
    public int size() throws PersistenceException {
        if (destroyed) {
            throw new IllegalStateException("This stored map was destroyed.");
        }
        return data.size();
    }

    @Override
    public void close() throws PersistenceException {
        data.clear();
    }

    @Override
    public Set<K> keySet() throws PersistenceException {
        if (destroyed) {
            throw new IllegalStateException("This stored map was destroyed.");
        }
        return Collections.unmodifiableSet(data.keySet());
    }

    @Override
    public void destroy() throws PersistenceException {
        destroyed = true;
        close();
    }
}
