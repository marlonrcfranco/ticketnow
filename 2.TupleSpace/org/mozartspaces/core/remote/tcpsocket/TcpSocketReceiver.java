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
package org.mozartspaces.core.remote.tcpsocket;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import net.jcip.annotations.Immutable;
import net.jcip.annotations.ThreadSafe;

import org.mozartspaces.core.Message;
import org.mozartspaces.core.remote.Receiver;
import org.mozartspaces.core.remote.RemoteMessageDistributor;
import org.mozartspaces.core.util.SerializationException;
import org.mozartspaces.core.util.Serializer;
import org.mozartspaces.util.LoggerFactory;
import org.slf4j.Logger;

/**
 * A receiver implementation with TCP communication over <code>java.io</code>
 * sockets. In a thread a server socket listens for incoming connections. For
 * each connection a task to receive messages is created and executed on the
 * thread pool. In this task, messages are received with a timeout.
 *
 * @author Tobias Doenz
 *
 * @see TcpSocketSender
 */
@ThreadSafe
public final class TcpSocketReceiver implements Receiver {

    private static final Logger log = LoggerFactory.get();

    /**
     * The timeout for receiving the first message (in milliseconds).
     */
    private static final int RECEIVE_TIMEOUT = 1000;

    /**
     * The timeout for receiving (subsequent) messages.
     */
    private static final int WAIT_TIMEOUT = 2000;

    /**
     * The timeout for awaiting running tasks at shutdown (in seconds).
     */
    private static final int SHUTDOWN_TIMEOUT = 60;

    private final Serializer serializer;
    private final ExecutorService connectionThreadPool;
    private final RemoteMessageDistributor messageDistributor;

    /**
     * Counter for the number of incoming connections. Note that a connection is
     * closed after some time, if no message is received.
     */
    private final AtomicLong connectionCounter;

    /**
     * Counter for the number of successfully received and deserialized
     * messages.
     */
    private final AtomicLong receivedMessagesCounter;

    /**
     * The server socket to listen for new connections.
     */
    private final ServerSocket serverSocket;

    /**
     * The port where the server socket is listening.
     */
    private final int port;

    /**
     * The thread for listening on the server socket.
     */
    private final Thread thread;

    /**
     * Constructs a <code>TcpSocketReceiver</code> and starts the internal
     * thread to listen for new connections.
     *
     * @param port
     *            the port for the server socket, to listen for connections
     * @param bindAddress
     *            the local address the server socket will be bound to
     * @param serializer
     *            the serializer to deserialize received messages
     * @param connectionThreadPool
     *            the thread pool for the connection tasks that receive messages
     * @param messageDistributor
     *            the message distributor to route received messages to the
     *            corresponding components
     * @throws IOException
     *             if creating the {@link ServerSocket} to listen for incoming
     *             connections fails
     */
    public TcpSocketReceiver(final int port, final InetAddress bindAddress, final Serializer serializer,
            final ExecutorService connectionThreadPool, final RemoteMessageDistributor messageDistributor)
            throws IOException {

        this.serializer = serializer;
        assert this.serializer != null;
        this.connectionThreadPool = connectionThreadPool;
        assert this.connectionThreadPool != null;
        this.messageDistributor = messageDistributor;
        assert this.messageDistributor != null;

        serverSocket = new ServerSocket(port, 0, bindAddress);
        this.port = serverSocket.getLocalPort();
        if (port == 0) {
            log.info("Bound server socket to free port {}", this.port);
        }
        thread = new ListenerThread();
        thread.setName("TcpSocketReceiver@port" + this.port);

        connectionCounter = new AtomicLong();
        receivedMessagesCounter = new AtomicLong();
    }

    /**
     * @return the port where the server socket is listening
     */
    public int getPort() {
        return port;
    }

    @Override
    public long getNumberOfReceivedMessages() {
        return receivedMessagesCounter.get();
    }

    @Override
    public void start() {
        thread.start();
    }

