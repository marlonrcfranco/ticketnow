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

import java.io.Serializable;
import java.util.List;

import org.mozartspaces.capi3.Capi3AspectPort;
import org.mozartspaces.capi3.SubTransaction;
import org.mozartspaces.capi3.Transaction;
import org.mozartspaces.core.ContainerReference;
import org.mozartspaces.core.requests.AddAspectRequest;
import org.mozartspaces.core.requests.DeleteEntriesRequest;
import org.mozartspaces.core.requests.DestroyContainerRequest;
import org.mozartspaces.core.requests.LockContainerRequest;
import org.mozartspaces.core.requests.LookupContainerRequest;
import org.mozartspaces.core.requests.ReadEntriesRequest;
import org.mozartspaces.core.requests.RemoveAspectRequest;
import org.mozartspaces.core.requests.TakeEntriesRequest;
import org.mozartspaces.core.requests.TestEntriesRequest;
import org.mozartspaces.core.requests.WriteEntriesRequest;

/**
 * Interface for container aspects (local aspects). It contains a method for
 * each {@link ContainerIPoint} constant. These methods are before
 * and after (potentially) container specific space operations and are called
 * <code>pre</code> and <code>post</code> aspect methods, respectively.
 * <p>
 * The method parameters are the request, the transaction and sub-transaction in
 * whose context the operation is executed, the container-specific CAPI-3
 * interface and the execution count, which indicates how many times the request
 * has been processed (including this processing). For <code>post</code> aspect
 * methods also the operation result is a parameter. All parameters are not
 * <code>null</code>, except for the methods related to adding and removing an
 * aspect, if the aspect is a space aspect. <code>null</code> is allowed as
 * return value for all methods and interpreted as {@link AspectStatus}
 * <code>OK</code>.
 * <p>
 * An abstract implementation of this interface to ease the implementation of
 * aspects that use only a few interception points can be found in
 * {@link AbstractContainerAspect}.
 *
 * @author Tobias Doenz
 *
 * @see AbstractContainerAspect
 * @see ContainerIPoint
 */
public interface ContainerAspect extends Serializable {

    // entries methods
    /**
     * Invoked by the Aspect Manager before a read operation is performed on the
     * space.
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
     * @return the aspect result
     */
    AspectResult preRead(ReadEntriesRequest<?> request, Transaction tx, SubTransaction stx, Capi3AspectPort capi3,
            int executionCount);

    /**
     * Invoked by the Aspect Manager after a read operation has been performed
     * on the space.
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
     * @param entries
     *            the read entries (result of the CAPI-3 operation)
     * @return the aspect result
     */
    AspectResult postRead(ReadEntriesRequest<?> request, Transaction tx, SubTransaction stx, Capi3AspectPort capi3,
            int executionCount, List<Serializable> entries);

    /**
     * Invoked by the Aspect Manager before a test operation is performed on
     * the space.
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
     * @return the aspect result
     */
    AspectResult preTest(TestEntriesRequest request, Transaction tx, SubTransaction stx, Capi3AspectPort capi3,
            int executionCount);

    /**
     * Invoked by the Aspect Manager after a test operation has been performed
     * on the space.
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
     * @param entries
     *            the deleted entries (result of the CAPI-3 operation)
     * @return the aspect result
     */
    AspectResult postTest(TestEntriesRequest request, Transaction tx, SubTransaction stx, Capi3AspectPort capi3,
            int executionCount, List<Serializable> entries);

    /**
     * Invoked by the Aspect Manager before a take operation is performed on the
     * space.
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
     * @return the aspect result
     */
    AspectResult preTake(TakeEntriesRequest<?> request, Transaction tx, SubTransaction stx, Capi3AspectPort capi3,
            int executionCount);

    /**
     * Invoked by the Aspect Manager after a take operation has been performed
     * on the space.
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
     * @param entries
     *            the taken entries (result of the CAPI-3 operation)
     * @return the aspect result
     */
    AspectResult postTake(TakeEntriesRequest<?> request, Transaction tx, SubTransaction stx, Capi3AspectPort capi3,
            int executionCount, List<Serializable> entries);

    /**
     * Invoked by the Aspect Manager before a delete operation is performed on
     * the space.
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
     * @return the aspect result
     */
    AspectResult preDelete(DeleteEntriesRequest request, Transaction tx, SubTransaction stx, Capi3AspectPort capi3,
            int executionCount);

    /**
     * Invoked by the Aspect Manager after a delete operation has been performed
     * on the space.
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
     * @param entries
     *            the deleted entries (result of the CAPI-3 operation)
     * @return the aspect result
     */
    AspectResult postDelete(DeleteEntriesRequest request, Transaction tx, SubTransaction stx, Capi3AspectPort capi3,
            int executionCount, List<Serializable> entries);

