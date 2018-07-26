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

import org.mozartspaces.capi3.Property;
import org.mozartspaces.capi3.Property.AbstractValuePropertyMatchmaker;
import org.mozartspaces.capi3.javanative.coordination.DefaultQueryCoordinator;

/**
 * TODO
 *
 * @author Martin Planer
 */
public abstract class AbstractDefaultValuePropertyMatchmaker implements NativeMatchmaker {

    protected final NativeProperty valueProperty;
    protected final NativeFilter filter;

    public AbstractDefaultValuePropertyMatchmaker(final AbstractValuePropertyMatchmaker matchmaker,
            final NativeFilter filter) {
        this.filter = filter;
        this.valueProperty = getValuePropertyOrNull(matchmaker);
    }

    /**
     * Returns either the valueProperty or <code>null</code>, if no valueProperty was given.
     *
     * @param matchmaker
     *            the original Matchmaker
     *
     * @return the valueProperty or <code>null</code>, if no valueProperty was given
     */
    protected final NativeProperty getValuePropertyOrNull(final AbstractValuePropertyMatchmaker matchmaker) {
        Property mmValueProperty = matchmaker.getValueProperty();
        DefaultQueryCoordinator coordinator = filter.getQuery().getSelector().getCoordinator();

        return (mmValueProperty == null) ? null : PropertyFactory.createProperty(mmValueProperty,
                coordinator.getPropertyValueCache());
    }

    /**
     * Returns either the value specified by the valueProperty or the value that was given to the property (if no
     * valueProperty was set).
     *
     * @param entry
     *            the entry to get the value of the valueProperty from
     * @param value
     *            the alternative value if no valueProperty was set
     * @return the value specified by the valueProperty or the value that was given to the property
     */
    protected final Object getCurrentValue(final Serializable entry, final Object value) {

        if (valueProperty == null) {
            return value;
        }
        return valueProperty.getValue(entry);
    }

}