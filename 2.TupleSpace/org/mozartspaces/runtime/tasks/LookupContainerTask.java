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
import org.mozartspaces.capi3.OperationStatus;
import org.mozartspaces.capi3.SubTransaction;
import org.mozartspaces.capi3.Transaction;
import org.mozartspaces.core.ContainerReference;
import org.mozartspaces.core.MzsConstants.RequestTimeout;
import org.mozartspaces.core.MzsCoreRuntimeException;
import org.mozartspaces.core.RequestMessage;
import org.mozartspaces.core.aspects.AspectResult;
import org.mozartspaces.core.aspects.AspectStatus;
import org.mozartspaces.core.requests.LookupContainerRequest;
import org.mozartspaces.runtime.RuntimeData;
import org.mozartspaces.runtime.aspects.AspectInvoker;
import org.mozartspaces.runtime.aspects.DefaultCapi3AspectPort;
import org.mozartspaces.runtime.blocking.WaitForCategory;
import org.mozartspaces.runtime.util.RuntimeUtils;
import org.mozartspaces.util.LoggerFactory;
import org.slf4j.Logger;

/**
 * A <code>LookupContainerTask</code> looks up a user container.
 * It calls the CAPI3 method to lookup a container,
 * {@link org.mozartspaces.capi3.Capi3#executeContainerLookupOperation(String,
 * org.mozartspaces.capi3.IsolationLevel, SubTransaction,
 * org.mozartspaces.core.RequestContext) executeContainerLookupOperation}, with
 * the arguments from the request.
 *
 * @author Tobias Doenz
 */
@NotThreadSafe
public final class LookupContainerTask extends TransactionalTask<ContainerReference> {

    private static final Logger log = LoggerFactory.get();

    private final LookupContainerRequest request;
    private final AspectInvoker aspectInvoker;
    private final Capi3 capi3;
    private final RuntimeUtils utils;

    /**
     * Constructs a <code>LookupContainerTask</code>.
     *
     * @param requestMessage
     *            the request message
     * @param runtimeData
     *            runtime objects and components
     */
    public LookupContainerTask(final RequestMessage requestMessage,
            final RuntimeData runtimeData) {
        super(requestMessage, runtimeData);
        this.request = (LookupContainerRequest) requestMessage.getContent();
        this.aspectInvoker = runtimeData.getAspectInvoker();
        this.capi3 = runtimeData.getCapi3();
        this.utils = runtimeData.getRuntimeUtils();
    }

    @Override
    protected ContainerReference runInSubtransaction(final Transaction tx, final SubTransaction stx) throws Throwable {

        log.debug("Looking up container named {}", request.getName());

        // prepare
        setContainerAndWaitData(null, WaitForCategory.INSERT);

        // invoke pre-aspects
        AspectResult result = aspectInvoker.preLookupContainer(request, tx, stx, getExecutionCount());
        AspectStatus status = result.getStatus();
        switch (status) {
        case OK:
        case SKIP:
            break;
        default:
            // TODO? throw exception for LOCKED, DELAYABLE (no Capi3AspectPort)
            handleSpecialResult(status.toOperationStatus(), result.getCause(), request.getTimeout());
            return null;
        }

        // invoke operation
        DefaultCapi3AspectPort containerCapi3 = null;
        ContainerReference cref = null;
        if (status != AspectStatus.SKIP) {
            ContainerOperationResult opResult = capi3.executeContainerLookupOperation(request.getName(), request
                    .getIsolation(), stx, request.getContext());
            log.debug("CAPI3 returned with status {}", opResult.getStatus());
            setCapi3Result(opResult);
            switch (opResult.getStatus()) {
            case OK:
                cref = utils.createContainerReference(opResult.getContainerReference());
                containerCapi3 = new DefaultCapi3AspectPort(cref, capi3);
                break;
            case NOTOK:
                // special handling of NOTOK to wait for container creation
                // (DELAYABLE for lookup is not supported in CAPI-3)
                if (request.getTimeout() == RequestTimeout.TRY_ONCE || request.getTimeout() == RequestTimeout.ZERO) {
                    throw opResult.getCause();
                }
                handleSpecialResult(OperationStatus.DELAYABLE, opResult.getCause(), request.getTimeout());
                return null;
            default:
                handleSpecialResult(opResult.getStatus(), opResult.getCause(), request.getTimeout());
                return null;
            }
        } else {
            log.info("Skipping operation due to pre-aspects status");
        }

        // invoke post-aspects
        result = aspectInvoker.postLookupContainer(request, tx, stx, containerCapi3, getExecutionCount(), cref);
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
            handleSpecialResult(status.toOperationStatus(), result.getCause(), request.getTimeout());
            return null;
        }

    }

}