    @Override
    public void shutdown(final boolean wait) {
        log.debug("Shutting down the {} listener thread", thread.getName());
        try {
            serverSocket.close();
        } catch (IOException ex) {
            log.info("Could not close server socket: {}", ex.toString());
        }

        log.debug("Shutting down the {} connection thread pool", thread.getName());
        // disable new tasks from being submitted
        connectionThreadPool.shutdown();
        if (wait) {
            awaitPoolTermination();
        } else {
            Thread terminationThread = new Thread() {
                @Override
                public void run() {
                    awaitPoolTermination();
                }
            };
            terminationThread.start();
        }
    }

    private void awaitPoolTermination() {
        try {
            // wait for existing tasks to terminate
            if (!connectionThreadPool.awaitTermination(SHUTDOWN_TIMEOUT, TimeUnit.SECONDS)) {
                // cancel currently executing tasks
                List<Runnable> queuedTasks = connectionThreadPool.shutdownNow();
                if (!queuedTasks.isEmpty()) {
                    log.error("Stopped the {} connection thread pool, {} tasks were still waiting", thread.getName(),
                            queuedTasks.size());
                }
                // wait for tasks to respond to being canceled
                if (!connectionThreadPool.awaitTermination(5, TimeUnit.SECONDS)) {
                    log.error("The {} connection thread pool did not terminate", thread.getName());
                    return;
                }
            }
            log.debug("The {} connection thread pool terminated", thread.getName());
        } catch (InterruptedException ex) {
            // (re-)cancel if current thread also interrupted
            connectionThreadPool.shutdownNow();
            // preserve interrupt status
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Thread that listens on the server socket for new incoming connections.
     * For each connection a ConnectionHandler task is created and submitted to
     * the connection thread pool.
     *
     * @author Tobias Doenz
     */
    private class ListenerThread extends Thread {

        @Override
        public void run() {
            try {
                log.debug("Accepting connections on {}", serverSocket.getLocalSocketAddress());
                while (!Thread.currentThread().isInterrupted()) {
                    Socket socket = serverSocket.accept();
                    socket.setTcpNoDelay(true);
                    connectionThreadPool.execute(new ConnectionHandler(socket));
                }
            } catch (IOException ex) {
                if (!serverSocket.isClosed()) {
                    log.error("Accepting connections failed: {}", ex.toString());
                } else {
                    log.debug("Exiting listener thread");
                }
            }
        }

    }

    /**
     * Task to receive, deserialize and distribute messages from a socket's
     * input stream.
     *
     * @author Tobias Doenz
     */
    @Immutable
    private class ConnectionHandler implements Runnable {

        private final Socket socket;

        public ConnectionHandler(final Socket socket) {
            this.socket = socket;
            long connectionCount = connectionCounter.incrementAndGet();
            log.debug("New connection from {} ({})", socket.getRemoteSocketAddress(), connectionCount);
        }

        @Override
        public void run() {
            DataInputStream in = null;
            try {
                // set timeout for receiving the first message
                socket.setSoTimeout(RECEIVE_TIMEOUT);
                in = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
                while (!Thread.currentThread().isInterrupted()) {
                    log.trace("Receiving serialized message");
                    int length = in.readInt();
                    log.trace("Message length: {} bytes", length);
                    byte[] bytes = new byte[length];
                    int readTotalNum = 0;
                    while (readTotalNum < length) {
                        int readBytesNum = in.read(bytes, readTotalNum, length - readTotalNum);
                        if (readBytesNum == -1) {
                            log.warn("Receiving message content failed (read {} bytes, but expected {} bytes",
                                    readTotalNum, length);
                            return;
                        }
                        readTotalNum += readBytesNum;
                    }
                    log.trace("Received {} bytes", bytes.length);
                    Message<?> message = null;
                    try {
                        message = serializer.deserialize(bytes);
                    } catch (SerializationException ex) {
                        log.error("Deserializing message failed", ex);
                        break;
                    }
                    receivedMessagesCounter.incrementAndGet();
                    log.debug("Received message for request {} ({} bytes)", message.getRequestReference(), length);
                    messageDistributor.distributeMessage(message);
                    // set timeout for receiving subsequent messages
                    socket.setSoTimeout(WAIT_TIMEOUT);
                }
            } catch (IOException ex) {
                log.debug("Receiving failed: {}", ex.toString());
            } finally {
                try {
                    socket.close();
                } catch (IOException ex) {
                    log.info("Could not close socket: {}", ex.toString());
                }
            }
        }

    }
}
