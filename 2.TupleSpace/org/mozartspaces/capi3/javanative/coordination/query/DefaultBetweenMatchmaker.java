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

import org.mozartspaces.capi3.ComparableProperty.BetweenMatchmaker;
import org.mozartspaces.capi3.javanative.coordination.DefaultQueryCoordinator;
import org.mozartspaces.capi3.javanative.coordination.query.NativeProperty.NoPathMatch;

/**
 * The DefaultGreaterThanMatchmaker evaluates to true if the Property is greater than the value.
 *
 * @author Martin Planer
 */
public final class DefaultBetweenMatchmaker implements NativeMatchmaker {

    private final NativeProperty property;
    private final Comparable<?> lowerBound;
    private final Comparable<?> upperBound;

    /**
     * Create a new EqualMatchmaker.
     *
     * @param matchmaker
     *            to base on
     * @param filter
     */
    public DefaultBetweenMatchmaker(final BetweenMatchmaker matchmaker, final NativeFilter filter) {
        DefaultQueryCoordinator coordinator = filter.getQuery().getSelector().getCoordinator();
        this.property = PropertyFactory.createProperty(matchmaker.getProperty(),
                coordinator.getPropertyValueCache());
        this.lowerBound = matchmaker.getLowerBound();
        this.upperBound = matchmaker.getUpperBound();
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean evaluate(final Serializable entry) {
        Object propertyValue = (this.property).getValue(entry);

        if (propertyValue == null) {
            return false;
        }

        // TODO check and define null semantics
        if (this.lowerBound == null || this.upperBound == null) {
            return false;
        }

        if (propertyValue.getClass().equals(NoPathMatch.class)) {
            return false;
        }

        // Check if bounds can be compared to propertyValue
        if (propertyValue.getClass().isAssignableFrom(this.lowerBound.getClass()) == false
                || propertyValue.getClass().isAssignableFrom(this.upperBound.getClass()) == false) {
            return false;
        }

        if ((Comparable.class.isAssignableFrom(propertyValue.getClass()))
                && (Comparable.class.isAssignableFrom(this.lowerBound.getClass()))
                && (Comparable.class.isAssignableFrom(this.upperBound.getClass()))) {

            int compareLower = (((Comparable<Object>) propertyValue).compareTo(this.lowerBound));
            int compareUpper = (((Comparable<Object>) propertyValue).compareTo(this.upperBound));

            if (compareLower >= 0 && compareUpper <= 0) {
                return true;
            }

        }

        return false;
    }

    @Override
    public String toString() {
        return lowerBound + " <= " + property + " <= " + upperBound;
    }

}
