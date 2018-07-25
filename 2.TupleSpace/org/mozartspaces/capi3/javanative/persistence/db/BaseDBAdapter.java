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

import java.util.Properties;
import java.util.Set;

import org.mozartspaces.capi3.javanative.persistence.PersistenceException;
import org.mozartspaces.capi3.javanative.persistence.key.PersistenceKey;

/**
 * A database adapter represents a container in the database (e.g. an SQL table) and it abstracts the database-specific
 * code.
 *
 * @param <K>
 *            the type of the keys
 *
 * @author Jan Zarnikov
 */
public interface BaseDBAdapter<K> {

    /**
     * Initialize the DB adapter. This method is called right after the instantiation.
     *
     * @param properties
     *            configuration properties
     * @throws PersistenceException
     *             when the instantiation fails
     */
    void init(Properties properties) throws PersistenceException;

    /**
     * Delete the contents of the database container including the container itself.
     *
     * @throws PersistenceException
     *             when the deleting fails
     */
    void destroy() throws PersistenceException;

    /**
     * Close the container and release all resources. Afterwords the adapter becomes unusable.
     *
     * @throws PersistenceException
     *             when closing fails
     */
    void close() throws PersistenceException;

    /**
     * Remove all key-value pairs from the container.
     *
     * @throws PersistenceException
     *             when deleting fails
     */
    void clear() throws PersistenceException;

    /**
     * Return the number of key-value pairs currently stored in the database container.
     *
     * @return number of key-value pair
     * @throws PersistenceException
     *             when counting fails
     */
    int count() throws PersistenceException;

    /**
     * Get the set of keys currently stored in the database container.
     *
     * @return the set of keys
     * @throws PersistenceException
     *             if retrieving of the keys fails
     */
    Set<PersistenceKey<K>> keySet() throws PersistenceException;

}
