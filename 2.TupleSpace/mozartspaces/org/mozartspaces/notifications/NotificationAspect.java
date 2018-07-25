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

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

import net.jcip.annotations.ThreadSafe;

import org.mozartspaces.capi3.Capi3AspectPort;
import org.mozartspaces.capi3.SubTransaction;
import org.mozartspaces.capi3.Transaction;
import org.mozartspaces.core.ContainerReference;
import org.mozartspaces.core.Entry;
import org.mozartspaces.core.MzsConstants.RequestTimeout;
import org.mozartspaces.core.Request;
import org.mozartspaces.core.aspects.AbstractContainerAspect;
import org.mozartspaces.core.aspects.AspectResult;
import org.mozartspaces.core.requests.DeleteEntriesRequest;
import org.mozartspaces.core.requests.DestroyContainerRequest;
import org.mozartspaces.core.requests.ReadEntriesRequest;
import org.mozartspaces.core.requests.TakeEntriesRequest;
import org.mozartspaces.core.requests.TestEntriesRequest;
import org.mozartspaces.core.requests.TransactionalRequest;
import org.mozartspaces.core.requests.WriteEntriesRequest;
import org.mozartspaces.util.LoggerFactory;
import org.slf4j.Logger;

/**
 * The aspect that "observes" a container, that is, to write special notification entries to the notification container
 * when one of the desired operations was successfully executed. This is done by implementing the methods for the "post"
 * interception points of the delete, read, take, and write operations. Additionally, the method that is called after a
 * container has been destroyed is implemented. It destroys the notification container because the notification is
 * meaningless after the container it observers has been destroyed. Writing the notification entries and destroying the
 * notification container is performed with requests that are executed asynchronously on the core.
 *
 * @author Tobias Doenz
 */
@ThreadSafe
final class NotificationAspect extends AbstractContainerAspect {

    private static final long serialVersionUID = 1L;

    private static final Logger log = LoggerFactory.get();

    private final ContainerReference notificationContainer;

    /**
     * Constructs a <code>NotificationAspect</code>.
     *
     * @param notificationContainer
     *            the reference of the notification container
     */
    NotificationAspect(final ContainerReference notificationContainer) {
        this.notificationContainer = notificationContainer;
        assert this.notificationContainer != null;
    }

    @Override
    public AspectResult postDelete(final DeleteEntriesRequest request, final Transaction tx, final SubTransaction stx,
            final Capi3AspectPort capi3, final int executionCount, final List<Serializable> entries) {

        log.debug("Writing entry for DELETE to notification container");
        writeNotificationEntry(Operation.DELETE, entries, request);
        return AspectResult.OK;
    }

    @Override
    public AspectResult postRead(final ReadEntriesRequest<?> request, final Transaction tx, final SubTransaction stx,
            final Capi3AspectPort capi3, final int executionCount, final List<Serializable> entries) {

        log.debug("Writing entry for READ to notification container");
        writeNotificationEntry(Operation.READ, entries, request);
        return AspectResult.OK;
    }

    @Override
    public AspectResult postTake(final TakeEntriesRequest<?> request, final Transaction tx, final SubTransaction stx,
            final Capi3AspectPort capi3, final int executionCount, final List<Serializable> entries) {

        log.debug("Writing entry for TAKE to notification container");
        writeNotificationEntry(Operation.TAKE, entries, request);
        return AspectResult.OK;
    }

    @Override
    public AspectResult postTest(final TestEntriesRequest request, final Transaction tx, final SubTransaction stx,
            final Capi3AspectPort capi3, final int executionCount, final List<Serializable> entries) {

        log.debug("Writing entry for TEST to notification container");
        writeNotificationEntry(Operation.TEST, entries, request);
        return AspectResult.OK;
    }

    @Override
    public AspectResult postWrite(final WriteEntriesRequest request, final Transaction tx, final SubTransaction stx,
            final Capi3AspectPort capi3, final int executionCount) {

        log.debug("Writing entry for WRITE to notification container");
        writeNotificationEntry(Operation.WRITE, request.getEntries(), request);
        return AspectResult.OK;
    }

    @Override
    public AspectResult postDestroyContainer(final DestroyContainerRequest request, final Transaction tx,
            final SubTransaction stx, final int executionCount) {

        log.debug("Destroying notification container {}", notificationContainer);
        Request<?> destroy = new DestroyContainerRequest(notificationContainer, request.getTransaction(),
                request.getIsolation(), request.getContext());
        getCore().send(destroy, notificationContainer.getSpace());
        return AspectResult.OK;
    }

    private void writeNotificationEntry(final Operation operation, final List<? extends Serializable> operationEntries,
            final TransactionalRequest<?> request) {

        NotificationEntry notifEntry = new NotificationEntry(operation, operationEntries);
        Entry entry = new Entry(notifEntry);
        /**
         * The notification container is unbounded and used exclusively by the notification, so practically no blocking
         * should occur and hence TRY_ONCE is used.
         */
        WriteEntriesRequest write = new WriteEntriesRequest(Collections.singletonList(entry), notificationContainer,
                RequestTimeout.TRY_ONCE, request.getTransaction(), request.getIsolation(), request.getContext());
        getCore().send(write, notificationContainer.getSpace());
    }
}
