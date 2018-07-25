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
import org.mozartspaces.capi3.Transaction;
import org.mozartspaces.core.MzsCoreRuntimeException;
import org.mozartspaces.core.RequestMessage;
import org.mozartspaces.core.TransactionReference;
import org.mozartspaces.core.MzsConstants.RequestTimeout;
import org.mozartspaces.core.aspects.AspectResult;
import org.mozartspaces.core.aspects.AspectStatus;
import org.mozartspaces.core.requests.CreateTransactionRequest;
import org.mozartspaces.runtime.RuntimeData;
import org.mozartspaces.runtime.TransactionManager;
import org.mozartspaces.runtime.aspects.AspectInvoker;
import org.mozartspaces.runtime.util.RuntimeUtils;
import org.mozartspaces.util.LoggerFactory;
import org.slf4j.Logger;

/**
 * A <code>CreateTransactionTask</code> creates a transaction. It calls
 * {@link org.mozartspaces.capi3.Capi3#newTransaction() CAPI3} to create a new
 * transaction, adds it to the transaction manager and returns its transaction
 * reference.
 *
 * @author Tobias Doenz
 * @see org.mozartspaces.capi3.Transaction
 * @see org.mozartspaces.runtime.TransactionManager
 */
@NotThreadSafe
public final class CreateTransactionTask extends AbstractTask<TransactionReference> {

    private static final Logger log = LoggerFactory.get();

    private final CreateTransactionRequest request;
    private final AspectInvoker aspectInvoker;
    private final Capi3 capi3;
    private final TransactionManager txManager;
    private final RuntimeUtils utils;

    /**
     * Constructs a <code>CreateTransactionTask</code>.
     *
     * @param requestMessage
     *            the request message
     * @param runtimeData
     *            runtime objects and components
     */
    public CreateTransactionTask(final RequestMessage requestMessage,
            final RuntimeData runtimeData) {
        super(requestMessage, runtimeData);
        this.request = (CreateTransactionRequest) requestMessage.getContent();
        this.aspectInvoker = runtimeData.getAspectInvoker();
        this.capi3 = runtimeData.getCapi3();
        this.txManager = runtimeData.getTxManager();
        this.utils = runtimeData.getRuntimeUtils();
    }

    @Override
    protected TransactionReference runSpecific() throws Throwable {

        // invoke pre-aspects
        AspectResult result = aspectInvoker.preCreateTransaction(request);
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
        Transaction tx = null;
        TransactionReference txRef = null;
        if (status != AspectStatus.SKIP) {
            // TODO add timeout parameter to newTransaction (store expireTime in Transaction)
            tx = capi3.newTransaction();
            txRef = utils.createTransactionReference(tx);
            log.debug("Created transaction {}", txRef);
            txManager.addTransaction(txRef, tx, request.getTimeout());
        } else {
            log.info("Skipping operation due to pre-aspects status");
        }

        // invoke post-aspects
        result = aspectInvoker.postCreateTransaction(request, txRef, tx);
        status = result.getStatus();
        switch (status) {
        case OK:
        case SKIP:
            if (txRef != null) {
                return txRef;
            } else {
                // TODO how should the behavior be in this case?
                throw new MzsCoreRuntimeException("No transaction reference to return");
            }
        default:
            // TODO? throw exception for LOCKED, DELAYABLE (no Capi3AspectPort)
            handleSpecialResult(status.toOperationStatus(), result.getCause(), RequestTimeout.INFINITE);
            return null;
        }

    }

}
