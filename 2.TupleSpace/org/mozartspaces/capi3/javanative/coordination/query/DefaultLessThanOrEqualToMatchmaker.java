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

import org.mozartspaces.capi3.ComparableProperty.LessThanOrEqualToMatchmaker;
import org.mozartspaces.capi3.javanative.coordination.DefaultQueryCoordinator;
import org.mozartspaces.capi3.javanative.coordination.query.NativeProperty.NoPathMatch;

/**
 * The DefaultLessThanOrEqualToMatchmaker evaluates to true if the Property is less than or equal to the value.
 *
 * @author Martin Barisits
 */
public final class DefaultLessThanOrEqualToMatchmaker extends AbstractDefaultValuePropertyMatchmaker {

    private final NativeProperty property;
    private final Comparable<?> value;

    /**
     * Create a new EqualMatchmaker.
     *
     * @param matchmaker
     *            to base on
     * @param filter
     */
    public DefaultLessThanOrEqualToMatchmaker(final LessThanOrEqualToMatchmaker matchmaker, final NativeFilter filter) {
        super(matchmaker, filter);
        DefaultQueryCoordinator coordinator = filter.getQuery().getSelector().getCoordinator();
        this.property = PropertyFactory.createProperty(matchmaker.getProperty(),
                coordinator.getPropertyValueCache());
        this.value = matchmaker.getValue();
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean evaluate(final Serializable entry) {
        Object propertyValue = (this.property).getValue(entry);
        Object currentValue = getCurrentValue(entry, this.value);

        if (propertyValue == null) {
            return false;
        }
        if (propertyValue.getClass().equals(NoPathMatch.class)) {
            return false;
        }
        if ((Comparable.class.isAssignableFrom(propertyValue.getClass()))
                && (Comparable.class.isAssignableFrom(currentValue.getClass()))
                && propertyValue.getClass().equals(currentValue.getClass())) {
            int compare = (((Comparable<Object>) propertyValue).compareTo(currentValue));
            if (compare <= 0) {
                return true;
            }

        }
        return false;
    }

    @Override
    public String toString() {
        if (valueProperty == null) {
            return property + " <= " + value;
        }

        return property + " <= " + valueProperty;
    }

}
