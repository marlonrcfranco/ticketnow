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

import java.io.Serializable;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

import org.mozartspaces.core.MzsCoreException;
import org.mozartspaces.core.metamodel.MetaModelException;
import org.mozartspaces.core.requests.MetaModelRequest;
import org.mozartspaces.util.LoggerFactory;
import org.slf4j.Logger;

import com.sun.jersey.api.Responses;

/**
 * The SpaceResource is used for all the Meta-Model-Requests except for those with a uri that have a
 * "/containers/container"-part in it.
 *
 * @author Christian Proinger
 */
@Path("/")
public class SpaceResource extends MzsResource {

    private static final Logger log = LoggerFactory.get();

    /**
     * this method matches any uri that is not matched by other resources with a more specific uri-pattern. the the
     * path-segments after that are injected in the uri-param.
     */
    @GET
    @Path("{uri:.*}")
    public Serializable getSpaceInfo(@PathParam("uri") final String uri) {
        // MultivaluedMap<String, String> segments = uriInfo.getPathParameters();
        log.info("meta-request: " + uriInfo.getRequestUri());

        MetaModelRequest req = MetaModelRequest.withPath(uri).depth(1).build();
        try {
            return core.send(req, null).getResult();
        } catch (MetaModelException e) {
            log.error("", e);
            throw new MzsApplicationException(Responses.notFound(), e);
        } catch (MzsCoreException e) {
            log.error("", e);
            throw new MzsApplicationException(Response.serverError(), e);
        } catch (InterruptedException e) {
            throw new WebApplicationException();
        }
    }
}
