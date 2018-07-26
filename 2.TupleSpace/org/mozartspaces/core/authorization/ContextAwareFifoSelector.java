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

import org.mozartspaces.capi3.FifoCoordinator;
import org.mozartspaces.capi3.FifoCoordinator.FifoSelector;
import org.mozartspaces.core.RequestContext;

/**
 * Context aware version of FifoSelector.
 *
 * @author Stefan Crass
 */
public final class ContextAwareFifoSelector extends AbstractContextAwareSelector<FifoSelector> {

    private static final long serialVersionUID = 1L;

    /**
     *
     * @param count dynamic parameter for selection count
     * @param name dynamic parameter for selector name
     */
    //TODO overloaded versions analogous to FifoSelector?
    public ContextAwareFifoSelector(final DynamicParameter<Integer> count,
            final DynamicParameter<String> name) {
        super(name, count);
    }


    @Override
    public FifoSelector computeSelector(final RequestContext context) {
        Integer count = this.getCountParam().computeValue(context);
        String name = this.getNameParam().computeValue(context);

        //TODO support for overloaded versions?
        if (count != null && name != null) {
            return FifoCoordinator.newSelector(count, name);
        } else {
            return null;
        }
    }

    @Override
    public String toString() {
        return "ContextAwareFifoSelector [count=" + getCountParam() + ", name=" + getNameParam() + "]";
    }

}