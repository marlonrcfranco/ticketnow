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
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.mozartspaces.capi3.Selector;

/**
 * A scope query used inside of a scope.
 *
 * @author Stefan Crass
 * @author Tobias Doenz
 */
public final class ScopeQuery implements Serializable {

    private static final long serialVersionUID = 1L;

    private final List<ContextAwareSelector<?>> selectors;

    /**
     * @param selectors
     *            the selector chain that should be used to select the matching entries
     */
    public ScopeQuery(final List<? extends ContextAwareSelector<?>> selectors) {
        this.selectors = new ArrayList<ContextAwareSelector<?>>(selectors);
    }

    /**
     * @param selectors
     *            the selector chain that should be used to select the matching entries
     */
    public ScopeQuery(final ContextAwareSelector<?>... selectors) {
        this(Arrays.asList(selectors));
    }

    /**
     * @param selectors
     *            the selector chain that should be used to select the matching entries
     * @return the scope query
     */
    public static ScopeQuery withSimpleSelectors(final List<? extends Selector> selectors) {
        List<ContextAwareSelector<?>> caSelectors = new ArrayList<ContextAwareSelector<?>>();
        for (Selector selector : selectors) {
            caSelectors.add(new SimpleContextAwareSelector<Selector>(selector));
        }
        return new ScopeQuery(caSelectors);
    }

    /**
     * @param selectors
     *            the selector chain that should be used to select the matching entries
     * @return the scope query
     */
    public static ScopeQuery withSimpleSelectors(final Selector... selectors) {
        return ScopeQuery.withSimpleSelectors(Arrays.asList(selectors));
    }

    /**
     * @return the selector list
     */
    public List<ContextAwareSelector<?>> getSelectors() {
        return Collections.unmodifiableList(selectors);
    }

    @Override
    public String toString() {
        return "ScopeQuery [selectors=" + selectors + "]";
    }

}
