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
import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.mozartspaces.capi3.LogItem;
import org.mozartspaces.capi3.Transaction;
import org.mozartspaces.capi3.javanative.operation.NativeEntry;
import org.mozartspaces.capi3.javanative.persistence.cache.EmptyCache;
import org.mozartspaces.capi3.javanative.persistence.cache.PersistenceCache;
import org.mozartspaces.capi3.javanative.persistence.key.LongPersistenceKey;
import org.mozartspaces.capi3.javanative.persistence.key.PersistenceKeyFactory;
import org.mozartspaces.core.util.JavaBuiltinSerializer;
import org.mozartspaces.core.util.Serializer;
import org.mozartspaces.util.LoggerFactory;
import org.slf4j.Logger;

/**
 * @author Jan Zarnikov
 */
public final class PersistenceContext {

    private Properties persistenceProperties;

    private final PersistenceBackend persistenceBackend;

    private final Serializer serializer;

    private static final Map<String, String> KNOWN_PROFILES = new HashMap<String, String>();

    private static final Logger log = LoggerFactory.get();

    /**
     * Name of the persistence profile that uses Berkeley DB Java Edition as peristence backend. In this profile a file
     * system sync is called after each transaction commit to ensure that the latest consistent state can be restored
     * after application or OS crash. Note that the price for this is significantly reduced performance when compared
     * with the other Berkeley DB profiles.<br/>
     * See <a href="http://docs.oracle.com/cd/E17277_02/html/java/com/sleepycat/je/Durability.SyncPolicy.html#SYNC">
     * Berkeley DB documentation</a> for more details.
     */
    public static final String TRANSACTIONAL_SYNC_BERKELEY = "berkeleydb-transactional-sync";

    /**
     * Name of the persistence profile that uses Berkeley DB Java Edition as peristence backend. In this profile a the
     * log of Berkeley DB is written after each transaction commit. This means that even committed data could get lost
     * if the application crashes before the log has been actually flushed to the hard drive by the file system.<br/>
     * See <a
     * href="http://docs.oracle.com/cd/E17277_02/html/java/com/sleepycat/je/Durability.SyncPolicy.html#WRITE_NO_SYNC">
     * Berkeley DB documentation</a> for more details.
     */
    public static final String TRANSACTIONAL_BERKELEY = "berkeleydb-transactional";

    /**
     * Name of the persistence profile that uses Berkeley DB Java Edition as peristence backend. In this profile all
     * write operations and transaction commits are buffered and later written to the filesystem asynchronously. This
     * leads to slightly better performance when compared to {@link PersistenceContext#TRANSACTIONAL_BERKELEY} but has
     * significantly higher risk of loosing data when the application crashes.<br/>
     * See <a href="http://docs.oracle.com/cd/E17277_02/html/java/com/sleepycat/je/Durability.SyncPolicy.html#NO_SYNC">
     * Berkeley DB documentation</a> for more details.
     */
    public static final String LAZY_BERKELEY = "berkeleydb-lazy";

    /**
     * Name of the persistence profile that uses only in-memory data. This means that there is no persistence and all
     * data is lost when the space is shut down or crashes. It also means that all data is held in memory all the time
     * which can lead to {@link OutOfMemoryError} if the JVM is not configured to use enough memory. The advantage of
     * this profile is higher throughput and lower latency when compared to the Berkeley DB or SQLite alternatives. This
     * is the default profile.
     */
    public static final String IN_MEMORY = "in-memory";

    /**
     * Name of the persistence profile that uses the embedded SQLite database of the Android OS.<br/>
     * This profile is only available on the Android version of MozartSpaces.
     */
    public static final String ANDROID_SQLITE = "sqlite-android";

    /**
     * The default size of value cache used by a stored map.
     */
    public static final long DEFAULT_CACHE_SIZE = 0;

    /**
     * The key used for configuration in the persistence properties to set the size of the value cache.
     */
    public static final String CACHE_SIZE_CONFIG_KEY = "value-cache-size";

