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

import java.util.concurrent.atomic.AtomicBoolean;

import net.jcip.annotations.Immutable;

import org.mozartspaces.core.ContainerReference;
import org.mozartspaces.core.MzsCore;
import org.mozartspaces.core.MzsCoreException;
import org.mozartspaces.core.TransactionReference;
import org.mozartspaces.util.LoggerFactory;
import org.slf4j.Logger;

/**
 * Simple implementation of transaction notifications, similar to the notifications for entry operations (
 * {@link Notification}). The notification is implicitly started in the constructor and an internal thread is taking
 * entries from the notification container and notifies the listener. It can be stopped by calling the
 * <code>destroy</code> method, which destroys stops the internal thread.
 *
 * @author Tobias Doenz
 */
@Immutable
public final class TransactionNotification {

    private static final Logger log = LoggerFactory.get();

    private final MzsCore core;
    private final TransactionReference observedTransaction;
    private final ContainerReference notificationContainer;
    private final NotificationListener listener;

    private final AtomicBoolean active;
    private final NotifierThread thread;

    /**
     * Constructs a <code>Notification</code>. Use the static factory method
     * {@link #newInstance(MzsCore, TransactionReference, ContainerReference, NotificationListener) newInstance}.
     */
    private TransactionNotification(final MzsCore core, final TransactionReference observedTransaction,
            final ContainerReference notificationContainer, final NotificationListener listener) {

        this.core = core;
        assert this.core != null;
        this.observedTransaction = observedTransaction;
        assert this.observedTransaction != null;
        this.notificationContainer = notificationContainer;
        assert this.notificationContainer != null;
        this.listener = listener;
        assert this.listener != null;

        active = new AtomicBoolean(true);
        thread = new NotifierThread();
        thread.setName("TransactionNotification-" + observedTransaction);
    }

    /**
     * Constructs a <code>TransactionNotification</code> and starts the internal thread.
     *
     * @param core
     *            the core instance to which the requests are sent
     * @param observedTransaction
     *            the reference of the observed transaction
     * @param notificationContainer
     *            the reference of the notification container
     * @param listener
     *            the notification listener
     */
    static TransactionNotification newInstance(final MzsCore core, final TransactionReference observedTransaction,
            final ContainerReference notificationContainer, final TransactionReference transaction,
            final NotificationListener listener) {
        TransactionNotification notif = new TransactionNotification(core, observedTransaction, notificationContainer,
                listener);
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
        // TODO implement
    }

    /**
     * @return the reference of the notification container
     */
    public ContainerReference getNotificationContainer() {
        return notificationContainer;
    }

    /**
     * Takes entries from the notification container and notifies the listener.
     *
     * @author Tobias Doenz
     */
    private final class NotifierThread extends Thread {
        // TODO implement
    }

}
