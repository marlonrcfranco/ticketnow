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
package org.mozartspaces.core.aspects;

import org.mozartspaces.capi3.Capi3AspectPort;
import org.mozartspaces.capi3.SubTransaction;
import org.mozartspaces.capi3.Transaction;
import org.mozartspaces.core.ContainerReference;
import org.mozartspaces.core.TransactionReference;
import org.mozartspaces.core.requests.CommitTransactionRequest;
import org.mozartspaces.core.requests.CreateContainerRequest;
import org.mozartspaces.core.requests.CreateTransactionRequest;
import org.mozartspaces.core.requests.LookupContainerRequest;
import org.mozartspaces.core.requests.PrepareTransactionRequest;
import org.mozartspaces.core.requests.RollbackTransactionRequest;
import org.mozartspaces.core.requests.ShutdownRequest;

/**
 * Interface for space aspects (global aspects). It contains a method for each
 * {@link SpaceIPoint} constant. These methods are before and after
 * non-container-specific space operations and are called <code>pre</code> and
 * <code>post</code> aspect methods, respectively. This interface extends the
 * interface for container aspects, so each space aspect is also a container
 * aspect and called for the {@link ContainerIPoint}s.
 * <p>
 * The method parameters are the request and, if possible and reasonable, other
 * objects like the transaction, the sub-transaction, or the execution count.
 * All parameters are not <code>null</code>. <code>null</code> is allowed as
 * return value for all methods and interpreted as {@link AspectStatus}
 * <code>OK</code>.
 * <p>
 * An abstract implementation of this interface to ease the implementation of
 * aspects that use only a few interception points can be found in
 * {@link AbstractSpaceAspect}.
 *
 * @author Tobias Doenz
 *
 * @see AbstractSpaceAspect
 * @see SpaceIPoint
 */
public interface SpaceAspect extends ContainerAspect {

    // transaction methods
    /**
     * Invoked by the Aspect Manager before a transaction is created.
     *
     * @param request
     *            the original request sent to the core.
     * @return the aspect result
     */
    AspectResult preCreateTransaction(CreateTransactionRequest request);

    /**
     * Invoked by the Aspect Manager after a transaction has been created.
     *
     * @param request
     *            the original request sent to the core.
     * @param txRef
     *            the reference of the created transaction
     * @param tx
     *            the created transaction
     * @return the aspect result
     */
    AspectResult postCreateTransaction(CreateTransactionRequest request, TransactionReference txRef, Transaction tx);

    /**
     * Invoked by the Aspect Manager before a transaction is prepared for
     * commit.
     *
     * @param request
     *            the original request sent to the core.
     * @param tx
     *            the transaction
     * @return the aspect result
     */
    AspectResult prePrepareTransaction(PrepareTransactionRequest request, Transaction tx);

    /**
     * Invoked by the Aspect Manager after a transaction has been prepared for
     * commit.
     *
     * @param request
     *            the original request sent to the core.
     * @param tx
     *            the transaction
     * @return the aspect result
     */
    AspectResult postPrepareTransaction(PrepareTransactionRequest request, Transaction tx);

    /**
     * Invoked by the Aspect Manager before a transaction is committed.
     *
     * @param request
     *            the original request sent to the core.
     * @param tx
     *            the transaction
     * @return the aspect result
     */
    AspectResult preCommitTransaction(CommitTransactionRequest request, Transaction tx);

    /**
     * Invoked by the Aspect Manager after a transaction has been committed.
     *
     * @param request
     *            the original request sent to the core.
     * @param tx
     *            the transaction
     * @return the aspect result
     */
    AspectResult postCommitTransaction(CommitTransactionRequest request, Transaction tx);

    /**
     * Invoked by the Aspect Manager before a transaction is rollbacked.
     *
     * @param request
     *            the original request sent to the core.
     * @param tx
     *            the transaction
     * @return the aspect result
     */
    AspectResult preRollbackTransaction(RollbackTransactionRequest request, Transaction tx);

    /**
     * Invoked by the Aspect Manager after a transaction has been rollbacked.
     *
     * @param request
     *            the original request sent to the core.
     * @param tx
     *            the transaction
     * @return the aspect result
     */
    AspectResult postRollbackTransaction(RollbackTransactionRequest request, Transaction tx);

    // container methods
    /**
     * Invoked by the Aspect Manager before a container create operation is
     * performed on the space.
     *
     * @param request
     *            the original request sent to the core.
     * @param tx
     *            the transaction, can be explicit or implicit
     * @param stx
     *            the sub-transaction for this operation
     * @param executionCount
     *            the number of processings of this request
     * @return the aspect result
     */
    AspectResult preCreateContainer(CreateContainerRequest request, Transaction tx, SubTransaction stx,
            int executionCount);

    /**
     * Invoked by the Aspect Manager after a container create operation has been
     * performed on the space.
     *
     * @param request
     *            the original request sent to the core.
     * @param tx
     *            the transaction, can be explicit or implicit
     * @param stx
     *            the sub-transaction for this operation
     * @param capi3
     *            the container-specific CAPI-3 interface
     * @param executionCount
     *            the number of processings of this request
     * @param container
     *            the reference of the created container (result of the CAPI-3
     *            operation)
     * @return the aspect result
     */
    AspectResult postCreateContainer(CreateContainerRequest request, Transaction tx, SubTransaction stx,
            Capi3AspectPort capi3, int executionCount, ContainerReference container);

    /**
     * Invoked by the Aspect Manager before a container lookup operation is
     * performed on the space.
     *
     * @param request
     *            the original request sent to the core.
     * @param tx
     *            the transaction, can be explicit or implicit
     * @param stx
     *            the sub-transaction for this operation
     * @param executionCount
     *            the number of processings of this request
     * @return the aspect result
     */
    AspectResult preLookupContainer(LookupContainerRequest request, Transaction tx, SubTransaction stx,
            int executionCount);

    // other methods
    /**
     * Invoked by the Aspect Manager before the space is shut down.
     *
     * @param request
     *            the original request sent to the core.
     * @return the aspect result
     */
    AspectResult preShutdown(ShutdownRequest request);

}
