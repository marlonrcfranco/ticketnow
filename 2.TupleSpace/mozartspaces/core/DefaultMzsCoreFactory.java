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

import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.net.InetAddress;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

import net.jcip.annotations.Immutable;

import org.mozartspaces.capi3.AnyCoordinator;
import org.mozartspaces.capi3.Capi3;
import org.mozartspaces.capi3.CoordinationTranslationFactory;
import org.mozartspaces.capi3.Coordinator;
import org.mozartspaces.capi3.CoordinatorTranslator;
import org.mozartspaces.capi3.QueryCoordinator;
import org.mozartspaces.capi3.Selector;
import org.mozartspaces.capi3.SelectorTranslator;
import org.mozartspaces.capi3.TypeCoordinator;
import org.mozartspaces.capi3.javanative.persistence.PersistenceContext;
import org.mozartspaces.capi3.javanative.persistence.PersistenceInitializationException;
import org.mozartspaces.core.aspects.SpaceAspect;
import org.mozartspaces.core.aspects.SpaceIPoint;
import org.mozartspaces.core.authentication.IdentityProvider;
import org.mozartspaces.core.authentication.RequestAuthenticationAspect;
import org.mozartspaces.core.authorization.AuthorizationLevel;
import org.mozartspaces.core.authorization.RequestAuthorizationAspect;
import org.mozartspaces.core.config.Configuration;
import org.mozartspaces.core.config.CoordinatorConfiguration;
import org.mozartspaces.core.config.EntryCopierConfiguration;
import org.mozartspaces.core.config.PersistenceConfiguration;
import org.mozartspaces.core.config.RestConfiguration;
import org.mozartspaces.core.config.SecurityConfiguration;
import org.mozartspaces.core.config.SerializingEntryCopierConfiguration;
import org.mozartspaces.core.config.TcpSocketConfiguration;
import org.mozartspaces.core.config.TransportConfiguration;
import org.mozartspaces.core.remote.Receiver;
import org.mozartspaces.core.remote.RemoteMessageDistributor;
import org.mozartspaces.core.remote.Sender;
import org.mozartspaces.core.remote.SimpleCommunicationManager;
import org.mozartspaces.core.remote.Transport;
import org.mozartspaces.core.remote.tcpsocket.TcpSocketReceiver;
import org.mozartspaces.core.remote.tcpsocket.TcpSocketSender;
import org.mozartspaces.core.requests.AddAspectRequest;
import org.mozartspaces.core.requests.CreateContainerRequest;
import org.mozartspaces.core.util.CoordinatorDefinition;
import org.mozartspaces.core.util.CoreUtils;
import org.mozartspaces.core.util.NamedThreadFactory;
import org.mozartspaces.core.util.SerializationCacheFactory;
import org.mozartspaces.core.util.Serializer;
import org.mozartspaces.core.util.WithinThreadExecutorService;
import org.mozartspaces.runtime.DefaultTransactionManager;
import org.mozartspaces.runtime.ResponseDistributor;
import org.mozartspaces.runtime.RuntimeData;
import org.mozartspaces.runtime.ThreadPoolRequestHandler;
import org.mozartspaces.runtime.aspects.AspectInvoker;
import org.mozartspaces.runtime.aspects.AspectManager;
import org.mozartspaces.runtime.aspects.SerialAspectInvoker;
import org.mozartspaces.runtime.aspects.SimpleAspectManager;
import org.mozartspaces.runtime.blocking.NonPollingTimeoutProcessor;
import org.mozartspaces.runtime.blocking.RequestTimeoutHandler;
import org.mozartspaces.runtime.blocking.SynchronizedWaitAndEventManager;
import org.mozartspaces.runtime.blocking.TimeoutProcessor;
import org.mozartspaces.runtime.blocking.deadlock.LockedTaskHandler;
import org.mozartspaces.runtime.tasks.Task;
import org.mozartspaces.runtime.util.CloningEntryCopier;
import org.mozartspaces.runtime.util.EntryCopier;
import org.mozartspaces.runtime.util.RuntimeUtils;
import org.mozartspaces.runtime.util.SerializingEntryCopier;
import org.mozartspaces.util.LoggerFactory;
import org.slf4j.Logger;

