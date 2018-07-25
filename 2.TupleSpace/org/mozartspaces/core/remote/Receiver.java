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

/**
 * A <code>Receiver</code> receives incoming message from other XVSM cores. The
 * received messages are deserialized and routed to the appropriate components
 * in the core.
 *
 * @author Tobias Doenz
 */
public interface Receiver {

    /**
     * @return the total number of received messages for this Receiver
     */
    long getNumberOfReceivedMessages();

    /**
     * Starts the receiver, that is, start listening for incoming messages.
     */
    void start();

    /**
     * Shuts down the Receiver.
     *
     * @param wait
     *            determines if the shutdown should be made synchronous
     *            (<code>true</code>, wait for shutdown to complete) or
     *            asynchronous in its own thread (<code>false</code>)
     */
    void shutdown(boolean wait);

}
