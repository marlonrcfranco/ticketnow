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
package org.mozartspaces.core.security;

import org.mozartspaces.core.RequestContext;
import org.mozartspaces.util.LoggerFactory;
import org.slf4j.Logger;

/**
 * Helper class for security-related properties in the request context.
 *
 * @author Tobias Doenz
 */
public final class RequestContextUtils {

    /**
     * Property key for flag that may be set for embedded requests to enforce authorization.
     */
    public static final String AUTH_REQUIRED = "authRequired";

    /**
     * Property key for flag that is set to true for all remote requests.
     */
    public static final String REMOTE_REQUEST = "remoteRequest";

    /**
     * The key for the user attributes used for authorization.
     */
    public static final String ATTRIBUTES_PROPERTY_KEY = "authAttributes";

    /**
     * The key for the extra attributes used for authorization.
     */
    public static final String EXTRA_ATTRIBUTES_PROPERTY_KEY = "authExtraAttributes";

    private static Logger log = LoggerFactory.get();

    /**
     * Checks whether authorization is required for an operation.
     *
     * @param context
     *            the request context
     * @return whether the operation needs to be authorized
     */
    public static boolean isAuthorizationRequired(final RequestContext context) {
        if (context == null) {
            log.debug("Auth not required (no context)");
            return false;
        }
        // flag REMOTE_REQUEST is set to to true for all remote requests
        Object remoteRequestProperty = context.getRequestProperty(REMOTE_REQUEST);
        // flag AUTH_REQUIRED may be set for embedded requests to enforce authorization
        Object authRequiredProperty = context.getRequestProperty(AUTH_REQUIRED);
        if ((remoteRequestProperty == null || remoteRequestProperty.equals(false))
                && (authRequiredProperty == null || authRequiredProperty.equals(false))) {
            log.debug("Auth not required (flags in context not set)");
            return false;
        }
        return true;
    }

    /**
     * Checks whether the request is a remote request.
     *
     * @param context
     *            the request context
     * @return whether the request is a remote request
     */
    public static boolean isRemoteRequest(final RequestContext context) {
        if (context == null) {
            log.debug("Request is not a remote request (no context)");
            return false;
        }
        // flag REMOTE_REQUEST is set to to true for all remote requests
        Object remoteRequestProperty = context.getRequestProperty(REMOTE_REQUEST);
        if ((remoteRequestProperty == null || remoteRequestProperty.equals(false))) {
            log.debug("Request is not a remote request (flag in context not set)");
            return false;
        }
        return true;
    }

    private RequestContextUtils() {
    }
}
