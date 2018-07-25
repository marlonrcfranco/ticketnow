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

import java.io.Serializable;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.jcip.annotations.ThreadSafe;

/**
 * Configuration of a MozartSpaces core.
 *
 * @author Tobias Doenz
 */
// TODO make the configuration API more convenient and uniform (with builder objects etc.)
@ThreadSafe
public final class Configuration implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * Default value of the embedded space flag.
     */
    public static final boolean EMBEDDED_SPACE_DEFAULT = true;
    private volatile boolean embeddedSpace = EMBEDDED_SPACE_DEFAULT;

    /**
     * Default value of the default scheme.
     */
    public static final String DEFAULTSCHEME_DEFAULT = "xvsm";
    private volatile String defaultScheme = DEFAULTSCHEME_DEFAULT;

    /**
     * Default value of the bind host.
     */
    public static final String BINDHOST_DEFAULT = "0.0.0.0";
    private volatile String bindHost = BINDHOST_DEFAULT;

    /**
     * Default value of the space URI.
     */
    public static final URI SPACEURI_DEFAULT = URI.create("xvsm://localhost:9876");
    private volatile URI spaceUri = SPACEURI_DEFAULT;

    /**
     * Default number of Core Processor threads.
     */
    public static final int XP_THREAD_NUMBER_DEFAULT = 10;
    private volatile int xpThreadNumber = XP_THREAD_NUMBER_DEFAULT;

    /**
     * Default serializer (ID).
     */
    public static final String SERIALIZER_DEFAULT = "javabuiltin";
    // TODO add sub-config for serializers?
    private volatile List<String> serializerIds = Collections.singletonList(SERIALIZER_DEFAULT);

    /**
     * Default transport configuration.
     */
    public static final TransportConfiguration TRANSPORT_CONFIGURATION_DEFAULT = new TcpSocketConfiguration();
    private volatile Map<String, TransportConfiguration> transportConfigurations =
            new HashMap<String, TransportConfiguration>(
            Collections.singletonMap(DEFAULTSCHEME_DEFAULT, TRANSPORT_CONFIGURATION_DEFAULT));

    /**
     * Default entry copier configuration.
     */
    public static final EntryCopierConfiguration ENTRY_COPIER_CONFIGURATION_DEFAULT = new EntryCopierConfiguration();
    private volatile EntryCopierConfiguration entryCopierConfiguration = ENTRY_COPIER_CONFIGURATION_DEFAULT;

    private volatile List<CoordinatorConfiguration> coordinatorConfigurations = Collections.emptyList();

    /**
     * Default CAPI-3 implementation (ID).
     */
    public static final String CAPI3_ID_DEFAULT = "javanative";
    private volatile String capi3Id = CAPI3_ID_DEFAULT;

    /**
     * Default persistence configuration.
     */
    public static final PersistenceConfiguration PERSISTENCE_CONFIGURATION_DEFAULT = new PersistenceConfiguration();
    private volatile PersistenceConfiguration persistenceConfiguration = PERSISTENCE_CONFIGURATION_DEFAULT;

    /**
     * Default security configuration.
     */
    public static final SecurityConfiguration SECURITY_CONFIGURATION_DEFAULT = new SecurityConfiguration();
    private volatile SecurityConfiguration securityConfiguration = SECURITY_CONFIGURATION_DEFAULT;

    /**
     * Default constructor.
     */
    public Configuration() {
    }

    /**
     * Copy constructor.
     *
     * @param config
     *            the configuration to copy
     */
    public Configuration(final Configuration config) {
        this.capi3Id = config.capi3Id;
        this.defaultScheme = config.defaultScheme;
        this.embeddedSpace = config.embeddedSpace;
        this.entryCopierConfiguration = new EntryCopierConfiguration(config.entryCopierConfiguration);
        this.serializerIds = config.getSerializerIds();
        this.spaceUri = config.spaceUri;
        this.transportConfigurations = new HashMap<String, TransportConfiguration>();
        for (Map.Entry<String, TransportConfiguration> transport : config.transportConfigurations.entrySet()) {
            this.transportConfigurations.put(transport.getKey(), transport.getValue().clone());
        }
        this.xpThreadNumber = config.xpThreadNumber;
        this.persistenceConfiguration = new PersistenceConfiguration(config.persistenceConfiguration);
    }

    /**
     * @param embeddedSpace
     *            the flag to determine whether the core should have an embedded space
     */
    public void setEmbeddedSpace(final boolean embeddedSpace) {
        this.embeddedSpace = embeddedSpace;
    }

    /**
     * @return {@code true} if the core should have an embedded space
     */
    public boolean isEmbeddedSpace() {
        return embeddedSpace;
    }

    /**
     * @param defaultScheme
     *            the default scheme
     */
    public void setDefaultScheme(final String defaultScheme) {
        this.defaultScheme = defaultScheme;
    }

    /**
     * @return the default scheme
     */
    public String getDefaultScheme() {
        return defaultScheme;
    }

    /**
     * @param bindHost
     *            the bind host
     */
    public void setBindHost(final String bindHost) {
        this.bindHost = bindHost;
    }

    /**
     * @return the bind host
     */
    public String getBindHost() {
        return bindHost;
    }

    /**
     * @param spaceUri
     *            the space URI
     */
    public void setSpaceUri(final URI spaceUri) {
        this.spaceUri = spaceUri;
    }

    /**
     * @return the space URI
     */
    public URI getSpaceUri() {
        return spaceUri;
    }

    /**
     * @param xpThreadNumber
     *            the number of threads of the Core Processor
     */
    public void setXpThreadNumber(final int xpThreadNumber) {
        this.xpThreadNumber = xpThreadNumber;
    }

    /**
     * @return the the number of threads of the Core Processor
     */
    public int getXpThreadNumber() {
        return xpThreadNumber;
    }

    /**
     * @param serializerIds
     *            the serializer ID list
     */
    public void setSerializerIds(final List<String> serializerIds) {
        if (serializerIds == null) {
            this.serializerIds = new ArrayList<String>();
        } else {
            this.serializerIds = new ArrayList<String>(serializerIds);
        }
    }

    /**
     * @return the serializer ID list
     */
    public List<String> getSerializerIds() {
        return new ArrayList<String>(serializerIds);
    }

    /**
     * @param transportConfigurations
     *            the transport configurations map, with the scheme as key
     */
    public void setTransportConfigurations(final Map<String, TransportConfiguration> transportConfigurations) {
        if (transportConfigurations == null) {
            this.transportConfigurations = new HashMap<String, TransportConfiguration>();
        } else {
            this.transportConfigurations = new HashMap<String, TransportConfiguration>(transportConfigurations);
        }
    }

    /**
     * @return the transport configurations map, with the scheme as key
     */
    public Map<String, TransportConfiguration> getTransportConfigurations() {
        // TODO copy map?
        return transportConfigurations;
    }

    /**
     * @return the security configuration
     */
    public SecurityConfiguration getSecurityConfiguration() {
        return securityConfiguration;
    }

    /**
     * @param securityConfiguration
     *            the security configuration
     */
    public void setSecurityConfiguration(final SecurityConfiguration securityConfiguration) {
        this.securityConfiguration = securityConfiguration;
    }

    /**
     * @return the persistence configuration
     */
    public PersistenceConfiguration getPersistenceConfiguration() {
        return persistenceConfiguration;
    }

    /**
     * @param persistenceConfiguration
     *            the persistence configuration
     */
    public void setPersistenceConfiguration(final PersistenceConfiguration persistenceConfiguration) {
        this.persistenceConfiguration = persistenceConfiguration;
    }

    /**
     * @param entryCopierConfiguration
     *            the configuration of the entry copier
     */
    public void setEntryCopierConfiguration(final EntryCopierConfiguration entryCopierConfiguration) {
        this.entryCopierConfiguration = entryCopierConfiguration;
    }

    /**
     * @return the configuration of the entry copier
     */
    public EntryCopierConfiguration getEntryCopierConfiguration() {
        return entryCopierConfiguration;
    }

    /**
     * @return the configurations of the coordinators
     */
    public List<CoordinatorConfiguration> getCoordinatorConfigurations() {
        return coordinatorConfigurations;
    }

    /**
     * @param coordinatorConfigurations
     *            the configurations of the coordinators
     */
    public void setCoordinatorConfigurations(final List<CoordinatorConfiguration> coordinatorConfigurations) {
        if (coordinatorConfigurations == null) {
            this.coordinatorConfigurations = Collections.emptyList();
        } else {
            this.coordinatorConfigurations = new ArrayList<CoordinatorConfiguration>(coordinatorConfigurations);
        }
    }

    /**
     * @param capi3Id
     *            the CAPI-3 ID
     */
    public void setCapi3Id(final String capi3Id) {
        this.capi3Id = capi3Id;
    }

    /**
     * @return the CAPI-3 ID
     */
    public String getCapi3Id() {
        return capi3Id;
    }

    @Override
    public String toString() {
        return "Configuration [embeddedSpace=" + embeddedSpace + ", defaultScheme=" + defaultScheme + ", bindHost="
                + bindHost + ", spaceUri=" + spaceUri + ", xpThreadNumber=" + xpThreadNumber + ", serializerIds="
                + serializerIds + ", transportConfigurations=" + transportConfigurations
                + ", entryCopierConfiguration=" + entryCopierConfiguration + ", coordinatorConfigurations="
                + coordinatorConfigurations + ", capi3Id=" + capi3Id + ", persistenceConfiguration="
                + persistenceConfiguration + ", securityConfiguration=" + securityConfiguration + "]";
    }

}
