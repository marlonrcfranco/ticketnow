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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;

import net.jcip.annotations.Immutable;

import org.mozartspaces.capi3.FifoCoordinator;
import org.mozartspaces.capi3.FifoCoordinator.FifoSelector;
import org.mozartspaces.core.ContainerReference;
import org.mozartspaces.core.MzsConstants;
import org.mozartspaces.core.MzsConstants.RequestTimeout;
import org.mozartspaces.core.MzsCore;
import org.mozartspaces.core.MzsCoreException;
import org.mozartspaces.core.MzsCoreRuntimeException;
import org.mozartspaces.core.Request;
import org.mozartspaces.core.TransactionException;
import org.mozartspaces.core.TransactionReference;
import org.mozartspaces.core.aspects.AspectReference;
import org.mozartspaces.core.requests.DestroyContainerRequest;
import org.mozartspaces.core.requests.TakeEntriesRequest;
import org.mozartspaces.util.LoggerFactory;
import org.slf4j.Logger;

/**
 * Simple implementation of notifications, similar to the notifications in <code>MozartSpaces</code> version 1 and the
 * first <code>XcoSpaces</code> version. The notification is implicitly started in the constructor and an internal
 * thread is taking entries from the notification container and notifies the listener. It can be stopped by calling the
 * <code>destroy</code> method, which destroys the notification container and stops the internal thread.
 *
 * @author Tobias Doenz
 */
@Immutable
public final class Notification {

    private static final Logger log = LoggerFactory.get();

    /**
     * Timeout (in milliseconds) for waiting for the result when the notification container is destroyed.
     */
    private static final int DESTROY_TIMEOUT = 10000;

    // TODO? add getters
    private final MzsCore core;
    private final ContainerReference observedContainer;
    private final ContainerReference notificationContainer;
    private final TransactionReference transaction;
    private final NotificationListener listener;
    private final AspectReference notificationAspect;

    private final AtomicBoolean active;
    private final NotifierThread thread;

    /**
     * Constructs a <code>Notification</code>. Use the static factory method
     * {@link #newInstance(MzsCore, ContainerReference, TransactionReference, NotificationListener, AspectReference)
     * newInstance}.
     */
    private Notification(final MzsCore core, final ContainerReference observedContainer,
            final ContainerReference notificationContainer, final TransactionReference transaction,
            final NotificationListener listener, final AspectReference notificationAspect) {

        this.core = core;
        assert this.core != null;
        this.observedContainer = observedContainer;
        assert this.observedContainer != null;
        this.notificationContainer = notificationContainer;
        assert this.notificationContainer != null;
        this.transaction = transaction;
        // assert this.transaction != null;
        this.listener = listener;
        assert this.listener != null;
        this.notificationAspect = notificationAspect;
        assert this.notificationAspect != null;

        active = new AtomicBoolean(true);
        thread = new NotifierThread();
        thread.setName("Notification-" + notificationAspect);
    }

    /**
     * Constructs a <code>Notification</code> and starts the internal thread.
     *
     * @param core
     *            the core instance to which the requests are sent
     * @param observedContainer
     *            the reference of the observed container
     * @param notificationContainer
     *            the reference of the notification container
     * @param transaction
     *            the reference of the transaction, may be <code>null</code>
     * @param listener
     *            the notification listener
     * @param notificationAspect
     *            the reference of the notification aspect
     */
    static Notification newInstance(final MzsCore core, final ContainerReference observedContainer,
            final ContainerReference notificationContainer, final TransactionReference transaction,
            final NotificationListener listener, final AspectReference notificationAspect) {
        Notification notif = new Notification(core, observedContainer, notificationContainer, transaction, listener,
                notificationAspect);
        notif.start();
        return notif;
    }

    void start() {
        thread.start();
    }

    /**
     * @return <code>true</code> if the notification is active, that is, has not been destroyed or is being destroyed,
     *         <code>false</code> otherwise
     */
    public boolean isActive() {
        return active.get();
    }

    /**
     * Destroys the notification container and thereby stops the notification. This method also stops the internal
     * thread and waits until it died.
     *
     * @throws MzsCoreException
     *             if destroying the notification container failed
     */
    public void destroy() throws MzsCoreException {
        if (!active.getAndSet(false)) {
            return;
        }
        log.debug("Destroying notification container {}", notificationContainer);
        try {
            Request<?> request = new DestroyContainerRequest(notificationContainer, null,
                    MzsConstants.DEFAULT_ISOLATION, null);
            core.send(request, notificationContainer.getSpace()).getResult(DESTROY_TIMEOUT);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        } catch (MzsCoreException ex) {
            log.info("Destroying notification container failed: {}", ex.toString());
        } catch (TransactionException ex) {
            log.info("Destroying notification container failed: {}", ex.toString());
        } catch (TimeoutException ex) {
            throw new MzsCoreException("Destroying notification container timed out after " + DESTROY_TIMEOUT + " ms");
        }
        thread.interrupt();
        try {
            thread.join(500);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
        log.debug("Notification destroyed");
    }

    /**
     * @return the reference of the observed container
     */
    public ContainerReference getObservedContainer() {
        return observedContainer;
    }

    /**
     * @return the reference of the notification container
     */
    public ContainerReference getNotificationContainer() {
        return notificationContainer;
    }

    @Override
    public String toString() {
        return "Notification [observedContainer=" + observedContainer + ", notificationContainer="
                + notificationContainer + ", transaction=" + transaction + "]";
    }

    /**
     * Takes entries from the notification container and notifies the listener.
     *
     * @author Tobias Doenz
     */
    private final class NotifierThread extends Thread {

        @Override
        public void run() {
            List<FifoSelector> fifo = Collections.singletonList(FifoCoordinator.newSelector(1));
            // List<AnySelector> fifo = Collections.singletonList(AnyCoordinator.newSelector(1));
            TakeEntriesRequest<Serializable> take = new TakeEntriesRequest<Serializable>(notificationContainer, fifo,
                    RequestTimeout.INFINITE, transaction, MzsConstants.DEFAULT_ISOLATION, null);
            while (!Thread.currentThread().isInterrupted()) {
                NotificationEntry notifEntry = null;
                try {
                    log.debug("Taking notification entries");
                    ArrayList<Serializable> result = core.send(take, notificationContainer.getSpace()).getResult();
                    notifEntry = (NotificationEntry) result.get(0);
                    listener.entryOperationFinished(Notification.this, notifEntry.getOperation(),
                            notifEntry.getEntries());
                } catch (MzsCoreException ex) {
                    log.warn("Taking notification entries failed", ex);
                    try {
                        Notification.this.destroy();
                    } catch (Exception ex2) {
                        log.info("Destroying notification failed: {}", ex2.toString());
                    }
                    return;
                } catch (MzsCoreRuntimeException ex) {
                    log.warn("Taking notification entries failed", ex);
                    try {
                        Notification.this.destroy();
                    } catch (Exception ex2) {
                        log.info("Destroying notification failed: {}", ex2.toString());
                    }
                    return;
                } catch (InterruptedException ex) {
                    log.info("Thread interrupted, stopping notification");
                    return;
                } catch (Exception ex) {
                    String info = "notification=" + Notification.this + ", operation=" + notifEntry.getOperation()
                            + ", entries=" + notifEntry.getEntries();
                    log.warn("Exception in listener method (" + info + ")", ex);
                }
            }
        }
    }
}
