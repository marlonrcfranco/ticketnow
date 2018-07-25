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

import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.net.URI;
import java.util.Map;

import org.mozartspaces.core.AnswerCoordinationKeyGenerationMethod;
import org.mozartspaces.core.ContainerReference;
import org.mozartspaces.core.Message;
import org.mozartspaces.core.MzsCore;
import org.mozartspaces.core.MzsCoreRuntimeException;
import org.mozartspaces.core.Request;
import org.mozartspaces.core.RequestCallbackHandler;
import org.mozartspaces.core.RequestFuture;
import org.mozartspaces.core.config.Configuration;
import org.mozartspaces.core.config.RestConfiguration;
import org.mozartspaces.core.remote.MessageDistributor;
import org.mozartspaces.core.remote.Receiver;
import org.mozartspaces.core.remote.RemoteMessageDistributor;
import org.mozartspaces.core.util.Serializer;

/**
 * consists of the REST-Reciever for requests from the "embedded" (pure REST-Client) and the receiver for other
 * XVSM-REST-Servers.
 *
 * for this receiver to work, the embedded web-server must be accessible trough the network. another possible
 * implementation using the atmosphere-framework or any other comet/bayeux-technology would be to create an
 * async-request to a proxy-server, this may be a good alternative for mobile MozartSpaces where beeing accessible
 * through the internet is not easy to achieve.
 *
 * @author Christian Proinger
 *
 */
public final class WebReceiver implements Receiver {
    /**
     * a wrapper that logs the requests that are sent to the wrapped core.
     *
     * @author cproinger
     *
     */
    private final class CountRequestsCoreWrapper implements MzsCore {
        private final MzsCore core;

        public CountRequestsCoreWrapper(final MzsCore core) {
            this.core = core;
        }

        @Override
        public Configuration getConfig() {
            return core.getConfig();
        }

        @Override
        public <R extends Serializable> RequestFuture<R> send(final Request<R> request, final URI space) {
            receivedMessages++;
            return core.send(request, space);
        }

        @Override
        public <R extends Serializable> void send(final Request<R> request, final URI space,
                final ContainerReference answerContainer) {
            receivedMessages++;
            core.send(request, space, answerContainer);
        }

        @Override
        public <R extends Serializable> String send(final Request<R> request, final URI space,
                final ContainerReference answerContainer,
                final AnswerCoordinationKeyGenerationMethod coordinationKeyGenerationMethod) {
            receivedMessages++;
            return core.send(request, space, answerContainer, coordinationKeyGenerationMethod);
        }

        @Override
        public <R extends Serializable> void send(final Request<R> request, final URI space,
                final ContainerReference answerContainer, final String coordinationKey) {
            receivedMessages++;
            core.send(request, space, answerContainer, coordinationKey);
        }

        @Override
        public <R extends Serializable> RequestFuture<R> send(final Request<R> request, final URI space,
                final RequestCallbackHandler<? extends Request<?>, ? extends Serializable> callbackHandler) {
            receivedMessages++;
            return core.send(request, space, callbackHandler);
        }

        @Override
        public void shutdown(final boolean wait) {
            core.shutdown(wait);
        }
    }

    /**
     * a wrapper that counts the messages sent to the message-Distributor.
     *
     * @author Christian Proinger
     *
     */
    private final class CountRequestsMessageDistributorWrapper implements MessageDistributor {
        private final RemoteMessageDistributor messageDistributor;

        private CountRequestsMessageDistributorWrapper(final RemoteMessageDistributor messageDistributor) {
            this.messageDistributor = messageDistributor;
        }

        @Override
        public void distributeMessage(final Message<?> message) {
            receivedMessages++;
            messageDistributor.distributeMessage(message);
        }
    }

    private long receivedMessages = 0;

    // wozu AtomicLong, ist ++ nicht atomic?
    // private AtomicLong receivedMessages = new AtomicLong(0);

    private final MzsBaseWebServer server;

    public WebReceiver(final RestConfiguration restConfig, final MzsCore core,
            final RemoteMessageDistributor messageDistributor, final Map<String, Serializer> serializers) {
        MzsCore coreWrapper = new CountRequestsCoreWrapper(core);
        MessageDistributor messageDistributorWrapper = new CountRequestsMessageDistributorWrapper(messageDistributor);
        // TODO read server id from config
        // server = new MzsJettyServer(restConfig, coreWrapper, messageDistributorWrapper, serializers);
        String className = "org.mozartspaces.rest.MzsJettyServer";
        try {
            Class<?> clazz = Class.forName(className);
            Constructor<?> constructor = clazz.getConstructor(RestConfiguration.class, MzsCore.class,
                    MessageDistributor.class, Map.class);
            server = (MzsBaseWebServer) constructor.newInstance(restConfig, coreWrapper, messageDistributorWrapper,
                    serializers);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }

        // server = new MzsGrizzleyServer(restConfig, coreWrapper, messageDistributorWrapper, serializers);
    }

    @Override
    public long getNumberOfReceivedMessages() {
        return receivedMessages;
    }

    @Override
    public void shutdown(final boolean wait) {
        try {
            server.stop(wait);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void start() {
        try {
            server.start();
        } catch (Exception e) {
            throw new MzsCoreRuntimeException("error while starting the server", e);
        }
    }
}