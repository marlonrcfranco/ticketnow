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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeoutException;

import net.jcip.annotations.GuardedBy;
import net.jcip.annotations.ThreadSafe;

import org.mozartspaces.capi3.FifoCoordinator;
import org.mozartspaces.core.ContainerReference;
import org.mozartspaces.core.MzsConstants;
import org.mozartspaces.core.MzsConstants.Container;
import org.mozartspaces.core.MzsCore;
import org.mozartspaces.core.MzsCoreException;
import org.mozartspaces.core.Request;
import org.mozartspaces.core.RequestContext;
import org.mozartspaces.core.TransactionReference;
import org.mozartspaces.core.aspects.AspectReference;
import org.mozartspaces.core.aspects.ContainerIPoint;
import org.mozartspaces.core.requests.AddAspectRequest;
import org.mozartspaces.core.requests.CreateContainerRequest;
import org.mozartspaces.core.requests.DestroyContainerRequest;
import org.mozartspaces.util.LoggerFactory;
import org.slf4j.Logger;

/**
 * Manages notifications, that is, allows the creation of notifications, stores a list of all created notifications and
 * destroys them when the shutdown method is called.
 *
 * @author Tobias Doenz
 */
@ThreadSafe
public final class NotificationManager {

    private static final Logger log = LoggerFactory.get();

    /**
     * Timeout (in milliseconds) for waiting for the result of each request that are sent when a notification is
     * created.
     */
    private static final int TIMEOUT = 10000;

    private final MzsCore core;

    @GuardedBy("this")
    private final List<Notification> notifications;

    /**
     * Constructs a <code>NotificationManager</code>.
     *
     * @param core
     *            the core instance to which the requests are sent
     */
    public NotificationManager(final MzsCore core) {
        this.core = core;
        assert this.core != null;

        notifications = new ArrayList<Notification>();
    }

    /**
     * Creates a new notification for events on the entries of a container, that is, specific operations (delete, read,
     * take, write) that are executed on them.
     *
     * @param container
     *            the reference of the container which should be observed
     * @param listener
     *            the object that should receive the notifications
     * @param operations
     *            the operation types that should be observed
     * @return the created notification
     * @throws InterruptedException
     *             if the current thread was interrupted while waiting for the result of one of the requests sent to the
     *             core
     * @throws MzsCoreException
     *             if one of the requests to create the notification failed
     */
    public Notification createNotification(final ContainerReference container, final NotificationListener listener,
            final Operation... operations) throws MzsCoreException, InterruptedException {
        Set<Operation> operationSet = new HashSet<Operation>(Arrays.asList(operations));
        return createNotification(container, listener, operationSet, null, null);
    }

