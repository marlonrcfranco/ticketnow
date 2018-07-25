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

import java.io.Serializable;
import java.util.ArrayList;

import net.jcip.annotations.NotThreadSafe;

import org.mozartspaces.capi3.Capi3;
import org.mozartspaces.capi3.EntryOperationResult;
import org.mozartspaces.capi3.LocalContainerReference;
import org.mozartspaces.capi3.SubTransaction;
import org.mozartspaces.capi3.Transaction;
import org.mozartspaces.core.RequestMessage;
import org.mozartspaces.core.aspects.AspectResult;
import org.mozartspaces.core.aspects.AspectStatus;
import org.mozartspaces.core.requests.DeleteEntriesRequest;
import org.mozartspaces.runtime.RuntimeData;
import org.mozartspaces.runtime.aspects.AspectInvoker;
import org.mozartspaces.runtime.aspects.DefaultCapi3AspectPort;
import org.mozartspaces.runtime.blocking.WaitForCategory;
import org.mozartspaces.util.LoggerFactory;
import org.slf4j.Logger;

/**
 * A <code>DeleteEntriesTask</code> deletes entries from a container.
 * It calls the CAPI3 method to take entries,
 * {@link org.mozartspaces.capi3.Capi3#executeTakeOperation(
 * LocalContainerReference, List, org.mozartspaces.capi3.IsolationLevel,
 * SubTransaction, org.mozartspaces.core.RequestContext) executeTakeOperation},
 * with the arguments from the request.
 *
 * @author Tobias Doenz
 */
@NotThreadSafe
public final class DeleteEntriesTask extends TransactionalTask<Integer> {

    private static final Logger log = LoggerFactory.get();

    private final DeleteEntriesRequest request;
    private final AspectInvoker aspectInvoker;
    private final Capi3 capi3;

    /**
     * Constructs a <code>DeleteEntriesTask</code>.
     *
     * @param requestMessage
     *            the request message
     * @param runtimeData
     *            runtime objects and components
     */
    public DeleteEntriesTask(final RequestMessage requestMessage,
            final RuntimeData runtimeData) {
        super(requestMessage, runtimeData);
        this.request = (DeleteEntriesRequest) requestMessage.getContent();
        this.aspectInvoker = runtimeData.getAspectInvoker();
        this.capi3 = runtimeData.getCapi3();
    }

    @Override
    protected Integer runInSubtransaction(final Transaction tx, final SubTransaction stx) throws Throwable {

        log.debug("Deleting from container {}", request.getContainer());

        // prepare
        WaitForCategory[] eventCategories = {WaitForCategory.REMOVE, WaitForCategory.UNLOCK_ST};
        LocalContainerReference lcref = new LocalContainerReference(request.getContainer().getId());
        setContainerAndWaitData(lcref, WaitForCategory.INSERT);

        // invoke pre-aspects
        DefaultCapi3AspectPort containerCapi3 = new DefaultCapi3AspectPort(request.getContainer(), capi3);
        AspectResult result = aspectInvoker.preDelete(request, tx, stx, containerCapi3, getExecutionCount());
        setAspectEventCategories(containerCapi3.getEventCategories());
        AspectStatus status = result.getStatus();
        switch (status) {
        case OK:
        case SKIP:
            break;
        default:
            handleSpecialResult(status.toOperationStatus(), result.getCause(), request.getTimeout());
            return null;
        }

        // invoke operation
        ArrayList<Serializable> entries = null;
        int deletedEntriesCount = 0;
        if (status != AspectStatus.SKIP) {
            EntryOperationResult opResult = capi3.executeTakeOperation(lcref, request.getSelectors(), request
                    .getIsolation(), stx, request.getContext());
            log.debug("CAPI3 returned with status {}", opResult.getStatus());
            setCapi3Result(opResult);
            switch (opResult.getStatus()) {
            case OK:
                setEventCategories(eventCategories);
                // prepare entries for aspects (overhead, if not needed/used there)
                entries = prepareEntriesForResponse(opResult.getResult());
                deletedEntriesCount = entries.size();
                break;
            default:
                handleSpecialResult(opResult.getStatus(), opResult.getCause(), request.getTimeout());
                return null;
            }
        } else {
            log.info("Skipping operation due to pre-aspects status");
        }

        // invoke post-aspects
        result = aspectInvoker.postDelete(request, tx, stx, containerCapi3, getExecutionCount(), entries);
        setAspectEventCategories(containerCapi3.getEventCategories());
        status = result.getStatus();
        switch (status) {
        case OK:
        case SKIP:
            return deletedEntriesCount;
        default:
            handleSpecialResult(status.toOperationStatus(), result.getCause(), request.getTimeout());
            return null;
        }

    }

}
