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
package org.mozartspaces.core;

import java.io.Serializable;
import java.util.concurrent.Future;
import java.util.concurrent.TimeoutException;

/**
 * An object to get the result of a request. Requests are sent asynchronously to
 * the core by {@link MzsCore}, whose send method returns a
 * <code>RequestFuture</code>. Its methods can be used to check if the request
 * has been processed (and a result is available) or wait for the result and
 * retrieve it.
 * <p>
 * This interface extends <code>Future</code> with methods to get the result
 * that throw any exception that occurred directly, not as cause of an
 * <code>ExecutionException</code>.
 * <p>
 * Note: Implementations of this interface do not need to support cancellation.
 *
 * @author Tobias Doenz
 *
 * @param <R>
 *            the result type
 */
public interface RequestFuture<R extends Serializable> extends Future<R> {

    /**
     * Waits for the request to be processed, and then retrieves the result.
     *
     * @return the request result
     * @throws MzsCoreException
     *             if the request processing was not successful and threw an
     *             exception
     * @throws InterruptedException
     *             if the current thread was interrupted while waiting
     */
    R getResult() throws MzsCoreException, InterruptedException;

    /**
     * Waits up to the specified time for the request to be processed, and then
     * retrieves the result or throws an exception.
     *
     * @param timeoutInMilliseconds
     *            the maximum time to wait
     * @return the request result
     * @throws MzsCoreException
     *             if the request processing was not successful and threw an
     *             exception
     * @throws InterruptedException
     *             if the current thread was interrupted while waiting
     * @throws TimeoutException
     *             if the wait timed out
     */
    R getResult(long timeoutInMilliseconds) throws MzsCoreException, InterruptedException, TimeoutException;

    /**
     * Returns <code>true</code> if an answer (result or exception) has been set.
     *
     * @return <code>true</code>, when an answer has been set
     */
    boolean isDone();
}
