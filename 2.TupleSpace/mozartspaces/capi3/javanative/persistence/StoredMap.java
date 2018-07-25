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

import java.io.Closeable;
import java.io.Serializable;
import java.util.Set;

import org.mozartspaces.capi3.Transaction;

/**
 * StoredMap is a key-value-store similar to {@link java.util.Map}. Depending on the implementation the content of the
 * map may or be persistently stored on the file system or in a database.
 *
 * All read operations ({@link org.mozartspaces.capi3.javanative.persistence.StoredMap#get(Object)},
 * {@link org.mozartspaces.capi3.javanative.persistence.StoredMap#size()},
 * {@link org.mozartspaces.capi3.javanative.persistence.StoredMap#keySet()},
 * {@link org.mozartspaces.capi3.javanative.persistence.StoredMap#containsKey}) use the read-uncommitted semantics. This
 * means that there is no transactional isolation! Changes done by one transaction are immediately visible to other
 * transactions. <br/>
 * <br/>
 * The write operations (
 * {@link org.mozartspaces.capi3.javanative.persistence.StoredMap#put(Object, java.io.Serializable, Transaction)}
 * , {@link org.mozartspaces.capi3.javanative.persistence.StoredMap#remove(Object, org.mozartspaces.capi3.Transaction)})
 * are bound to the specified transaction to provide atomicity. This means that if the StoredMap is persistent
 * (file system or database) then the changes are written to persistent storage atomically (all or nothing) on
 * transaction commit or undone on transaction rollback. <br/>
 * <br/>
 *
 * You may also use {@code null} as transaction which will result in writing the changes immediately using an implicit
 * transaction. <br/>
 * <br/>
 *
 * Neither keys nor values can be {@code null}. <br/>
 * <br/>
 *
 * You can obtain an instance of StoredMap from
 * {@link PersistenceContext#createStoredMap(String,
 * org.mozartspaces.capi3.javanative.persistence.key.PersistenceKeyFactory)}.
 *
 * @param <K>
 *            the type of keys
 * @param <V>
 *            the type of values
 *
 * @author Jan Zarnikov
 * @author Tobias Doenz
 */
public interface StoredMap<K, V extends Serializable> extends Closeable {

    /**
     * @return the name of the stored map
     */
    String getName();

    /**
     * Get the value mapped to the given key.
     *
     * @param key
     *            the key of the value that should be retrieved.
     * @return the value associated with the given key or null if the key is not present in the map.
     * @throws PersistenceException
     *             if the underlying storage engine fails during this operation.
     */
    V get(K key) throws PersistenceException;

    /**
     * Store a new key-value pair in this map. If there is already a key-value pair with the same key (according to
     * {@link Object#equals(Object)} and {@link Object#hashCode()} it will be overwritten with the new value. The change
     * will be persisted once the transaction is committed.
     *
     * @param key
     *            the key that shall be written.
     * @param value
     *            the new value associated with the key.
     * @param tx
     *            Transaction associated with this write operation. May be null which is result in an implicit
     *            transaction beeing used - in that case data will be persisted immediately.
     * @throws PersistenceException
     *             if the underlying storage engine fails during this operation.
     */
    void put(K key, V value, Transaction tx) throws PersistenceException;

    /**
     * Store a new key-value pair in this map. Equivalent to {@code put(key, value, null)}.
     *
     * @param key
     *            the key that shall be written.
     * @param value
     *            the new value associated with the key.
     * @throws PersistenceException
     *             if the underlying storage engine fails during this operation.
     */
    void put(K key, V value) throws PersistenceException;

    /**
     * Remove a key-value pair from this map. If the given key is not present in the map this method returns without any
     * effects.
     *
     * @param key
     *            the key that should be removed from this map.
     * @param tx
     *            Transaction associated with this write operation. May be null which is result in an implicit
     *            transaction beeing used - in that case data will be removed from persistent storage immediately.
     * @throws PersistenceException
     *             if the underlying storage engine fails during this operation.
     */
    void remove(K key, Transaction tx) throws PersistenceException;

    /**
     * Remove a key-value pair from this map. Equivalent to {@code remove(key, null)}.
     *
     * @param key
     *            the key that should be removed from this map.
     * @throws PersistenceException
     *             if the underlying storage engine fails during this operation.
     */
    void remove(K key) throws PersistenceException;

    /**
     * Returns {@code true} if this map contains the given key. This is equivalent to {@code get(key) == null} but
     * depending on the implementation might be faster.
     *
     * @param key
     *            key whose presence you want to test
     * @return true if the given key is present in the map.
     * @throws PersistenceException
     *             if the underlying storage engine fails during this operation.
     */
    boolean containsKey(K key) throws PersistenceException;

    /**
     * Compute the current number of key-value pairs in the map including data from uncommitted transactions.
     *
     * @return number of key-value pairs in this map.
     * @throws PersistenceException
     *             if the underlying storage engine fails during this operation.
     */
    int size() throws PersistenceException;

    /**
     * Remove all key-value pairs from this map. If there is any data writen into the map by not yet committed
     * transaction the behaviour of this method is undefined.
     *
     * @throws PersistenceException
     *             if the underlying storage engine fails during this operation.
     */
    void clear() throws PersistenceException;

    /**
     * Close the map and all ressources associated with it. This is usually done by the {@link PersistenceContext}. This
     * method may fail if there are uncommitted transactions that modified this map.
     *
     * @throws PersistenceException
     *             if the underlying storage engine fails during this operation.
     */
    @Override
    void close() throws PersistenceException;

    /**
     *
     * @return The set of keys currently stored in the map.
     * @throws PersistenceException
     *             if the underlying storage engine fails during this operation.
     */
    Set<K> keySet() throws PersistenceException;

    /**
     * Delete this stored map and all of its content. All data as well as the map itself will be deleted from the
     * persistent storage. After calling this method the map becomes invalid and cannot be used or restored. You may
     * {@link #close() close} the map before destroying it but it is not necessary.
     *
     * @throws PersistenceException
     *             if the underlying storage engine fails during this operation.
     */
    void destroy() throws PersistenceException;

}
