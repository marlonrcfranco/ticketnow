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
import java.net.URI;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriBuilder;

import org.mozartspaces.capi3.ContainerNameNotAvailableException;
import org.mozartspaces.core.ContainerReference;
import org.mozartspaces.core.TransactionReference;
import org.mozartspaces.core.requests.CreateContainerRequest;
import org.mozartspaces.core.requests.LookupContainerRequest;
import org.mozartspaces.core.requests.MetaModelRequest;
import org.mozartspaces.util.LoggerFactory;
import org.slf4j.Logger;

import com.sun.jersey.api.core.ResourceContext;

/**
 * The ContainersResource contains any methods that do not concern a specific container but containers in general. This
 * includes
 *
 * <ul>
 * <li>lookup of a container by name: GET [transactions/transaction/txId]/containers/container/lookup?name=<name></li>
 * <li>getting a collection of all containers: GET [transactions/transaction/txId]/containers/container</li>
 * <li>creating a new container: POST [transactions/transaction/txId]/containers/container/</li>
 * </ul>
 *
 * also this resource owns the subresource ContainerResource.
 *
 * @author Christian Proinger
 *
 */
@Path(ResourceConstants.CONTAINERS)
public final class ContainersResource extends MzsResource {

    private static final Logger log = LoggerFactory.get();

    /**
     * the container reference this containersResource should use.
     */
    private TransactionReference txRef;

    /**
     * @param txRef
     *            the Transaction reference this containerResource should use
     */
    public void setTransactionReference(final TransactionReference txRef) {
        this.txRef = txRef;
    }

    /**
     * makes a lookupContainerRequest and wraps the result in a RESTContainerResponse which contains the URI for
     * container that was looked up.
     *
     * @param name
     *            the container-name that should be looked up.
     * @param rc
     * @return a RESTContainerResponse.
     */
    @GET
    @Path("/lookup")
    public RESTContainerResponse lookupContainer(@QueryParam("name") final String name,
            @Context final ResourceContext rc) {
        ContainerReference ref = null;
        try {
            ref = core.send(LookupContainerRequest.withName(name).build(), null).getResult();
        } catch (Exception e) {
            throw new MzsApplicationException(Response.ok(), e);
        }

        URI uri = uriInfo.getBaseUriBuilder().path(ContainersResource.class).path(ref.getId()).build();

        /*
         * Link-Headers http://tools.ietf.org/html/draft-nottingham-http-link-header-06
         *
         * Atom-Links http://tools.ietf.org/html/rfc4287#section-4.2.7
         *
         * Links in JBoss-RestEasy http://docs.jboss.org/resteasy/2.0.0.GA/userguide/html/LinkHeader.html
         *
         * Jersey-guide: http://jersey.java.net/nonav/documentation/latest/user-guide.html#linking
         */
        // Response.ok().header("Link", uri.toASCIIString());
        // return new JAXBElement<String>(new QName("Location"), String.class, uri.toASCIIString());
        // ContainerResource containerResource = rc.getResource(ContainerResource.class);
        // containerResource.setContainerReference(ref);
        // return containerResource;
        RESTContainerResponse res = new RESTContainerResponse(ref.getId());
        // der LinkFilter funktioniert irgendwie nicht, möglicherweise wegen atmosphere
        // da muss man vielleicht noch was extra konfigurieren.
        // das ist fürs erste nur ein hack. injection wär schöner.

        // TODO!!! das sollt helfen: http://wikis.sun.com/display/Jersey/Hypermedia+Examples
        res.setLink(uri);
        return res;
    }

    /**
     * @return retrieves an array of RESTContainerResponses which contain the lookup-links for all the containers of the
     *         Space.
     */
    @GET
    public RESTContainerResponse[] getAllContainers() {
        // @see
        // https://sbc.complang.tuwien.ac.at/svn/xvsm/source/trunk/xvsm-tb/src/MozartSpaces2/main/core/examples/src/main/java/org/mozartspaces/core/examples/MetaDataTest.java
        // meta = MetaModelRequest.withPath("/containers").build();
        // System.out.println("Meta data at " + meta.getPath() + ":");
        // result = core.send(meta, null).getResult();
        // System.out.println(result);
        MetaModelRequest mmr = MetaModelRequest.withPath("/containers").depth(1).build();
        /* HashMap */RESTContainerResponse[] res = null;
        try {
            Serializable ser = core.send(mmr, null).getResult();
            log.info("getAllContainers returned: " + res);
            @SuppressWarnings("unchecked")
            HashMap<String, Object> map = (HashMap<String, Object>) ser;
            @SuppressWarnings("unchecked")
            Set<String> names = (Set<String>) map.get("names");
            // RESTContainerResponse cl[] = new RESTContainerResponse[names.size()];
            Set<RESTContainerResponse> cls = new HashSet<RESTContainerResponse>();
            UriBuilder lookupBuilder = uriInfo.getBaseUriBuilder().path(ResourceConstants.CONTAINERS + "/lookup");
            for (String name : names) {
                RESTContainerResponse r = new RESTContainerResponse();
                r.setLink(lookupBuilder.clone().queryParam("name", name).build());
                cls.add(r);
            }
            return cls.toArray(new RESTContainerResponse[0]);
            // return Response.ok(/* res <- wrap in JAXB-XmlRootElement*/).build();
        } catch (Exception e) {
            handleException(e);
        }
        return res;
    }

    /**
     * creates a container (201 CREATED with Location-Link) or responds with a ContainerNameNotAvailableException (200
     * OK).
     *
     * @param areq
     *            the CreateContainerRequest
     * @return a Response 201 or 200.
     */
    @POST
    @Consumes({ MediaType.APPLICATION_JSON })
    public Response createContainer(final CreateContainerRequest areq) {
        // i need to create a new one because i can't set the transaction afterwards.
        // it's okay for a client not to send the TX-Reference in the request-object
        // since it is already encoded in the uri. The Implementation in this project
        // sends it anyway though.
        CreateContainerRequest req = new CreateContainerRequest(areq.getName(), areq.getSize(),
                areq.getObligatoryCoords(), areq.getOptionalCoords(), txRef, areq.getIsolation(), areq.getAuthLevel(),
                areq.isForceInMemory(), areq.getContext());
        try {
            ContainerReference cref = core.send(req, null).getResult();
            return Response.created(uriInfo.getAbsolutePathBuilder().path(cref.getId()).build()).build();
        } catch (ContainerNameNotAvailableException e) {
            throw new MzsApplicationException(Response.status(Status.CONFLICT), e);
        } catch (Exception e) {
            return handleException(e);
        }
    }

    /**
     * this is only called if the container is requested through a transaction in which case the transaction has to be
     * set.
     *
     * @param rc
     *            the resource-context is injected by the container
     * @param cid
     * @return
     */
    @Path("/{cid}")
    public ContainerResource getContainerResource(@Context final ResourceContext rc,
            @PathParam("cid") final String cid) {
        ContainerResource cres = rc.getResource(ContainerResource.class);
        cres.setTransactionReference(txRef);
        return cres;
    }
}
