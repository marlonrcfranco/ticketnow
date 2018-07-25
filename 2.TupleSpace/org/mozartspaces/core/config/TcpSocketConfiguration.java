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
 * Configuration of a TCP Socket transport.
 *
 * @author Tobias Doenz
 */
public final class TcpSocketConfiguration extends TransportConfiguration {

    private static final long serialVersionUID = 1L;

    /**
     * Default number of threads for the receiver/sender thread pool.
     */
    public static final int THREAD_NUMBER_DEFAULT = 40;
    private volatile int threadNumber = THREAD_NUMBER_DEFAULT;

    /**
     * Default receiver port.
     */
    public static final int RECEIVER_PORT_DEFAULT = 9876;
    private volatile int receiverPort = RECEIVER_PORT_DEFAULT;

    /**
     * Default serializer (ID).
     */
    public static final String SERIALIZER_ID_DEFAULT = "javabuiltin";
    private volatile String serializerId = SERIALIZER_ID_DEFAULT;

    /**
     * @param threadNumber
     *            the number of threads for the receiver/sender threadpool.
     */
    public void setThreadNumber(final int threadNumber) {
        this.threadNumber = threadNumber;
    }

    /**
     * @return the threadNumber the number of threads for the receiver/sender
     *         threadpool.
     */
    public int getThreadNumber() {
        return threadNumber;
    }

    /**
     * @param receiverPort the receiver port
     */
    public void setReceiverPort(final int receiverPort) {
        this.receiverPort = receiverPort;
    }

    /**
     * @return the receiver port
     */
    public int getReceiverPort() {
        return receiverPort;
    }

    /**
     * @param serializerId the serializer ID
     */
    public void setSerializerId(final String serializerId) {
        this.serializerId = serializerId;
    }

    /**
     * @return the serializer ID
     */
    public String getSerializerId() {
        return serializerId;
    }

    @Override
    public TcpSocketConfiguration clone() {
        TcpSocketConfiguration newConfig = new TcpSocketConfiguration();
        newConfig.receiverPort = receiverPort;
        newConfig.serializerId = serializerId;
        newConfig.threadNumber = threadNumber;
        return newConfig;
    }

    @Override
    public String toString() {
        return "TcpSocketConfiguration [threadNumber=" + threadNumber + ", receiverPort=" + receiverPort
                + ", serializerId=" + serializerId + "]";
    }

}
