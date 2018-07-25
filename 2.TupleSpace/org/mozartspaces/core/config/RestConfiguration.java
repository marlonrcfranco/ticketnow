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

/**
 * Configuration for the REST-module. The receiver-port and the sender strategy are configured here as well as the
 * uri-scheme. The serializer to be used is also configured here, by now only the xstream-json serializer is supported
 * though.
 *
 * for the sender-strategy the following two can be used.
 * <ul>
 * <li>simple: only the message-resource is used, this sender behaves more like the TcpSocketSender</li>
 * <li>atmosphere: a sender which uses all the rest-resources (container, transaction, ...) instead of only the
 * message-resource. Asynchronous request are supported through an AsyncHttpClient</li>
 * </ul>
 *
 * the defaults for the configuration are:
 * <ul>
 * <li>serializer: "xvsm-json"</li>
 * <li>scheme: "http"</li>
 * <li>port: 9877</li>
 * <li>sender: "atmosphere"</li>
 * </ul>
 *
 * @author Christian Proinger
 *
 */
public final class RestConfiguration extends TransportConfiguration {

    private static final String DEFAULT_REST_SERIALIZER = "xstream-json";

    /**
     * THe default scheme for this transport.
     */
    public static final String DEFAULT_SCHEME = "http";

    /**
     * when complex rest-resources (/containers, /transactions, ...) should be used and reverse-ajax/ajax capabilities
     * are needed.
     */
    public static final String SENDER_ATMOSPHERE = "atmosphere";

    /**
     * for when only a simple rest-resource (/message) should be used.
     */
    public static final String SENDER_SIMPLE = "simple";

    private static final long serialVersionUID = -4675770367348467096L;

    private int port = 9877;

    private String scheme = DEFAULT_SCHEME;

    private String sender = SENDER_SIMPLE;

    private String serializerId = DEFAULT_REST_SERIALIZER;

    @Override
    public TransportConfiguration clone() {
        RestConfiguration nc = new RestConfiguration();
        nc.setPort(port);
        nc.setScheme(scheme);
        nc.setSender(sender);
        nc.setSerializerId(serializerId);
        return nc;
    }

    /**
     * @return the port
     */
    public int getPort() {
        return port;
    }

    /**
     * @return the scheme
     */
    public String getScheme() {
        return scheme;
    }

    /**
     * @return the sender ID
     */
    public String getSender() {
        return sender;
    }

    /**
     * @return the serializer ID
     */
    public String getSerializerId() {
        return serializerId;
    }

    public void setPort(final int port) {
        this.port = port;
    }

    public void setScheme(final String scheme) {
        this.scheme = scheme;
    }

    public void setSender(final String sender) {
        this.sender = sender;
    }

    public void setSerializerId(final String serializerId) {
        this.serializerId = serializerId;
    }

    @Override
    public String toString() {
        return "RestConfiguration [port=" + port + ", scheme=" + scheme + ", sender=" + sender + ", serializerId="
                + serializerId + "]";
    }
}
