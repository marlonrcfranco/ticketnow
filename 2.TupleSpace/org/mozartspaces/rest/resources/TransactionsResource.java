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

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.mozartspaces.core.RequestFuture;
import org.mozartspaces.core.TransactionReference;
import org.mozartspaces.core.requests.CreateTransactionRequest;

import com.sun.jersey.api.core.ResourceContext;

/**
 * With The TransactionResource Transactions can be created through REST.
 *
 * @author Christian Proinger
 *
 */
@Path(ResourceConstants.TRANSACTIONS)
public class TransactionsResource extends MzsResource {

    // private final static Logger log = LoggerFactory.get();

    @POST
    @Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public Response createTransaction(final CreateTransactionRequest ctr) {

        //
        RequestFuture<TransactionReference> resp = core.send(ctr, null);
        try {
            String id = resp.get().getId();
            // log.debug("created transaction " + id)
            return Response.created(uriInfo.getAbsolutePathBuilder().path(id).build()).build();
        } catch (Exception e) {
            throw new WebApplicationException(Response.Status.INTERNAL_SERVER_ERROR);
        }
    }

    @Path("/{id}")
    public TransactionResource getTransactionResource(@Context final ResourceContext ctx,
            @PathParam("id") final String id) {
        // this doesn't work because transactions cannot be queried with MetaModelRequests yet.
        // try {
        // Serializable res = core.send(MetaModelRequest.withPath("/transactions/transaction/" + id).depth(0).build(),
        // null).getResult();
        // log.debug("result: " + res);
        // } catch (MzsCoreException e) {
        // InvalidTransactionException txe = new InvalidTransactionException(id);
        // throw new MzsApplicationException(Responses.notFound(), txe);
        // } catch (InterruptedException e) {
        // log.error("", e);
        // throw new WebApplicationException(500);
        // }

        TransactionResource res = ctx.getResource(TransactionResource.class);
        res.setTransactionId(id);
        return res;
    }
}
