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
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import net.jcip.annotations.Immutable;

import org.mozartspaces.core.config.CommonsXmlConfiguration;
import org.mozartspaces.core.config.Configuration;
import org.mozartspaces.core.metamodel.MetaDataProvider;
import org.mozartspaces.core.metamodel.MetaModelUtils;
import org.mozartspaces.core.metamodel.Navigable;
import org.mozartspaces.core.remote.Sender;
import org.mozartspaces.core.requests.AbstractRequest;
import org.mozartspaces.core.requests.WriteEntriesRequest;
import org.mozartspaces.core.util.CoreUtils;
import org.mozartspaces.core.util.SerializationException;
import org.mozartspaces.runtime.RequestHandler;
import org.mozartspaces.runtime.util.EntryCopier;
import org.mozartspaces.runtime.util.EntryCopyingException;
import org.mozartspaces.util.LoggerFactory;
import org.slf4j.Logger;

/**
 * The default implementation of the asynchronous interface for requests.
 *
 * @author Tobias Doenz
 * @author Stefan Crass
 */
@Immutable
public final class DefaultMzsCore implements MzsCore, Navigable, MetaDataProvider {

    private static final Logger log = LoggerFactory.get();

    private final AtomicLong requestIdCounter;

    private final RequestHandler requestHandler;
    private final UnansweredRequestStore vac;
    private final Sender sender;
    private final URI thisSpace;
    private final EntryCopier entryCopier;
    private final CoreUtils coreUtils;
    private final Configuration config;

    private final Map<String, Object> metaModel;

    /**
     * Creates an instance of <code>DefaultMzsCore</code>. The configuration is
     * loaded from the configuration file.
     *
     * @return a new <code>DefaultMzsCore</code> instance
     */
    public static DefaultMzsCore newInstance() {
        Configuration config = CommonsXmlConfiguration.load();
        return DefaultMzsCoreFactory.newCore(config);
    }

    /**
     * Creates an instance of <code>DefaultMzsCore</code>. The configuration is
     * loaded from the configuration file, then the port argument is set.
     *
     * @param port
     *            the port where the first TCP Socket Receiver is listening for
     *            new connections
     * @return a new <code>DefaultMzsCore</code> instance
     */
    public static DefaultMzsCore newInstance(final int port) {
        Configuration config = CommonsXmlConfiguration.load(port);
        return DefaultMzsCoreFactory.newCore(config);
    }

    /**
     * Creates an instance of <code>DefaultMzsCore</code> with the passed
     * configuration.
     *
     * @param config
     *            the configuration to use for creating the core
     * @return a new <code>DefaultMzsCore</code> instance
     */
    public static DefaultMzsCore newInstance(final Configuration config) {
        return DefaultMzsCoreFactory.newCore(config);
    }

    /**
     * Creates an instance of <code>DefaultMzsCore</code> without an embedded space and a random port.
     *
     * @return a new <code>DefaultMzsCore</code> instance
     */
    public static DefaultMzsCore newInstanceWithoutSpace() {
        Configuration config = CommonsXmlConfiguration.load(0);
        config.setEmbeddedSpace(false);
        return DefaultMzsCoreFactory.newCore(config);
    }

    /**
     * Constructs the MozartSpaces Core.
     *
     * @param requestHandler
     *            the request handler
     * @param vac
     *            the virtual answer container
     * @param sender
     *            the sender, may be <code>null</code>
     * @param thisSpace
     *            the URI of this space
     * @param entryCopier
     *            the entry copier, may be <code>null</code>
     * @param coreUtils
     *            core helper functions
     */
    DefaultMzsCore(final RequestHandler requestHandler, final UnansweredRequestStore vac,
            final Sender sender, final URI thisSpace, final EntryCopier entryCopier, final CoreUtils coreUtils,
            final Configuration config) {

        this.requestHandler = requestHandler;
        //assert this.requestHandler != null;
        this.vac = vac;
        assert this.vac != null;
        this.sender = sender;
        // assert this.sender != null;
        this.thisSpace = thisSpace;
        assert this.thisSpace != null;
        this.entryCopier = entryCopier;
        this.coreUtils = coreUtils;
        assert this.coreUtils != null;
        this.config = config;
        //assert this.config != null;

        requestIdCounter = new AtomicLong(0);

        metaModel = new HashMap<String, Object>();
        if (requestHandler instanceof MetaDataProvider) {
            // TODO move "xp" as constant to MetaModelKeys (or remove)
            metaModel.put("xp", requestHandler);
        }
    }

    @Override
    public Configuration getConfig() {
        return config;
    }

    @Override
    public <R extends Serializable> RequestFuture<R> send(final Request<R> request, final URI space) {
        return send(request, space, (RequestCallbackHandler<? extends Request<?>, ? extends Serializable>) null);
    }

    @Override
    public <R extends Serializable> RequestFuture<R> send(final Request<R> request, final URI space,
            final RequestCallbackHandler<? extends Request<?>, ? extends Serializable> callbackHandler) {

        if (request == null) {
            throw new NullPointerException("Request is null");
        }

        RequestReference requestRef = createRequestReference();

        // add request to virtual answer container
        GenericRequestFuture<R> future = new GenericRequestFuture<R>(false);
        vac.addRequest(requestRef, request, future, callbackHandler);

        routeRequestToDestinationSpace(requestRef, request, space, null);

        return future;
    }

