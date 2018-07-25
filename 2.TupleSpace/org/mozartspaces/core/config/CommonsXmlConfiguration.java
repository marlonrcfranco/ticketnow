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
package org.mozartspaces.core.config;

import static org.mozartspaces.core.config.Configuration.BINDHOST_DEFAULT;
import static org.mozartspaces.core.config.Configuration.CAPI3_ID_DEFAULT;
import static org.mozartspaces.core.config.Configuration.DEFAULTSCHEME_DEFAULT;
import static org.mozartspaces.core.config.Configuration.EMBEDDED_SPACE_DEFAULT;
import static org.mozartspaces.core.config.Configuration.SERIALIZER_DEFAULT;
import static org.mozartspaces.core.config.Configuration.XP_THREAD_NUMBER_DEFAULT;
import static org.mozartspaces.core.config.EntryCopierConfiguration.COPY_CONTEXT;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import net.jcip.annotations.ThreadSafe;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.HierarchicalConfiguration;
import org.apache.commons.configuration.XMLConfiguration;
import org.mozartspaces.core.MzsCoreRuntimeException;
import org.mozartspaces.util.LoggerFactory;
import org.slf4j.Logger;

/**
 * Utility class with methods to load the configuration from an XML file with Apache Commons Configuration.
 *
 * @author Tobias Doenz
 * @author Florian Lukschander
 */
@ThreadSafe
public final class CommonsXmlConfiguration {

    private static final Logger log = LoggerFactory.get();

    // constants to find configuration file
    private static final String PROPERTYKEY = "mozartspaces.configurationFile";
    private static final String FILENAME = "mozartspaces.xml";

    // key names
    private static final String EMBEDDED_SPACE_KEY = "embeddedSpace";

    private static final String XP_KEY = "coreProcessor";
    private static final String XP_THREADS_KEY = "threads";

    private static final String SERIALIZERS_KEY = "serializers";
    private static final String SERIALIZER_KEY = "serializer";
    // remoting
    private static final String REMOTING_KEY = "remoting";
    private static final String DEFAULTSCHEME_KEY = "defaultScheme";
    private static final String BINDHOST_KEY = "bindHost";

    private static final String TRANSPORTS_KEY = "transports";
    // tcpsocket
    private static final String TCPSOCKET_KEY = "tcpsocket";
    private static final String TCPSOCKET_THREADS_KEY = "threads";
    private static final String TCPSOCKET_RECEIVERPORT_KEY = "receiverPort";
    private static final String TCPSOCKET_SERIALIZER_KEY = "serializer";
    private static final String TCPSOCKET_SCHEME_KEY = "[@scheme]";
    // REST
    private static final String REST_KEY = "rest";
    private static final String REST_PORT = "port";
    private static final String REST_SCHEME = "[@scheme]";
    private static final String REST_SENDER = "sender";

    private static final String SPACEURI_KEY = "spaceURI";
    // private static final String SPACEURI_DEFAULT = "xvsm://localhost:9876";
    private static final String SPACEURI_DEFAULT =
            "${remoting.defaultScheme}://localhost:${remoting.transports.tcpsocket.receiverPort}";

    private static final String ENTRYCOPIER_KEY = "entryCopier";
    private static final String ENTRYCOPIER_COPYCONTEXT_KEY = "[@copyContext]";
    private static final String ENTRYCOPIER_SERIALIZING_KEY = "serializing";
    private static final String ENTRYCOPIER_CLONING_KEY = "cloning";

    private static final String COORDINATORS_KEY = "coordinators";
    private static final String COORDINATOR_KEY = "coordinator";
    private static final String COORDINATOR_API_CLASS_KEY = "apiClass";
    private static final String COORDINATOR_API_SELECTOR_CLASS_KEY = "apiSelectorClass";
    private static final String COORDINATOR_CAPI3_TRANSLATOR_CLASS_KEY = "capi3TranslatorClass";
    private static final String COORDINATOR_CAPI3_SELECTOR_TRANSLATOR_CLASS_KEY = "capi3SelectorTranslatorClass";

    private static final String CAPI3_ID_KEY = "capi3";

    private static final String PERSISTENCE_KEY = "persistence";
    private static final String PERSISTENCE_PROFILE_KEY = "profile";
    private static final String PERSISTENCE_SERIALIZER_KEY = "serializer";
    private static final String PERSISTENCE_SERIALIZER_CACHE_SIZE_KEY = "serializerCacheSize";
    private static final String PERSISTENCE_PROPERTIES_KEY = "properties";
    private static final String PERSISTENCE_PROPERTY_KEY = "property";
    private static final String PERSISTENCE_PROPERTY_KEY_KEY = "[@key]";