    /**
     * Creates a new notification for events on the entries of a container, that is, specific operations (delete, read,
     * take, write) that are executed on them.
     *
     * @param container
     *            the reference of the container which should be observed
     * @param listener
     *            the object that should receive the notifications
     * @param operations
     *            the operation types that should be observed
     * @param transaction
     *            the reference of the transaction that should be used for isolation/visibility, may be
     *            <code>null</code>
     * @param context
     *            the context that should be passed to the requests that are used for creating the notification
     * @return the created notification
     * @throws InterruptedException
     *             if the current thread was interrupted while waiting for the result of one of the requests sent to the
     *             core
     * @throws MzsCoreException
     *             if one of the requests to create the notification failed
     */
    public Notification createNotification(final ContainerReference container, final NotificationListener listener,
            final Set<Operation> operations, final TransactionReference transaction, final RequestContext context)
            throws MzsCoreException, InterruptedException {

        log.debug("Creating notification for {} on container {}", operations, container);
        List<FifoCoordinator> coords = Collections.singletonList(new FifoCoordinator());
        Request<ContainerReference> createNotifContainer = new CreateContainerRequest(Container.UNNAMED,
                Container.UNBOUNDED, coords, null, null, MzsConstants.DEFAULT_ISOLATION,
                MzsConstants.DEFAULT_AUTHORIZATION, MzsConstants.Container.DEFAULT_FORCE_IN_MEMORY, context);

        ContainerReference ncref;
        try {
            ncref = core.send(createNotifContainer, container.getSpace()).getResult(TIMEOUT);
            log.debug("Created notification container {}", ncref);
        } catch (TimeoutException ex) {
            throw new MzsCoreException("Could not create notification container", ex);
        }

        AspectReference aspectRef;
        try {
            log.debug("Adding notification aspect");
            NotificationAspect notifAspect = new NotificationAspect(ncref);
            Set<ContainerIPoint> iPoints = operationsToInterceptionPoints(operations);
            iPoints.add(ContainerIPoint.POST_DESTROY_CONTAINER);
            Request<AspectReference> addNotifAspect = new AddAspectRequest(notifAspect, container, iPoints, null,
                    MzsConstants.DEFAULT_ISOLATION, context);
            aspectRef = core.send(addNotifAspect, container.getSpace()).getResult(TIMEOUT);
            log.debug("Added notification aspect {}", aspectRef);

            log.debug("Adding aspect to notification container");
            NotificationContainerAspect notifContainerAspect = new NotificationContainerAspect(aspectRef);
            iPoints = Collections.singleton(ContainerIPoint.POST_DESTROY_CONTAINER);
            Request<AspectReference> addNotifContainerAspect = new AddAspectRequest(notifContainerAspect, ncref,
                    iPoints, null, MzsConstants.DEFAULT_ISOLATION, context);
            core.send(addNotifContainerAspect, ncref.getSpace()).getResult(TIMEOUT);
            log.debug("Added aspect to notification container");
        } catch (TimeoutException ex) {
            destroyNotificationContainer(ncref, transaction);
            throw new MzsCoreException("Could not add aspects for notification", ex);
        } catch (MzsCoreException ex) {
            destroyNotificationContainer(ncref, transaction);
            throw new MzsCoreException("Could not add aspects for notification", ex);
        }

        Notification notif = Notification.newInstance(core, container, ncref, transaction, listener, aspectRef);
        synchronized (notifications) {
            notifications.add(notif);
        }

        return notif;
    }

    // TODO complete transaction notification
//    public TransactionNotification createTransactionNotification(final TransactionReference container,
//            final TransactionNotificationListener listener) {
        // try to add aspect (should only be added once for a space)
        // create TransactionNotification
//        throw new UnsupportedOperationException();
//    }

    /**
     * Destroys all notifications and removes them from the internal list.
     */
    public void shutdown() {
        synchronized (notifications) {
            Iterator<Notification> it = notifications.iterator();
            while (it.hasNext()) {
                Notification notif = it.next();
                try {
                    notif.destroy();
                } catch (MzsCoreException ex) {
                    log.error("Could not destroy a notification", ex);
                }
                it.remove();
            }
        }
    }

    private static Set<ContainerIPoint> operationsToInterceptionPoints(final Set<Operation> operations) {
        Set<ContainerIPoint> iPoints = new HashSet<ContainerIPoint>();
        if (operations.contains(Operation.READ)) {
            iPoints.add(ContainerIPoint.POST_READ);
        }
        if (operations.contains(Operation.TEST)) {
            iPoints.add(ContainerIPoint.POST_TEST);
        }
        if (operations.contains(Operation.TAKE)) {
            iPoints.add(ContainerIPoint.POST_TAKE);
        }
        if (operations.contains(Operation.DELETE)) {
            iPoints.add(ContainerIPoint.POST_DELETE);
        }
        if (operations.contains(Operation.WRITE)) {
            iPoints.add(ContainerIPoint.POST_WRITE);
        }
        return iPoints;
    }

    private void destroyNotificationContainer(final ContainerReference ncref, final TransactionReference transaction)
            throws InterruptedException {

        log.debug("Destroying the notification container {}", ncref);
        Request<?> request = new DestroyContainerRequest(ncref, null, MzsConstants.DEFAULT_ISOLATION, null);
        try {
            core.send(request, ncref.getSpace()).getResult(TIMEOUT);
        } catch (TimeoutException ex) {
            log.error("Could not destroy notification container", ex);
        } catch (MzsCoreException ex) {
            log.error("Could not destroy notification container", ex);
        }
    }

}
