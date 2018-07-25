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

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import net.jcip.annotations.NotThreadSafe;
import net.jcip.annotations.ThreadSafe;

import org.mozartspaces.capi3.LogItem;
import org.mozartspaces.capi3.Transaction;

/**
 * The deferred database is a container for {@link PersistenceOperation} and ensures that when a transaction is
 * committed all persistence operations of a given transaction are executed.
 *
 * @author Jan Zarnikov
 */
@ThreadSafe
public final class DeferredDB {

    private final Map<Transaction, PersistentTransaction> activeTransactions =
            new ConcurrentHashMap<Transaction, PersistentTransaction>();

    /**
     * A persistent transaction is basically a container for write operations. It is attached to a CAPI transaction.
     * All write operations are executed once the CAPI transaction is committed.
     *
     * Thread-safety: Only adding persistence operations is thread-safe, commit and rollback not.
     */
    @NotThreadSafe
    public final class PersistentTransaction implements LogItem {

        private final List<PersistenceOperation> operations;
        private final DeferredDBBatchMode deferredDBBatchMode;
        private final Transaction capiTransaction;

        /**
         * Create a new persistent transaction that is attached to a CAPI transaction.
         * @param deferredDBBatchMode a callback that will be used during the committing of the transaction.
         * @param capiTransaction the CAPI transaction that the new persistent transaction will be attached to.
         */
        public PersistentTransaction(final DeferredDBBatchMode deferredDBBatchMode, final Transaction capiTransaction) {
            this.operations = new LinkedList<PersistenceOperation>();
            this.deferredDBBatchMode = deferredDBBatchMode;
            this.capiTransaction = capiTransaction;
        }

        @Override
        public void commitSubTransaction() {
        }

        @Override
        public void commitTransaction() {
            deferredDBBatchMode.startBatchMode();
            for (PersistenceOperation operation : operations) {
                try {
                    operation.commit();
                } catch (PersistenceException e) {
                    deferredDBBatchMode.endBatchMode(false);
                    e.printStackTrace();
                }
            }
            deferredDBBatchMode.endBatchMode(true);
            activeTransactions.remove(capiTransaction);
        }

        @Override
        public void rollbackTransaction() {
            for (PersistenceOperation operation : operations) {
                try {
                    operation.rollback();
                } catch (PersistenceException e) {
                    e.printStackTrace();
                }
            }
            activeTransactions.remove(capiTransaction);
        }

        @Override
        public void rollbackSubTransaction() {
        }

        /**
         * Add a new write operation to this transaction.
         * @param operation an operation that will be executed once the transaction is committed.
         */
        public void addPersistenceOperation(final PersistenceOperation operation) {
            synchronized (operations) {
                operations.add(operation);
            }
        }
    }

    /**
     * Create a new LogItem that can be added to the transaction's log. The resulting LogItem will execute all
     * persistent actions added to the transaction using this deferred database.
     * @param tx an active transaction
     * @param deferredDBBatchMode a callback that will be used during the committing of the transaction.
     * @return a new LogItem where all persistent operations can be stored.
     */
    public LogItem createPersistentTransaction(final Transaction tx, final DeferredDBBatchMode deferredDBBatchMode) {
        PersistentTransaction persistentTransaction = new PersistentTransaction(deferredDBBatchMode, tx);
        activeTransactions.put(tx, persistentTransaction);
        return persistentTransaction;
    }

    /**
     * Add a new persistence operation that will be executed when the given transaction is committed or rolled back.
     * @param tx an active transaction
     * @param operation the persistence operation
     */
    public void addPersistenceOperation(final Transaction tx, final PersistenceOperation operation) {
        activeTransactions.get(tx).addPersistenceOperation(operation);
    }
}
