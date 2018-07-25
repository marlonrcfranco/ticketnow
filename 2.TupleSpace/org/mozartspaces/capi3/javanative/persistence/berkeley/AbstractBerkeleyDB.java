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
package org.mozartspaces.capi3.javanative.persistence.berkeley;

import java.io.File;
import java.io.Serializable;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;

import org.mozartspaces.capi3.LogItem;
import org.mozartspaces.capi3.javanative.operation.LazyNativeEntry;
import org.mozartspaces.capi3.javanative.operation.NativeEntry;
import org.mozartspaces.capi3.javanative.persistence.CachedKeySetStoredMapWrapper;
import org.mozartspaces.capi3.javanative.persistence.OrderedLongSet;
import org.mozartspaces.capi3.javanative.persistence.PersistenceBackend;
import org.mozartspaces.capi3.javanative.persistence.PersistenceException;
import org.mozartspaces.capi3.javanative.persistence.StoredMap;
import org.mozartspaces.capi3.javanative.persistence.StoredOrderedLongSet;
import org.mozartspaces.capi3.javanative.persistence.TransactionalStoredMap;
import org.mozartspaces.capi3.javanative.persistence.berkeley.adapter.BerkeleyDBAdapter;
import org.mozartspaces.capi3.javanative.persistence.cache.PersistenceCache;
import org.mozartspaces.capi3.javanative.persistence.key.PersistenceKeyFactory;
import org.mozartspaces.core.util.Serializer;
import org.mozartspaces.util.LoggerFactory;
import org.slf4j.Logger;

import com.sleepycat.je.Durability;
import com.sleepycat.je.Environment;
import com.sleepycat.je.EnvironmentConfig;
import com.sleepycat.je.Transaction;

/**
 * A persistence backend that contains the common code for all Berkeley profiles.
 *
 * @author Jan Zarnikov
 */
public abstract class AbstractBerkeleyDB implements PersistenceBackend, TransactionMapper {

    private final Map<String, Transaction> activeTransactions = new ConcurrentHashMap<String, Transaction>();

    private Properties properties;

    private Environment environment;

    private final List<StoredMap<?, ?>> openStoredMaps = new Vector<StoredMap<?, ?>>();

    private static final Logger log = LoggerFactory.get();

    @Override
    public final Transaction getTransaction(final String capiTransactionId) {
        if (capiTransactionId == null) {
            return null;
        }
        return activeTransactions.get(capiTransactionId);
    }

    @Override
    public final Transaction getTransaction(final org.mozartspaces.capi3.Transaction capiTransaction) {
        if (capiTransaction != null) {
            return getTransaction(capiTransaction.getId());
        } else {
            return null;
        }
    }

    /**
     * Initialize the persistence backend.
     *
     * @param properties
     *            a set of key-value pairs containing some optional configuration. The only keys currently used are
     *            {@link BerkeleyConfig#LOCATION} and {@link BerkeleyConfig#CACHE_SIZE}.
     * @throws PersistenceException
     *             when the initialization fails.
     */
    @Override
    public final void init(final Properties properties) throws PersistenceException {
        log.info("Inititalizing Berkeley DB persistence backend.");
        String location = properties.getProperty(BerkeleyConfig.LOCATION, BerkeleyConfig.LOCATION_DEFAULT);
        this.properties = properties;
        EnvironmentConfig environmentConfig = new EnvironmentConfig();
        environmentConfig.setTransactional(true);
        environmentConfig.setAllowCreate(true);
        long cacheSize;
        try {
            cacheSize = Long.parseLong(properties.getProperty(BerkeleyConfig.CACHE_SIZE));
        } catch (Exception e) {
            cacheSize = BerkeleyConfig.CACHE_SIZE_DEFAULT;
        }
        log.debug("Berkeley DB will use a cache of " + cacheSize + "B.");
        environmentConfig.setCacheSize(cacheSize);
        environmentConfig.setDurability(getDurability());
        final File envHome = new File(location);
        if (envHome.exists() && !envHome.isDirectory()) {
            throw new PersistenceException("The persistent storage location \"" + location
                    + "\" must be a directory, not a file.");
        } else if (!envHome.exists()) {
            if (!envHome.mkdirs()) {
                throw new PersistenceException("Cannot create directory for persistent storage \"" + location + "\"");
            }
        }
        log.info("All data will be stored in " + envHome.getAbsolutePath());
        environment = new Environment(envHome, environmentConfig);
    }

