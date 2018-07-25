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
package org.mozartspaces.capi3.javanative.coordination.query;

import java.io.Serializable;

import org.mozartspaces.capi3.ComparableProperty.RegexMatchmaker;
import org.mozartspaces.capi3.javanative.coordination.DefaultQueryCoordinator;
import org.mozartspaces.capi3.javanative.coordination.query.NativeProperty.NoPathMatch;

/**
 * The DefaultRegexMatchmaker evaluates to true if the string property matches the given regular expression.
 *
 * @author Martin Planer
 */
public final class DefaultRegexMatchmaker implements NativeMatchmaker {

    private final NativeProperty property;
    private final String regex;

    /**
     * Create a new RegexMatchmaker.
     *
     * @param matchmaker
     *            to base on
     * @param filter
     */
    public DefaultRegexMatchmaker(final RegexMatchmaker matchmaker, final NativeFilter filter) {
        DefaultQueryCoordinator coordinator = filter.getQuery().getSelector().getCoordinator();
        this.property = PropertyFactory.createProperty(matchmaker.getProperty(),
                coordinator.getPropertyValueCache());
        this.regex = matchmaker.getValue();
    }

    @Override
    public boolean evaluate(final Serializable entry) {
        Object propertyValue = (this.property).getValue(entry);

        if (propertyValue == null || regex == null) {
            return false;
        }

        if (propertyValue.getClass().equals(NoPathMatch.class)) {
            return false;
        }

        if (String.class.isAssignableFrom(propertyValue.getClass())) {
            return ((String) propertyValue).matches(regex);
        }

        return false;
    }

    @Override
    public String toString() {
        return property + " MATCHES " + regex;
    }

}
