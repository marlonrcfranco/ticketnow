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

import javax.ws.rs.DELETE;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;

import org.mozartspaces.core.TransactionReference;
import org.mozartspaces.core.requests.CommitTransactionRequest;
import org.mozartspaces.core.requests.RollbackTransactionRequest;
import org.mozartspaces.util.LoggerFactory;
import org.slf4j.Logger;

import com.sun.jersey.api.core.ResourceContext;
import com.sun.jersey.spi.resource.PerRequest;

/**
 * The TransactionResource contains methods performed on single Transactions which are commitTransaction and
 * rollbackTransaction.
 *
 * @author Christian Proinger
 *
 */
@PerRequest
public final class TransactionResource extends MzsResource {

    private static final Logger log = LoggerFactory.get();

    private String id;

    public void setTransactionId(final String id) {
        this.id = id;
    }

    @PUT
    public Response commitTransaction() {
        TransactionReference tref = new TransactionReference(id, core.getConfig().getSpaceUri());
        CommitTransactionRequest ctr = CommitTransactionRequest.withTransaction(tref).build();
        try {
            core.send(ctr, null).getResult();
        } catch (Exception e) {
            handleException(e);
        }
        return Response.noContent().build();
    }

    @Path(ResourceConstants.CONTAINERS)
    public ContainersResource getContainersResource(@Context final ResourceContext rc) {
        ContainersResource cr = rc.getResource(ContainersResource.class);
        cr.setTransactionReference(new TransactionReference(id, core.getConfig().getSpaceUri()));
        return cr;
    }

    /**
     * rolls back
     *
     * @return
     */
    @DELETE
    public Response rollbackTransaction() {
        RollbackTransactionRequest rtr = RollbackTransactionRequest.withTransaction(
                new TransactionReference(id, core.getConfig().getSpaceUri())).build();

        try {
            core.send(rtr, null).getResult();
            return Response.noContent().build();
        } catch (Exception e) {
            return handleException(e);
        }
    }

}
