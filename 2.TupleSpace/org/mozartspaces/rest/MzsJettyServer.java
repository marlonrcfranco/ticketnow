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

import java.net.URL;
import java.util.EnumSet;
import java.util.Map;

import org.atmosphere.container.Jetty7CometSupport;
import org.atmosphere.cpr.AtmosphereServlet;
import org.eclipse.jetty.server.DispatcherType;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.mozartspaces.capi3.AnyCoordinator;
import org.mozartspaces.core.Capi;
import org.mozartspaces.core.ContainerReference;
import org.mozartspaces.core.DefaultMzsCore;
import org.mozartspaces.core.Entry;
import org.mozartspaces.core.MzsCore;
import org.mozartspaces.core.MzsCoreException;
import org.mozartspaces.core.config.CommonsXmlConfiguration;
import org.mozartspaces.core.config.Configuration;
import org.mozartspaces.core.config.RestConfiguration;
import org.mozartspaces.core.remote.MessageDistributor;
import org.mozartspaces.core.util.Serializer;
import org.mozartspaces.util.LoggerFactory;
import org.slf4j.Logger;

/**
 * This class wraps the setup of a jetty7 Server with the jersey- and amtosphere-framework configured.
 *
 * @author Christian Proinger
 */
public class MzsJettyServer extends MzsBaseWebServer {

    private static final Logger log = LoggerFactory.get();

    /**
     * Test method.
     *
     * @param args
     *            not used
     * @throws MzsCoreException
     *             should not be thrown
     */
    public static void main(final String[] args) throws MzsCoreException {
        URL resource = MzsJettyServer.class.getResource("/mozartspaces_rest.xml");
        Configuration conf = CommonsXmlConfiguration.loadFrom(resource);
        DefaultMzsCore core = DefaultMzsCore.newInstance(conf);
        Capi capi = new Capi(core);
        ContainerReference c1 = capi.createContainer("name1", null, 100, null, new AnyCoordinator());
        capi.write(c1, new Entry("asdf"));
        capi.createContainer("name2", null, 100, null, new AnyCoordinator());
    }

    /**
     * The jetty server.
     */
    private Server server;

    /**
     * @param restConfig
     *            the REST module configuration
     * @param core
     *            the MozartSpaces core
     * @param messageDistributor
     *            to distribute received message
     * @param serializers
     *            to serialize the messages
     */
    public MzsJettyServer(final RestConfiguration restConfig, final MzsCore core,
            final MessageDistributor messageDistributor, final Map<String, Serializer> serializers) {
        super(restConfig, core, messageDistributor, serializers);
    }

    /**
     * sets up the Jetty-Server, the atmosphere-framework, jersey framework, and this projects base-package as the one
     * jersey should look for resources.
     *
     * https://github.com/jfarcand/atmosphere/blob/master/modules/jersey/src/test/java/org/atmosphere/jersey/tests/
     * BaseTest.java
     * https://github.com/jfarcand/atmosphere/blob/master/modules/jersey/src/test/java/org/atmosphere/jersey
     * /tests/Jetty7JerseyTest.java
     */
    @Override
    protected final void setup() {

        int port = config.getPort();
        log.info("jetty-port: " + port);
        server = new Server(port);

        ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
        // ServletContextHandler context = new ServletContextHandler(ServletContextHandler.NO_SESSIONS);

        // add CORS headers to every response
        context.addFilter(CrossOriginResourceSharingFilter.class, "/*", EnumSet.of(DispatcherType.REQUEST));

        context.setAttribute(MZS_CORE, core);
        context.setAttribute(MZS_MESSAGE_DISTRIBUTOR, messageDistributor);
        context.setAttribute(SERIALIZERS, serializers);
        context.setContextPath("/");
        server.setHandler(context);

        AtmosphereServlet atmoServlet = createAtmosphereServlet();
        // atmoServlet.setCometSupport(new JettyCometSupport(atmoServlet.getAtmosphereConfig()));
        atmoServlet.setCometSupport(new Jetty7CometSupport(atmoServlet.getAtmosphereConfig()));
        // atmoServlet.setCometSupport(new JettyCometSupportWithWebSocket(atmoServlet.getAtmosphereConfig()));
        // atmoServlet.setCometSupport(new Jetty8WebSocketSupport(atmoServlet.getAtmosphereConfig()));

        ServletHolder sh = new ServletHolder(atmoServlet);

        // JSON with Jackson: would require default constructor for serialized classes
        // sh.setInitParameter(JSONConfiguration.FEATURE_POJO_MAPPING, "true");

        // declarative hyperlinking: ConcurrentModificationException
        // sh.setInitParameter("com.sun.jersey.spi.container.ContainerResponseFilters",
        // "com.sun.jersey.server.linking.LinkFilter");

        context.addServlet(sh, "/*");
    }

    @Override
    public final void start() throws Exception {
        server.start();
    }

    /**
     * Stops the server and optionally wait for it to stop.
     *
     * @param wait
     *            flag to indicate whether to wait for the server to stop
     * @throws Exception
     *             should not be thrown
     */
    @Override
    public final void stop(final boolean wait) throws Exception {
        server.stop();
        if (wait) {
            server.join();
        }
    }
}