    private static final String SECURITY_KEY = "security";
    private static final String SECURITY_AUTHENTICATION_KEY = "authentication";
    private static final String SECURITY_AUTHENTICATION_ENABLED_KEY = "[@enabled]";
    private static final String SECURITY_AUTHORIZATION_KEY = "authorization";
    private static final String SECURITY_AUTHORIZATION_ENABLED_KEY = "[@enabled]";
    private static final String SECURITY_AUTHORIZATION_AUTHORIZE_REQUESTS_KEY = "authorizeRequests";

    /**
     * Loads the configuration from the default location or the location specified by the system property
     * "mozartspaces.configurationFile".
     *
     * @return the loaded configuration
     */
    public static Configuration load() {
        URL location = getConfigurationLocation();
        return loadFrom(location);
    }

    /**
     * Loads the configuration from the default location or the location specified by the system property
     * "mozartspaces.configurationFile" and sets the port of the first TCP Socket Receiver.
     *
     * @param firstTcpSocketReceiverPort
     *            the port where the first TCP Socket Receiver is listening for new connections
     * @return the loaded configuration
     */
    public static Configuration load(final int firstTcpSocketReceiverPort) {
        log.info("Loading MozartSpaces core configuration");
        URL location = getConfigurationLocation();
        XMLConfiguration config = loadConfigurationFromFile(location);
        log.info("Setting receiver port of first TCP socket transport to {}", firstTcpSocketReceiverPort);
        String key = REMOTING_KEY + "." + TRANSPORTS_KEY + "." + TCPSOCKET_KEY + "(0)." + TCPSOCKET_RECEIVERPORT_KEY;
        config.setProperty(key, firstTcpSocketReceiverPort);
        return parse(config);
    }

    /**
     * Loads the configuration from the specified location.
     *
     * @param location
     *            the location of the configuration
     * @return the loaded configuration
     */
    public static Configuration loadFrom(final URL location) {
        log.info("Loading MozartSpaces core configuration");
        XMLConfiguration config = loadConfigurationFromFile(location);
        return parse(config);
    }

