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

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.net.Socket;
import java.net.URI;
import java.util.ArrayList;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import net.jcip.annotations.Immutable;
import net.jcip.annotations.ThreadSafe;

import org.mozartspaces.core.GenericResponse;
import org.mozartspaces.core.Message;
import org.mozartspaces.core.MzsCoreRuntimeException;
import org.mozartspaces.core.RequestMessage;
import org.mozartspaces.core.RequestReference;
import org.mozartspaces.core.Response;
import org.mozartspaces.core.ResponseHandler;
import org.mozartspaces.core.ResponseMessage;
import org.mozartspaces.core.remote.OutgoingMessage;
import org.mozartspaces.core.remote.Sender;
import org.mozartspaces.core.util.SerializationException;
import org.mozartspaces.core.util.Serializer;
import org.mozartspaces.util.LoggerFactory;
import org.slf4j.Logger;

/**
 * A sender implementation with TCP communication over <code>java.io</code>
 * sockets. It decouples the invocations of {@link #sendMessage(Message)} and
 * the actual blocking sending with queues to buffer the messages. So calling
 * the method to send a message is non-blocking, except when a message queue is
 * full. Furthermore, when this method returns, the Message has already been
 * serialized to a byte array. Hence it is not necessary to additionally create
 * defensive copies of messages, e.g., request messages with write requests that
 * contains mutable entries.
 * <p>
 * Internally there is a message queue for each endpoint, identified by the
 * authority part of the destination URI. In the method
 * {@link #sendMessage(Message)} the message is offered to the queue
 * corresponding to its destination endpoint (serialized and encapsulated into
 * an {@link OutgoingMessage}). There is also a task (MessageSendTask) for each
 * endpoint, in which the messages are taken from the queue and sent to the
 * remote endpoint.
 *
 * @author Tobias Doenz
 *
 * @see TcpSocketReceiver
 */
@ThreadSafe
public final class TcpSocketSender implements Sender {

    private static final Logger log = LoggerFactory.get();

    // TODO should any of this constants be made configurable?
    /**
     * The size of a message queue for an endpoint.
     */
    private static final int QUEUE_SIZE = 10000;

    /**
     * The timeout for polling message queues and offering messages to it (in
     * milliseconds).
     */
    private static final int QUEUE_TIMEOUT = 1000;

    /**
     * The maximal number of connection attempts to an endpoint.
     */
    private static final int MAX_CONNECTION_ATTEMPTS = 3;

    /**
     * The maximal number of send attempts for a message.
     */
    private static final int MAX_SEND_ATTEMPTS = 3;

    private final Serializer serializer;
    private final ExecutorService threadPool;
    private final ResponseHandler responseHandler;

    /**
     * Counter for the number of opened connections. Note that a connection is
     * closed after some time, if no message is received.
     */
    private final AtomicLong connectionCounter;

    /**
     * Counter for the number of successfully sent messages.
     */
    private final AtomicLong sentMessagesCounter;

    /**
     * The mapping of endpoints (authority part of destination URIs) to message
     * buffering queues.
     */
    private final ConcurrentHashMap<String, BlockingQueue<OutgoingMessage>> endpointQueues;

    /**
     * The mapping of endpoints to tasks to send messages.
     */
    private final ConcurrentHashMap<String, MessageSendTask> endpointTasks;

    /**
     * Constructs an <code>TcpSocketSender</code>.
     *
     * @param serializer
     *            the serializer to serialize the messages
     * @param threadPool
     *            the thread pool where the send tasks are executed
     * @param responseHandler
     *            the response handler which is used in case of errors for
     *            sending request messages
     */
    public TcpSocketSender(final Serializer serializer, final ExecutorService threadPool,
            final ResponseHandler responseHandler) {
        this.serializer = serializer;
        assert this.serializer != null;
        this.threadPool = threadPool;
        assert this.threadPool != null;
        this.responseHandler = responseHandler;
        assert this.responseHandler != null;

        endpointQueues = new ConcurrentHashMap<String, BlockingQueue<OutgoingMessage>>();
        endpointTasks = new ConcurrentHashMap<String, MessageSendTask>();

        connectionCounter = new AtomicLong();
        sentMessagesCounter = new AtomicLong();
    }

