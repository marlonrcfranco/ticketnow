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

import org.atmosphere.cpr.AtmosphereServlet;
import org.atmosphere.cpr.BroadcasterLifeCyclePolicy;
import org.mozartspaces.core.MzsCore;
import org.mozartspaces.core.config.RestConfiguration;
import org.mozartspaces.core.remote.MessageDistributor;
import org.mozartspaces.core.util.Serializer;

/**
 * An abstract class for wrapping embedded web-servers. Its responsible for instantiating and configuring the
 * AtmosphereServlet.
 *
 * @see org.mozartspaces.rest.MzsJettyServer
 *
 * @author Christian Proinger
 */
public abstract class MzsBaseWebServer {

    protected static final String SERIALIZERS = "serializers";
    protected static final String MZS_MESSAGE_DISTRIBUTOR = "mzsMessageDistributor";
    protected static final String MZS_CORE = "mzsCore";

    protected MzsCore core;
    protected MessageDistributor messageDistributor;
    protected Map<String, Serializer> serializers;
    /**
     * the rest-configuration of the MzsCore. initialized with the default configuration.
     */
    protected RestConfiguration config; // = new RestConfiguration();

    public MzsBaseWebServer(final RestConfiguration restConfig, final MzsCore core,
            final MessageDistributor messageDistributor, final Map<String, Serializer> serializers) {
        this.core = core;
        this.messageDistributor = messageDistributor;
        this.config = restConfig;
        this.serializers = serializers;
        setup();
    }

    protected abstract void setup();

    protected AtmosphereServlet createAtmosphereServlet() {
        AtmosphereServlet atmoServlet = new AtmosphereServlet();
        // atmoServlet.addInitParameter("org.atmosphere.cpr.broadcasterClass", RecyclableBroadcaster.class.getName());

        atmoServlet.addInitParameter("com.sun.jersey.config.property.packages", "org.mozartspaces.rest");
        atmoServlet.addInitParameter("org.atmosphere.cpr.broadcasterLifeCyclePolicy",
        // atmoServlet.addInitParameter(ApplicationConfig.BROADCASTER_LIFECYCLE_POLICY,
                BroadcasterLifeCyclePolicy.ATMOSPHERE_RESOURCE_POLICY.EMPTY_DESTROY.name());
        // BroadcasterLifeCyclePolicy.ATMOSPHERE_RESOURCE_POLICY.EMPTY.name());
        // BroadcasterLifeCyclePolicy.ATMOSPHERE_RESOURCE_POLICY.IDLE_DESTROY.name());
        // atmoServlet.addInitParameter("org.atmosphere.cpr.broadcasterClass", RecyclableBroadcaster.class.getName());
        // damit funktionierts nicht
        // atmoServlet.addInitParameter(JSONConfiguration.FEATURE_POJO_MAPPING, "true");
        atmoServlet.addInitParameter("com.sun.jersey.spi.container.ContainerResponseFilters",
                "com.sun.jersey.server.linking.LinkFilter");
        // atmoServlet.addInitParameter("org.atmosphere.cpr.recoverFromDestroyedBroadcaster", "true");
        //
        return atmoServlet;
    }

    public abstract void start() throws Exception;

    public abstract void stop(boolean wait) throws Exception;
}