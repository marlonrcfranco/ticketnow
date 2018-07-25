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

import org.mozartspaces.capi3.Transaction;
import org.mozartspaces.core.MzsConstants.RequestTimeout;
import org.mozartspaces.core.RequestMessage;
import org.mozartspaces.core.TransactionException;
import org.mozartspaces.core.TransactionReference;
import org.mozartspaces.core.aspects.AspectResult;
import org.mozartspaces.core.aspects.AspectStatus;
import org.mozartspaces.core.requests.PrepareTransactionRequest;
import org.mozartspaces.core.util.Nothing;
import org.mozartspaces.runtime.RuntimeData;
import org.mozartspaces.runtime.TransactionManager;
import org.mozartspaces.runtime.aspects.AspectInvoker;
import org.mozartspaces.util.LoggerFactory;
import org.slf4j.Logger;

/**
 * A <code>PrepareTransactionTask</code> prepares the specified transaction for
 * commit.
 * It takes the <code>TransactionReference</code> from the request, gets the
 * internally used transaction from the transaction manager and ... TODO
 *
 * @author Tobias Doenz
 * @see org.mozartspaces.capi3.Transaction
 * @see org.mozartspaces.runtime.TransactionManager
 */
@NotThreadSafe
public final class PrepareTransactionTask extends AbstractTask<Nothing> {

    private static final Logger log = LoggerFactory.get();

    private final PrepareTransactionRequest request;
    private final AspectInvoker aspectInvoker;
    private final TransactionManager txManager;

    /**
     * Constructs a <code>PrepareTransactionTask</code>.
     *
     * @param requestMessage
     *            the request message
     * @param runtimeData
     *            runtime objects and components
     */
    public PrepareTransactionTask(final RequestMessage requestMessage,
            final RuntimeData runtimeData) {
        super(requestMessage, runtimeData);
        this.request = (PrepareTransactionRequest) requestMessage.getContent();
        this.aspectInvoker = runtimeData.getAspectInvoker();
        this.txManager = runtimeData.getTxManager();
    }

    @Override
    protected Nothing runSpecific() throws Throwable {

        log.debug("Preparing transaction {}", request.getTransaction());

        // prepare
        TransactionReference txRef = request.getTransaction();
        Transaction tx = txManager.getTransaction(txRef);

        // invoke pre-aspects
        AspectResult result = aspectInvoker.prePrepareTransaction(request, tx);
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
        if (status != AspectStatus.SKIP) {
            if (tx.isValid()) {
                // TODO use other method, something like Transaction.isActive
                tx.lockAndWaitForSubTransactions();
            } else {
                throw new TransactionException("Transaction " + txRef + " is not valid");
            }
        } else {
            log.info("Skipping operation due to pre-aspects status");
        }

        // invoke post-aspects
        result = aspectInvoker.postPrepareTransaction(request, tx);
        status = result.getStatus();
        switch (status) {
        case OK:
        case SKIP:
            return Nothing.INSTANCE;
        default:
            // TODO? throw exception for LOCKED, DELAYABLE (no Capi3AspectPort)
            handleSpecialResult(status.toOperationStatus(), result.getCause(), RequestTimeout.INFINITE);
            return null;
        }

    }

}
