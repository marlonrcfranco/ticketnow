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
package org.mozartspaces.core.authorization;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.mozartspaces.capi3.AccessDeniedException;
import org.mozartspaces.capi3.Capi3AspectPort;
import org.mozartspaces.capi3.IsolationLevel;
import org.mozartspaces.capi3.SubTransaction;
import org.mozartspaces.capi3.Transaction;
import org.mozartspaces.core.Capi;
import org.mozartspaces.core.ContainerReference;
import org.mozartspaces.core.Entry;
import org.mozartspaces.core.MzsConstants.RequestTimeout;
import org.mozartspaces.core.MzsConstants.TransactionTimeout;
import org.mozartspaces.core.MzsCoreException;
import org.mozartspaces.core.RequestContext;
import org.mozartspaces.core.TransactionReference;
import org.mozartspaces.core.aspects.AbstractSpaceAspect;
import org.mozartspaces.core.aspects.AspectReference;
import org.mozartspaces.core.aspects.AspectResult;
import org.mozartspaces.core.aspects.AspectStatus;
import org.mozartspaces.core.requests.AbstractRequest;
import org.mozartspaces.core.requests.AddAspectRequest;
import org.mozartspaces.core.requests.CommitTransactionRequest;
import org.mozartspaces.core.requests.CreateContainerRequest;
import org.mozartspaces.core.requests.CreateTransactionRequest;
import org.mozartspaces.core.requests.DeleteEntriesRequest;
import org.mozartspaces.core.requests.DestroyContainerRequest;
import org.mozartspaces.core.requests.LockContainerRequest;
import org.mozartspaces.core.requests.LookupContainerRequest;
import org.mozartspaces.core.requests.PrepareTransactionRequest;
import org.mozartspaces.core.requests.ReadEntriesRequest;
import org.mozartspaces.core.requests.RemoveAspectRequest;
import org.mozartspaces.core.requests.RollbackTransactionRequest;
import org.mozartspaces.core.requests.ShutdownRequest;
import org.mozartspaces.core.requests.TakeEntriesRequest;
import org.mozartspaces.core.requests.TestEntriesRequest;
import org.mozartspaces.core.requests.WriteEntriesRequest;
import org.mozartspaces.core.security.RequestContextUtils;
import org.mozartspaces.util.LoggerFactory;
import org.slf4j.Logger;

/**
 * Internal aspect used for access control to allow request-based queries.
 *
 * Temporarily puts request objects into a request container on which access control rules can be defined.
 * Requests are immediately removed after being written. Returns NOTOK and includes the corresponding
 * AccessDeniedException if the access manager denies the write operation.
 *
 * @author Stefan Crass
 */
// Note: overwrite methods for new/additional ipoints as required
public final class RequestAuthorizationAspect extends AbstractSpaceAspect {

    private static final long serialVersionUID = 1L;

    private static Logger log = LoggerFactory.get();

    private final ContainerReference requestC;

    //map storing transactions used by aspect to prevent recursive calls when writing into request container
    private final Map<TransactionReference, TransactionReference> ownTransactions;

    private transient Capi capi;

    private AspectReference myReference;

    /**
     * Creates a new RequestAuthorizationAspect.
     * @param requestC the request container where requests are stored temporarily
     */
    public RequestAuthorizationAspect(final ContainerReference requestC) {
        this.requestC = requestC;
        this.ownTransactions = new ConcurrentHashMap<TransactionReference, TransactionReference>();
    }

    private AspectResult checkRequestAuthorization(final AbstractRequest<?> request) {
        RequestContext context = request.getContext();
        if (!RequestContextUtils.isRemoteRequest(context)) {
            log.debug("Request authorization not required (embedded request)");
            return AspectResult.OK;
        }

        log.debug("Checking authorization for request {}", request);

        if (this.capi == null) {
            this.capi = new Capi(getCore());
        }

        Entry requestEntry = new Entry(request);
        TransactionReference tx = null;
        AspectResult result = null;
        //check if write to request container is allowed for user
        try {
            tx = this.capi.createTransaction(TransactionTimeout.INFINITE, null);
            this.ownTransactions.put(tx, tx);
            this.capi.write(Collections.singletonList(requestEntry), this.requestC, RequestTimeout.TRY_ONCE,
                    tx, IsolationLevel.READ_COMMITTED, context);
        } catch (AccessDeniedException e) {
            //TODO use own DENIED aspect status, include cause in exception
            result = new AspectResult(AspectStatus.NOTOK, e);
        } catch (MzsCoreException e) {
            log.warn("Failure when writing request to request container for request-based authorization test");
            result = new AspectResult(AspectStatus.NOTOK, e);
        } finally {
            this.ownTransactions.remove(tx);
            try {
                this.capi.rollbackTransaction(tx);
            } catch (MzsCoreException e) {
                log.warn("Transaction rollback error while testing request-based authorization");
                result = new AspectResult(AspectStatus.NOTOK, e);
            }
        }
        if (result != null) {
            return result;
        } else {
            return AspectResult.OK;
        }
    }