    static {
        KNOWN_PROFILES.put(TRANSACTIONAL_SYNC_BERKELEY,
                "org.mozartspaces.capi3.javanative.persistence.berkeley.profile.TransactionalSyncBerkeleyDB");
        KNOWN_PROFILES.put(TRANSACTIONAL_BERKELEY,
                "org.mozartspaces.capi3.javanative.persistence.berkeley.profile.TransactionalBerkeleyDB");
        KNOWN_PROFILES.put(LAZY_BERKELEY,
                "org.mozartspaces.capi3.javanative.persistence.berkeley.profile.LazyBerkeleyDB");
        KNOWN_PROFILES.put(ANDROID_SQLITE, "org.mozartspaces.capi3.javanative.persistence.android.AndroidSQLite");
        KNOWN_PROFILES.put(IN_MEMORY, InMemoryDB.class.getName());
    }

    /**
     * Creates a persistence context with default profile which is {@link PersistenceContext#IN_MEMORY} and the default
     * serializer ({@link JavaBuiltinSerializer}).
     */
    public PersistenceContext() {
        this(getDefaultInitializedPersistenceBackend(), new JavaBuiltinSerializer());
    }

    /**
     * Create a persistence context with the given profileand the default serializer ({@link JavaBuiltinSerializer}).
     *
     * @param persistenceProfile
     *            must be one of the following: {@link PersistenceContext#IN_MEMORY},
     *            {@link PersistenceContext#LAZY_BERKELEY}, {@link PersistenceContext#TRANSACTIONAL_BERKELEY},
     *            {@link PersistenceContext#TRANSACTIONAL_SYNC_BERKELEY}, {@link PersistenceContext#ANDROID_SQLITE} or
     *            the name of a class that implements the {@link PersistenceBackend} interface.
     * @throws PersistenceInitializationException
     *             when the persistenceProfile is not one of the allowed profile names or a class implementing the
     *             {@link PersistenceBackend} interface or if the instantiation or initialization fails.
     */
    public PersistenceContext(final String persistenceProfile) throws PersistenceInitializationException {
        this(persistenceProfile, new JavaBuiltinSerializer());
    }

    /**
     * Create a persistence context with the given profile and serializer.
     *
     * @param persistenceProfile
     *            must be one of the following: {@link PersistenceContext#IN_MEMORY},
     *            {@link PersistenceContext#LAZY_BERKELEY}, {@link PersistenceContext#TRANSACTIONAL_BERKELEY},
     *            {@link PersistenceContext#TRANSACTIONAL_SYNC_BERKELEY}, {@link PersistenceContext#ANDROID_SQLITE} or
     *            the name of a class that implements the {@link PersistenceBackend} interface.
     * @param serializer
     *            this serializer will be used to transform the objects to/from binary form in order to store them in
     *            the persistent storage.
     * @throws PersistenceInitializationException
     *             when the persistenceProfile is not one of the allowed profile names or a class implementing the
     *             {@link PersistenceBackend} interface or if the instantiation or initialization fails.
     */
    public PersistenceContext(final String persistenceProfile, final Serializer serializer)
            throws PersistenceInitializationException {
        this(persistenceProfile, new Properties(), serializer);
    }

    /**
     *
     * @param persistenceProfile
     *            must be one of the following: {@link PersistenceContext#IN_MEMORY},
     *            {@link PersistenceContext#LAZY_BERKELEY}, {@link PersistenceContext#TRANSACTIONAL_BERKELEY},
     *            {@link PersistenceContext#TRANSACTIONAL_SYNC_BERKELEY}, {@link PersistenceContext#ANDROID_SQLITE} or
     *            the name of a class that implements the {@link PersistenceBackend} interface.
     * @param persistenceProperties
     *            configuration of the persistence backend. The possible keys depend on the selected persistence
     *            profile.
     * @param serializer
     *            this serializer will be used to transform the objects to/from binary form in order to store them in
     *            the persistent storage.
     * @throws PersistenceInitializationException
     *             when the persistenceProfile is not one of the allowed profile names or a class implementing the
     *             {@link PersistenceBackend} interface or if the instantiation or initialization fails.
     */
    public PersistenceContext(final String persistenceProfile, final Properties persistenceProperties,
            final Serializer serializer) throws PersistenceInitializationException {
        this(resolvePersistenceBackend(persistenceProfile, persistenceProperties), serializer);
        this.persistenceProperties = persistenceProperties;
    }

