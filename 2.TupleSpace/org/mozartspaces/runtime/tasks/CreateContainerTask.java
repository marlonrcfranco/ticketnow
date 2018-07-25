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
import org.mozartspaces.capi3.ContainerOperationResult;
import org.mozartspaces.capi3.SubTransaction;
import org.mozartspaces.capi3.Transaction;
import org.mozartspaces.core.ContainerReference;
import org.mozartspaces.core.MzsConstants.RequestTimeout;
import org.mozartspaces.core.MzsCoreRuntimeException;
import org.mozartspaces.core.RequestMessage;
import org.mozartspaces.core.aspects.AspectResult;
import org.mozartspaces.core.aspects.AspectStatus;
import org.mozartspaces.core.requests.CreateContainerRequest;
import org.mozartspaces.runtime.RuntimeData;
import org.mozartspaces.runtime.aspects.AspectInvoker;
import org.mozartspaces.runtime.aspects.DefaultCapi3AspectPort;
import org.mozartspaces.runtime.blocking.WaitForCategory;
import org.mozartspaces.runtime.util.RuntimeUtils;
import org.mozartspaces.util.LoggerFactory;
import org.slf4j.Logger;

/**
 * A <code>CreateContainerTask</code> creates a user container. It calls the
 * CAPI3 method to create a container,
 * {@link org.mozartspaces.capi3.Capi3#executeContainerCreateOperation(String, java.util.List, java.util.List, int,
 * org.mozartspaces.capi3.IsolationLevel, SubTransaction, org.mozartspaces.core.authorization.AuthorizationLevel,
 * boolean, org.mozartspaces.core.RequestContext) executeContainerCreateOperation},
 * with the arguments from the request.
 *
 * @author Tobias Doenz
 * @author Stefan Crass
 */
@NotThreadSafe
public final class CreateContainerTask extends TransactionalTask<ContainerReference> {

    private static final Logger log = LoggerFactory.get();

    private final CreateContainerRequest request;
    private final AspectInvoker aspectInvoker;
    private final Capi3 capi3;
    private final RuntimeUtils utils;

    /**
     * Constructs a <code>CreateContainerTask</code>.
     *
     * @param requestMessage
     *            the request message
     * @param runtimeData
     *            runtime objects and components
     */
    public CreateContainerTask(final RequestMessage requestMessage,
            final RuntimeData runtimeData) {
        super(requestMessage, runtimeData);
        this.request = (CreateContainerRequest) requestMessage.getContent();
        this.aspectInvoker = runtimeData.getAspectInvoker();
        this.capi3 = runtimeData.getCapi3();
        this.utils = runtimeData.getRuntimeUtils();
    }

    @Override
    protected ContainerReference runInSubtransaction(final Transaction tx, final SubTransaction stx) throws Throwable {

        // prepare
        setContainerAndWaitData(null, WaitForCategory.UNLOCK_ST);

        // invoke pre-aspects
        AspectResult result = aspectInvoker.preCreateContainer(request, tx, stx, getExecutionCount());
        AspectStatus status = result.getStatus();
        switch (status) {
        case OK:
        case SKIP:
            break;
        default:
            // TODO? throw exception for LOCKED, DELAYABLE (no Capi3AspectPort)
            handleSpecialResult(status.toOperationStatus(), result.getCause(), RequestTimeout.INFINITE);
            return null;
        }

        // invoke operation
        DefaultCapi3AspectPort containerCapi3 = null;
        ContainerReference cref = null;
        if (status != AspectStatus.SKIP) {
            ContainerOperationResult opResult = capi3.executeContainerCreateOperation(request.getName(), request
                    .getObligatoryCoords(), request.getOptionalCoords(), request.getSize(), request.getIsolation(),
                    stx, request.getAuthLevel(), request.isForceInMemory(), request.getContext());
            log.debug("CAPI3 returned with status {}", opResult.getStatus());
            setCapi3Result(opResult);
            switch (opResult.getStatus()) {
            case OK:
                cref = utils.createContainerReference(opResult.getContainerReference());
                log.debug("Created container {}", cref);
                containerCapi3 = new DefaultCapi3AspectPort(cref, capi3);
                setContainerAndWaitData(opResult.getContainerReference(), WaitForCategory.UNLOCK_ST);
                break;
            default:
                handleSpecialResult(opResult.getStatus(), opResult.getCause(), RequestTimeout.ZERO);
                return null;
            }
        } else {
            log.info("Skipping operation due to pre-aspects status");
        }

        // invoke post-aspects
        result = aspectInvoker.postCreateContainer(request, tx, stx, containerCapi3, getExecutionCount(), cref);
        setAspectEventCategories(containerCapi3.getEventCategories());
        status = result.getStatus();
        switch (status) {
        case OK:
        case SKIP:
            if (cref != null) {
                return cref;
            } else {
                // TODO how should the behavior be in this case?
                throw new MzsCoreRuntimeException("No container reference to return");
            }
        default:
            handleSpecialResult(status.toOperationStatus(), result.getCause(), RequestTimeout.INFINITE);
            return null;
        }

    }

}
