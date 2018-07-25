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

import org.mozartspaces.capi3.LindaCoordinator;
import org.mozartspaces.capi3.LindaCoordinator.LindaSelector;
import org.mozartspaces.core.RequestContext;

/**
 * Context aware version of LindaSelector.
 *
 * @author Stefan Crass
 */
public final class ContextAwareLindaSelector extends AbstractContextAwareSelector<LindaSelector> {

    private static final long serialVersionUID = 1L;

    private final DynamicParameter<Serializable> templateParam;

    /**
     *
     * @param template dynamic parameter for selection template
     * @param count dynamic parameter for selection count
     * @param name dynamic parameter for selector name
     */
    //TODO overloaded versions analogous to LindaSelector?
    //TODO overloaded version for dynamic evaluation of individual template fields?
    public ContextAwareLindaSelector(final DynamicParameter<Serializable> template,
            final DynamicParameter<Integer> count, final DynamicParameter<String> name) {
        super(name, count);
        this.templateParam = template;
    }

    /**
     *
     * @return the dynamic template parameter
     */
    public DynamicParameter<Serializable> getTemplateParam() {
        return this.templateParam;
    }

    @Override
    public LindaSelector computeSelector(final RequestContext context) {
        Integer count = this.getCountParam().computeValue(context);
        String name = this.getNameParam().computeValue(context);
        Serializable template = this.templateParam.computeValue(context);

        //TODO support for overloaded versions?
        if (template != null && count != null && name != null) {
            return LindaCoordinator.newSelector(template, count, name);
        } else {
            return null;
        }
    }

    @Override
    public String toString() {
        return "ContextAwareLindaSelector [template=" + templateParam + ", count=" + getCountParam() + ", name="
                + getNameParam() + "]";
    }

}
