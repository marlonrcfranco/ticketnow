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
import java.lang.reflect.Method;

import org.mozartspaces.capi3.Transaction;
import org.mozartspaces.capi3.javanative.persistence.key.PersistenceKey;
import org.mozartspaces.capi3.javanative.persistence.key.PersistenceKeyFactory;

/**
 * This class that implements the StoredMap interface. It takes care of translating between the keys of type K and the
 * keys used by the database of type PersistenceKey<K>
 *
 * @param <K>
 *            type of the keys
 * @param <V>
 *            type of the values
 *
 * @author Jan Zarnikov
 */
public abstract class AbstractStoredMap<K, V extends Serializable> implements StoredMap<K, V> {

    private final String name;
    private final PersistenceKeyFactory<K> persistenceKeyFactory;

    /**
     * The method {@link StoredMap#size()}.
     */
    public static final Method SIZE_METHOD;

    static {
        try {
            SIZE_METHOD = StoredMap.class.getMethod("size", (Class<?>[]) null);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    /**
     * Creates an new AbstractStored map.
     *
     * @param name
     *            the name of the map
     * @param persistenceKeyFactory
     *            the factory that translates between keys K and PersistenceKey<K>. Must be threadsafe.
     */
    protected AbstractStoredMap(final String name, final PersistenceKeyFactory<K> persistenceKeyFactory) {
        this.name = name;
        this.persistenceKeyFactory = persistenceKeyFactory;
    }

    @Override
    public final String getName() {
        return name;
    }

    @Override
    public final V get(final K key) throws PersistenceException {
        return get(persistenceKeyFactory.createPersistenceKey(key));
    }

    /**
     * See {@link StoredMap#get(Object)} for the definition of the semantics. The only difference is that this method
     * uses {@link PersistenceKey} for wrapping of keys.
     *
     * @param key
     *            the key of the value that should be retrieved
     * @return the value associated with the given key or null if the key is not present in the map.
     * @throws PersistenceException
     *             if the underlying storage engine fails during this operation
     */
    public abstract V get(PersistenceKey<K> key) throws PersistenceException;

    @Override
    public final void put(final K key, final V value, final Transaction tx) throws PersistenceException {
        put(persistenceKeyFactory.createPersistenceKey(key), value, tx);
    }

    @Override
    public final void put(final K key, final V value) throws PersistenceException {
        put(key, value, null);
    }

    /**
     * See {@link StoredMap#put(Object, java.io.Serializable, org.mozartspaces.capi3.Transaction)} for the definition of
     * the semantics. The only difference is that this method uses {@link PersistenceKey} for wrapping of keys.
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
    public abstract void put(final PersistenceKey<K> key, final V value, final Transaction tx)
            throws PersistenceException;

    @Override
    public final void remove(final K key, final Transaction tx) throws PersistenceException {
        remove(persistenceKeyFactory.createPersistenceKey(key), tx);
    }

    @Override
    public final void remove(final K key) throws PersistenceException {
        remove(key, null);
    }

    /**
     * See {@link StoredMap#remove(Object, org.mozartspaces.capi3.Transaction)} for the definition of the semantics. The
     * only difference is that this method uses {@link PersistenceKey} for wrapping of keys.
     *
     * @param key
     *            the key that should be removed from this map.
     * @param tx
     *            Transaction associated with this write operation. May be null which is result in an implicit
     *            transaction beeing used - in that case data will be removed from persistent storage immediately.
     * @throws PersistenceException
     *             if the underlying storage engine fails during this operation.
     */
    public abstract void remove(final PersistenceKey<K> key, final Transaction tx) throws PersistenceException;

    @Override
    public final boolean containsKey(final K key) throws PersistenceException {
        return get(key) != null;
    }
}
