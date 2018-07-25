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

import org.mozartspaces.core.Message;

/**
 * The <code>RemoteMessageDistributor</code> distributes received messages depending on their type. It passes
 * {@link org.mozartspaces.core.RequestMessage RequestMessage}s to the {@link org.mozartspaces.runtime.RequestHandler
 * RequestHandler} and {@link org.mozartspaces.core.ResponseMessage ResponseMessage}s to the
 * {@link org.mozartspaces.core.ResponseHandler ResponseHandler}.
 *
 * @author Christian Proinger (extracted the interface from RemoteMessageDistributor)
 */
public interface MessageDistributor {

    /**
     * Distributes a received message.
     *
     * @param message
     *            the received message
     */
    void distributeMessage(final Message<?> message);

}