/**
 * Utility class with a factory method for {@code DefaultMzsCore}.
 *
 * @author Tobias Doenz
 * @author Christian Proinger
 */
@Immutable
public final class DefaultMzsCoreFactory {

    private static final Logger log = LoggerFactory.get();

    private static final Map<String, String> KNOWN_SERIALIZERS;

    /**
     * ID of the serializer using the built-in binary serialization.
     */
    public static final String SERIALIZER_JAVABUILTIN_ID = "javabuiltin";
    private static final String SERIALIZER_JAVABUILTIN_CLASS = "org.mozartspaces.core.util.JavaBuiltinSerializer";

    /**
     * ID of the JAXB XML serializer.
     */
    public static final String SERIALIZER_JAXB_ID = "jaxb";
    private static final String SERIALIZER_JAXB_CLASS = "org.mozartspaces.xvsmp.jaxb.JAXBSerializer";

    /**
     * ID of the XStream XML serializer.
     */
    public static final String SERIALIZER_XSTREAM_XML_ID = "xstream-xml";
    private static final String SERIALIZER_XSTREAM_XML_CLASS = "org.mozartspaces.xvsmp.xstream.XStreamXmlSerializer";

    /**
     * ID of the XStream JSON serializer.
     */
    public static final String SERIALIZER_XSTREAM_JSON_ID = "xstream-json";
    private static final String SERIALIZER_XSTREAM_JSON_CLASS = "org.mozartspaces.xvsmp.xstream.XStreamJsonSerializer";

    /**
     * ID of the kryo serializer.
     */
    public static final String SERIALIZER_KRYO_ID = "kryo";
    private static final String SERIALIZER_KRYO_CLASS = "org.mozartspaces.xvsmp.kryo.KryoSerializer";

    /**
     * ID of the native CAPI-3 implementation.
     */
    public static final String CAPI3_JAVANATIVE_ID = "javanative";
    private static final String CAPI3_JAVANATIVE_CLASS = "org.mozartspaces.capi3.javanative.DefaultCapi3Native";

    /**
     * ID of the DB CAPI-3 implementation.
     */
    public static final String CAPI3_DB_ID = "db";
    private static final String CAPI3_DB_CLASS = "org.mozartspaces.capi3.db.Capi3DB";

    private static final String IDP_OPENAM_CLASS = "org.mozartspaces.security.openam.OpenAmIdentityProvider";

    static {
        /**
         * Maps IDs to class names. Serializer classes need to implement org.mozartspaces.core.util.Serializer and have
         * a public no-arg constructor.
         */
        KNOWN_SERIALIZERS = new HashMap<String, String>();
        KNOWN_SERIALIZERS.put(SERIALIZER_JAVABUILTIN_ID, SERIALIZER_JAVABUILTIN_CLASS);
        KNOWN_SERIALIZERS.put(SERIALIZER_JAXB_ID, SERIALIZER_JAXB_CLASS);
        KNOWN_SERIALIZERS.put(SERIALIZER_XSTREAM_XML_ID, SERIALIZER_XSTREAM_XML_CLASS);
        KNOWN_SERIALIZERS.put(SERIALIZER_XSTREAM_JSON_ID, SERIALIZER_XSTREAM_JSON_CLASS);
        KNOWN_SERIALIZERS.put(SERIALIZER_KRYO_ID, SERIALIZER_KRYO_CLASS);
    }

