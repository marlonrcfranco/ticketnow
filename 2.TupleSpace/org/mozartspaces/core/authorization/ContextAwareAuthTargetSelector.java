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

import org.mozartspaces.capi3.AuthTargetCoordinator;
import org.mozartspaces.capi3.AuthTargetCoordinator.AuthTargetSelector;
import org.mozartspaces.core.RequestContext;

/**
 * Context aware version of AuthTargetSelector.
 *
 * @author Stefan Crass
 */
public final class ContextAwareAuthTargetSelector extends AbstractContextAwareSelector<AuthTargetSelector> {

    private static final long serialVersionUID = 1L;

    private final DynamicParameter<RequestAuthTarget> authTargetParam;

    /**
     *
     * @param authTarget dynamic parameter for selected authorization target
     * @param count dynamic parameter for selection count
     * @param name dynamic parameter for selector name
     */
    //TODO overloaded versions analogous to AuthTargetSelector?
    //TODO overloaded version for dynamic evaluation of individual RequestAuthTarget fields?
    public ContextAwareAuthTargetSelector(final DynamicParameter<RequestAuthTarget> authTarget,
            final DynamicParameter<Integer> count, final DynamicParameter<String> name) {
        super(name, count);
        this.authTargetParam = authTarget;
    }

    /**
     *
     * @return the dynamic authorization target parameter
     */
    public DynamicParameter<RequestAuthTarget> getAuthTargetParam() {
        return this.authTargetParam;
    }

    @Override
    public AuthTargetSelector computeSelector(final RequestContext context) {
        Integer count = this.getCountParam().computeValue(context);
        String name = this.getNameParam().computeValue(context);
        RequestAuthTarget target = this.authTargetParam.computeValue(context);

        //TODO support for overloaded versions?
        if (target != null && count != null && name != null) {
            return AuthTargetCoordinator.newSelector(target, count, name);
        } else {
            return null;
        }
    }

    @Override
    public String toString() {
        return "ContextAwareAuthTargetSelector [authTarget=" + authTargetParam + ", count=" + getCountParam()
                + ", name=" + getNameParam() + "]";
    }

}