    @Override
    public void sendMessage(final Message<?> message) throws SerializationException {

        assert message != null;

        // serialize the message and encapsulate it in an OutgoingMessage
        URI destination = message.getDestinationSpace();
        log.debug("Trying to send message to {}", destination);
        byte[] serializedMessage = serializer.serialize(message);
        log.debug("Serialized message ({} bytes)", serializedMessage.length);
        boolean request = (message instanceof RequestMessage) ? true : false;
        OutgoingMessage outMessage = new OutgoingMessage(serializedMessage, message.getRequestReference(), request);
        // only the serialized message or its immutable parts will be used from
        // now on

        // enqueue it to the queue corresponding to the destination's endpoint
        String remoteEndpoint = destination.getAuthority();
        BlockingQueue<OutgoingMessage> queue = endpointQueues.get(remoteEndpoint);
        if (queue == null) {
            queue = new LinkedBlockingQueue<OutgoingMessage>(QUEUE_SIZE);
            if (endpointQueues.putIfAbsent(remoteEndpoint, queue) != null) {
                queue = endpointQueues.get(remoteEndpoint);
            }
        }
        try {
            boolean enqueued = false;
            while (!enqueued) {
                enqueued = queue.offer(outMessage, QUEUE_TIMEOUT, TimeUnit.MILLISECONDS);
                if (!enqueued) {
                    log.info("Send queue for {} is full. Retrying.", remoteEndpoint);
                }
            }
        } catch (InterruptedException ex) {
            throw new MzsCoreRuntimeException("Interrupted while enqueueing message into send queue of "
                    + remoteEndpoint);
        }

        // start a send task for this endpoint, if it is not running
        MessageSendTask task = endpointTasks.get(remoteEndpoint);
        if (task == null) {
            task = new MessageSendTask(destination, queue);
            MessageSendTask previousTask = endpointTasks.putIfAbsent(remoteEndpoint, task);
            if (previousTask == null) {
                threadPool.execute(task);
            }
        }
    }

    @Override
    public long getNumberOfSentMessages() {
        return sentMessagesCounter.get();
    }

    @Override
    public void shutdown(final boolean wait) {
        // nothing to do, assuming that the thread pool is externally shut down
        // note that TcpSocketReceiver shuts down its thread pool
    }

    private void processMessageError(final Throwable error, final OutgoingMessage... messages) {
        for (OutgoingMessage message : messages) {
            RequestReference requestRef = message.getRequestRef();
            if (message.isRequest()) {
                Response<?> response = new GenericResponse<Serializable>(null, error);
                ResponseMessage responseMessage = new ResponseMessage(requestRef, response, null);
                // TODO does not work when a remote answer container is used
                responseHandler.processResponse(responseMessage);
            } else {
                log.error("Sending response message for request {} failed", requestRef);
            }
        }
    }

    /**
     * Task to create a socket connection to an endpoint and send messages. This
     * task will run until it has sent all messages in the message queue and no
     * new message arrived before the timeout elapsed. Creating the connection
     * to the endpoint and sending messages is retried when an exception occurs,
     * in total for a {@link TcpSocketSender#MAX_CONNECTION_ATTEMPTS} and
     * {@link TcpSocketSender#MAX_SEND_ATTEMPTS} times, respectively. Besides using
     * blocking I/O to send the messages, the task will wait and block for the
     * duration of {@link TcpSocketSender#QUEUE_TIMEOUT} on the (empty) queue before
     * it ends.
     *
     * @author Tobias Doenz
     */
    @Immutable
    private final class MessageSendTask implements Runnable {

        private final URI destination;
        private final BlockingQueue<OutgoingMessage> queue;

        private final AtomicInteger connectionAttempts;

