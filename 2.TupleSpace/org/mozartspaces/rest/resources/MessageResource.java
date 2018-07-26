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
package org.mozartspaces.rest.resources;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.mozartspaces.core.AbstractMessage;

/**
 * a resource that accepts all kind of messages. this is used from other XVSM-Servers that communicate through REST. The
 * SimpleWebSender uses this resource the AtmosphereWebSender does not.
 *
 * @author Christian Proinger
 *
 */
@Path("/messages")
public final class MessageResource extends MzsResource {

    // private final static Logger log = LoggerFactory.get();

    @POST
    public Response receiveMessage(final AbstractMessage<?> message) {
        messageDistributor.distributeMessage(message);
        return Response.status(Status.ACCEPTED).build();
    }
}