    /**
     * Creates a new {@code MzsCore} instance.
     *
     * @param config
     *            the configuration to use
     * @return the created core instance
     */
    static DefaultMzsCore newCore(final Configuration config) {

        log.debug("Space URI: " + config.getSpaceUri());
        DelegateMzsCore delegateCore = new DelegateMzsCore(config);

        // Utility classes
        CoreUtils coreUtils = new CoreUtils(config.getSpaceUri());
        VirtualAnswerContainer vac = new VirtualAnswerContainer();
        EmbeddedResponseHandler embeddedResponseHandler = new EmbeddedResponseHandler(coreUtils, vac);

        // Core Processor, Request Container
        int xpThreadNumber = 0;
        ThreadPoolRequestHandler requestHandler = null;
        if (config.isEmbeddedSpace()) {
            xpThreadNumber = config.getXpThreadNumber();
            ExecutorService xpThreadPool = createThreadPool(xpThreadNumber, "CoreProcessor-");
            requestHandler = new ThreadPoolRequestHandler(xpThreadPool);
        }
        RemoteMessageDistributor messageDistributor = new RemoteMessageDistributor(requestHandler,
                embeddedResponseHandler);

        // Serializers
        Map<String, Serializer> serializers = new HashMap<String, Serializer>();
        for (String serializerId : config.getSerializerIds()) {
            Serializer serializer = createSerializer(serializerId);
            serializers.put(serializerId, serializer);
        }

        // Transports
        List<Receiver> receivers = new ArrayList<Receiver>();
        Map<String, Sender> senders = new HashMap<String, Sender>();
        Map<String, TransportConfiguration> transportConfigs = config.getTransportConfigurations();
        int tcpSocketNum = 0;
        for (Map.Entry<String, TransportConfiguration> entry : transportConfigs.entrySet()) {
            TransportConfiguration transportConfig = entry.getValue();
            if (transportConfig instanceof TcpSocketConfiguration) {
                tcpSocketNum++;
                TcpSocketConfiguration tcpSocketConfig = (TcpSocketConfiguration) transportConfig;
                int threadNumber = tcpSocketConfig.getThreadNumber();
                int receiverPort = tcpSocketConfig.getReceiverPort();
                String serializerId = tcpSocketConfig.getSerializerId();
                log.debug("TCP Socket configuration {}:", tcpSocketNum);
                log.debug("Receiver port: {}", receiverPort);
                log.debug("Serializer ID: {}", serializerId);
                Serializer serializer = serializers.get(serializerId);
                if (serializer == null) {
                    serializer = createSerializer(serializerId);
                    serializers.put(serializerId, serializer);
                }
                String threadNamePrefix = "TcpSocket-" + tcpSocketNum + "-";
                ExecutorService transportThreadPool = createThreadPool(threadNumber, threadNamePrefix);
                try {
                    URI spaceUri = config.getSpaceUri();
                    // InetAddress bindAddress = InetAddress.getByName(spaceUri.getHost());
                    InetAddress bindAddress = InetAddress.getByName(config.getBindHost());
                    TcpSocketReceiver receiver = new TcpSocketReceiver(receiverPort, bindAddress, serializer,
                            transportThreadPool, messageDistributor);
                    if (receiverPort == 0) {
                        tcpSocketConfig.setReceiverPort(receiver.getPort());
                        if (spaceUri.getPort() == 0) {
                            URI newSpaceUri = CoreUtils.cloneUriWithNewPort(spaceUri, receiver.getPort());
                            config.setSpaceUri(newSpaceUri);
                            coreUtils.setSpaceUri(newSpaceUri);
                            log.debug("Changed space URI to {}", newSpaceUri);
                        }
                    }
                    receivers.add(receiver);
                } catch (IOException ex) {
                    transportThreadPool.shutdown();
                    if (requestHandler != null) {
                        requestHandler.shutdown(true);
                    }
                    throw new MzsCoreRuntimeException(ex);
                }
                Sender sender = new TcpSocketSender(serializer, transportThreadPool, embeddedResponseHandler);
                senders.put(entry.getKey(), sender);
            } else if (transportConfig instanceof RestConfiguration) {
                RestConfiguration restConfig = (RestConfiguration) transportConfig;
                try {
                    // TODO make transport instantiation more modular
                    Class<?> clazz = Class.forName("org.mozartspaces.rest.WebTransport");
                    Constructor<?> constructor = clazz.getConstructor(RestConfiguration.class, MzsCore.class,
                            RemoteMessageDistributor.class, EmbeddedResponseHandler.class, Map.class);
                    Transport transport = (Transport) constructor.newInstance(restConfig, delegateCore,
                            messageDistributor, embeddedResponseHandler, serializers);
                    receivers.add(transport.getReceiver());
                    senders.put(entry.getKey(), transport.getSender());
                } catch (Exception ex) {
                    throw new MzsCoreRuntimeException("Could not create REST transport. "
                            + "Make sure you have mozartspaces-transport-rest-common in the classpath.", ex);
                }
            }
        }
        SimpleCommunicationManager remoteManager = new SimpleCommunicationManager(receivers, senders,
                config.getDefaultScheme());

        if (!config.isEmbeddedSpace()) {
            DefaultMzsCore core = new DefaultMzsCore(requestHandler, vac, remoteManager, config.getSpaceUri(), null,
                    coreUtils, config);
            // must be called before the receivers are started then we're fine
            embeddedResponseHandler.setCore(core);
            delegateCore.setCore(core);
            startReceivers(receivers);
            log.info("MozartSpaces client core is running");
            return core;
        }

        // Entry copier
        EntryCopier entryCopier = createEntryCopier(config.getEntryCopierConfiguration(), serializers);

        // Coordinator definitions
        List<CoordinatorDefinition> coordinatorDefs = loadCoordinatorDefinitions(config.getCoordinatorConfigurations());
        Map<Class<? extends Coordinator>, CoordinatorTranslator<?, ?>> coordinatorTranslators =
                new HashMap<Class<? extends Coordinator>, CoordinatorTranslator<?, ?>>();
        final Map<Class<? extends Selector>, SelectorTranslator<?, ?>> selectorTranslators =
                new HashMap<Class<? extends Selector>, SelectorTranslator<?, ?>>();
        for (CoordinatorDefinition coordinatorDef : coordinatorDefs) {
            try {
                Class<? extends Coordinator> coordApiClass = coordinatorDef.getApiClass();
                CoordinatorTranslator<?, ?> coordCapi3Translator = coordinatorDef.getCapi3TranslatorClass()
                        .newInstance();
                coordinatorTranslators.put(coordApiClass, coordCapi3Translator);
                Class<? extends Selector> selectorApiClass = coordinatorDef.getApiSelectorClass();
                SelectorTranslator<?, ?> selectorCapi3Translator = coordinatorDef.getCapi3SelectorTranslatorClass()
                        .newInstance();
                selectorTranslators.put(selectorApiClass, selectorCapi3Translator);
            } catch (Exception ex) {
                throw new MzsCoreRuntimeException("Cannot instantiate coordination translator", ex);
            }
        }
        CoordinationTranslationFactory coordTranslationFactory = new CoordinationTranslationFactory(
                coordinatorTranslators, selectorTranslators);

        // PersistenceContext
        PersistenceConfiguration persistencConfig = config.getPersistenceConfiguration();
        String serializerId = persistencConfig.getPersistenceSerializer();
        Serializer serializer = serializers.get(serializerId);
        if (serializer == null) {
            serializer = createSerializer(serializerId);
            serializers.put(serializerId, serializer);
        }
        if (persistencConfig.getPersistenceSerializerCacheSize() > 0) {
            serializer = SerializationCacheFactory.createCachingSerializer(serializer,
                    persistencConfig.getPersistenceSerializerCacheSize());
        }
        PersistenceContext persistenceContext = createPersistenceContext(persistencConfig.getPersistenceProfile(),
                persistencConfig.getPersistenceProperties(), serializer);

        SecurityConfiguration securityConfig = config.getSecurityConfiguration();
        // CAPI-3
        Capi3 capi3 = createCapi3(config.getCapi3Id(), coordTranslationFactory, persistenceContext,
                securityConfig.isAuthorizationEnabled());

        // create rest of core instance (Runtime with components)
        ResponseDistributor responseDistributor = new ResponseDistributor(embeddedResponseHandler, remoteManager,
                coreUtils);

        DefaultTransactionManager txManager = new DefaultTransactionManager(coreUtils);
        AspectManager aspectManager = new SimpleAspectManager(config.getSpaceUri());
        AspectInvoker aspectInvoker = new SerialAspectInvoker(aspectManager);

        RuntimeUtils runtimeUtils = new RuntimeUtils(config.getSpaceUri());
        LockedTaskHandler lockedRequestHandler = null; // new TransactionLockGraph();
        RuntimeData runtimeData = new RuntimeData(responseDistributor, capi3, txManager, aspectInvoker, aspectManager,
                entryCopier, runtimeUtils, lockedRequestHandler);
        requestHandler.setRuntimeData(runtimeData);

        // TODO make TimeoutProcessor implementation configurable (with timeout)
        TimeoutProcessor<Task> requestTP = new NonPollingTimeoutProcessor<Task>("Request-TP");
        runtimeData.setRequestTimeoutProcessor(requestTP);

        boolean asyncRescheduling = xpThreadNumber == 0;
        SynchronizedWaitAndEventManager waitEventManager = new SynchronizedWaitAndEventManager(requestHandler,
                requestTP, txManager, lockedRequestHandler, asyncRescheduling);
        runtimeData.setWaitEventManager(waitEventManager);
        txManager.setWaitEventManager(waitEventManager);

        RequestTimeoutHandler requestTH = new RequestTimeoutHandler(requestHandler, waitEventManager);
        requestTP.setTimeoutHandler(requestTH);

        DefaultMzsCore core = new DefaultMzsCore(requestHandler, vac, remoteManager, config.getSpaceUri(), entryCopier,
                coreUtils, config);
        runtimeData.setCore(core);
        txManager.setCore(core);
        embeddedResponseHandler.setCore(core);
        delegateCore.setCore(core);

        // add authorization and authentication aspects
        prepareSecurityAspects(core, securityConfig);

        // start receivers at end of core creation, otherwise message may be
        // received before the core is completely constructed
        startReceivers(receivers);

        log.info("MozartSpaces core is running");

        return core;
    }

