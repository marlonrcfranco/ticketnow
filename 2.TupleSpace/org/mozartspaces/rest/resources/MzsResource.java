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
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.mozartspaces.capi3.InvalidContainerException;
import org.mozartspaces.core.MzsCore;
import org.mozartspaces.core.MzsCoreException;
import org.mozartspaces.core.TransactionException;
import org.mozartspaces.core.remote.MessageDistributor;
import org.mozartspaces.util.LoggerFactory;
import org.slf4j.Logger;

import com.sun.jersey.api.NotFoundException;
import com.sun.jersey.api.Responses;

/**
 * The Base resource for all Mozart-spaces resource. Common Jersey- and Mozartspaces-Components are injected in here.
 *
 * @author Christian Proinger
 *
 */
@Consumes({ MediaType.APPLICATION_JSON })
@Produces({ MediaType.APPLICATION_JSON })
public class MzsResource {

    private static final Logger log = LoggerFactory.get();

    @Context
    protected MzsCore core;

    /**
     * the uri-info of the resource.
     */
    @Context
    protected UriInfo uriInfo;

    @Context
    protected MessageDistributor messageDistributor;

    public MzsResource() {
        super();
    }

    /**
     * handles exceptions.
     *
     * @param ex
     * @return unreachable
     */
    protected Response handleException(final Exception ex) {
        try {
            throw ex;
            // return Response.serverError().build();
        } catch (InvalidContainerException e) {
            log.info("requested invalid container", e);
            throw new NotFoundException(e.getMessage());
        } catch (TransactionException e) {
            log.error("tx-exception", e);
            throw new MzsApplicationException(Responses.notFound(), e);
        } catch (MzsCoreException e) {
            log.error("", e);
            throw new WebApplicationException(Response.Status.INTERNAL_SERVER_ERROR);
        } catch (Exception e) {
            log.error("", e);
            throw new WebApplicationException();
        }
    }
}