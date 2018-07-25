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
package org.mozartspaces.capi3.javanative.persistence.berkeley.adapter;

import java.util.Properties;
import java.util.Set;

import org.mozartspaces.capi3.Transaction;
import org.mozartspaces.capi3.javanative.persistence.PersistenceException;
import org.mozartspaces.capi3.javanative.persistence.berkeley.TransactionMapper;
import org.mozartspaces.capi3.javanative.persistence.db.TransactionalDBAdapter;
import org.mozartspaces.capi3.javanative.persistence.key.PersistenceKey;
import org.mozartspaces.capi3.javanative.persistence.key.PersistenceKeyFactory;

import com.sleepycat.bind.EntryBinding;
import com.sleepycat.collections.StoredCollections;
import com.sleepycat.collections.StoredKeySet;
import com.sleepycat.je.CursorConfig;
import com.sleepycat.je.Database;
import com.sleepycat.je.DatabaseConfig;
import com.sleepycat.je.DatabaseEntry;
import com.sleepycat.je.Environment;
import com.sleepycat.je.LockConflictException;
import com.sleepycat.je.LockMode;

/**
 * Adapter for the Berkeley DB.
 *
 * @param <K> the type used as keys
 *
 * @author Jan Zarnikov
 */
public final class BerkeleyDBAdapter<K> implements TransactionalDBAdapter<K> {

    private final Environment environment;

    private Database database;

    private Properties properties;

    private Set<PersistenceKey<K>> keySet;

    private final TransactionMapper transactionMapper;

    private final PersistenceKeyFactory<K> persistenceKeyFactory;

    private final DatabaseConfig databaseConfig;

    private final String databaseName;

    /**
     * Create a new Berkeley DB adapter.
     * @param environment a configured berkeley db environment.
     * @param transactionMapper a transaction mapper that can convert capi transactions to berkeley transactions
     * @param persistenceKeyFactory a persistence key factory for creating new keys
     * @param databaseName a unique database name.
     */
    public BerkeleyDBAdapter(final Environment environment, final TransactionMapper transactionMapper,
                             final PersistenceKeyFactory<K> persistenceKeyFactory, final String databaseName) {
        this.environment = environment;
        this.transactionMapper = transactionMapper;
        this.persistenceKeyFactory = persistenceKeyFactory;
        this.databaseName = databaseName;
        databaseConfig = new DatabaseConfig();
        databaseConfig.setTransactional(true);
        databaseConfig.setAllowCreate(true);
        databaseConfig.setDeferredWrite(false);
    }

    @Override
    public void close() throws PersistenceException {
        database.close();
    }

    @Override
    public void destroy() throws PersistenceException {
        database.close();
        environment.removeDatabase(null, databaseName);
    }

    @Override
    public void clear() throws PersistenceException {
        try {
            database.close();
            environment.truncateDatabase(null, databaseName, false);
        } catch (LockConflictException e) {
            throw new PersistenceException(
                    "This stored map cannot be cleared because there are still opened transactions", e);
        }
        init(properties);
    }

    @Override
    public void init(final Properties properties) throws PersistenceException {
        this.properties = properties;
        database = environment.openDatabase(null, databaseName, databaseConfig);
        keySet = StoredCollections.configuredSet(
                new StoredKeySet<PersistenceKey<K>>(database, new KeyBinding(), false), CursorConfig.READ_UNCOMMITTED);
    }

    @Override
    public int count() throws PersistenceException {
        return (int) database.count();
    }

    protected DatabaseEntry getDBKey(final PersistenceKey<K> key) {
        return new DatabaseEntry(key.asByteArray());
    }

    /**
     *
     */
    protected class KeyBinding implements EntryBinding<PersistenceKey<K>> {
        @Override
        public final PersistenceKey<K> entryToObject(final DatabaseEntry databaseEntry) {
            return persistenceKeyFactory.createPersistenceKeyFromByteArray(databaseEntry.getData());
        }

        @Override
        public final void objectToEntry(final PersistenceKey<K> key, final DatabaseEntry databaseEntry) {
            databaseEntry.setData(key.asByteArray());
        }
    }

    @Override
    public void delete(final PersistenceKey<K> key, final Transaction tx) throws PersistenceException {
        database.delete(transactionMapper.getTransaction(tx), getDBKey(key));
    }

    @Override
    public byte[] get(final PersistenceKey<K> key) throws PersistenceException {
        DatabaseEntry value = new DatabaseEntry();
        database.get(null, getDBKey(key), value, LockMode.READ_UNCOMMITTED);
        return value.getData();
    }

    @Override
    public void put(final PersistenceKey<K> key, final byte[] data, final Transaction tx) throws PersistenceException {
        database.put(transactionMapper.getTransaction(tx), getDBKey(key), new DatabaseEntry(data));
    }

    @Override
    public Set<PersistenceKey<K>> keySet() throws PersistenceException {
        return keySet;
    }
}