    /**
     * Create a new persistence context with a given peristence backend and serializer.
     *
     * @param persistenceBackend
     *            an already initialized persistence backend.
     * @param serializer
     *            this serializer will be used to transform the objects to/from binary form in order to store them in
     *            the persistent storage.
     */
    private PersistenceContext(final PersistenceBackend persistenceBackend, final Serializer serializer) {
        this.persistenceBackend = persistenceBackend;
        this.serializer = serializer;
        log.info("Persistence context successfully initialized with the backend \"" + persistenceBackend.toString()
                + "\" and the serializer \"" + serializer.toString() + "\".");
    }

    private static PersistenceBackend getDefaultInitializedPersistenceBackend() {
        PersistenceBackend persistenceBackend = new InMemoryDB();
        persistenceBackend.init(new Properties());
        return persistenceBackend;
    }

    private static PersistenceBackend resolvePersistenceBackend(final String persistenceProfile,
            final Properties persistenceProperties) throws PersistenceInitializationException {

        PersistenceBackend persistenceBackend;
        log.info("Initializing persistence profile '{}'", persistenceProfile);
        Class<?> profileClass;
        if (KNOWN_PROFILES.containsKey(persistenceProfile)) {
            String profileClassName = KNOWN_PROFILES.get(persistenceProfile);
            try {
                profileClass = Class.forName(profileClassName);
            } catch (ClassNotFoundException e) {
                throw new PersistenceInitializationException("Could not load class " + profileClassName
                        + " for persistence profile '" + persistenceProfile + "'.", e);
            }
        } else {
            try {
                profileClass = Class.forName(persistenceProfile);
            } catch (ClassNotFoundException e) {
                throw new PersistenceInitializationException("Could not load the persistence profile class '"
                        + persistenceProfile + "'.", e);
            }
        }
        try {
            log.debug("Instantiating persistence backend.");
            Object factoryInstance = profileClass.newInstance();
            persistenceBackend = (PersistenceBackend) factoryInstance;
            log.debug("Initializing persistence backend.");
            persistenceBackend.init(persistenceProperties);
        } catch (InstantiationException e) {
            throw new PersistenceInitializationException("Could not initialize persistence profile.", e);
        } catch (IllegalAccessException e) {
            throw new PersistenceInitializationException("Could not initialize persistence profile.", e);
        } catch (ClassCastException e) {
            throw new PersistenceInitializationException("Could not initialize persistence profile.", e);
        }
        return persistenceBackend;
    }

    /**
     * Create or restore a StoredMap with the given name. If a name with the given name was already used during previous
     * sessions it will be restored and repopulated with data form the persistent storage.
     *
     * @param name
     *            name of the stored map. Note that depending on the selected persistence profile some special
     *            characters might be forbidden. The name has to be unique within this persistence context. Use
     *            {@link PersistenceContext#generateStoredMapName(Class, String)} or
     *            {@link PersistenceContext#generateStoredMapName(Class, String, String)} to generate a name.
     * @param persistenceKeyFactory
     *            the objects used as keys (of type {@code <K>} will be translated to objects suitable for the
     *            persistence backend using this factory.
     * @param <K>
     *            the type of the keys stored in this map.
     * @param <V>
     *            the type of the values stored in the map. Must be serializable by the {@link Serializer} which was
     *            passed to the constructor of this persistence context.
     * @return a new map for persistent storage, possibly already populated with data from previous sessions.
     * @throws PersistenceException
     *             if the persistence backend fails to create the stored map
     */
    public <K, V extends Serializable> StoredMap<K, V> createStoredMap(final String name,
            final PersistenceKeyFactory<K> persistenceKeyFactory) throws PersistenceException {
        long cacheSize;
        try {
            cacheSize = Long.parseLong(persistenceProperties.getProperty(CACHE_SIZE_CONFIG_KEY));
        } catch (Exception e) {
            cacheSize = DEFAULT_CACHE_SIZE;
        }
        PersistenceCache<V> cache = createCache(cacheSize);
        StoredMap<K, V> storedMap = persistenceBackend.createNewStoredMap(name, persistenceKeyFactory, serializer,
                cache);
        return storedMap;
    }

