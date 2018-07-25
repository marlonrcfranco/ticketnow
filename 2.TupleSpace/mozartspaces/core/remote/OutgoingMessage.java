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
package org.mozartspaces.core.remote;

import java.util.concurrent.atomic.AtomicInteger;

import net.jcip.annotations.Immutable;

import org.mozartspaces.core.RequestReference;

/**
 * Represents a message in the Sender component. It consists of the message
 * itself, serialized to a byte array, which is the data that is sent over the
 * remote connection, and some additional information: a reference to the
 * request this message corresponds to is stored, as well as the message type
 * (request or response) and a counter for the number of send attempts to
 * support a robust sending mechanism that retries sending a message on failure.
 *
 * @author Tobias Doenz
 */
@Immutable
public final class OutgoingMessage {

    private final byte[] serializedMessage;
    private final RequestReference requestRef;
    private final boolean request;

    private final AtomicInteger sendAttempts;

    /**
     * Constructs an <code>OutgoingMessage</code>.
     *
     * @param serializedMessage
     *            the message, serialized to a byte array. For performance
     *            reasons, the array is not copied in the constructor. Do NOT
     *            change the contents of the passed byte array after calling
     *            this constructor.
     * @param requestRef
     *            the reference of the request this message corresponds to
     * @param request
     *            <code>true</code> if the message is a request,
     *            <code>false</code> otherwise (if it is a response)
     */
    public OutgoingMessage(final byte[] serializedMessage, final RequestReference requestRef, final boolean request) {
        this.serializedMessage = serializedMessage;
        assert this.serializedMessage != null;
        this.requestRef = requestRef;
        assert this.requestRef != null;
        this.request = request;

        sendAttempts = new AtomicInteger(0);
    }

    /**
     * @return the number of send attempts for this message
     */
    public int getSendAttempts() {
        return sendAttempts.get();
    }

    /**
     * Increments the number of send attempts.
     *
     * @return the incremented number of send attempts for this message
     */
    public int incrementSendAttempts() {
        return sendAttempts.incrementAndGet();
    }

    /**
     * @return the message, serialized to a byte array. For performance reasons,
     *         the array is not a new copy but the internally used field. Do NOT
     *         change the contents of the returned byte array.
     */
    public byte[] getSerializedMessage() {
        return serializedMessage;
    }

    /**
     * @return the reference of the request this message corresponds to
     */
    public RequestReference getRequestRef() {
        return requestRef;
    }

    /**
     * @return <code>true</code> if the message is a request, <code>false</code>
     *         otherwise (if it is a response)
     */
    public boolean isRequest() {
        return request;
    }

}
