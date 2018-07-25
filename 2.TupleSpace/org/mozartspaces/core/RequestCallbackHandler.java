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
package org.mozartspaces.core;

import java.io.Serializable;

/**
 * Represents the callback handler that is called when a request has been processed and a result or error has been set
 * for this request.
 *
 * @author Tobias Doenz
 *
 * @param <REQ>
 *            the type of the request
 * @param <RES>
 *            the type of the result of the request
 */
public interface RequestCallbackHandler<REQ extends Request<RES>, RES extends Serializable> {

    /**
     * Invoked when the request has been successfully processed.
     *
     * @param request
     *            the request
     * @param result
     *            the request result
     */
    void requestProcessed(REQ request, RES result);

    /**
     * Invoked when the request processing failed.
     *
     * @param request
     *            the request
     * @param error
     *            the error
     */
    void requestFailed(REQ request, Throwable error);

}
