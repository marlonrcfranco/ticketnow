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

import net.jcip.annotations.ThreadSafe;

import org.mozartspaces.capi3.SubTransaction;
import org.mozartspaces.capi3.Transaction;
import org.mozartspaces.core.MzsCoreException;
import org.mozartspaces.core.MzsCoreRuntimeException;
import org.mozartspaces.core.Request;
import org.mozartspaces.core.aspects.AbstractContainerAspect;
import org.mozartspaces.core.aspects.AspectException;
import org.mozartspaces.core.aspects.AspectReference;
import org.mozartspaces.core.aspects.AspectResult;
import org.mozartspaces.core.requests.DestroyContainerRequest;
import org.mozartspaces.core.requests.RemoveAspectRequest;
import org.mozartspaces.util.LoggerFactory;
import org.slf4j.Logger;

/**
 * Removes the notification aspect from the observed container when the notification container is destroyed. This aspect
 * is added to the notification container and the method that is executed after the container is destroyed is
 * implemented.
 *
 * @author Tobias Doenz
 */
@ThreadSafe
final class NotificationContainerAspect extends AbstractContainerAspect {

    private static final long serialVersionUID = 1L;

    private static final Logger log = LoggerFactory.get();

    private final AspectReference aspect;

    /**
     * Constructs a <code>NotificationContainerAspect</code>.
     *
     * @param aspect
     *            the reference of the notification aspect
     */
    NotificationContainerAspect(final AspectReference aspect) {
        this.aspect = aspect;
        assert this.aspect != null;
    }

    @Override
    public AspectResult postDestroyContainer(final DestroyContainerRequest request, final Transaction tx,
            final SubTransaction stx, final int executionCount) {

        log.debug("Removing notification aspect");
        Request<?> removeAspect = new RemoveAspectRequest(aspect, null, request.getTransaction(),
                request.getIsolation(), request.getContext());
        try {
            getCore().send(removeAspect, aspect.getSpace()).getResult();
        } catch (AspectException ex) {
            log.info("Removing notification aspect failed: {}", ex.toString());
            /**
             * Do not throw exception here!
             * Otherwise destroying the notification container in the notification aspect does not work.
             */
        } catch (MzsCoreException ex) {
            throw new MzsCoreRuntimeException("Removing notification aspect failed", ex);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
        return AspectResult.OK;
    }

}