    private static void prepareSecurityAspects(final DefaultMzsCore core, final SecurityConfiguration securityConfig) {
        if (securityConfig.isAuthenticationEnabled()) {
            // TODO make more flexible, allow different authentication adapters
            IdentityProvider idp = null;
            try {
                @SuppressWarnings("unchecked")
                Class<? extends IdentityProvider> idpClass = (Class<? extends IdentityProvider>) Class
                        .forName(IDP_OPENAM_CLASS);
                Constructor<? extends IdentityProvider> idpConstructor = idpClass.getConstructor(String.class,
                        String.class);
                idp = idpConstructor.newInstance("/", "DataStore");
            } catch (Exception ex) {
                throw new MzsCoreRuntimeException("Cannot create identity provider", ex);
            }
            SpaceAspect authenticationAspect = new RequestAuthenticationAspect(idp);
            AddAspectRequest addAspect = AddAspectRequest.withAspect(authenticationAspect)
                    .iPoints(SpaceIPoint.ALL_PRE_POINTS).build();
            try {
                core.send(addAspect, null).getResult();
                log.debug("Added aspect for request authentication");
            } catch (Exception ex) {
                throw new MzsCoreRuntimeException("Adding request authentication aspect failed", ex);
            }
        }
        if (securityConfig.isAuthorizationEnabled()) {
            // note: the policy container is created internally in CAPI-3
            if (securityConfig.isAuthorizeRequests()) {
                // TODO create container as internal meta-container (special authorization semantics)
                try {
                    CreateContainerRequest createContainer = CreateContainerRequest.withBuilder()
                            .name(MzsConstants.REQUEST_CONTAINER_NAME)
                            .obligatoryCoords(new AnyCoordinator(), new QueryCoordinator(), new TypeCoordinator())
                            .forceInMemory(true).authLevel(AuthorizationLevel.SECURE).build();
                ContainerReference requestContainer = core.send(createContainer, null).getResult();
                SpaceAspect requestAuthAspect = new RequestAuthorizationAspect(requestContainer);
                    AddAspectRequest addAspect = AddAspectRequest.withAspect(requestAuthAspect)
                            .iPoints(SpaceIPoint.ALL_PRE_POINTS).build();
                    core.send(addAspect, null).getResult();
                log.debug("Added aspect for request-based authorization with container {}", requestContainer.getId());
                } catch (Exception ex) {
                    throw new MzsCoreRuntimeException("Configuring request-based authorization failed", ex);
                }
            }
        }

    }

