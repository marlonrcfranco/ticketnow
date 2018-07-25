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
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.mozartspaces.capi3.InvalidTransactionException;
import org.mozartspaces.capi3.LogItem;
import org.mozartspaces.capi3.Transaction;

/**
 * A wrapper for {@link StoredMap} that caches the set of keys in memory for better performance.
 *
 * @param <K>
 *     type of keys
 * @param <V>
 *     type of values
 *
 * @author Jan Zarnikov
 */
public final class CachedKeySetStoredMapWrapper<K, V extends Serializable> implements StoredMap<K, V> {

    private final StoredMap<K, V> wrapped;

    private final Map<K, K> keys = new ConcurrentHashMap<K, K>();

    /**
     * Creates a new stored map wrapper.
     *
     * @param wrapped the stored map on which all operations should be delegated.
     */
    public CachedKeySetStoredMapWrapper(final StoredMap<K, V> wrapped) {
        this.wrapped = wrapped;
        for (K key: wrapped.keySet()) {
            keys.put(key, key);
        }
    }

    @Override
    public String getName() {
        return wrapped.getName();
    }

    @Override
    public V get(final K key) throws PersistenceException {
        return wrapped.get(key);
    }

    @Override
    public synchronized void put(final K key, final V value, final Transaction tx) throws PersistenceException {
        wrapped.put(key, value, tx);
        keys.put(key, key);
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
                        keys.remove(key);
                    }

                    @Override
                    public void rollbackSubTransaction() {
                    }
                });
            } catch (InvalidTransactionException e) {
                throw new PersistenceException("Error while writing keys cache.", e);
            }
        }
    }

    @Override
    public synchronized void put(final K key, final V value) throws PersistenceException {
        wrapped.put(key, value);
        keys.put(key, key);
    }

    @Override
    public synchronized void remove(final K key, final Transaction tx) throws PersistenceException {
        wrapped.remove(key, tx);
        keys.remove(key);
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
                        keys.put(key, key);
                    }

                    @Override
                    public void rollbackSubTransaction() {
                    }
                });
            } catch (InvalidTransactionException e) {
                throw new PersistenceException("Error while writing keys cache.", e);
            }
        }
    }

    @Override
    public synchronized void remove(final K key) throws PersistenceException {
        wrapped.remove(key);
        keys.remove(key);
    }

    @Override
    public boolean containsKey(final K key) throws PersistenceException {
        return keys.containsKey(key);
    }

    @Override
    public int size() throws PersistenceException {
        return keys.size();
    }

    @Override
    public synchronized void clear() throws PersistenceException {
        wrapped.clear();
        keys.clear();
    }

    @Override
    public void close() throws PersistenceException {
        wrapped.close();
    }

    @Override
    public Set<K> keySet() throws PersistenceException {
        return keys.keySet();
    }

    @Override
    public void destroy() throws PersistenceException {
        wrapped.destroy();
    }
}
