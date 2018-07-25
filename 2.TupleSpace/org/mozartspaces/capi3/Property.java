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
package org.mozartspaces.capi3;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

/**
 * Defines a Property in an XVSM Query.
 *
 * @author Martin Barisits
 * @author Tobias Doenz
 * @author Martin Planer
 */
public class Property implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * Special property path that refers to the entry object itself and can be used to evaluate its value in a query.
     */
    public static final String PATH_THIS = "#this";

    /**
     * Special property path that matches against any field at its position in the path.
     */
    public static final String PATH_WILDCARD = "*";

    /**
     * Special property path that matches against any number of nested fields in the path.
     */
    public static final String PATH_DEEPWILDCARD = "**";

    private final String[] path;
    private final Class<?> entryClass;
    private final Class<?> propertyClass;
    private final boolean returnSize;
    private final boolean cached;

    protected Property(final String[] path, final Class<?> entryClass, final Class<?> propertyClass) {
        this(path, entryClass, propertyClass, false, false);
    }

    protected Property(final String[] path, final Class<?> entryClass, final Class<?> propertyClass,
            final boolean returnSize, final boolean cached) {
        this.path = path;
        this.entryClass = entryClass;
        this.propertyClass = propertyClass;
        this.returnSize = returnSize;
        this.cached = cached;
    }

    // for serialization
    @SuppressWarnings("unused")
    private Property() {
        this.path = null;
        this.entryClass = null;
        this.propertyClass = null;
        this.returnSize = false;
        this.cached = false;
    }

    /**
     * @return the path to the Property
     */
    public final String[] getPath() {
        return path;
    }

    /**
     * @return the Class of the Entry
     */
    public final Class<?> getEntryClazz() {
        return entryClass;
    }

    /**
     * @return the Class of the Property
     */
    public final Class<?> getPropertyClass() {
        return propertyClass;
    }

    /**
     * Returns if caching has been enabled for this Property.
     *
     * @return <code>true</code> if caching has been enabled for this Property, <code>false</code> otherwise
     */
    public final boolean isCached() {
        return cached;
    }

    /**
     * Returns if this Property only returns the size of the matched value.
     *
     * @return <code>true</code> or <code>false</code>
     */
    public final boolean isReturnSize() {
        return returnSize;
    }

    @Override
    public final String toString() {
        return "Property [path=" + Arrays.toString(path) + ", entryClass=" + entryClass + ", propertyClass="
                + propertyClass + ", cached=" + cached + "]";
    }

    /**
     * Returns a Property identified by a path.
     *
     * @param path
     *            to the Property
     * @return the Property
     */
    public static Property forName(final String... path) {
        return new Property(path, null, null);
    }

    /**
     * Returns a Property on an Entry with the defined type and identified by a path.
     *
     * @param clazz
     *            of the Entry containing the Property
     * @param path
     *            to the property
     * @return the Property
     */
    public static Property forClass(final Class<?> clazz, final String... path) {
        return new Property(path, clazz, null);
    }

    /**
     * Creates a property builder.
     *
     * @param path
     *            to the property
     * @return the property builder
     */
    public static Builder withName(final String... path) {
        return new Builder(path);
    }

    /**
     * Builder for a property.
     */
    public static final class Builder {

        private final String[] path;

        private Class<?> entryClass;
        private Class<?> propertyClass;
        private boolean returnSize;
        private boolean cached;

        private Builder(final String... path) {
            this.path = path;
        }

        /**
         * Sets the entry class type.
         *
         * @param entryClass
         *            the entry class
         * @return the builder
         */
        public Builder entryClass(final Class<?> entryClass) {
            this.entryClass = entryClass;
            return this;
        }

        /**
         * Sets the property class type.
         *
         * @param propertyClass
         *            the property class
         * @return the builder
         */
        public Builder propertyClass(final Class<?> propertyClass) {
            this.propertyClass = propertyClass;
            return this;
        }

        /**
         * Sets that "only" the size of the property is returned.
         *
         * @return the builder
         */
        public Builder returnSize() {
            this.returnSize = true;
            return this;
        }

        /**
         * Enables caching for this property. <br />
         * If caching is enabled, the resolved values of this property will be cached for the entries in the queried
         * container and will also be retrieved from the cache, if a corresponding cache entry exists. This can
         * accelerate queries that query for a specific property very often but also consumes some amount of memory.
         *
         * @return the builder
         */
        public Builder enableCache() {
            this.cached = true;
            return this;
        }

        /**
         * Creates a new property.
         *
         * @return the property
         */
        public Property build() {
            return new Property(path, entryClass, propertyClass, returnSize, cached);
        }

        /**
         * Creates a new comparable property.
         *
         * @return the comparable property
         */
        public ComparableProperty buildComparable() {
            return new ComparableProperty(path, entryClass, propertyClass, returnSize, cached);
        }

    }

    /**
     * Returns a Matchmaker for this Property, which evaluates positive if the Property exists.
     *
     * @return a Matchmaker
     */
    public final Matchmaker exists() {
        return new ExistsMatchmaker(this);
    }

    /**
     * Returns a Matchmaker for this Property, which evaluates positive if it is equal to the specified object.
     *
     * @param value
     *            to evaluate
     * @return a Matchmaker
     */
    public final Matchmaker equalTo(final Object value) {
        return new EqualMatchmaker(this, value);
    }

    /**
     * Returns a Matchmaker for this Property, which evaluates positive if it is equal to the specified property value.
     *
     * @param property
     *            the Property to compare this Property to
     * @return a Matchmaker
     */
    public final Matchmaker equalTo(final Property property) {
        return new EqualMatchmaker(this, property);
    }

    /**
     * Returns a Matchmaker for this Property, which evaluates positive if all of its values are equal to the specified
     * object.
     *
     * @param value
     *            to evaluate
     * @return a Matchmaker
     */
    public final Matchmaker allEqualTo(final Object value) {
        return new AllEqualMatchmaker(this, value);
    }

    /**
     * Returns a Matchmaker for this Property, which evaluates positive if all of its values are equal to the specified
     * property value.
     *
     * @param property
     *            the Property to compare this Property to
     * @return a Matchmaker
     */
    public final Matchmaker allEqualTo(final Property property) {
        return new AllEqualMatchmaker(this, property);
    }

    /**
     * Returns a Matchmaker for this Property, which evaluates positive if it is not equal to the specified object.
     *
     * @param value
     *            to evaluate
     * @return a Matchmaker
     */
    public final Matchmaker notEqualTo(final Object value) {
        return new NotEqualMatchmaker(this, value);
    }

    /**
     * Returns a Matchmaker for this Property, which evaluates positive if it is not equal to the specified property
     * value.
     *
     * @param property
     *            the Property to compare this Property to
     * @return a Matchmaker
     */
    public final Matchmaker notEqualTo(final Property property) {
        return new NotEqualMatchmaker(this, property);
    }

    /**
     * Returns a Matchmaker for this Property, which evaluates positive if all of its values are not equal to the
     * specified object.
     *
     * @param value
     *            to evaluate
     * @return a Matchmaker
     */
    public final Matchmaker allNotEqualTo(final Object value) {
        return new AllNotEqualMatchmaker(this, value);
    }

    /**
     * Returns a Matchmaker for this Property, which evaluates positive if all of its values are not equal to the
     * specified property value.
     *
     * @param property
     *            the Property to compare this Property to
     * @return a Matchmaker
     */
    public final Matchmaker allNotEqualTo(final Property property) {
        return new AllNotEqualMatchmaker(this, property);
    }

    /**
     * Returns a Matchmaker for this Property, which evaluates to true if the Property is an element of the given set of
     * values.
     *
     * @param values
     *            to evaluate
     * @return a Matchmaker
     */
    public final Matchmaker elementOf(final Object... values) {
        return new ElementOfMatchmaker(this, (values == null) ? null : Arrays.asList(values));
    }

    /**
     * Returns a Matchmaker for this Property, which evaluates to true if the all of its values evaluate to true with
     * the given matchmaker.
     *
     * @param matchmaker
     *            the matchmaker to evaluate for all values
     * @return a Matchmaker
     */
    public final Matchmaker forAll(final Matchmaker matchmaker) {
        return new ForAllMatchmaker(this, matchmaker);
    }

    /**
     * The Exists Matchmaker evaluates to true if the Property exists.
     *
     * @author Martin Barisits
     */
    public static final class ExistsMatchmaker implements Matchmaker {

        private static final long serialVersionUID = 1L;

        private final Property property;

        /**
         * @return the property
         */
        public Property getProperty() {
            return property;
        }

        private ExistsMatchmaker(final Property property) {
            this.property = property;
        }

        @Override
        public String toString() {
            return "ExistsMatchmaker [property=" + property + "]";
        }
    }

    /**
     * The Equal Matchmaker evaluates to true if the Property equals the value.
     *
     * @author Martin Barisits
     * @author Martin Planer
     */
    public static final class EqualMatchmaker extends AbstractValuePropertyMatchmaker {

        private static final long serialVersionUID = 1L;

        private final Property property;
        private final Object value;

        /**
         * @return the property
         */
        @Override
        public Property getProperty() {
            return property;
        }

        /**
         * @return the value
         */
        @Override
        public Object getValue() {
            return value;
        }

        private EqualMatchmaker(final Property property, final Object value) {
            super(null);
            this.property = property;
            this.value = value;
        }

        private EqualMatchmaker(final Property property, final Property valueProperty) {
            super(valueProperty);
            this.property = property;
            this.value = null;
        }

        @Override
        public String toString() {
            return "EqualMatchmaker [property=" + property + ", value=" + value + ", valueProperty="
                    + getValueProperty() + "]";
        }

    }

    /**
     * The Equal Matchmaker evaluates to true if all Property values equal the (property) value.
     *
     * @author Martin Planer
     */
    public static final class AllEqualMatchmaker extends AbstractValuePropertyMatchmaker {

        private static final long serialVersionUID = 1L;

        private final Property property;
        private final Object value;

        /**
         * @return the property
         */
        @Override
        public Property getProperty() {
            return property;
        }

        /**
         * @return the value
         */
        @Override
        public Object getValue() {
            return value;
        }

        private AllEqualMatchmaker(final Property property, final Object value) {
            super(null);
            this.property = property;
            this.value = value;
        }

        private AllEqualMatchmaker(final Property property, final Property valueProperty) {
            super(valueProperty);
            this.property = property;
            this.value = null;
        }

        @Override
        public String toString() {
            return "AllEqualMatchmaker [property=" + property + ", value=" + value + ", valueProperty="
                    + getValueProperty() + "]";
        }
    }

    /**
     * The Not Equal Matchmaker evaluates to true if the Property does not equals the value.
     *
     * @author Martin Barisits
     * @author Martin Planer
     */
    public static final class NotEqualMatchmaker extends AbstractValuePropertyMatchmaker {

        private static final long serialVersionUID = 1L;

        private final Property property;
        private final Object value;

        /**
         * @return the property
         */
        @Override
        public Property getProperty() {
            return property;
        }

        /**
         * @return the value
         */
        @Override
        public Object getValue() {
            return value;
        }

        private NotEqualMatchmaker(final Property property, final Object value) {
            super(null);
            this.property = property;
            this.value = value;
        }

        private NotEqualMatchmaker(final Property property, final Property valueProperty) {
            super(valueProperty);
            this.property = property;
            this.value = null;
        }

        @Override
        public String toString() {
            return "NotEqualMatchmaker [property=" + property + ", value=" + value + ", getValueProperty()="
                    + getValueProperty() + "]";
        }

    }

    /**
     * The Not Equal Matchmaker evaluates to true if the Property does not equals the value.
     *
     * @author Martin Barisits
     * @author Martin Planer
     */
    public static final class AllNotEqualMatchmaker extends AbstractValuePropertyMatchmaker {

        private static final long serialVersionUID = 1L;

        private final Property property;
        private final Object value;

        /**
         * @return the property
         */
        @Override
        public Property getProperty() {
            return property;
        }

        /**
         * @return the value
         */
        @Override
        public Object getValue() {
            return value;
        }

        private AllNotEqualMatchmaker(final Property property, final Object value) {
            super(null);
            this.property = property;
            this.value = value;
        }

        private AllNotEqualMatchmaker(final Property property, final Property valueProperty) {
            super(valueProperty);
            this.property = property;
            this.value = null;
        }

        @Override
        public String toString() {
            return "AllNotEqualMatchmaker [property=" + property + ", value=" + value + ", getValueProperty()="
                    + getValueProperty() + "]";
        }

    }

    /**
     * The ElementOfMatchmaker evaluates to true if the Property is an element of the given set of values.
     *
     * @author Martin Planer
     */
    public static final class ElementOfMatchmaker implements Matchmaker {

        private static final long serialVersionUID = 1L;

        private final Property property;
        private final List<Object> values;

        /**
         * @return the property
         */
        public Property getProperty() {
            return property;
        }

        /**
         * @return the value
         */
        public List<Object> getValues() {
            return values;
        }

        private ElementOfMatchmaker(final Property property, final List<Object> values) {
            this.property = property;
            this.values = values;
        }

        @Override
        public String toString() {
            return "ElementOfMatchmaker [property=" + property + ", values=" + values.toString() + "]";
        }
    }

    /**
     * The ForAllMatchmaker evaluates to true if the all property values evaluate to true with the given matchmaker.
     *
     * @author Martin Planer
     */
    public static final class ForAllMatchmaker implements Matchmaker {

        private static final long serialVersionUID = 1L;

        private final Property property;
        private final Matchmaker matchmaker;

        private ForAllMatchmaker(final Property property, final Matchmaker matchmaker) {
            this.property = property;
            this.matchmaker = matchmaker;
        }

        /**
         * @return the property
         */
        public Property getProperty() {
            return property;
        }

        /**
         * @return the matchmaker
         */
        public Matchmaker getMatchmaker() {
            return matchmaker;
        }

        @Override
        public String toString() {
            return "ForAllMatchmaker [property=" + property + ", matchmaker=" + matchmaker + "]";
        }
    }

    /**
     * The ValuePropertyMatchmaker provides functionality to compare Properties against other Properties.
     *
     * @author Martin Planer
     */
    public abstract static class AbstractValuePropertyMatchmaker implements Matchmaker {

        private static final long serialVersionUID = 1L;

        private final Property valueProperty;

        protected AbstractValuePropertyMatchmaker(final Property valueProperty) {
            this.valueProperty = valueProperty;
        }

        /**
         * @return the value property
         */
        public final Property getValueProperty() {
            return valueProperty;
        }

        /**
         * Returns the value to be used in the matchmaker.
         *
         * @return the value
         */
        public abstract Object getValue();

        /**
         * Returns the property to be used in the matchmaker.
         *
         * @return the property
         */
        public abstract Property getProperty();
    }

}