    @Override
    public <R extends Serializable> void send(final Request<R> request, final URI space,
            final ContainerReference answerContainer) {
        send(request, space, answerContainer, (String) null);
    }

    @Override
    public <R extends Serializable> void send(final Request<R> request, final URI space,
            final ContainerReference answerContainer, final String coordinationKey) {
        checkSendWithAnswerContainerParams(request, answerContainer);
        RequestReference requestRef = createRequestReference();
        sendWithAnswerContainer(requestRef, request, space, answerContainer, coordinationKey);
    }

    @Override
    public <R extends Serializable> String send(final Request<R> request, final URI space,
            final ContainerReference answerContainer, final AnswerCoordinationKeyGenerationMethod coordKeyGenMethod) {
        checkSendWithAnswerContainerParams(request, answerContainer);
        if (coordKeyGenMethod == null) {
            throw new NullPointerException("Coordination key type is null");
        }
        RequestReference requestRef = createRequestReference();
        // fixed use of request ref as coordination key (only one possible coordKeyGenMethod value)
        String coordinationKey = requestRef.toString();
        sendWithAnswerContainer(requestRef, request, space, answerContainer, coordinationKey);
        return coordinationKey;
    }

    @Override
    public synchronized void shutdown(final boolean wait) {
        log.info("Starting core shutdown");
        if (requestHandler != null) {
            requestHandler.shutdown(wait);
        }
        if (sender != null) {
            sender.shutdown(wait);
        }
        // TODO when to shutdown VAC?
        // should be done asynchronusly, otherwise an error is set on the ShutdownRequest itself
        //vac.shutdown();

        // TODO improve shutdown process
        // (try to ensure that no new request is enqueued)
        // is synchronizing this method reasonable?
    }

    private RequestReference createRequestReference() {
        String id = Long.toString(requestIdCounter.incrementAndGet());
        RequestReference requestRef = new RequestReference(id, thisSpace);
        log.debug("Created reference for request #{}", id);
        return requestRef;
    }

    private void sendWithAnswerContainer(final RequestReference requestRef, final Request<?> request, final URI space,
            final ContainerReference answerContainer, final String coordinationKey) {
        AnswerContainerInfo answerContainerInfo = new AnswerContainerInfo(answerContainer, coordinationKey);
        routeRequestToDestinationSpace(requestRef, request, space, answerContainerInfo);
    }

    private void checkSendWithAnswerContainerParams(final Request<?> request,
            final ContainerReference answerContainer) {

        if (request == null) {
            throw new NullPointerException("Request is null");
        }
        if (answerContainer == null) {
            throw new NullPointerException("Answer container is null");
        }
    }

    private void routeRequestToDestinationSpace(final RequestReference requestRef, final Request<?> request,
            final URI space, final AnswerContainerInfo answerContainerInfo) {

        RequestMessage requestMessage = new RequestMessage(requestRef, request, space, answerContainerInfo);

        // route request message to the destination space
        if (coreUtils.isEmbeddedSpace(space)) {
            // write request for embedded space to request container
            if (requestHandler == null) {
                throw new MzsCoreRuntimeException(
                        "Request handler not set, cannot process requests for embedded space.");
            }

            copyEntriesAndContext(request);

            requestHandler.processRequest(requestMessage);
        } else {
            // send request for remote space with Sender
            if (sender == null) {
                throw new MzsCoreRuntimeException("Sender not set, cannot process remote requests.");
            }
            try {
                sender.sendMessage(requestMessage);
            } catch (SerializationException ex) {
                throw new MzsCoreRuntimeException(ex);
            }
        }
    }

    private void copyEntriesAndContext(final Request<?> request) {
        if (entryCopier != null) {
            if (config.getEntryCopierConfiguration().isCopyContext() && request instanceof AbstractRequest<?>) {
                // copy context
                AbstractRequest<?> abstractRequest = (AbstractRequest<?>) request;
                try {
                    abstractRequest.setContext(entryCopier.copyContext(abstractRequest.getContext()));
                } catch (EntryCopyingException ex) {
                    throw new MzsCoreRuntimeException(ex);
                }
            }
            if (request instanceof WriteEntriesRequest) {
                // copy the entries (List<Entry>)
                WriteEntriesRequest writeRequest = (WriteEntriesRequest) request;
                try {
                    writeRequest.setEntries(entryCopier.copyEntries(writeRequest.getEntries()));
                } catch (EntryCopyingException ex) {
                    throw new MzsCoreRuntimeException(ex);
                }
            }
        }
    }

    @Override
    public Object navigate(final String path) {
        return MetaModelUtils.navigate(path, this, metaModel);
    }

    @Override
    public Object getMetaData(final int depth) {
        return MetaModelUtils.getData(depth, metaModel);
    }

    @Override
    public void setMetaDataProperty(final String key, final Object value) {
        throw new UnsupportedOperationException("Properties cannot be set here");
    }

}
