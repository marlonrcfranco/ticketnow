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
package org.mozartspaces.runtime;

import java.io.Serializable;
import java.net.URI;

import net.jcip.annotations.Immutable;

import org.mozartspaces.core.AnswerContainerInfo;
import org.mozartspaces.core.GenericResponse;
import org.mozartspaces.core.MzsCoreRuntimeException;
import org.mozartspaces.core.RequestMessage;
import org.mozartspaces.core.RequestReference;
import org.mozartspaces.core.Response;
import org.mozartspaces.core.ResponseHandler;
import org.mozartspaces.core.ResponseMessage;
import org.mozartspaces.core.remote.Sender;
import org.mozartspaces.core.util.CoreUtils;
import org.mozartspaces.core.util.SerializationException;
import org.mozartspaces.util.LoggerFactory;
import org.slf4j.Logger;

/**
 * Routes request responses to the embedded response handler or the sender.
 *
 * @author Tobias Doenz
 */
@Immutable
public final class ResponseDistributor {

    private static final Logger log = LoggerFactory.get();

    private final ResponseHandler responseHandler;
    private final Sender sender;
    private final CoreUtils coreUtils;

    /**
     * Constructs a <code>ResponseDistributor</code>.
     *
     * @param responseHandler
     *            the response handler
     * @param sender
     *            the sender, may be <code>null</code>
     * @param coreUtils
     *            class with core helper functions
     */
    public ResponseDistributor(final ResponseHandler responseHandler, final Sender sender,
            final CoreUtils coreUtils) {
        this.responseHandler = responseHandler;
        assert this.responseHandler != null;
        this.sender = sender;
        this.coreUtils = coreUtils;
        assert coreUtils != null;
    }

    /**
     * Distribute an answer (result or error) to a request, that is, hands it to
     * the embedded or remote response handler.
     *
     * @param <R>
     *            the result type
     * @param requestMessage
     *            the request message to which this is the answer
     * @param result
     *            the result, may be <code>null</code>
     * @param error
     *            the error, may be <code>null</code>
     */
    public <R extends Serializable> void distributeAnswer(final RequestMessage requestMessage,
            final R result, final Throwable error) {

        assert requestMessage != null;
        assert result != null || error != null;

        RequestReference requestRef = requestMessage.getRequestReference();
        Response<?> response = new GenericResponse<R>(result, error);
        AnswerContainerInfo answerContainerInfo = requestMessage.getAnswerContainerInfo();
        ResponseMessage responseMessage = new ResponseMessage(requestRef, response, answerContainerInfo);

        log.debug("Distributing response for request {} to {}", requestRef, answerContainerInfo);
        distribute(responseMessage);
    }

    /**
     * Distributes a response message, that is, hands it to the embedded or
     * remote response handler.
     *
     * @param responseMessage
     *            the response message
     */
    private void distribute(final ResponseMessage responseMessage) {

        assert responseMessage != null;

        URI destinationSpace = responseMessage.getDestinationSpace();
        if (coreUtils.isEmbeddedSpace(destinationSpace)) {
            responseHandler.processResponse(responseMessage);
        } else {
            if (sender == null) {
                throw new MzsCoreRuntimeException("No sender set");
            }
            try {
                sender.sendMessage(responseMessage);
            } catch (SerializationException ex) {
                throw new MzsCoreRuntimeException(ex);
            }
        }
    }

}