    /**
     * Invoked by the Aspect Manager before a write operation is performed on
     * the space.
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
     * @return the aspect result
     */
    AspectResult preWrite(WriteEntriesRequest request, Transaction tx, SubTransaction stx, Capi3AspectPort capi3,
            int executionCount);

    /**
     * Invoked by the Aspect Manager after a write operation has been performed
     * on the space.
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
     * @return the aspect result
     */
    AspectResult postWrite(WriteEntriesRequest request, Transaction tx, SubTransaction stx, Capi3AspectPort capi3,
            int executionCount);

    // container methods

    /**
     * Invoked by the Aspect Manager before a container destroy operation is
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
     * @return the aspect result
     */
    AspectResult preDestroyContainer(DestroyContainerRequest request, Transaction tx, SubTransaction stx,
            Capi3AspectPort capi3, int executionCount);

    /**
     * Invoked by the Aspect Manager after a container destroy operation has been
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
    AspectResult postDestroyContainer(DestroyContainerRequest request, Transaction tx, SubTransaction stx,
            int executionCount);

    /**
     * Invoked by the Aspect Manager before a lock container operation is
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
     * @return the aspect result
     */
    AspectResult preLockContainer(LockContainerRequest request, Transaction tx, SubTransaction stx,
            Capi3AspectPort capi3, int executionCount);

    /**
     * Invoked by the Aspect Manager after a lock container operation has been
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
     * @return the aspect result
     */
    AspectResult postLockContainer(LockContainerRequest request, Transaction tx, SubTransaction stx,
            Capi3AspectPort capi3, int executionCount);

    /**
     * Invoked by the Aspect Manager after a container lookup operation has been
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
    AspectResult postLookupContainer(LookupContainerRequest request, Transaction tx, SubTransaction stx,
            Capi3AspectPort capi3, int executionCount, ContainerReference container);

    // aspect methods
    /**
     * Invoked by the Aspect Manager before an aspect is added.
     *
     * @param request
     *            the original request sent to the core.
     * @param tx
     *            the transaction, can be explicit or implicit
     * @param stx
     *            the sub-transaction for this operation
     * @param capi3
     *            the container-specific CAPI-3 interface, <code>null</code> if
     *            the aspect to add is a space aspect
     * @param executionCount
     *            the number of processings of this request
     * @return the aspect result
     */
    AspectResult preAddAspect(AddAspectRequest request, Transaction tx, SubTransaction stx,
            Capi3AspectPort capi3, int executionCount);

    /**
     * Invoked by the Aspect Manager after an aspect has been added.
     *
     * @param request
     *            the original request sent to the core.
     * @param tx
     *            the transaction, can be explicit or implicit
     * @param stx
     *            the sub-transaction for this operation
     * @param capi3
     *            the container-specific CAPI-3 interface, <code>null</code> if
     *            a space aspect has been added
     * @param executionCount
     *            the number of processings of this request
     * @param aspect
     *            the reference of the added aspect
     * @return the aspect result
     */
    AspectResult postAddAspect(AddAspectRequest request, Transaction tx, SubTransaction stx,
            Capi3AspectPort capi3, int executionCount, AspectReference aspect);

    /**
     * Invoked by the Aspect Manager before an aspect is removed.
     *
     * @param request
     *            the original request sent to the core.
     * @param tx
     *            the transaction, can be explicit or implicit
     * @param stx
     *            the sub-transaction for this operation
     * @param container
     *            the reference of the container where the aspect should be
     *            removed, <code>null</code> for a space aspect
     * @param capi3
     *            the container-specific CAPI-3 interface, <code>null</code> if
     *            the aspect to remove is a space aspect
     * @param executionCount
     *            the number of processings of this request
     * @return the aspect result
     */
    AspectResult preRemoveAspect(RemoveAspectRequest request, Transaction tx, SubTransaction stx,
            ContainerReference container, Capi3AspectPort capi3, int executionCount);

    /**
     * Invoked by the Aspect Manager after an aspect has been removed.
     *
     * @param request
     *            the original request sent to the core.
     * @param tx
     *            the transaction, can be explicit or implicit
     * @param stx
     *            the sub-transaction for this operation
     * @param container
     *            the reference of the container where the aspect has been
     *            removed, <code>null</code> for a space aspect
     * @param capi3
     *            the container-specific CAPI-3 interface, <code>null</code> if
     *            a space aspect has been removed
     * @param executionCount
     *            the number of processings of this request
     * @return the aspect result
     */
    AspectResult postRemoveAspect(RemoveAspectRequest request, Transaction tx, SubTransaction stx,
            ContainerReference container, Capi3AspectPort capi3, int executionCount);

}
