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

import java.io.Serializable;
import java.util.Set;

import org.mozartspaces.core.authorization.NamedValue;

/**
 * The identity data for authentication and authorization.
 *
 * @author Tobias Doenz
 */
public final class IdentityData implements Serializable {

    private static final long serialVersionUID = 1L;

    private final String tokenId;
    private final Set<NamedValue> attributes;
    private final Set<NamedValue> extraAttributes;
    private final IdentityProviderConfiguration idpConfiguration;

    /**
     * @param tokenId
     *            the ID of the SSO token
     * @param attributes
     *            the identity attributes (claimed roles, security assertions)
     * @param extraAttributes
     *            the optional extra attributes, may be {@code null}; they are not checked during authentication but are
     *            used for authorization (that is, combined with the other identity attributes); this attributes can be
     *            used to "impersonate" or "forward" rights
     * @param idpConfiguration
     *            the optional configuration of the identity provider, may be {@code null}
     */
    public IdentityData(final String tokenId, final Set<NamedValue> attributes,
            final Set<NamedValue> extraAttributes, final IdentityProviderConfiguration idpConfiguration) {
        this.tokenId = tokenId;
        this.attributes = attributes;
        this.extraAttributes = extraAttributes;
        this.idpConfiguration = idpConfiguration;
    }

    /**
     * @return the tokenId
     */
    public String getTokenId() {
        return tokenId;
    }

    /**
     * @return the attributes
     */
    public Set<NamedValue> getAttributes() {
        return attributes;
    }

    /**
     * @return the extra attributes
     */
    public Set<NamedValue> getExtraAttributes() {
        return extraAttributes;
    }

    /**
     * @return the idpConfiguration
     */
    public IdentityProviderConfiguration getIdpConfiguration() {
        return idpConfiguration;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((attributes == null) ? 0 : attributes.hashCode());
        result = prime * result + ((extraAttributes == null) ? 0 : extraAttributes.hashCode());
        result = prime * result + ((idpConfiguration == null) ? 0 : idpConfiguration.hashCode());
        result = prime * result + ((tokenId == null) ? 0 : tokenId.hashCode());
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        IdentityData other = (IdentityData) obj;
        if (attributes == null) {
            if (other.attributes != null) {
                return false;
            }
        } else if (!attributes.equals(other.attributes)) {
            return false;
        }
        if (extraAttributes == null) {
            if (other.extraAttributes != null) {
                return false;
            }
        } else if (!extraAttributes.equals(other.extraAttributes)) {
            return false;
        }
        if (idpConfiguration == null) {
            if (other.idpConfiguration != null) {
                return false;
            }
        } else if (!idpConfiguration.equals(other.idpConfiguration)) {
            return false;
        }
        if (tokenId == null) {
            if (other.tokenId != null) {
                return false;
            }
        } else if (!tokenId.equals(other.tokenId)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "IdentityData [tokenId=" + tokenId + ", attributes=" + attributes + ", extraAttributes="
                + extraAttributes + ", idpConfiguration=" + idpConfiguration + "]";
    }

}
