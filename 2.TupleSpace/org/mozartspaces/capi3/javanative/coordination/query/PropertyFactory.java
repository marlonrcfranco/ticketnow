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

import org.mozartspaces.capi3.ArithmeticProperty;
import org.mozartspaces.capi3.ArithmeticProperty.ArithmeticOperation;
import org.mozartspaces.capi3.Property;
import org.mozartspaces.capi3.javanative.coordination.query.cache.PropertyValueCache;

public class PropertyFactory {

    /**
     * Creates a new {@link NativeProperty} with the given {@link Property}. If a {@link PropertyValueCache} is given,
     * it will be used to look up property values.
     *
     * @param property
     *            the property parameters to copy
     * @param cache
     *            the PropertyValueCache to use, if any. Can be <code>null</code>.
     * @return
     */
    public static NativeProperty createProperty(final Property property, final PropertyValueCache cache) {

        if (property == null) {
            return null;
        }

        if (property.getClass().equals(ArithmeticProperty.class)) {
            return createDefaultArithmeticProperty((ArithmeticProperty) property, cache);
        }

        NativeProperty defaultProperty = new DefaultPathProperty(property);

        if (cache != null && property.isCached()) {
            return new DefaultCacheProperty(defaultProperty, cache);
        }

        return defaultProperty;
    }

    private static NativeProperty createDefaultArithmeticProperty(final ArithmeticProperty property,
            final PropertyValueCache cache) {

        ArithmeticOperation operation = property.getOperation();
        NativeProperty property1 = PropertyFactory.createProperty(property.getProperty1(), cache);
        NativeProperty property2 = PropertyFactory.createProperty(property.getProperty2(), cache);
        Comparable<?> value1 = property.getValue1();
        Comparable<?> value2 = property.getValue2();

        return new DefaultArithmeticProperty(operation, property1, property2, value1, value2);
    }
}
