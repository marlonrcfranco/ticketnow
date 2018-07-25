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
package org.mozartspaces.core.authentication;

import java.util.Set;

import javax.security.auth.callback.Callback;

import org.mozartspaces.core.authorization.NamedValue;

/**
 * Identity provider for authentication.
 *
 * @author Tobias Doenz
 */
public interface IdentityProvider {

    /**
     * Returns the authentication requirements as callback objects that need to be populated with the credentials.
     *
     * @return an array of callback objects
     * @throws AuthenticationException
     *             if logging in or getting the authentication callbacks fails
     */
    Callback[] getAuthenticationCallbacks() throws AuthenticationException;

    /**
     * Authenticates a user.
     *
     * @param credentials
     *            the array of callback objects populated with the credentials
     * @return the identity data, or {@code null} if the authentication is not completed
     * @throws AuthenticationException
     *             if authenticating fails
     */
    IdentityData authenticate(Callback[] credentials) throws AuthenticationException;

    /**
     * @return whether the user is authenticated.
     */
    boolean isAuthenticated();

    /**
     * Checks the SSO token and returns the attributes (claimed roles) of the user associated with the token.
     *
     * @param tokenId
     *            the ID of the SSO token
     * @return the attributes from the principal of the SSO token
     * @throws AuthenticationException
     *             if checking the token fails
     */
    Set<NamedValue> checkToken(String tokenId) throws AuthenticationException;

    /**
     * Logs the user out.
     *
     * @throws AuthenticationException
     *             if logging out fails
     */
    void logout() throws AuthenticationException;
}