    private static Configuration parse(final XMLConfiguration xmlConfig) {
        Configuration config = new Configuration();

        boolean embeddedSpace = xmlConfig.getBoolean(EMBEDDED_SPACE_KEY, EMBEDDED_SPACE_DEFAULT);
        log.debug("Embedded space: " + embeddedSpace);
        config.setEmbeddedSpace(embeddedSpace);

        int xpThreadNumber = xmlConfig.getInt(XP_KEY + "." + XP_THREADS_KEY, XP_THREAD_NUMBER_DEFAULT);
        log.debug("Number of XP threads: {}", xpThreadNumber);
        config.setXpThreadNumber(xpThreadNumber);

        // Serializers
        @SuppressWarnings("unchecked")
        List<HierarchicalConfiguration> serializersConfig = xmlConfig.configurationsAt(SERIALIZERS_KEY + "."
                + SERIALIZER_KEY);
        log.debug("Loading {} serializer configurations", serializersConfig.size());
        if (serializersConfig.size() > 0) {
            List<String> serializerIds = new ArrayList<String>();
            for (HierarchicalConfiguration serializerConfig : serializersConfig) {
                String serializerId = serializerConfig.getString("", SERIALIZER_DEFAULT);
                log.debug("Serializer ID: {}", serializerId);
                serializerIds.add(serializerId);
            }
            config.setSerializerIds(serializerIds);
        }

        // Remoting
        String defaultScheme = xmlConfig.getString(REMOTING_KEY + "." + DEFAULTSCHEME_KEY, DEFAULTSCHEME_DEFAULT);
        log.debug("Default scheme: {}", defaultScheme);
        config.setDefaultScheme(defaultScheme);
        String bindHost = xmlConfig.getString(REMOTING_KEY + "." + BINDHOST_KEY, BINDHOST_DEFAULT);
        log.debug("Bind host: {}", bindHost);
        config.setBindHost(bindHost);

        // Transports
        Map<String, TransportConfiguration> transportConfigurations = new HashMap<String, TransportConfiguration>();
        String absoluteTransportsKey = REMOTING_KEY + "." + TRANSPORTS_KEY;
        @SuppressWarnings("unchecked")
        List<HierarchicalConfiguration> transportsConfigs = xmlConfig.configurationsAt(absoluteTransportsKey);
        HierarchicalConfiguration transportsConfig = null;
        if (transportsConfigs.size() > 0) {
            // read only first element
            transportsConfig = transportsConfigs.get(0);
        } else {
            transportsConfig = new XMLConfiguration();
            transportsConfig.addProperty(TCPSOCKET_KEY, new Object()); // dummy entry
            log.debug("Using default TCP socket configuration");
        }

        // TCP socket transports
        @SuppressWarnings("unchecked")
        List<HierarchicalConfiguration> tcpSocketConfigs = transportsConfig.configurationsAt(TCPSOCKET_KEY);
        log.debug("Loading {} TCP socket configuration(s)", tcpSocketConfigs.size());
        for (int i = 0; i < tcpSocketConfigs.size(); i++) {
            HierarchicalConfiguration tcpSocketConfig = tcpSocketConfigs.get(i);
            TcpSocketConfiguration transportConfig = new TcpSocketConfiguration();

            int threadNumber = tcpSocketConfig.getInt(TCPSOCKET_THREADS_KEY,
                    TcpSocketConfiguration.THREAD_NUMBER_DEFAULT);
            log.debug("Number of threads: {}", threadNumber);
            transportConfig.setThreadNumber(threadNumber);

            int receiverPort = tcpSocketConfig.getInt(TCPSOCKET_RECEIVERPORT_KEY,
                    TcpSocketConfiguration.RECEIVER_PORT_DEFAULT);
            log.debug("Receiver port: {}", receiverPort);
            transportConfig.setReceiverPort(receiverPort);

            String serializerId = tcpSocketConfig.getString(TCPSOCKET_SERIALIZER_KEY, config.getSerializerIds()
                    .iterator().next());
            log.debug("Serializer: {}", serializerId);
            transportConfig.setSerializerId(serializerId);

            String scheme = tcpSocketConfig.getString(TCPSOCKET_SCHEME_KEY, DEFAULTSCHEME_DEFAULT);
            log.debug("Sender scheme: {}", scheme);
            transportConfigurations.put(scheme, transportConfig);
        }

        // REST transports
        @SuppressWarnings("unchecked")
        List<HierarchicalConfiguration> restConfigs = transportsConfig.configurationsAt(REST_KEY);
        log.debug("Loading {} REST configuration(s)", restConfigs.size());
        for (HierarchicalConfiguration conf : restConfigs) {
            RestConfiguration restConfig = new RestConfiguration();
            Integer restPort = conf.getInteger(REST_PORT, null);
            if (restPort != null) {
                restConfig.setPort(restPort);
            }
            String scheme = conf.getString(REST_SCHEME);
            if (scheme != null) {
                restConfig.setScheme(scheme);
            }
            String restSender = conf.getString(REST_SENDER);
            if (restSender != null) {
                restConfig.setSender(restSender);
            }
            log.debug("REST scheme: {}, port: {}, sender: {}",
                    new Object[] {restConfig.getScheme(), restConfig.getPort(), restConfig.getSender() });
            transportConfigurations.put(restConfig.getScheme(), restConfig);
        }

        config.setTransportConfigurations(transportConfigurations);

        // Space URI
        // ensure that variables can be resolved when the default configuration is used
        String key = REMOTING_KEY + "." + DEFAULTSCHEME_KEY;
        if (xmlConfig.getString(key) == null) {
            xmlConfig.setProperty(key, DEFAULTSCHEME_DEFAULT);
        }
        key = REMOTING_KEY + "." + TRANSPORTS_KEY + "." + TCPSOCKET_KEY + "(0)." + TCPSOCKET_RECEIVERPORT_KEY;
        if (xmlConfig.getProperty(key) == null) {
            xmlConfig.setProperty(key, TcpSocketConfiguration.RECEIVER_PORT_DEFAULT);
        }
        // actually read it
        String thisSpace = xmlConfig.getString(SPACEURI_KEY, SPACEURI_DEFAULT);
        log.debug("Space URI: {}", thisSpace);
        URI spaceUri = null;
        try {
            spaceUri = new URI(thisSpace);
        } catch (URISyntaxException ex) {
            throw new RuntimeException("Invalid space URI", ex);
        }
        config.setSpaceUri(spaceUri);

        // Entry copier
        @SuppressWarnings("unchecked")
        List<HierarchicalConfiguration> entryCopierConfigs = xmlConfig.configurationsAt(ENTRYCOPIER_KEY);
        if (entryCopierConfigs.size() > 0) {
            HierarchicalConfiguration entryCopierConfig = entryCopierConfigs.get(0);
            boolean copyContext = entryCopierConfig.getBoolean(ENTRYCOPIER_COPYCONTEXT_KEY, COPY_CONTEXT);
            if (entryCopierConfig.getString(ENTRYCOPIER_CLONING_KEY) != null) {
                EntryCopierConfiguration copierConfig = new EntryCopierConfiguration();
                String name = ENTRYCOPIER_CLONING_KEY;
                log.debug("Entry copier: {}", name);
                copierConfig.setName(name);
                log.debug("Copy request context: {}", copyContext);
                copierConfig.setCopyContext(copyContext);
                config.setEntryCopierConfiguration(copierConfig);
            } else if (entryCopierConfig.getString(ENTRYCOPIER_SERIALIZING_KEY) != null) {
                SerializingEntryCopierConfiguration copierConfig = new SerializingEntryCopierConfiguration();
                String name = ENTRYCOPIER_SERIALIZING_KEY;
                log.debug("Entry copier: {}", name);
                copierConfig.setName(name);
                log.debug("Copy request context: {}", copyContext);
                copierConfig.setCopyContext(copyContext);
                String serializerId = entryCopierConfig.getString(ENTRYCOPIER_SERIALIZING_KEY);
                log.debug("Serializer: {}", serializerId);
                copierConfig.setSerializerId(serializerId);
                config.setEntryCopierConfiguration(copierConfig);
            }
        }

        // Coordinator definitions
        @SuppressWarnings("unchecked")
        List<HierarchicalConfiguration> coordinatorConfigs = xmlConfig.configurationsAt(COORDINATORS_KEY + "."
                + COORDINATOR_KEY);
        log.debug("Loading {} coordinator configuration(s)", coordinatorConfigs.size());
        List<CoordinatorConfiguration> coordinatorConfigurations = new ArrayList<CoordinatorConfiguration>();
        for (HierarchicalConfiguration coordinatorConfig : coordinatorConfigs) {
            String apiClassName = coordinatorConfig.getString(COORDINATOR_API_CLASS_KEY);
            String apiSelectorClassName = coordinatorConfig.getString(COORDINATOR_API_SELECTOR_CLASS_KEY);
            String capi3TranslatorClassName = coordinatorConfig.getString(COORDINATOR_CAPI3_TRANSLATOR_CLASS_KEY);
            String capi3SelectorTranslatorClassName = coordinatorConfig
                    .getString(COORDINATOR_CAPI3_SELECTOR_TRANSLATOR_CLASS_KEY);
            CoordinatorConfiguration coordinatorConfiguration = new CoordinatorConfiguration(apiClassName,
                    apiSelectorClassName, capi3TranslatorClassName, capi3SelectorTranslatorClassName);
            coordinatorConfigurations.add(coordinatorConfiguration);
        }
        config.setCoordinatorConfigurations(coordinatorConfigurations);

        // CAPI-3
        String capi3Id = xmlConfig.getString(CAPI3_ID_KEY, CAPI3_ID_DEFAULT);
        log.debug("CAPI-3 ID: {}", capi3Id);
        config.setCapi3Id(capi3Id);

        // Persistence
        PersistenceConfiguration persistenceConfig = new PersistenceConfiguration();
        String persistenceProfile = xmlConfig.getString(PERSISTENCE_KEY + "." + PERSISTENCE_PROFILE_KEY,
                PersistenceConfiguration.PERSISTENCE_PROFILE_DEFAULT);
        log.debug("Loading persistence configuration for profile {}", persistenceProfile);
        persistenceConfig.setPersistenceProfile(persistenceProfile);
        String persistenceSerializer = xmlConfig.getString(PERSISTENCE_KEY + "." + PERSISTENCE_SERIALIZER_KEY,
                PersistenceConfiguration.PERSISTENCE_SERIALIZER_DEFAULT);
        persistenceConfig.setPersistenceSerializer(persistenceSerializer);
        int persistenceSerializerCacheSize = xmlConfig.getInt(
                PERSISTENCE_KEY + "." + PERSISTENCE_SERIALIZER_CACHE_SIZE_KEY,
                PersistenceConfiguration.PERSISTENCE_SERIALIZER_CACHE_SIZE_DEFAULT);
        persistenceConfig.setPersistenceSerializerCacheSize(persistenceSerializerCacheSize);
        @SuppressWarnings("unchecked")
        List<HierarchicalConfiguration> persistencePropertiesConfig = xmlConfig
                .configurationsAt(PERSISTENCE_KEY + "." + PERSISTENCE_PROPERTIES_KEY + "." + PERSISTENCE_PROPERTY_KEY);
        Properties persistenceProperties = PersistenceConfiguration.PERSISTENCE_PROPERTIES_DEFAULT;
        for (HierarchicalConfiguration persistencePropertyConfig : persistencePropertiesConfig) {
            String persistencePropertyKey = persistencePropertyConfig.getString(PERSISTENCE_PROPERTY_KEY_KEY, "");
            String persistencePropertyValue = persistencePropertyConfig.getString("", "");
            if (!persistencePropertyKey.equals("")) {
                persistenceProperties.setProperty(persistencePropertyKey, persistencePropertyValue);
            }
        }
        persistenceConfig.setPersistenceProperties(persistenceProperties);
        config.setPersistenceConfiguration(persistenceConfig);

        // Security
        String authenticationKey = SECURITY_KEY + "." + SECURITY_AUTHENTICATION_KEY;
        boolean authenticationEnabled = xmlConfig.getBoolean(authenticationKey + "."
                + SECURITY_AUTHENTICATION_ENABLED_KEY, SecurityConfiguration.SECURITY_AUTHENTICATION_ENABLED_DEFAULT);
        String authorizationKey = SECURITY_KEY + "." + SECURITY_AUTHORIZATION_KEY;
        boolean authorizationEnabled = xmlConfig.getBoolean(authorizationKey + "." + SECURITY_AUTHORIZATION_ENABLED_KEY,
                SecurityConfiguration.SECURITY_AUTHORIZATION_ENABLED_DEFAULT);
        boolean authorizeRequests = xmlConfig.getBoolean(authorizationKey + "."
                + SECURITY_AUTHORIZATION_AUTHORIZE_REQUESTS_KEY,
                SecurityConfiguration.SECURITY_AUTHORIZE_REQUESTS_DEFAULT);
        SecurityConfiguration securityConfig = new SecurityConfiguration(authenticationEnabled, authorizationEnabled,
                authorizeRequests);
        config.setSecurityConfiguration(securityConfig);

        return config;
    }

