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

/**
 * Stores requests for which an answer is expected.
 *
 * @author Tobias Doenz
 */
public interface UnansweredRequestStore {

    /**
     * Adds a request to the store.
     *
     * @param requestRef
     *            the request reference
     * @param request
     *            the request
     * @param requestFuture
     *            the request future, used to get the answer
     * @param callbackHandler
     *            the callback handler, may be <code>null</code>
     */
    void addRequest(RequestReference requestRef, Request<?> request, GenericRequestFuture<?> requestFuture,
            RequestCallbackHandler<? extends Request<?>, ? extends Serializable> callbackHandler);

    /**
     * Removes a request from the store and set an answer on its future and/or
     * invokes the callback handler.
     *
     * @param <R>
     *            the result type
     * @param requestRef
     *            the request reference
     * @param response
     *            the response
     */
    <R extends Serializable> void removeRequestAndSetResponse(RequestReference requestRef, Response<R> response);

    /**
     * Shuts down the store, that is, set an error on all open request futures and callbacks.
     */
    void shutdown();

}
