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
package org.mozartspaces.core.config;

import java.io.Serializable;

import net.jcip.annotations.Immutable;

/**
 * Configuration of the security - authentication and authorization.
 *
 * @author Tobias Doenz
 */
@Immutable
public final class SecurityConfiguration implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * Authentication default status.
     */
    public static final boolean SECURITY_AUTHENTICATION_ENABLED_DEFAULT = false;
    /**
     * Authorization default status.
     */
    public static final boolean SECURITY_AUTHORIZATION_ENABLED_DEFAULT = false;
    /**
     * Request-based authorization default status.
     */
    public static final boolean SECURITY_AUTHORIZE_REQUESTS_DEFAULT = false;

    private final boolean authenticationEnabled;

    // TODO add authentication modules (space-based, OpenAM SSO)

    private final boolean authorizationEnabled;

    private final boolean authorizeRequests;

    /**
     * @param authenticationEnabled
     *            whether authentication is enabled
     * @param authorizationEnabled
     *            whether authorization is enabled
     * @param authorizeRequests
     *            whether request-based authorization is enabled
     */
    public SecurityConfiguration(final boolean authenticationEnabled, final boolean authorizationEnabled,
            final boolean authorizeRequests) {
        this.authenticationEnabled = authenticationEnabled;
        this.authorizationEnabled = authorizationEnabled;
        this.authorizeRequests = authorizeRequests;
    }

    /**
     * Default configuration.
     */
    public SecurityConfiguration() {
        this.authenticationEnabled = SECURITY_AUTHENTICATION_ENABLED_DEFAULT;
        this.authorizationEnabled = SECURITY_AUTHORIZATION_ENABLED_DEFAULT;
        this.authorizeRequests = SECURITY_AUTHORIZE_REQUESTS_DEFAULT;
    }

    /**
     * @return the authenticationEnabled
     */
    public boolean isAuthenticationEnabled() {
        return authenticationEnabled;
    }

    /**
     * @return the authorizationEnabled
     */
    public boolean isAuthorizationEnabled() {
        return authorizationEnabled;
    }

    /**
     * @return the authorizeRequests
     */
    public boolean isAuthorizeRequests() {
        return authorizeRequests;
    }

    @Override
    public String toString() {
        return "SecurityConfiguration [authenticationEnabled=" + authenticationEnabled + ", authorizationEnabled="
                + authorizationEnabled + ", authorizeRequests=" + authorizeRequests + "]";
    }

}
