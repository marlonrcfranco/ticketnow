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
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import net.jcip.annotations.Immutable;

import org.mozartspaces.util.LoggerFactory;
import org.slf4j.Logger;

/**
 * Stores request from the Core API where a response should be set. If a request
 * sent with the Core API of this core uses no answer container or an answer
 * container on this core, then the request reference is stored here until an
 * answer is set.
 *
 * @author Tobias Doenz
 */
@Immutable
public final class VirtualAnswerContainer implements UnansweredRequestStore {

    private static final Logger log = LoggerFactory.get();

    private final Map<RequestReference, GenericRequestFuture<?>> requestFutures;
    private final Map<RequestReference,
                        RequestCallbackHandler<? extends Request<?>, ? extends Serializable>> requestCallbacks;
    private final Map<RequestReference, Request<?>> requests; // for callbacks

    /**
     * Constructs a <code>VirtualAnswerContainer</code>.
     */
    public VirtualAnswerContainer() {
        requestFutures = new ConcurrentHashMap<RequestReference, GenericRequestFuture<?>>();
        requestCallbacks = new ConcurrentHashMap<RequestReference,
                RequestCallbackHandler<? extends Request<?>, ? extends Serializable>>();
        requests = new ConcurrentHashMap<RequestReference, Request<?>>();
    }

    @Override
    public void addRequest(final RequestReference requestRef, final Request<?> request,
            final GenericRequestFuture<?> requestFuture,
            final RequestCallbackHandler<? extends Request<?>, ? extends Serializable> callbackHandler) {

        assert requestRef != null;
        assert request != null;
        assert requestFuture != null;

        requestFutures.put(requestRef, requestFuture);
        if (callbackHandler != null) {
            requestCallbacks.put(requestRef, callbackHandler);
            requests.put(requestRef, request);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public <R extends Serializable> void removeRequestAndSetResponse(final RequestReference requestRef,
            final Response<R> response) {

        assert requestRef != null;
        assert response != null;

        R result = response.getResult();
        Throwable error = response.getError();

        // request future
        GenericRequestFuture<R> future = (GenericRequestFuture<R>) requestFutures.remove(requestRef);
        if (future != null) {
            log.debug("Setting answer to request future");
            if (result != null) {
                future.setResult(result);
            } else {
                future.setError(error);
            }
        } else {
            log.warn("No request future for {}, but got response {}", requestRef, response);
        }

        // callback handler
        @SuppressWarnings("rawtypes")
        RequestCallbackHandler callbackHandler = requestCallbacks.remove(requestRef);
        if (callbackHandler != null) {
            log.debug("Invoking callback handler");
            Request<?> request = requests.remove(requestRef);
            try {
                if (result != null) {
                    callbackHandler.requestProcessed(request, result);
                } else {
                    callbackHandler.requestFailed(request, error);
                }
            } catch (RuntimeException ex) {
                log.info("Callback handler has thrown exception", ex);
            }
        }
    }

    @Override
    public synchronized void shutdown() {
        log.debug("Setting error on all open request futures and callbacks");
        Exception error = new MzsCoreException("Core shutdown");
        for (Map.Entry<RequestReference, GenericRequestFuture<?>> requestFutureEntry : requestFutures.entrySet()) {
            GenericRequestFuture<?> requestFuture = requestFutures.remove(requestFutureEntry.getKey());
            // null-check because concurrent remove possible (method above not synchronized)
            if (requestFuture != null) {
                requestFuture.setError(error);
            }
        }
        for (Entry<RequestReference,
                RequestCallbackHandler<? extends Request<?>, ? extends Serializable>> requestCbhEntry : requestCallbacks
                .entrySet()) {
            @SuppressWarnings("unchecked")
            RequestCallbackHandler<Request<?>, ?> cbh = (RequestCallbackHandler<Request<?>, ?>) requestCallbacks
                    .remove(requestCbhEntry.getKey());
            Request<?> request = requests.remove(requestCbhEntry.getKey());
            if (cbh != null) {
                cbh.requestFailed(request, error);
            }
        }
    }
}
