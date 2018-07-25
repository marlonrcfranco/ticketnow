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
package org.mozartspaces.capi3.javanative.authorization;
import java.util.Map;

import org.mozartspaces.capi3.javanative.operation.NativeEntry;

/**
 * The result of an authorization request for a specific access.
 *
 * @author Stefan Crass
 */
public final class AuthorizationResult {

    private final Map<NativeEntry, AuthorizationType> entryDecisions;

    private final AuthorizationType generalDecision;

    /**
     * Creates a new AuthorizationResult based on a map of single authorization decisions for each entry.
     * @param entryDecisions
     *          the map of entry decisions
     */
    public AuthorizationResult(final Map<NativeEntry, AuthorizationType> entryDecisions) {
        this.entryDecisions = entryDecisions;
        this.generalDecision = null;
    }

    /**
     * Creates a new AuthorizationResult based on a general authorization decision for all entries.
     * @param generalDecision
     *          the decision valid for all entries
     */
    public AuthorizationResult(final AuthorizationType generalDecision) {
        this.entryDecisions = null;
        this.generalDecision = generalDecision;
    }


    /**
     * @return the general decision
     */
    public AuthorizationType getGeneralDecision() {
        return generalDecision;
    }

    /**
     * @param entry the entry to get the entry decision for
     * @return the entry decision
     */
    public AuthorizationType getEntryDecision(final NativeEntry entry) {
        return (entryDecisions == null) ? null : entryDecisions.get(entry);
    }

    /**
     * Checks if an entry is authorized according to the authorization result.
     * Returns NOT_APPLICABLE if no decision is available for the entry.
     * @param entry the entry to be checked
     * @return the authorization decision for the entry
     */
    public AuthorizationType checkEntryAuthorization(final NativeEntry entry) {
        if (entry == null) {
            throw new NullPointerException("The Entry must not be null");
        }
        if (generalDecision != null) {
            return generalDecision;
        } else if (entryDecisions != null) {
            AuthorizationType decision = this.entryDecisions.get(entry);
            if (decision == null) {
                return AuthorizationType.NOT_APPLICABLE;
            } else {
                return decision;
            }
        } else {
            throw new NullPointerException("Decision must not be null.");
        }
    }

    @Override
    public String toString() {
        if (entryDecisions != null) {
            return entryDecisions.size() + " entry result(s)";
        }
        return generalDecision + " (general decision)";
    }

}
