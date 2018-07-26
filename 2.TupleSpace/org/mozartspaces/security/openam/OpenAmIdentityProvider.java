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
package org.mozartspaces.security.openam;

import java.security.Principal;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.security.auth.callback.Callback;

import org.mozartspaces.core.authentication.AuthenticationException;
import org.mozartspaces.core.authentication.IdentityData;
import org.mozartspaces.core.authentication.IdentityProvider;
import org.mozartspaces.core.authentication.IdentityProviderConfiguration;
import org.mozartspaces.core.authorization.NamedValue;
import org.mozartspaces.util.LoggerFactory;
import org.slf4j.Logger;

import com.iplanet.sso.SSOException;
import com.iplanet.sso.SSOToken;
import com.iplanet.sso.SSOTokenManager;
import com.sun.identity.authentication.AuthContext;
import com.sun.identity.authentication.spi.AuthLoginException;
import com.sun.identity.idm.AMIdentity;
import com.sun.identity.idm.IdRepoException;
import com.sun.identity.idm.IdUtils;

/**
 * Identity provider for <a href="http://forgerock.com/openam.html">OpenAM</a>.
 *
 * @author Tobias Doenz
 */
public final class OpenAmIdentityProvider implements IdentityProvider {

    private static final Logger log = LoggerFactory.get();

    /**
     * Name of the LDAP uid attribute.
     */
    public static final String LDAP_UID_ATTRIBUTE_NAME = "uid";
    /**
     * Name of the uid attribute, used for PeerSpaces/XVSM in the NamedValue.
     */
    public static final String XVSM_UID_ATTRIBUTE_NAME = "user";
    /**
     * Name of the LDAP group attribute.
     */
    public static final String LDAP_GROUP_ATTRIBUTE_NAME = "memberof";
    /**
     * Name of the group attribute, used for PeerSpaces/XVSM in the NamedValue.
     */
    public static final String XVSM_GROUP_ATTRIBUTE_NAME = "role";

    private final String realm;
    private final String moduleName;
    private final IdentityProviderConfiguration config;

    private AuthContext authContext;
    private boolean authenticated;

    /**
     * @param realm
     *            the realm (organization name for {@link AuthContext})
     * @param moduleName
     *            (index name for {@link AuthContext#login(AuthContext.IndexType, String)}
     */
    public OpenAmIdentityProvider(final String realm, final String moduleName) {
        this.realm = realm;
        this.moduleName = moduleName;
        this.config = null;
    }

    /**
     * @param config
     *            the configuration, used to create the {@code IdentityCoData}
     * @param realm
     *            the realm (organization name for {@link AuthContext})
     * @param moduleName
     *            (index name for {@link AuthContext#login(AuthContext.IndexType, String)}
     */
    public OpenAmIdentityProvider(final IdentityProviderConfiguration config, final String realm,
            final String moduleName) {
        this.realm = realm;
        this.moduleName = moduleName;
        this.config = config;
    }

    private void login() throws AuthenticationException {
        log.debug("Logging in to realm {} and module {}", realm, moduleName);
        try {
            authContext = new AuthContext(realm);
            AuthContext.IndexType indexType = AuthContext.IndexType.MODULE_INSTANCE;
            authContext.login(indexType, moduleName);
        } catch (AuthLoginException ex) {
            throw new AuthenticationException("Login failed", ex);
        }
    }

    @Override
    public Callback[] getAuthenticationCallbacks() throws AuthenticationException {
        if (authContext == null) {
            login();
        }
        if (authContext.hasMoreRequirements()) {
            return authContext.getRequirements();
        } else {
            return new Callback[0];
        }
    }

    private void requireLogin() {
        if (authContext == null) {
            throw new IllegalStateException("AuthContext not set, login first");
        }
    }

