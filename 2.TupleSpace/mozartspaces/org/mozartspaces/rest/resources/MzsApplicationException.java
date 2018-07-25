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

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response.ResponseBuilder;

import org.mozartspaces.core.GenericResponse;

/**
 * wrapper-WebApplicationException for MzsCoreExceptions. It can be initializes with a ResponseBuilder to server
 * serveral HTTP-Status-codes. The Stacktrace of the Exception is removed because it causes errors in the
 * serialization-process.
 *
 * @author Christian Proinger
 *
 */
public class MzsApplicationException extends WebApplicationException {

    private static final long serialVersionUID = 2102533484428620869L;

    public MzsApplicationException(final ResponseBuilder rb, final Exception ce) {
        super(rb.entity(new GenericResponse<Serializable>(null, removeStacktrace(ce))).type(MediaType.APPLICATION_JSON)
                .build()); // TODO what if the user accepts XML only?
    }

    private static Exception removeStacktrace(final Exception e) {
        if (e != null) {
            e.setStackTrace(new StackTraceElement[0]);
        }
        return e;
    }
}