    private static void startReceivers(final List<Receiver> receivers) {
        for (Receiver receiver : receivers) {
            receiver.start();
        }
    }

    private static List<CoordinatorDefinition> loadCoordinatorDefinitions(
            final List<CoordinatorConfiguration> coordinatorConfigurations) {
        List<CoordinatorDefinition> coordinatorDefs = new ArrayList<CoordinatorDefinition>();
        for (CoordinatorConfiguration coordinatorConfig : coordinatorConfigurations) {
            try {
                log.debug("Loading definition for coordinator {}", coordinatorConfig.getApiClassName());
                @SuppressWarnings("unchecked")
                Class<? extends Coordinator> apiClass = (Class<? extends Coordinator>) Class.forName(coordinatorConfig
                        .getApiClassName());
                @SuppressWarnings("unchecked")
                Class<? extends Selector> apiSelectorClass = (Class<? extends Selector>) Class
                        .forName(coordinatorConfig.getApiSelectorClassName());
                @SuppressWarnings("unchecked")
                Class<? extends CoordinatorTranslator<?, ?>> capi3TranslatorClass =
                        (Class<? extends CoordinatorTranslator<?, ?>>) Class
                        .forName(coordinatorConfig.getCapi3TranslatorClassName());
                @SuppressWarnings("unchecked")
                Class<? extends SelectorTranslator<?, ?>> capi3SelectorTranslatorClass =
                        (Class<? extends SelectorTranslator<?, ?>>) Class
                        .forName(coordinatorConfig.getCapi3SelectorTranslatorClassName());
                coordinatorDefs.add(new CoordinatorDefinition(apiClass, apiSelectorClass, capi3TranslatorClass,
                        capi3SelectorTranslatorClass));
            } catch (ClassNotFoundException ex) {
                throw new MzsCoreRuntimeException("Cannot load coordination class", ex);
            } catch (ClassCastException ex) {
                throw new MzsCoreRuntimeException("Coordination class is of wrong type", ex);
            }
        }
        return coordinatorDefs;
    }

