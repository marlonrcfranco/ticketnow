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
package org.mozartspaces.runtime.tasks;

import org.mozartspaces.capi3.Capi3;
import org.mozartspaces.capi3.SubTransaction;
import org.mozartspaces.capi3.Transaction;
import org.mozartspaces.core.ContainerReference;
import org.mozartspaces.core.MzsConstants.RequestTimeout;
import org.mozartspaces.core.RequestMessage;
import org.mozartspaces.core.aspects.AbstractContainerAspect;
import org.mozartspaces.core.aspects.AspectReference;
import org.mozartspaces.core.aspects.AspectRegistration;
import org.mozartspaces.core.aspects.AspectResult;
import org.mozartspaces.core.aspects.AspectStatus;
import org.mozartspaces.core.aspects.ContainerAspect;
import org.mozartspaces.core.aspects.ContainerIPoint;
import org.mozartspaces.core.aspects.InterceptionPoint;
import org.mozartspaces.core.aspects.SpaceIPoint;
import org.mozartspaces.core.requests.RemoveAspectRequest;
import org.mozartspaces.core.util.Nothing;
import org.mozartspaces.runtime.RuntimeData;
import org.mozartspaces.runtime.aspects.AspectInvoker;
import org.mozartspaces.runtime.aspects.AspectManager;
import org.mozartspaces.runtime.aspects.DefaultCapi3AspectPort;
import org.mozartspaces.util.LoggerFactory;
import org.slf4j.Logger;

/**
 *
 * @author Tobias Doenz
 */
public final class RemoveAspectTask extends TransactionalTask<Nothing> {

    private static final Logger log = LoggerFactory.get();

    private final RemoveAspectRequest request;
    private final AspectInvoker aspectInvoker;
    private final Capi3 capi3;
    private final AspectManager aspectManager;

    /**
     * Constructs a <code>RemoveAspectTask</code>.
     *
     * @param requestMessage
     *            the request message
     * @param runtimeData
     *            runtime objects and components
     */
    public RemoveAspectTask(final RequestMessage requestMessage,
            final RuntimeData runtimeData) {
        super(requestMessage, runtimeData);
        this.request = (RemoveAspectRequest) requestMessage.getContent();
        this.aspectInvoker = runtimeData.getAspectInvoker();
        this.capi3 = runtimeData.getCapi3();
        this.aspectManager = runtimeData.getAspectManager();
    }

    @Override
    protected Nothing runInSubtransaction(final Transaction tx, final SubTransaction stx) throws Throwable {

        // prepare
        AspectReference aspectRef = request.getAspect();
        ContainerReference cref = aspectManager.getContainerWhereAspectIsRegistered(aspectRef, tx);
        DefaultCapi3AspectPort containerCapi3 = null;
        if (cref != null) {
            containerCapi3 = new DefaultCapi3AspectPort(cref, capi3);
            if (request.getIPoints() != null) {
                for (InterceptionPoint point : request.getIPoints()) {
                    if (!(point instanceof ContainerIPoint)) {
                        throw new IllegalArgumentException(point + " is not a ContainerIPoint");
                    }
                }
            }
        } else {
            if (request.getIPoints() != null) {
                for (InterceptionPoint point : request.getIPoints()) {
                    if (!(point instanceof SpaceIPoint)) {
                        throw new IllegalArgumentException(point + " is not a SpaceIPoint");
                    }
                }
            }
        }

        // invoke pre-aspects
        AspectResult result = aspectInvoker.preRemoveAspect(request, tx, stx, cref, containerCapi3,
                getExecutionCount());
        if (containerCapi3 != null) {
            setAspectEventCategories(containerCapi3.getEventCategories());
        }
        AspectStatus status = result.getStatus();
        switch (status) {
        case OK:
        case SKIP:
            break;
        default:
            // TODO? throw exception for LOCKED, DELAYABLE (when no Capi3AspectPort)
            handleSpecialResult(status.toOperationStatus(), result.getCause(), RequestTimeout.INFINITE);
            return null;
        }

        // invoke operation
        if (status != AspectStatus.SKIP) {
            AspectRegistration registration = aspectManager.removeAspect(aspectRef, request.getIPoints(), tx);
            ContainerAspect aspect = registration.getAspect();
            if (aspect instanceof AbstractContainerAspect) {
                AbstractContainerAspect aspectInstance = (AbstractContainerAspect) aspect;
                try {
                    aspectInstance.aspectRemoved(registration);
                } catch (RuntimeException ex) {
                    log.warn("Exception in aspectRemoved method of {}", aspectRef, ex);
                }
            }
        } else {
            log.info("Skipping operation due to pre-aspects status");
        }

        // invoke post-aspects
        result = aspectInvoker.postRemoveAspect(request, tx, stx, cref, containerCapi3, getExecutionCount());
        if (containerCapi3 != null) {
            setAspectEventCategories(containerCapi3.getEventCategories());
        }
        status = result.getStatus();
        switch (status) {
        case OK:
        case SKIP:
            return Nothing.INSTANCE;
        default:
            // TODO? throw exception for LOCKED, DELAYABLE (when no Capi3AspectPort)
            handleSpecialResult(status.toOperationStatus(), result.getCause(), RequestTimeout.INFINITE);
            return null;
        }

    }

}
