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

/**
 * The constants used for storing the identity properties for authentication and authorization in the
 * {@link org.mozartspaces.core.RequestContext}.
 *
 * @author Tobias Doenz
 */
public final class IdentityConstants {

    // TODO move constants somewhere else?

    /**
     * The ID of the SSO token (issued by an IDP).
     */
    public static final String TOKEN_ID_PROPERTY_KEY = "tokenId";

    /**
     * The key for the configuration of the Identity Provider.
     */
    public static final String IDP_CONFIGURATION_PROPERTY_KEY = "idpConfiguration";

    private IdentityConstants() {
    }
}