    private static EntryCopier createEntryCopier(final EntryCopierConfiguration entryCopierConfig,
            final Map<String, Serializer> serializers) {

        String name = entryCopierConfig.getName();
        if (name.equals(EntryCopierConfiguration.NAME_NONE)) {
            log.debug("No entry copier (element 'none' found)");
            return null;
        }
        if (name.equals(EntryCopierConfiguration.NAME_SERIALIZING)
                && entryCopierConfig instanceof SerializingEntryCopierConfiguration) {
            SerializingEntryCopierConfiguration config = (SerializingEntryCopierConfiguration) entryCopierConfig;
            String serializer = config.getSerializerId();
            log.debug("Serializing entry copier with serializer {}", serializer);
            return new SerializingEntryCopier(serializers.get(serializer));
        }
        if (name.equals(EntryCopierConfiguration.NAME_CLONING)) {
            log.debug("Cloning entry copier");
            return new CloningEntryCopier();
        }
        log.debug("No entry copier");
        return null;
    }

    private static Capi3 createCapi3(final String capi3Id,
            final CoordinationTranslationFactory coordTranslationFactory,
            final PersistenceContext persistenceContext, final boolean authorizationEnabled) {

        Capi3 capi3 = null;
        if (capi3Id.equals(CAPI3_JAVANATIVE_ID)) {
            log.debug("JavaNative CAPI-3 selected");
            try {
                @SuppressWarnings("unchecked")
                Class<? extends Capi3> capi3Class = (Class<? extends Capi3>) Class.forName(CAPI3_JAVANATIVE_CLASS);
                Constructor<? extends Capi3> constr = capi3Class.getConstructor(CoordinationTranslationFactory.class,
                        PersistenceContext.class, boolean.class);
                capi3 = constr.newInstance(coordTranslationFactory, persistenceContext, authorizationEnabled);
            } catch (Exception ex) {
                throw new MzsCoreRuntimeException("Cannot create CAPI-3 instance", ex);
            }
        } else if (capi3Id.equals("db")) {
            log.debug("DB CAPI-3 selected");
            try {
                capi3 = (Capi3) Class.forName(CAPI3_DB_CLASS).newInstance();
            } catch (Exception ex) {
                throw new MzsCoreRuntimeException("Cannot create CAPI-3 instance", ex);
            }
        } else {
            throw new MzsCoreRuntimeException("Invalid CAPI-3 identifier " + capi3Id);
        }
        return capi3;
    }

