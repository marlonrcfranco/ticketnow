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

import java.util.Map;

import javax.ws.rs.core.MediaType;

import org.mozartspaces.core.remote.MessageDistributor;
import org.mozartspaces.core.remote.Sender;
import org.mozartspaces.core.util.Serializer;
import org.mozartspaces.util.LoggerFactory;
import org.mozartspaces.xvsmp.xstream.AbstractXStreamSerializer;
import org.slf4j.Logger;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.jersey.api.client.ClientRequest;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import com.sun.jersey.api.client.filter.ClientFilter;

/**
 * a Base Sender-class for REST-requests. The Jersey-Client is configured and instantiated here.
 *
 * @author Christian Proinger
 */
public abstract class BaseWebSender implements Sender {

    private static final Logger log = LoggerFactory.get();

    protected long sentMessages = 0;
    /**
     * this client is used for simple calls that are implemented synchronously on the server.
     */
    protected Client jerseyClient;

    /**
     * used for response-messages.
     */
    protected MessageDistributor messageDistributor;

    protected Map<String, Serializer> serializers;

    public BaseWebSender(final MessageDistributor messageDistributor, final Map<String, Serializer> serializers) {
        this.messageDistributor = messageDistributor;
        this.serializers = serializers;
        ClientConfig config = new DefaultClientConfig();

        // config.getClasses().add(XStreamJsonProvider.class);
        AbstractXStreamSerializer xstreamSerializer = (AbstractXStreamSerializer) serializers.get("xstream-json");
        config.getSingletons().add(new XStreamJsonProvider(xstreamSerializer.getXStream()));
        // config.getFeatures().put(JSONConfiguration.FEATURE_POJO_MAPPING,
        // Boolean.TRUE);
        jerseyClient = Client.create(config);
        jerseyClient.addFilter(new ClientFilter() {

            @Override
            public ClientResponse handle(final ClientRequest cr) throws ClientHandlerException {
                // log.debug(cr.getEntity() + "");
                sentMessages++;
                return getNext().handle(cr);
            }
        });
    }

    @Override
    public final long getNumberOfSentMessages() {
        return sentMessages;
    }

    @Override
    public void shutdown(final boolean wait) {
        try {
            jerseyClient.destroy();
        } catch (Exception e) {
            // prevent runtime-exceptions from escaping.
            log.warn("error shutting down jersey-client", e);
        }
    }

    protected String getMediaType() {
        return MediaType.APPLICATION_JSON;
    }

}