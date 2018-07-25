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
package org.mozartspaces.core.authorization;

import java.io.Serializable;

/**
 * Authorization rule for access control. Referenced by policies to specify access rights.
 * @author Stefan Crass
 */
public final class AuthorizationRule implements Serializable {

    private static final long serialVersionUID = 1L;

    private final String ruleId;
    private final AuthorizationTarget target;
    private final Scope scope;
    private final Condition condition;
    private final Effect effect;

    /**
     * @param ruleId
     *            unique rule id used to reference the rule within policies
     * @param target
     *            target of the rule, defines for which requests it shall be evaluated
     * @param scope
     *            defines for which entries of a container the rule applies, {@code null} for all entries of a
     *            container
     * @param condition
     *            defines additional constraints that must hold for the rule to apply, {@code null} for no
     *            additional constraints
     * @param effect
     *            effect of the rule (PERMIT or DENY)
     */
    public AuthorizationRule(final String ruleId, final AuthorizationTarget target, final Scope scope,
            final Condition condition, final Effect effect) {
        this.ruleId = ruleId;
        if (this.ruleId == null) {
            throw new NullPointerException("ruleId");
        }
        this.target = target;
        if (this.target == null) {
            throw new NullPointerException("target");
        }
        this.scope = scope;
        this.condition = condition;
        this.effect = effect;
        if (this.effect == null) {
            throw new NullPointerException("effect");
        }
    }

    /**
     * @return the rule id
     */
    public String getRuleId() {
        return ruleId;
    }

    /**
     * @return the authorization target
     */
    public AuthorizationTarget getTarget() {
        return target;
    }

    /**
     * @return the scope of the rule
     */
    public Scope getScope() {
        return scope;
    }

    /**
     * @return the condition of the rule
     */
    public Condition getCondition() {
        return condition;
    }

    /**
     * @return the effect of the rule
     */
    public Effect getEffect() {
        return effect;
    }

    @Override
    public String toString() {
        return "AuthorizationRule [ruleId=" + ruleId + ", target=" + target + ", scope=" + scope + ", condition="
                + condition + ", effect=" + effect + "]";
    }

}