    @Override
    public IdentityData authenticate(final Callback[] credentials) throws AuthenticationException {
        assert credentials != null;
        log.debug("Authenticating with {} credentials", credentials.length);
        requireLogin();
        authContext.submitRequirements(credentials);
        if (authContext.hasMoreRequirements()) {
            log.debug("Credentials not sufficient, authentication has more requirements");
            return null;
        }
        if (authContext.getStatus() == AuthContext.Status.SUCCESS) {
            log.debug("Login succeeded.");
            try {
                SSOToken token = authContext.getSSOToken();
                String tokenId = token.getTokenID().toString();
                log.debug("Token ID: {}", tokenId);
                Principal principal = token.getPrincipal();
                log.debug("Principal: {}", principal.getName());
                Set<NamedValue> attributes = parseTokenAttributes(token);
                authenticated = true;
                return new IdentityData(tokenId, attributes, null, config);
            } catch (Exception ex) {
                throw new AuthenticationException("Cannot get or process SSO token", ex);
            }
        }
        throw new AuthenticationException("Cannot authenticate: Status " + authContext.getStatus());
    }

    @Override
    public boolean isAuthenticated() {
        return authenticated;
    }

    private Set<NamedValue> parseTokenAttributes(final SSOToken token) throws SSOException, IdRepoException {
        AMIdentity userIdentity = IdUtils.getIdentity(token);
        @SuppressWarnings("unchecked")
        Map<String, Set<String>> attrs = userIdentity.getAttributes();
        log.trace("{} user attributes", attrs.size());
        Set<NamedValue> attributes = new HashSet<NamedValue>();
        for (Map.Entry<String, Set<String>> attr : attrs.entrySet()) {
            String name = attr.getKey();
            Set<String> values = attr.getValue();
            log.trace("{}={}", name, values);
            if (name.equals(LDAP_UID_ATTRIBUTE_NAME)) {
                NamedValue uid = new NamedValue(XVSM_UID_ATTRIBUTE_NAME, values.iterator().next());
                log.debug("UID: {}", uid);
                attributes.add(uid);
            } else if (name.equals(LDAP_GROUP_ATTRIBUTE_NAME)) {
                String[] splittedValue = values.iterator().next().split(",");
                for (String groupValue : splittedValue) {
                    // strip the "cn=" at the start
                    String groupName = groupValue.substring(3);
                    NamedValue group = new NamedValue(XVSM_GROUP_ATTRIBUTE_NAME, groupName);
                    log.debug("Group: " + group);
                    attributes.add(group);
                }
            }
        }
        // TODO use Map<String, String> with mapping LDAP <-> XVSM
        return attributes;
    }

    @Override
    public Set<NamedValue> checkToken(final String tokenId) throws AuthenticationException {
        log.debug("Checking token with ID {}", tokenId);
        SSOTokenManager manager = null;
        SSOToken token = null;
        try {
            manager = SSOTokenManager.getInstance();
            token = manager.createSSOToken(tokenId);
        } catch (SSOException ex) {
            throw new AuthenticationException("Cannot get SSO token", ex);
        }
        try {
            manager.validateToken(token);
            log.debug("Token {} is valid", tokenId);
        } catch (SSOException ex) {
            throw new AuthenticationException("Token is invalid", ex);
        }
        try {
            log.debug("Principal: {}", token.getPrincipal().getName());
            log.debug("Host name: {}", token.getHostName());
            return parseTokenAttributes(token);
        } catch (SSOException ex) {
            throw new AuthenticationException("Cannot process SSO token", ex);
        } catch (IdRepoException ex) {
            throw new AuthenticationException("Cannot process SSO token", ex);
        }
    }

    @Override
    public void logout() throws AuthenticationException {
        //requireLogin();
        if (!authenticated) {
            return;
        }
        try {
            authContext.logout();
            authContext = null;
            authenticated = false;
        } catch (AuthLoginException ex) {
            throw new AuthenticationException("Cannot log out", ex);
        }
    }

}
