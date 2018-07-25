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
import java.net.URI;

import org.mozartspaces.core.config.Configuration;

/**
 * The central part of the Core API, used to send requests to an XVSM core.
 *
 * @author Tobias Doenz
 */
public interface MzsCore {

    /**
     * @return the configuration used to create the core
     */
    Configuration getConfig();

    /**
     * Sends a request asynchronously to an XVSM core. The request is routed to the core specified in the request, or
     * the embedded core, if no space is explicitly set. The request result is set in the request future that is
     * returned by this method.
     *
     * @param <R>
     *            the result type returned by the <code>RequestFuture</code>
     * @param request
     *            the request to send
     * @param space
     *            the space to which the request should be sent, you can use <code>null</code> for the embedded space
     * @return a <code>RequestFuture</code> to obtain the request result
     */
    <R extends Serializable> RequestFuture<R> send(Request<R> request, URI space);

    /**
     * Sends a request asynchronously to an XVSM core. The request is routed to the core specified in the request, or
     * the embedded core, if no space is explicitly set. The answer to this request is set in the request future that is
     * returned by this method and the according method of the callback handler is invoked.
     *
     * @param <R>
     *            the result type returned by the <code>RequestFuture</code>
     * @param request
     *            the request to send
     * @param space
     *            the space to which the request should be sent, you can use <code>null</code> for the embedded space
     * @param callbackHandler
     *            the callback handler, gets invoked when an answer for this request is received
     * @return a <code>RequestFuture</code> to obtain the request result
     */
    <R extends Serializable> RequestFuture<R> send(Request<R> request, URI space,
            RequestCallbackHandler<? extends Request<?>, ? extends Serializable> callbackHandler);

    /**
     * Sends a request asynchronously to an XVSM core. The request is routed to the core specified in the request, or
     * the embedded core, if no space is explicitly set. The answer to this request is written to the specified answer
     * container with a default FIFO selector.
     *
     * @param <R>
     *            the result type returned by the <code>RequestFuture</code>
     * @param request
     *            the request to send
     * @param space
     *            the space to which the request should be sent, you can use <code>null</code> for the embedded space
     * @param answerContainer
     *            the container where the answer is written
     */
    <R extends Serializable> void send(Request<R> request, URI space, ContainerReference answerContainer);

    /**
     * Sends a request asynchronously to an XVSM core. The request is routed to the core specified in the request, or
     * the embedded core, if no space is explicitly set. The answer to this request is written to the specified answer
     * container with a default FIFO coordinator and a default Key Coordinator with the specified "coordination key" as
     * key.
     *
     * @param <R>
     *            the result type returned by the <code>RequestFuture</code>
     * @param request
     *            the request to send
     * @param space
     *            the space to which the request should be sent, you can use <code>null</code> for the embedded space
     * @param answerContainer
     *            the container where the answer is written
     * @param coordinationKey
     *            the coordination key, used as key for writing the answer
     */
    <R extends Serializable> void send(Request<R> request, URI space, ContainerReference answerContainer,
            String coordinationKey);

    /**
     * Sends a request asynchronously to an XVSM core. The request is routed to the core specified in the request, or
     * the embedded core, if no space is explicitly set. The answer to this request is written to the specified answer
     * container with a default FIFO coordinator and a default Key Coordinator with the internally generated
     * "coordination key" as key.
     *
     * @param <R>
     *            the result type returned by the <code>RequestFuture</code>
     * @param request
     *            the request to send
     * @param space
     *            the space to which the request should be sent, you can use <code>null</code> for the embedded space
     * @param answerContainer
     *            the container where the answer is written
     * @param coordinationKeyGenerationMethod
     *            determines how the coordination key is internally generated
     * @return the generated coordination key
     */
    <R extends Serializable> String send(Request<R> request, URI space, ContainerReference answerContainer,
            final AnswerCoordinationKeyGenerationMethod coordinationKeyGenerationMethod);

    /**
     * Shuts down the core. This stops the internal thread pools without calling any aspects beforehand.
     *
     * @param wait
     *            determines if the shutdown should be made synchronous (<code>true</code>, wait for shutdown to
     *            complete) or asynchronous in its own thread (<code>false</code>)
     */
    void shutdown(boolean wait);
}
