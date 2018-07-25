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

import net.jcip.annotations.NotThreadSafe;

import org.mozartspaces.capi3.Capi3;
import org.mozartspaces.capi3.SubTransaction;
import org.mozartspaces.capi3.Transaction;
import org.mozartspaces.core.MzsConstants.RequestTimeout;
import org.mozartspaces.core.MzsCore;
import org.mozartspaces.core.MzsCoreRuntimeException;
import org.mozartspaces.core.RequestMessage;
import org.mozartspaces.core.aspects.AbstractContainerAspect;
import org.mozartspaces.core.aspects.AspectReference;
import org.mozartspaces.core.aspects.AspectRegistration;
import org.mozartspaces.core.aspects.AspectResult;
import org.mozartspaces.core.aspects.AspectStatus;
import org.mozartspaces.core.aspects.ContainerAspect;
import org.mozartspaces.core.aspects.SpaceAspect;
import org.mozartspaces.core.requests.AddAspectRequest;
import org.mozartspaces.runtime.RuntimeData;
import org.mozartspaces.runtime.aspects.AspectInvoker;
import org.mozartspaces.runtime.aspects.AspectManager;
import org.mozartspaces.runtime.aspects.DefaultCapi3AspectPort;
import org.mozartspaces.util.LoggerFactory;
import org.slf4j.Logger;

/**
 * An <code>AddAspectTask</code> adds an aspect to a user container. It
 * calls the Aspect Manager method to add a container aspect,
 * {@link AspectManager#addContainerAspect(ContainerAspect,
 * org.mozartspaces.core.ContainerReference, java.util.Set, Transaction)
 * addContainerAspect}, with the arguments from the request.
 *
 * @author Tobias Doenz
 */
@NotThreadSafe
public final class AddAspectTask extends TransactionalTask<AspectReference> {

    private static final Logger log = LoggerFactory.get();

    private final AddAspectRequest request;
    private final AspectInvoker aspectInvoker;
    private final Capi3 capi3;
    private final AspectManager aspectManager;
    private final MzsCore core;

    /**
     * Constructs an <code>AddAspectTask</code>.
     *
     * @param requestMessage
     *            the request message
     * @param runtimeData
     *            runtime objects and components
     */
    public AddAspectTask(final RequestMessage requestMessage,
            final RuntimeData runtimeData) {
        super(requestMessage, runtimeData);
        this.request = (AddAspectRequest) requestMessage.getContent();
        this.aspectInvoker = runtimeData.getAspectInvoker();
        this.capi3 = runtimeData.getCapi3();
        this.aspectManager = runtimeData.getAspectManager();
        this.core = runtimeData.getCore();
    }

    @Override
    protected AspectReference runInSubtransaction(final Transaction tx, final SubTransaction stx) throws Throwable {

        // TODO check that container exists/is valid? (if yes, how?)
        // invoke pre-aspects
        DefaultCapi3AspectPort containerCapi3 = null;
        if (request.getContainer() != null) {
            containerCapi3 = new DefaultCapi3AspectPort(request.getContainer(), capi3);
        }
        AspectResult result = aspectInvoker.preAddAspect(request, tx, stx, containerCapi3, getExecutionCount());
        if (containerCapi3 != null) {
            setAspectEventCategories(containerCapi3.getEventCategories());
        }
        AspectStatus status = result.getStatus();
        switch (status) {
        case OK:
        case SKIP:
            break;
        default:
            handleSpecialResult(status.toOperationStatus(), result.getCause(), RequestTimeout.INFINITE);
            return null;
        }

        // invoke operation
        AspectReference aspectRef = null;
        if (status != AspectStatus.SKIP) {
            ContainerAspect aspect = request.getAspect();
            AspectRegistration registration = null;
            if (request.getContainer() != null) {
                registration = aspectManager.addContainerAspect(aspect, request.getContainer(), request.getIPoints(),
                        tx);
            } else {
                registration = aspectManager.addSpaceAspect((SpaceAspect) aspect, request.getIPoints(), tx);
            }
            aspectRef = registration.getAspectReference();
            log.debug("Added aspect with reference {}", aspectRef);
            if (aspect instanceof AbstractContainerAspect) {
                AbstractContainerAspect aspectInstance = (AbstractContainerAspect) aspect;
                // aspect initialization
                aspectInstance.setCore(core);
                try {
                    aspectInstance.aspectAdded(core, registration);
                } catch (RuntimeException ex) {
                    log.warn("Exception in aspectAdded method of {}", aspectRef, ex);
                }
            }
        } else {
            log.info("Skipping operation due to pre-aspects status");
        }

        // invoke post-aspects
        result = aspectInvoker.postAddAspect(request, tx, stx, containerCapi3, getExecutionCount(), aspectRef);
        if (containerCapi3 != null) {
            setAspectEventCategories(containerCapi3.getEventCategories());
        }
        status = result.getStatus();
        switch (status) {
        case OK:
        case SKIP:
            if (aspectRef != null) {
                return aspectRef;
            } else {
                // TODO how should the behavior be in this case?
                throw new MzsCoreRuntimeException("No aspect reference to return");
            }
        default:
            handleSpecialResult(status.toOperationStatus(), result.getCause(), RequestTimeout.INFINITE);
            return null;
        }

    }

}