    private static XMLConfiguration loadConfigurationFromFile(final URL configLocation) {
        XMLConfiguration config = null;
        if (configLocation != null) {
            log.info("Loading configuration from {}", configLocation);
            try {
                config = new XMLConfiguration(configLocation);
            } catch (ConfigurationException ex) {
                throw new MzsCoreRuntimeException("Loading configuration from " + configLocation + " failed", ex);
            }
        } else {
            log.info("No external configuration found, will use default configuration");
            config = new XMLConfiguration();
        }
        return config;
    }

    private static URL getConfigurationLocation() {
        String filename = System.getProperty(PROPERTYKEY);
        if (filename == null) {
            filename = FILENAME;
            log.debug("Using default configuration file name {}", FILENAME);
        } else {
            log.debug("Property with configuration location provided: {}", filename);
        }
        try {
            // current directory
            File configFile = new File(filename);
            if (configFile.isFile() && configFile.canRead()) {
                return configFile.toURI().toURL();
            }
            // user's home directory
            String parentPathname = System.getProperty("user.home");
            configFile = new File(parentPathname, filename);
            if (configFile.isFile() && configFile.canRead()) {
                log.debug("Loading configuration from user.home");
                return configFile.toURI().toURL();
            }
            // classpath
            URL resource = CommonsXmlConfiguration.class.getResource("/" + filename);
            if (resource != null) {
                log.debug("Loading configuration from classpath");
                return resource;
            }
            // for android
            resource = CommonsXmlConfiguration.class.getResource("/assets/" + filename);
            if (resource != null) {
                log.debug("Loading configuration from assets directory");
                return resource;
            }
        } catch (Exception ex) {
            // should not happen!
            throw new MzsCoreRuntimeException(ex);
        }
        return null;
    }

    private CommonsXmlConfiguration() {
    }

}
