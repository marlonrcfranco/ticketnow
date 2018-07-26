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

import org.mozartspaces.capi3.KeyCoordinator;
import org.mozartspaces.capi3.KeyCoordinator.KeySelector;
import org.mozartspaces.core.RequestContext;

/**
 * Context aware version of KeySelector.
 *
 * @author Stefan Crass
 */
public final class ContextAwareKeySelector extends AbstractContextAwareSelector<KeySelector> {

    private static final long serialVersionUID = 1L;

    private final DynamicParameter<String> keyParam;

    /**
     *
     * @param key dynamic parameter for selection key
     * @param count dynamic parameter for selection count
     * @param name dynamic parameter for selector name
     */
    //TODO overloaded versions analogous to KeySelector?
    public ContextAwareKeySelector(final DynamicParameter<String> key, final DynamicParameter<Integer> count,
            final DynamicParameter<String> name) {
        super(name, count);
        this.keyParam = key;
    }

    /**
     *
     * @return the dynamic key parameter
     */
    public DynamicParameter<String> getKeyParam() {
        return keyParam;
    }

    @Override
    public KeySelector computeSelector(final RequestContext context) {
        Integer count = this.getCountParam().computeValue(context);
        String name = this.getNameParam().computeValue(context);
        String key = this.keyParam.computeValue(context);

        //TODO support for overloaded versions?
        if (key != null && count != null && name != null) {
            return KeyCoordinator.newSelector(key, count, name);
        } else {
            return null;
        }
    }

    @Override
    public String toString() {
        return "ContextAwareKeySelector [key=" + keyParam + ", count=" + getCountParam() + ", name=" + getNameParam()
                + "]";
    }

}
