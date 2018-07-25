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
package org.mozartspaces.rest;

import java.net.URI;
import java.util.Map;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriBuilder;

import org.mozartspaces.core.Message;
import org.mozartspaces.core.remote.MessageDistributor;
import org.mozartspaces.core.remote.Sender;
import org.mozartspaces.core.util.SerializationException;
import org.mozartspaces.core.util.Serializer;
import org.mozartspaces.util.LoggerFactory;
import org.slf4j.Logger;

import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.ClientResponse.Status;

/**
 * a sender that sends messages to a remote XVSM-REST-Server trough a jersey-client expecting a HTTP 202 Accepted return
 * value as long as the message deserialized correctly on the other end. Application-Exceptions like
 * ContainerNotFoundException will be sent back from the other XVSM-Server asynchronously to the MessageResource.
 *
 * @author Christian Proinger
 *
 */
class SimpleWebSender extends BaseWebSender implements Sender {

    private static final Logger log = LoggerFactory.get();

    public SimpleWebSender(final MessageDistributor messageDistributor, final Map<String, Serializer> serializers) {
        super(messageDistributor, serializers);
    }

    @Override
    public void sendMessage(final Message<?> message) throws SerializationException {
            log.debug("Trying to send message to {}", message.getDestinationSpace());
        URI destinationSpace = message.getDestinationSpace();
        URI uri = UriBuilder.fromUri(destinationSpace).path("/messages").build();
        // AbstractMessage pmessage = MARSHALLER_HELPER.marshal(message);
        // JAXBElement<AbstractMessage> jaxbe = new ObjectFactory().createMessage(pmessage);
        try {
            ClientResponse cres = jerseyClient.resource(uri).entity(message)
                    .header("Content-Type", MediaType.APPLICATION_JSON).post(ClientResponse.class);
            Status resStatus = cres.getClientResponseStatus();
            if (resStatus == ClientResponse.Status.ACCEPTED) {
                // success
                sentMessages++;
            } else {
                // something happened
                log.error("error while sending message to {}. HTTP-Status was {}", destinationSpace, resStatus);
                // TODO reschedule like in TcpSocketSender.
            }
        } catch (ClientHandlerException e) {
            // TODO reschedule like in TcpSocketSender.
            log.error("", e);
        }
    }

}