        /**
         * Constructs a <code>MessageSendTask</code>.
         *
         * @param destination
         *            the destination URI
         * @param queue
         *            the message queue for the destination URI
         */
        public MessageSendTask(final URI destination, final BlockingQueue<OutgoingMessage> queue) {
            this.destination = destination;
            assert this.destination != null;
            this.queue = queue;
            assert this.queue != null;

            connectionAttempts = new AtomicInteger(0);
        }

        @Override
        public void run() {

            // open a connection to the endpoint
            Socket socket = null;
            DataOutputStream out = null;
            try {
                connectionAttempts.incrementAndGet();
                String hostname = destination.getHost();
                int port = destination.getPort();
                socket = new Socket(hostname, port);
                socket.setTcpNoDelay(true);
                long connectionCount = connectionCounter.incrementAndGet();
                log.debug("Created socket connection to {} ({})", socket.getRemoteSocketAddress(), connectionCount);
                out = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));
            } catch (IOException ex) {
                log.debug("Cannot connect to {}: {}", destination.getAuthority(), ex.toString());
                closeSocket(socket);
                // handle connection retries
                if (connectionAttempts.get() == MAX_CONNECTION_ATTEMPTS) {
                    log.warn("Maximum number of connection attempts ({}) " + "to {} reached", MAX_CONNECTION_ATTEMPTS,
                            destination.getAuthority());
                    endpointTasks.remove(destination.getAuthority());
                    ArrayList<OutgoingMessage> failedMessages = new ArrayList<OutgoingMessage>();
                    queue.drainTo(failedMessages);
                    processMessageError(ex, failedMessages.toArray(new OutgoingMessage[failedMessages.size()]));
                } else {
                    // retry to create connection
                    // TODO wait some time before retrying?
                    threadPool.execute(this);
                }
                return;
            }

            /*
             * loop to send all messages in the queue; ends if polling on the
             * queue times out (or is interrupted); when sending a message fails,
             * the message is queued again (up to the configured number of
             * attempts) and the whole task is rescheduled
             */
            for (;;) {
                OutgoingMessage message = null;
                try {
                    message = queue.poll(QUEUE_TIMEOUT, TimeUnit.MILLISECONDS);
                    if (message == null) {
                        endpointTasks.remove(destination.getAuthority());
                        // poll for new messages to avoid race-condition
                        // (task still in map but not sending new messages)
                        message = queue.poll();
                        if (message == null) {
                            log.debug("Queue empty, ending MessageSendTask");
                            break;
                        }
                    }
                } catch (InterruptedException ex) {
                    log.debug("Polling interrupted: {}", ex.toString());
                    break;
                }
                log.debug("Sending message for request {}", message.getRequestRef());
                message.incrementSendAttempts();
                byte[] messageBytes = message.getSerializedMessage();
                try {
                    out.writeInt(messageBytes.length);
                    out.write(messageBytes);
                    out.flush();
                    long sentMessagesCount = sentMessagesCounter.incrementAndGet();
                    log.trace("Sent message ({} bytes, {})", messageBytes.length, sentMessagesCount);
                } catch (IOException ex) {
                    log.warn("Cannot send message: {}", ex.toString());
                    closeSocket(socket);
                    // handle message send retries
                    if (message.getSendAttempts() == MAX_SEND_ATTEMPTS) {
                        log.warn("Maximum number of send attempts ({}) reached", MAX_SEND_ATTEMPTS);
                        processMessageError(ex, message);
                    } else {
                        try {
                            // try to send message with new connection
                            queue.put(message);
                            threadPool.execute(this);
                        } catch (InterruptedException ex1) {
                            processMessageError(ex1, message);
                        }
                        return;
                    }
                }
            }
            closeSocket(socket);
        }

        private void closeSocket(final Socket socket) {
            if (socket == null) {
                return;
            }
            try {
                socket.close();
            } catch (IOException ex) {
                log.info("Could not close socket: {}", ex.toString());
            }
        }
    }
}
