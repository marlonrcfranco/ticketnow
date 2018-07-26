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

import java.net.URI;
import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.mozartspaces.core.ContainerReference;
import org.mozartspaces.core.MzsCoreException;
import org.mozartspaces.core.aspects.AspectReference;
import org.mozartspaces.core.aspects.ContainerAspect;
import org.mozartspaces.core.aspects.ContainerIPoint;
import org.mozartspaces.core.aspects.InterceptionPoint;
import org.mozartspaces.core.aspects.SpaceIPoint;
import org.mozartspaces.core.requests.AddAspectRequest;
import org.mozartspaces.core.requests.MetaModelRequest;
import org.mozartspaces.core.requests.RemoveAspectRequest;

import com.sun.jersey.api.Responses;

/**
 * if the request path is /aspects than a space-aspect is added
 *
 * if the request path is /containers/container/<cid>/aspects a container-aspect is added.
 *
 * in this case the ContainerResource looksup this resource sets its container-reference and dispatches it back to the
 * container which invokes the corresponding method.
 *
 * @author Christian Proinger
 */
@Path(ResourceConstants.ASPECTS_ASPECT)
public final class AspectsResource extends MzsResource {

    private ContainerReference containerReference;

    /**
     * adds an aspect to a container or the space itself. uses the header-param "X-ipoints" for the interception-points
     * of the aspect.
     *
     * @param ca
     * @param sIPoints
     * @return
     */
    @POST
    @Consumes({ MediaType.APPLICATION_JSON })
    public Response addAspect(final ContainerAspect ca, @HeaderParam("X-ipoints") final String sIPoints) {
        Set<InterceptionPoint> ipoints = new HashSet<InterceptionPoint>();
        if (sIPoints != null && sIPoints.length() > 0 && !"[]".equals(sIPoints)) {
            String[] ps = sIPoints.substring(1, sIPoints.length() - 1).split(", ");
            for (String p : ps) {
                ipoints.add(retrieveIPoint(p));
            }
        }
        AspectReference res = null;
        try {
            AddAspectRequest.Builder abuilder = AddAspectRequest.withAspect(ca).iPoints(ipoints);
            if (containerReference != null) {
                abuilder.container(containerReference);
            }
            res = core.send(abuilder.build(), null).getResult();
        } catch (MzsCoreException e) {
            return Response.ok().entity(e).build();
        } catch (InterruptedException e) {
            return handleException(e);
        }

        return Response.created(URI.create(res.toString())).build();
    }

    private InterceptionPoint retrieveIPoint(final String p) {
        if (containerReference == null)
            return SpaceIPoint.valueOf(p);
        else
            return ContainerIPoint.valueOf(p);
    }

    @DELETE
    @Path("/{id}")
    public Response removeAspect(@PathParam("id") final String id, @HeaderParam("X-ipoints") final String sIPoints) {
        Set<InterceptionPoint> ipoints = new HashSet<InterceptionPoint>();
        if (sIPoints != null && sIPoints.length() > 0 && !"[]".equals(sIPoints)) {
            String[] ps = sIPoints.substring(1, sIPoints.length() - 1).split(", ");
            for (String p : ps) {
                ipoints.add(retrieveIPoint(p));
            }
        }
        RemoveAspectRequest rq = RemoveAspectRequest
                .withAspect(new AspectReference(id, core.getConfig().getSpaceUri())).iPoints(ipoints).build();

        try {
            // if this throws an exception the aspect is not available.
            core.send(MetaModelRequest.withPath(ResourceConstants.ASPECTS_ASPECT + "/" + id).build(), null).getResult();

            core.send(rq, null).getResult();
        } catch (MzsCoreException e) {
            // 404. Aspect-Not Found.
            throw new MzsApplicationException(Responses.notFound(), e);
        } catch (InterruptedException e) {
            handleException(e);
        }
        return Response.noContent().build();
    }

    public void setContainerReference(final ContainerReference containerReference) {
        this.containerReference = containerReference;
    }
}
