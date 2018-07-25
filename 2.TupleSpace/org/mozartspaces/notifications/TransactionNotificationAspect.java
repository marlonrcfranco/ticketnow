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
package org.mozartspaces.notifications;

import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.mozartspaces.capi3.Capi3AspectPort;
import org.mozartspaces.capi3.LabelCoordinator;
import org.mozartspaces.capi3.SubTransaction;
import org.mozartspaces.capi3.Transaction;
import org.mozartspaces.core.ContainerReference;
import org.mozartspaces.core.Entry;
import org.mozartspaces.core.TransactionReference;
import org.mozartspaces.core.aspects.AbstractSpaceAspect;
import org.mozartspaces.core.aspects.AspectReference;
import org.mozartspaces.core.aspects.AspectResult;
import org.mozartspaces.core.requests.AddAspectRequest;
import org.mozartspaces.core.requests.CommitTransactionRequest;
import org.mozartspaces.core.requests.RollbackTransactionRequest;
import org.mozartspaces.core.requests.WriteEntriesRequest;
import org.mozartspaces.util.LoggerFactory;
import org.slf4j.Logger;

/**
 * This aspect writes notification entries to the transaction notification container when an observed transaction is
 * committed or rolled back. It uses the {@code preAddAspect} interception point to prevent that it is added more than
 * once, as this is not necessary, and the {@code postAddAspect} interception point to add the passed transaction to the
 * map of observed transactions.
 *
 * @author Tobias Doenz
 */
public final class TransactionNotificationAspect extends AbstractSpaceAspect {

    private static final long serialVersionUID = 1L;

    private static final Logger log = LoggerFactory.get();

    private final ContainerReference notificationContainer;
    private final TransactionReference transaction;

    private Map<TransactionReference, AtomicInteger> observedTransactions;

    /**
     * @param notificationContainer
     *            the notification container
     * @param transaction
     *            the transaction to observe
     */
    public TransactionNotificationAspect(final ContainerReference notificationContainer,
            final TransactionReference transaction) {
        this.notificationContainer = notificationContainer;
        assert this.notificationContainer != null;
        this.transaction = transaction;
        assert this.transaction != null;
    }

    @Override
    public AspectResult postCommitTransaction(final CommitTransactionRequest request, final Transaction tx) {
        writeNotificationEntry(request.getTransaction(), new TransactionNotificationEntry(
                TransactionEndOperation.COMMIT));
        return AspectResult.OK;
    }

    @Override
    public AspectResult postRollbackTransaction(final RollbackTransactionRequest request, final Transaction tx) {
        writeNotificationEntry(request.getTransaction(), new TransactionNotificationEntry(
                TransactionEndOperation.ROLLBACK));
        return AspectResult.OK;
    }

    private void writeNotificationEntry(final TransactionReference transaction,
            final TransactionNotificationEntry notificationEntry) {
        AtomicInteger count = observedTransactions.remove(transaction);
        if (count == null) {
            return;
        }
        int observerCount = count.get();
        Entry entry = new Entry(notificationEntry, LabelCoordinator.newCoordinationData(transaction
                .getStringRepresentation()));
        Entry[] entries = new Entry[observerCount];
        Arrays.fill(entries, entry);
        WriteEntriesRequest request = WriteEntriesRequest.withContainer(notificationContainer).entries(entries).build();
        log.debug("Writing {} {} notification entries for TX {} to container {}", new Object[] {observerCount,
                notificationEntry.getOperation(), transaction.getId(), notificationContainer});
        getCore().send(request, notificationContainer.getSpace());
    }

    @Override
    public AspectResult preAddAspect(final AddAspectRequest request, final Transaction tx, final SubTransaction stx,
            final Capi3AspectPort capi3, final int executionCount) {
        if (!(request.getAspect() instanceof TransactionNotificationAspect)) {
            return AspectResult.OK;
        }
        TransactionNotificationAspect aspect = (TransactionNotificationAspect) request.getAspect();
        int observerCount = observedTransactions.get(aspect.transaction).incrementAndGet();
        log.debug("Added observer #{} to TX {}", observerCount, aspect.transaction.getId());
        return AspectResult.SKIP;
    }

    @Override
    public AspectResult postAddAspect(final AddAspectRequest request, final Transaction tx, final SubTransaction stx,
            final Capi3AspectPort capi3, final int executionCount, final AspectReference aspect) {
        if (!(request.getAspect() instanceof TransactionNotificationAspect)) {
            return AspectResult.OK;
        }
        if (aspect != null) {
            log.debug("Added TX notification aspect");
        }
        if (observedTransactions == null) {
            observedTransactions = new ConcurrentHashMap<TransactionReference, AtomicInteger>();
            TransactionNotificationAspect aspectInstance = (TransactionNotificationAspect) request.getAspect();
            observedTransactions.put(aspectInstance.transaction, new AtomicInteger(1));
            log.debug("Added TX {} to observed transactions", aspectInstance.transaction.getId());
        }
        return AspectResult.OK;
    }

}