    /**
     * Get the {@link Durability} type used by this backend.
     *
     * @return a durability type
     */
    public abstract Durability getDurability();

    @Override
    public final void close() {
        log.info("Closing Berkeley DB persistence context.");
        PersistenceException exception = null;
        Iterator<StoredMap<?, ?>> mapIterator = openStoredMaps.iterator();
        while (mapIterator.hasNext()) {
            StoredMap<?, ?> storedMap = mapIterator.next();
            try {
                storedMap.close();
                mapIterator.remove();
            } catch (PersistenceException e) {
                // keep it for later and try to close as many resources as possible.
                exception = e;
            }
        }

        // this is a workaround for a bug in Berkeley DB.
        // for some reason Environment.close() sometimes throws ConcurrentModificationException
        // it helps to wait a little.
        for (int i = 50; i < 1000; i *= 2) {
            try {
                environment.close();
                break;
            } catch (ConcurrentModificationException e) {
                try {
                    Thread.sleep(i);
                } catch (InterruptedException e2) {
                    break;
                }
            }
        }
        if (exception != null) {
            throw exception;
        }
    }

    /**
     * Create a new StoredMap backed by Berkeley DB database.
     *
     * @param name
     *            name of the stored map. The name has to be unique within this persistence backend.
     * @param persistenceKeyFactory
     *            the objects used as keys (of type {@code <K>} will be translated to objects suitable for the
     *            persistence backend using this factory.
     * @param serializer
     *            the serializer that will be used to convert the values from/to the persistent form.
     * @param cache
     *            configured value cache
     * @param <K>
     *            the type of the keys stored in this map.
     * @param <V>
     *            the type of the values stored in the map. Must be serializable by the {@link Serializer}
     * @return a new map for persistent storage, possibly already populated with data from previous sessions.
     * @throws PersistenceException
     *             when the initialization of the StoredMap fails.
     */
    @Override
    public final <K, V extends Serializable> StoredMap<K, V> createNewStoredMap(final String name,
            final PersistenceKeyFactory<K> persistenceKeyFactory, final Serializer serializer,
            final PersistenceCache<V> cache) throws PersistenceException {
        if (environment == null || properties == null) {
            throw new IllegalStateException("The persistence backend has not been properly initialized. "
                    + "Call init(Properties) first.");
        }
        log.debug("Creating new stored map with the name \"" + name + "\"");
        BerkeleyDBAdapter<K> adapter = new BerkeleyDBAdapter<K>(environment, this, persistenceKeyFactory, name);
        adapter.init(properties);
        final TransactionalStoredMap<K, V> storedMap = new TransactionalStoredMap<K, V>(name, persistenceKeyFactory,
                cache, adapter, serializer);
        openStoredMaps.add(storedMap);
        return new CachedKeySetStoredMapWrapper<K, V>(storedMap);
    }

    @Override
    public final OrderedLongSet createOrderedLongSet(final StoredMap<Long, long[]> data) {
        return new StoredOrderedLongSet(data);
    }

    @Override
    public final LogItem createPersistentTransaction(final org.mozartspaces.capi3.Transaction tx) {
        String capiTransactionId = tx.getId();
        Transaction berkeleyTransaction = environment.beginTransaction(null, null);
        activeTransactions.put(capiTransactionId, berkeleyTransaction);
        return new PersistenceLogItem(capiTransactionId, berkeleyTransaction);
    }

    /**
     * A {@link LogItem} that is attached to every transaction that uses a berkeley-based persistence context. It is
     * responsible to commit (or rollback) the berkeley transaction once the capi transaction commits (or is
     * rollbacked).
     */
    private final class PersistenceLogItem implements LogItem {

        private final Transaction transaction;

        private final String capiTransactionId;

        private PersistenceLogItem(final String capiTransactionId, final Transaction transaction) {
            this.capiTransactionId = capiTransactionId;
            this.transaction = transaction;
        }

        @Override
        public void commitSubTransaction() {
        }

        @Override
        public void commitTransaction() {
            transaction.commit();
            activeTransactions.remove(capiTransactionId);
        }

        @Override
        public void rollbackTransaction() {
            transaction.abort();
            activeTransactions.remove(capiTransactionId);
        }

        @Override
        public void rollbackSubTransaction() {
        }
    }

    @Override
    public final NativeEntry makeEntryLazy(final NativeEntry nativeEntry) {
        return new LazyNativeEntry(nativeEntry);
    }
}