    /**
     * Creates a new cache with the given size and concurrency or an empty cache if the optional cache module is not
     * present.
     *
     * @param size
     *            size of the cache
     * @return the cache
     */
    protected <T extends Serializable> PersistenceCache<T> createCache(final long size) {
        if (size <= 0) {
            return new EmptyCache<T>();
        }
        try {
            @SuppressWarnings("unchecked")
            Class<PersistenceCache<T>> guavaCacheClass = (Class<PersistenceCache<T>>) Class
                    .forName("org.mozartspaces.capi3.javanative.persistence.cache.GuavaCache");
            Constructor<PersistenceCache<T>> guavaCacheConstructor = guavaCacheClass.getConstructor(long.class);
            return guavaCacheConstructor.newInstance(size);
        } catch (Exception e) {
            log.error("Could not resolve value cache, cache will be disabled.", e);
            return new EmptyCache<T>();
        }
    }

    /**
     * Creates a {@link LogItem} to mark this transaction as using this persistence context. The resulting LogItem must
     * be added to the transaction by the caller ({@link Transaction#addLog(org.mozartspaces.capi3.LogItem)}).
     *
     * @param tx
     *            transaction that wants to use this persistence context.
     * @return a LogItem that should be added to the transaction's log. It cannot be used for any transactions other
     *         than the one supplied as parameter.
     */
    public LogItem createPersistentTransaction(final Transaction tx) {
        log.trace("Marking transaction {} as persistent in backend {}", tx.getId(), this.persistenceBackend);
        return persistenceBackend.createPersistentTransaction(tx);
    }

    /**
     * Close the persistence context and release all resources. After calling this method this persistence context and
     * all stored maps created by it will become unusable.
     *
     * @throws PersistenceException
     *             if there are still any open transactions that used this persistence context by calling
     *             {@link PersistenceContext#createPersistentTransaction(org.mozartspaces.capi3.Transaction)} using
     *             StoredMaps created by this persistence context.
     */
    public void close() throws PersistenceException {
        log.info("Closing persistence context.");
        persistenceBackend.close();
    }

    /**
     * Create a new ordered long set with the given which is backed by a stored map automatically created by this
     * persistence context.
     *
     * @param name
     *            of the set of longs. Note that depending on the selected persistence profile some special characters
     *            might be forbidden. The name has to be unique within this persistence context. Use
     *            {@link PersistenceContext#generateStoredMapName(Class, String)} or
     *            {@link PersistenceContext#generateStoredMapName(Class, String, String)} to generate a name.
     * @return a set of ordered longs.
     */
    public OrderedLongSet createOrderedLongSet(final String name) {
        StoredMap<Long, long[]> setData = createStoredMap(name, new LongPersistenceKey.LongPersistenceKeyFactory());
        return persistenceBackend.createOrderedLongSet(setData);
    }

    /**
     * If supported create a new proxy for the entry which allows the garbage collector to reclaim the space used by the
     * value of the entry and lazy-load it later if it is needed again. Otherwise return the entry unchanged.
     *
     * @param nativeEntry
     *            an entry that should be adapted for lazy-loading of its value
     * @return a lazy-loading proxy for the given entry or the unchanged entry itself.
     */
    public NativeEntry makeEntryLazy(final NativeEntry nativeEntry) {
        return persistenceBackend.makeEntryLazy(nativeEntry);
    }

    /**
     * Generates a suitable name for a {@link StoredMap} that can be used for
     * {@link PersistenceContext#createStoredMap(String, PersistenceKeyFactory)} or
     * {@link PersistenceContext#createOrderedLongSet(String)}.
     *
     * @param containerClass
     *            the class which creating the stored map
     * @param id
     *            an ID of the instance creating the stored map. Must be unique in combination with the class.
     * @return a unique name that can be used for creating a stored map
     */
    public String generateStoredMapName(final Class<?> containerClass, final String id) {
        return generateStoredMapName(containerClass, id, null);
    }

    /**
     * Generates a suitable name for a {@link StoredMap} that can be used for
     * {@link PersistenceContext#createStoredMap(String, PersistenceKeyFactory)} or
     * {@link PersistenceContext#createOrderedLongSet(String)}.
     *
     * @param containerClass
     *            the class which creating the stored map
     * @param id
     *            an ID of the instance creating the stored map. Must be unique in combination with the class.
     * @param postfix
     *            an optional postfix that will be added to the name. Can be {@code null}.
     * @return a unique name that can be used for creating a stored map
     */
    public String generateStoredMapName(final Class<?> containerClass, final String id, final String postfix) {
        return containerClass.getName() + "_" + id + ((postfix == null) ? "" : ("_" + postfix));
    }
}