    private static ExecutorService createThreadPool(final int nThreads, final String namePrefix) {
        ThreadFactory threadFactory = new NamedThreadFactory(namePrefix);
        if (nThreads > 0) {
            log.debug("Creating fixed thread pool with {} threads", nThreads);
            return Executors.newFixedThreadPool(nThreads, threadFactory);
        } else if (nThreads == 0) {
            log.debug("Creating synchronous executor service without a thread pool");
            return new WithinThreadExecutorService();
        } else {
            log.debug("Creating cached thread pool");
            return Executors.newCachedThreadPool(threadFactory);
        }
    }

    private static Serializer createSerializer(final String serializerId) {
        String className = KNOWN_SERIALIZERS.get(serializerId);
        // instantiate known serializer
        if (className != null) {
            log.debug("Creating instance of serializer {}", serializerId);
            try {
                return (Serializer) Class.forName(className).newInstance();
            } catch (Exception ex) {
                throw new MzsCoreRuntimeException("Cannot instantiate serializer " + serializerId, ex);
            }
        }
        // try to use string as class name
        log.debug("Trying to instantiate serializer {}", serializerId);
        try {
            Class<?> clazz = Class.forName(serializerId);
            return (Serializer) clazz.newInstance();
        } catch (Exception ex) {
            throw new MzsCoreRuntimeException("Cannot instantiate serializer " + serializerId, ex);
        }
    }

    private static PersistenceContext createPersistenceContext(final String persistenceProfile,
                                                               final Properties properties,
                                                               final Serializer serializer) {
        PersistenceContext persistenceContext;
        try {
            persistenceContext = new PersistenceContext(persistenceProfile,  properties, serializer);
        } catch (PersistenceInitializationException e) {
            log.error("Error while initializing persistence context, falling back to default in-memory.", e);
            persistenceContext = new PersistenceContext();
        }
        return persistenceContext;
    }

    private DefaultMzsCoreFactory() {
    }

    /**
     * delegates invocations to the underlying core. This class acts as a value-holder for a core. it is used in cases
     * where the core itself is not constructed yet but is needed for initialization purposes. i do realize that this is
     * kind of a hack ;).
     *
     * @author Christian Proinger
     */
    private static class DelegateMzsCore implements MzsCore {
        private MzsCore core;
        private final Configuration config;

        DelegateMzsCore(final Configuration config) {
            this.config = config;
        }

        @Override
        public Configuration getConfig() {
            return core == null ? config : core.getConfig();
        }

        /**
         * sets the core of the delegate.
         *
         * @param core
         */
        public void setCore(final MzsCore core) {
            this.core = core;
            assert this.core != null;
        }

        // delegate methods
        @Override
        public <R extends Serializable> RequestFuture<R> send(final Request<R> request, final URI space) {
            return core.send(request, space);
        }

        @Override
        public <R extends Serializable> RequestFuture<R> send(final Request<R> request, final URI space,
                final RequestCallbackHandler<? extends Request<?>, ? extends Serializable> callbackHandler) {
            return core.send(request, space, callbackHandler);
        }

        @Override
        public <R extends Serializable> void send(final Request<R> request, final URI space,
                final ContainerReference answerContainer) {
            core.send(request, space, answerContainer);
        }

        @Override
        public <R extends Serializable> void send(final Request<R> request, final URI space,
                final ContainerReference answerContainer, final String coordinationKey) {
            core.send(request, space, answerContainer, coordinationKey);
        }

        @Override
        public <R extends Serializable> String send(final Request<R> request, final URI space,
                final ContainerReference answerContainer,
                final AnswerCoordinationKeyGenerationMethod coordinationKeyGenerationMethod) {
            return core.send(request, space, answerContainer, coordinationKeyGenerationMethod);
        }

        @Override
        public void shutdown(final boolean wait) {
            core.shutdown(wait);
        }

    }

}
