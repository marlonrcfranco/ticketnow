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
package org.mozartspaces.core.remote;

import net.jcip.annotations.Immutable;

import org.mozartspaces.core.Message;
import org.mozartspaces.core.MzsCoreRuntimeException;
import org.mozartspaces.core.Request;
import org.mozartspaces.core.RequestContext;
import org.mozartspaces.core.RequestMessage;
import org.mozartspaces.core.ResponseHandler;
import org.mozartspaces.core.ResponseMessage;
import org.mozartspaces.core.requests.AbstractRequest;
import org.mozartspaces.core.security.RequestContextUtils;
import org.mozartspaces.runtime.RequestHandler;
import org.mozartspaces.util.LoggerFactory;
import org.slf4j.Logger;

/**
 * The <code>RemoteMessageDistributor</code> distributes received messages
 * depending on their type. It passes {@link RequestMessage}s to the
 * {@link RequestHandler} and {@link ResponseMessage}s to the
 * {@link ResponseHandler}.
 *
 * @author Tobias Doenz
 */
@Immutable
public final class RemoteMessageDistributor implements MessageDistributor {

    private static final Logger log = LoggerFactory.get();

    private final RequestHandler requestHandler;
    private final ResponseHandler responseHandler;

    /**
     * Constructs a <code>RemoteMessageDistributor</code>.
     *
     * @param requestHandler
     *            the request handler (for request messages)
     * @param responseHandler
     *            the response handler (for response messages)
     */
    public RemoteMessageDistributor(final RequestHandler requestHandler, final ResponseHandler responseHandler) {
        this.requestHandler = requestHandler;
        // assert this.requestHandler != null;
        this.responseHandler = responseHandler;
        assert this.responseHandler != null;
    }

    @Override
    public void distributeMessage(final Message<?> message) {
        assert message != null;
        log.debug("Distributing message {}", message);
        if (message instanceof RequestMessage) {
            if (requestHandler == null) {
                log.error("No request handler set");
                return;
            }
            RequestMessage requestMessage = (RequestMessage) message;

            // set flag that indicates that this request is a remote request
            // Note: this does not secure us from malicious aspects, they can just change or replace the context
            Request<?> request = requestMessage.getContent();
            if (request instanceof AbstractRequest) {
                AbstractRequest<?> abstractRequest = (AbstractRequest<?>) request;
                RequestContext context = abstractRequest.getContext();
                if (context == null) {
                    context = new RequestContext();
                    abstractRequest.setContext(context);
                }
                context.setRequestProperty(RequestContextUtils.REMOTE_REQUEST, Boolean.TRUE);
            }

            requestHandler.processRequest(requestMessage);
        } else if (message instanceof ResponseMessage) {
            ResponseMessage responseMessage = (ResponseMessage) message;
            responseHandler.processResponse(responseMessage);
        } else {
            throw new MzsCoreRuntimeException("Message of invalid type " + message.getClass().getName());
        }
    }
}
