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

import org.mozartspaces.core.EmbeddedResponseHandler;
import org.mozartspaces.core.MzsCore;
import org.mozartspaces.core.MzsCoreRuntimeException;
import org.mozartspaces.core.config.RestConfiguration;
import org.mozartspaces.core.remote.Receiver;
import org.mozartspaces.core.remote.RemoteMessageDistributor;
import org.mozartspaces.core.remote.Sender;
import org.mozartspaces.core.remote.Transport;
import org.mozartspaces.core.util.Serializer;
import org.mozartspaces.util.LoggerFactory;
import org.slf4j.Logger;

/**
 * Wrapps a sender and a receiver.
 *
 * Determines from the RestConfiguration which kind of sender it should create.
 *
 * @author Christian Proinger
 *
 */
public final class WebTransport implements Transport {

    private final static Logger log = LoggerFactory.get();

    private final WebReceiver receiver;
    private BaseWebSender sender;

    public WebTransport(final RestConfiguration restConfig, final MzsCore core,
            final RemoteMessageDistributor messageDistributor, final EmbeddedResponseHandler embeddedResponseHandler,
            final Map<String, Serializer> serializers) {

        this.receiver = new WebReceiver(restConfig, core, messageDistributor, serializers);
        String confSender = restConfig.getSender();

        if ("simple".equals(confSender)) {
            this.sender = new SimpleWebSender(messageDistributor, serializers);
        } else if ("atmosphere".equals(confSender)) {
            this.sender = new AtmosphereWebSender(messageDistributor, serializers);
        } else {
            throw new MzsCoreRuntimeException(
                    confSender
                            + " is not a valid value for the sender configuration-element. valid values are 'simple' and 'atmosphere'");
        }
        log.info("instantiated WebTransport with " + confSender + " sender");
    }

    @Override
    public Receiver getReceiver() {
        return receiver;
    }

    @Override
    public Sender getSender() {
        return sender;
    }
}
