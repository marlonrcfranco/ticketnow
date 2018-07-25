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
package org.mozartspaces.capi3.javanative.persistence.db;

import org.mozartspaces.capi3.Transaction;
import org.mozartspaces.capi3.javanative.persistence.key.PersistenceKey;

/**
 * An adapter for databases that support concurrent long running transactions.
 *
 * @param <K> the type of the keys
 *
 * @author Jan Zarnikov
 */
public interface TransactionalDBAdapter<K> extends BaseDBAdapter<K> {

    /**
     * Get the value associated with the key.
     * @param key the key
     * @return the value of the key-value pair or {@code null} if no such key is stored in the database.
     */
    byte[] get(PersistenceKey<K> key);

    /**
     * Store a new key-value pair in the database. If the key is already in the DB it will be overwritten
     * without any warnings.
     * @param key the key
     * @param data the value
     * @param tx transaction in which the operation should be executed or {@code null} if an implicit transaction
     *           should be used
     */
    void put(PersistenceKey<K> key, byte[] data, Transaction tx);

    /**
     * Delete a key-value pair from the database.
     * @param key the key that should be deleted
     * @param tx transaction in which the operation should be executed or {@code null} if an implicit transaction
     *           should be used
     */
    void delete(PersistenceKey<K> key, Transaction tx);

}
