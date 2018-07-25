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

import org.mozartspaces.core.RequestContext;
import org.mozartspaces.core.authorization.NamedValue;
import org.mozartspaces.core.security.IdentityConstants;
import org.mozartspaces.core.security.RequestContextUtils;

/**
 * Helper class for authentication.
 *
 * @author Tobias Doenz
 */
public final class IdentityUtils {

    /**
     * Sets the identity data in the RequestContext.
     *
     * @param context
     *            the RequestContext
     * @param identity
     *            the identity data
     */
    public static void setIdentityPropertiesInContext(final RequestContext context, final IdentityData identity) {
        if (context == null || identity == null) {
            return;
        }
        if (identity.getTokenId() != null) {
            context.setRequestProperty(IdentityConstants.TOKEN_ID_PROPERTY_KEY, identity.getTokenId());
        }
        if (identity.getAttributes() != null) {
            context.setRequestProperty(RequestContextUtils.ATTRIBUTES_PROPERTY_KEY, identity.getAttributes());
        }
        if (identity.getExtraAttributes() != null) {
            context.setRequestProperty(RequestContextUtils.EXTRA_ATTRIBUTES_PROPERTY_KEY,
                    identity.getExtraAttributes());
        }
        if (identity.getIdpConfiguration() != null) {
            context.setRequestProperty(IdentityConstants.IDP_CONFIGURATION_PROPERTY_KEY,
                    identity.getIdpConfiguration());
        }
    }

    /**
     * Creates an identity data object from the properties set in the request context.
     *
     * @param context
     *            the request context
     * @return the created identity data object
     */
    public static IdentityData createIdentityDataFromContext(final RequestContext context) {
        String tokenId = (String) context.getRequestProperty(IdentityConstants.TOKEN_ID_PROPERTY_KEY);
        @SuppressWarnings("unchecked")
        Set<NamedValue> attributes = (Set<NamedValue>) context
                .getRequestProperty(RequestContextUtils.ATTRIBUTES_PROPERTY_KEY);
        @SuppressWarnings("unchecked")
        Set<NamedValue> extraAttributes = (Set<NamedValue>) context
                .getRequestProperty(RequestContextUtils.EXTRA_ATTRIBUTES_PROPERTY_KEY);
        IdentityProviderConfiguration idpConfiguration = (IdentityProviderConfiguration) context
                .getRequestProperty(IdentityConstants.IDP_CONFIGURATION_PROPERTY_KEY);
        return new IdentityData(tokenId, extraAttributes, attributes, idpConfiguration);
    }

    private IdentityUtils() {
    }
}
