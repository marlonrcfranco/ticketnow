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
import java.util.Properties;

import org.mozartspaces.capi3.LogItem;
import org.mozartspaces.capi3.Transaction;
import org.mozartspaces.capi3.javanative.operation.NativeEntry;
import org.mozartspaces.capi3.javanative.persistence.cache.PersistenceCache;
import org.mozartspaces.capi3.javanative.persistence.key.PersistenceKeyFactory;
import org.mozartspaces.core.util.Serializer;

/**
 * This interface describes a persistence backend. This is an abstraction of the database-specific implementation
 * details.<br/>
 * <br/>
 *
 * The implementation class must provide a public default constructor.
 *
 * @author Jan Zarnikov
 */
public interface PersistenceBackend {

    /**
     * Initialize the persistence backend. This method is called right after the backend is instantiated (the default
     * constructor through reflections).
     *
     * @param properties
     *            a set of key-value pairs containing some optional configuration. The keys are implementation-specific.
     *            May be empty but not null.
     * @throws PersistenceException
     *             when the initialization fails
     */
    void init(Properties properties) throws PersistenceException;

    /**
     * Create or restore a StoredMap with the given name. If a name with the given name was already used during previous
     * sessions it will be restored and repopulated with data form the persistent storage.
     *
     * @param name
     *            name of the stored map. Note that depending on the implementation some special characters might be
     *            forbidden. The name has to be unique within this persistence backend.
     * @param persistenceKeyFactory
     *            the objects used as keys (of type {@code <K>} will be translated to objects suitable for the
     *            persistence backend using this factory.
     * @param serializer
     *            the serializer that will be used to convert the values from/to the persistent form.
     * @param cache
     *            a configured value-cache
     * @param <K>
     *            the type of the keys stored in this map.
     * @param <V>
     *            the type of the values stored in the map. Must be serializable by the {@link Serializer}
     * @return a new map for persistent storage, possibly already populated with data from previous sessions.
     * @throws PersistenceException
     *             when the backend fails to create the stored map
     */
    <K, V extends Serializable> StoredMap<K, V> createNewStoredMap(String name,
            PersistenceKeyFactory<K> persistenceKeyFactory, Serializer serializer, PersistenceCache<V> cache)
            throws PersistenceException;

    /**
     * Create a new ordered long set backed by the given stored map. Can be an empty implementation if this backend does
     * not support actual persistence.
     *
     * @param data
     *            a stored map backing the set
     * @return a set of ordered longs backed by the given stored map.
     */
    OrderedLongSet createOrderedLongSet(StoredMap<Long, long[]> data);

    /**
     * Creates a {@link LogItem} to mark this transaction as using this persistence context. The resulting LogItem must
     * be added to the transaction by the caller ({@link Transaction#addLog(org.mozartspaces.capi3.LogItem)}).
     *
     * @param tx
     *            transaction that wants to use this persistence context.
     * @return a LogItem that should be added to the transaction's log. It cannot be used for any transactions other
     *         than the one supplied as parameter.
     */
    LogItem createPersistentTransaction(Transaction tx);

    /**
     * Close the persistence backend and release all resources. After calling this method this persistence backend and
     * all stored maps created by it will become unusable.
     *
     * @throws PersistenceException
     *             if there are still any open transactions that used this persistence context by calling
     *             {@link PersistenceBackend#createPersistentTransaction(org.mozartspaces.capi3.Transaction)} using
     *             StoredMaps created by this persistence context.
     */
    void close();

    /**
     * If supported create a new proxy for the entry which allows the garbage collector to reclaim the space used by the
     * value of the entry and lazy-load it later if it is needed again. Otherwise return the entry unchanged.
     *
     * @param nativeEntry
     *            an entry that should be adapted for lazy-loading of its value
     * @return a lazy-loading proxy for the given entry or the unchanged entry itself.
     */
    NativeEntry makeEntryLazy(NativeEntry nativeEntry);
}
