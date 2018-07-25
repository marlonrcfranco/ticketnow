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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.mozartspaces.capi3.LocalContainerReference;

/**
 * A condition query used inside of a condition.
 *
 * @author Stefan Crass
 */
public final class ConditionQuery implements Serializable {

    private static final long serialVersionUID = 1L;

    private final LocalContainerReference container;
    private final List<ContextAwareSelector<?>> selectors;

    /**
     * @param container
     *            the container on which the query shall be evaluated
     * @param selectors
     *            the selector chain that shall be used to determine if the condition is fulfilled
     */
    public ConditionQuery(final LocalContainerReference container,
            final List<? extends ContextAwareSelector<?>> selectors) {
        this.container = container;
        if (this.container == null) {
            throw new NullPointerException("container");
        }
        this.selectors = new ArrayList<ContextAwareSelector<?>>(selectors);
        if (this.selectors.isEmpty()) {
            throw new IllegalArgumentException("Empty selector list");
        }
    }

    /**
     * @return the target container reference
     */
    public LocalContainerReference getContainer() {
        return container;
    }

    /**
     * @return the selector list
     */
    public List<ContextAwareSelector<?>> getSelectors() {
        return Collections.unmodifiableList(selectors);
    }

    @Override
    public String toString() {
        return "ConditionQuery [container=" + container + ", selectors=" + selectors + "]";
    }

}