    @Override
    public AspectResult postAddAspect(final AddAspectRequest request, final Transaction tx, final SubTransaction stx,
            final Capi3AspectPort capi3, final int executionCount, final AspectReference aspect) {
        if (myReference == null) {
            // store aspect reference to prevent removal (see preRemoveAspect)
            myReference = aspect;
        }
        return AspectResult.OK;
    }

    @Override
    public AspectResult preRead(final ReadEntriesRequest<?> request, final Transaction tx, final SubTransaction stx,
            final Capi3AspectPort capi3, final int executionCount) {
        return this.checkRequestAuthorization(request);
    }

    @Override
    public AspectResult preTest(final TestEntriesRequest request, final Transaction tx, final SubTransaction stx,
            final Capi3AspectPort capi3, final int executionCount) {
        return this.checkRequestAuthorization(request);
    }


    @Override
    public AspectResult preTake(final TakeEntriesRequest<?> request, final Transaction tx, final SubTransaction stx,
            final Capi3AspectPort capi3, final int executionCount) {
        return this.checkRequestAuthorization(request);
    }

    @Override
    public AspectResult preDelete(final DeleteEntriesRequest request, final Transaction tx, final SubTransaction stx,
            final Capi3AspectPort capi3, final int executionCount) {
        return this.checkRequestAuthorization(request);
    }

    @Override
    public AspectResult preWrite(final WriteEntriesRequest request, final Transaction tx, final SubTransaction stx,
            final Capi3AspectPort capi3, final int executionCount) {
        //hack to prevent recursive evaluation of request when writing to request container
        if (request.getContainer().equals(this.requestC)
                && request.getTransaction() != null
                && this.ownTransactions.containsKey(request.getTransaction())) {
            return AspectResult.OK;
        } else {
            return this.checkRequestAuthorization(request);

        }
    }

    @Override
    public AspectResult preDestroyContainer(final DestroyContainerRequest request, final Transaction tx,
            final SubTransaction stx, final Capi3AspectPort capi3, final int executionCount) {
        return this.checkRequestAuthorization(request);
    }


    @Override
    public AspectResult preLockContainer(final LockContainerRequest request, final Transaction tx,
            final SubTransaction stx, final Capi3AspectPort capi3, final int executionCount) {
        return this.checkRequestAuthorization(request);
    }


    @Override
    public AspectResult preAddAspect(final AddAspectRequest request, final Transaction tx,
            final SubTransaction stx, final Capi3AspectPort capi3, final int executionCount) {
        return this.checkRequestAuthorization(request);
    }

    @Override
    public AspectResult preRemoveAspect(final RemoveAspectRequest request, final Transaction tx,
            final SubTransaction stx, final ContainerReference container, final Capi3AspectPort capi3,
            final int executionCount) {
        // prevent removal of this aspect!
        if (request.getAspect().equals(myReference)) {
            return new AspectResult(new AccessDeniedException("Removal of RequestAuthorizationAspect not allowed"));
        }
        return this.checkRequestAuthorization(request);
    }

    @Override
    public AspectResult preCreateTransaction(final CreateTransactionRequest request) {
        return this.checkRequestAuthorization(request);
    }

    @Override
    public AspectResult prePrepareTransaction(final PrepareTransactionRequest request, final Transaction tx) {
        return this.checkRequestAuthorization(request);
    }


    @Override
    public AspectResult preCommitTransaction(final CommitTransactionRequest request, final Transaction tx) {
        return this.checkRequestAuthorization(request);
    }

    @Override
    public AspectResult preRollbackTransaction(final RollbackTransactionRequest request, final Transaction tx) {
        return this.checkRequestAuthorization(request);
    }

    @Override
    public AspectResult preCreateContainer(final CreateContainerRequest request, final Transaction tx,
            final SubTransaction stx, final int executionCount) {
        return this.checkRequestAuthorization(request);
    }

    @Override
    public AspectResult preLookupContainer(final LookupContainerRequest request, final Transaction tx,
            final SubTransaction stx, final int executionCount) {
        return this.checkRequestAuthorization(request);
    }

    @Override
    public AspectResult preShutdown(final ShutdownRequest request) {
        return this.checkRequestAuthorization(request);
    }
}
