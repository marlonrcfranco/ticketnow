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

import org.mozartspaces.capi3.Selector;

/**
 * Base class for context aware selectors that allow replacing selector fields with context data.
 *
 * @author Stefan Crass
 *
 * @param <T>
 *            type of the selector that the context aware selector produces
 */
public abstract class AbstractContextAwareSelector<T extends Selector> implements ContextAwareSelector<T> {

    private static final long serialVersionUID = 1L;

    private final DynamicParameter<Integer> countParam;
    private final DynamicParameter<String> nameParam;

    /**
     * @param name
     *            dynamic parameter for the selector name
     * @param count
     *            dynamic parameter for the selector count
     */
    public AbstractContextAwareSelector(final DynamicParameter<String> name, final DynamicParameter<Integer> count) {
        this.countParam = count;
        if (this.countParam == null) {
            throw new NullPointerException("countParam");
        }
        this.nameParam = name;
        if (this.nameParam == null) {
            throw new NullPointerException("nameParam");
        }
    }

    /**
     *
     * @return the dynamic count parameter
     */
    public final DynamicParameter<Integer> getCountParam() {
        return countParam;
    }

    /**
     *
     * @return the dynamic name parameter
     */
    public final DynamicParameter<String> getNameParam() {
        return nameParam;
    }

}
