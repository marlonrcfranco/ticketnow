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



/**
 * Defines a Comparable Property in a XVSM Query.
 *
 * @author Martin Barisits
 * @author Tobias Doenz
 */
public class ComparableProperty extends Property {

    private static final long serialVersionUID = 1L;

    protected ComparableProperty(final String[] path, final Class<?> entryClass, final Class<?> propertyClass) {
        super(path, entryClass, propertyClass);
    }

    protected ComparableProperty(final String[] path, final Class<?> entryClass, final Class<?> propertyClass,
            final boolean returnSize, final boolean cached) {
        super(path, entryClass, propertyClass, returnSize, cached);
    }

    // for serialization
    private ComparableProperty() {
        super(null, null, null, false, false);
    }

    /**
     * Returns a comparable property identified by a name (specified as path).
     *
     * @param path
     *            to the property
     * @return the property
     */
    public static ComparableProperty forName(final String... path) {
        return new ComparableProperty(path, null, null);
    }

    /**
     * Returns a comparable property identified by a type (class) and path.
     *
     * @param entryClass
     *            type of the entries that should be matched
     * @param path
     *            to the property
     * @return the property
     */
    public static ComparableProperty forClass(final Class<?> entryClass, final String... path) {
        return new ComparableProperty(path, entryClass, null);
    }

    /**
     * Returns a Matchmaker which evaluates positive if the Property is greaterThan the value.
     *
     * @param value
     *            to evaluate
     * @return a Matchmaker
     */
    public final Matchmaker greaterThan(final Comparable<?> value) {
        return new GreaterThanMatchmaker(this, value);
    }

    /**
     * Returns a Matchmaker which evaluates positive if the Property is greaterThan the specified property value.
     *
     * @param property
     *            the Property to compare this Property to
     * @return a Matchmaker
     */
    public final Matchmaker greaterThan(final ComparableProperty property) {
        return new GreaterThanMatchmaker(this, property);
    }

    /**
     * Returns a Matchmaker which evaluates positive if the Property is greaterThan or equalTo the value.
     *
     * @param value
     *            to evalute
     * @return a Matchmaker
     */
    public final Matchmaker greaterThanOrEqualTo(final Comparable<?> value) {
        return new GreaterThanOrEqualToMatchmaker(this, value);
    }

    /**
     * Returns a Matchmaker which evaluates positive if the Property is greaterThan or equalTo the specified property
     * value.
     *
     * @param property
     *            the Property to compare this Property to
     * @return a Matchmaker
     */
    public final Matchmaker greaterThanOrEqualTo(final ComparableProperty property) {
        return new GreaterThanOrEqualToMatchmaker(this, property);
    }

    /**
     * Returns a Matchmaker which evaluates positive if the Propery is lessThan the value.
     *
     * @param value
     *            to evaluate
     * @return a Matchmaker
     */
    public final Matchmaker lessThan(final Comparable<?> value) {
        return new LessThanMatchmaker(this, value);
    }

    /**
     * Returns a Matchmaker which evaluates positive if the Propery is lessThan the specified property value.
     *
     * @param property
     *            the Property to compare this Property to
     * @return a Matchmaker
     */
    public final Matchmaker lessThan(final ComparableProperty property) {
        return new LessThanMatchmaker(this, property);
    }

    /**
     * Returns a Matchmaker which evaluates positive if the Property is lessthan or equal the value.
     *
     * @param value
     *            to evaluate
     * @return a Matchmaker
     */
    public final Matchmaker lessThanOrEqualTo(final Comparable<?> value) {
        return new LessThanOrEqualToMatchmaker(this, value);
    }

    /**
     * Returns a Matchmaker which evaluates positive if the Property is lessthan or equal the specified property value.
     *
     * @param property
     *            the Property to compare this Property to
     * @return a Matchmaker
     */
    public final Matchmaker lessThanOrEqualTo(final ComparableProperty property) {
        return new LessThanOrEqualToMatchmaker(this, property);
    }

    /**
     * Returns a Matchmaker which evaluates to true if the Property is between the given values (including the values).
     *
     * @param lowerBound
     *            the lower bound to check against (value >= lowerBound)
     * @param upperBound
     *            the upper bound to check against (value <= upperBound)
     * @return a Matchmaker
     */
    public final Matchmaker between(final Comparable<?> lowerBound, final Comparable<?> upperBound) {
        return new BetweenMatchmaker(this, lowerBound, upperBound);
    }

    /**
     * Returns a Matchmaker which evaluates to true if the string property matches the given regular expression.
     *
     * @param regex
     *            the pattern to match
     * @return a Matchmaker
     */
    public final Matchmaker matches(final String regex) {
        return new RegexMatchmaker(this, regex);
    }

    /**
     * Returns a Matchmaker which evaluates positive if all property values are greaterThan the value.
     *
     * @param value
     *            to evaluate
     * @return a Matchmaker
     */
    public final Matchmaker allGreaterThan(final Comparable<?> value) {
        return new AllGreaterThanMatchmaker(this, value);
    }

    /**
     * Returns a Matchmaker which evaluates positive if all property values are greaterThan the specified property
     * value.
     *
     * @param property
     *            the Property to compare this Property to
     * @return a Matchmaker
     */
    public final Matchmaker allGreaterThan(final ComparableProperty property) {
        return new AllGreaterThanMatchmaker(this, property);
    }

    /**
     * Returns a Matchmaker which evaluates positive if all property values are greaterThan or equalTo the value.
     *
     * @param value
     *            to evaluate
     * @return a Matchmaker
     */
    public final Matchmaker allGreaterThanOrEqualTo(final Comparable<?> value) {
        return new AllGreaterThanOrEqualToMatchmaker(this, value);
    }

    /**
     * Returns a Matchmaker which evaluates positive if all property values are greaterThan or equalTo the specified
     * property value.
     *
     * @param property
     *            the Property to compare this Property to
     * @return a Matchmaker
     */
    public final Matchmaker allGreaterThanOrEqualTo(final ComparableProperty property) {
        return new AllGreaterThanOrEqualToMatchmaker(this, property);
    }

    /**
     * Returns a Matchmaker which evaluates positive if all property values are lessThan the value.
     *
     * @param value
     *            to evaluate
     * @return a Matchmaker
     */
    public final Matchmaker allLessThan(final Comparable<?> value) {
        return new AllLessThanMatchmaker(this, value);
    }

    /**
     * Returns a Matchmaker which evaluates positive if all property values are lessThan the specified property value.
     *
     * @param property
     *            the Property to compare this Property to
     * @return a Matchmaker
     */
    public final Matchmaker allLessThan(final ComparableProperty property) {
        return new AllLessThanMatchmaker(this, property);
    }

    /**
     * Returns a Matchmaker which evaluates positive if all property values are lessthan or equal the value.
     *
     * @param value
     *            to evaluate
     * @return a Matchmaker
     */
    public final Matchmaker allLessThanOrEqualTo(final Comparable<?> value) {
        return new AllLessThanOrEqualToMatchmaker(this, value);
    }

    /**
     * Returns a Matchmaker which evaluates positive if all property values are lessthan or equal the specified property
     * value.
     *
     * @param property
     *            the Property to compare this Property to
     * @return a Matchmaker
     */
    public final Matchmaker allLessThanOrEqualTo(final ComparableProperty property) {
        return new AllLessThanOrEqualToMatchmaker(this, property);
    }

    /**
     * The GreaterThanMatchmaker evaluates to true if the Property is greater than the value.
     *
     * @author Martin Barisits, Martin Planer
     */
    public static final class GreaterThanMatchmaker extends AbstractValuePropertyMatchmaker {

        private static final long serialVersionUID = 1L;

        private final ComparableProperty property;
        private final Comparable<?> value;

        /**
         * @return the property
         */
        @Override
        public ComparableProperty getProperty() {
            return property;
        }

        /**
         * @return the value
         */
        @Override
        public Comparable<?> getValue() {
            return value;
        }

        private GreaterThanMatchmaker(final ComparableProperty property, final Comparable<?> value) {
            super(null);
            this.property = property;
            this.value = value;
        }

        private GreaterThanMatchmaker(final ComparableProperty property, final ComparableProperty valueProperty) {
            super(valueProperty);
            this.property = property;
            this.value = null;
        }

        @Override
        public String toString() {
            return "GreaterThanMatchmaker [property=" + property + ", value=" + value + ", valueProperty="
                    + getValueProperty() + "]";
        }

    }

    /**
     * The GreaterThanOrEqualToMatchmaker evaluates to true if the Property is greater than or equal the value.
     *
     * @author Martin Barisits, Martin Planer
     */
    public static final class GreaterThanOrEqualToMatchmaker extends AbstractValuePropertyMatchmaker {

        private static final long serialVersionUID = 1L;

        private final ComparableProperty property;
        private final Comparable<?> value;

        /**
         * @return the property
         */
        @Override
        public ComparableProperty getProperty() {
            return property;
        }

        /**
         * @return the value
         */
        @Override
        public Comparable<?> getValue() {
            return value;
        }

        private GreaterThanOrEqualToMatchmaker(final ComparableProperty property, final Comparable<?> value) {
            super(null);
            this.property = property;
            this.value = value;
        }

        private GreaterThanOrEqualToMatchmaker(final ComparableProperty property,
                final ComparableProperty valueProperty) {
            super(valueProperty);
            this.property = property;
            this.value = null;
        }

        @Override
        public String toString() {
            return "GreaterThanOrEqualToMatchmaker [property=" + property + ", value=" + value
                    + ", getValueProperty()=" + getValueProperty() + "]";
        }

    }

    /**
     * The LessThanMatchmaker evaluates to true if the Property is less than the value.
     *
     * @author Martin Barisits, Martin Planer
     */
    public static final class LessThanMatchmaker extends AbstractValuePropertyMatchmaker {

        private static final long serialVersionUID = 1L;

        private final ComparableProperty property;
        private final Comparable<?> value;

        /**
         * @return the property
         */
        @Override
        public ComparableProperty getProperty() {
            return property;
        }

        /**
         * @return the value
         */
        @Override
        public Comparable<?> getValue() {
            return value;
        }

        private LessThanMatchmaker(final ComparableProperty property, final Comparable<?> value) {
            super(null);
            this.property = property;
            this.value = value;
        }

        private LessThanMatchmaker(final ComparableProperty property, final ComparableProperty valueProperty) {
            super(valueProperty);
            this.property = property;
            this.value = null;
        }

        @Override
        public String toString() {
            return "LessThanMatchmaker [property=" + property + ", value=" + value + ", getValueProperty()="
                    + getValueProperty() + "]";
        }

    }

    /**
     * The LessThanOrEqualToMatchmaker evaluates to true if the Property is less than or equal to the value.
     *
     * @author Martin Barisits, Martin Planer
     */
    public static final class LessThanOrEqualToMatchmaker extends AbstractValuePropertyMatchmaker {

        private static final long serialVersionUID = 1L;

        private final ComparableProperty property;
        private final Comparable<?> value;

        /**
         * @return the property
         */
        @Override
        public ComparableProperty getProperty() {
            return property;
        }

        /**
         * @return the value
         */
        @Override
        public Comparable<?> getValue() {
            return value;
        }

        private LessThanOrEqualToMatchmaker(final ComparableProperty property, final Comparable<?> value) {
            super(null);
            this.property = property;
            this.value = value;
        }

        private LessThanOrEqualToMatchmaker(final ComparableProperty property, final ComparableProperty valueProperty) {
            super(valueProperty);
            this.property = property;
            this.value = null;
        }

        @Override
        public String toString() {
            return "LessThanOrEqualToMatchmaker [property=" + property + ", value=" + value + ", getValueProperty()="
                    + getValueProperty() + "]";
        }

    }

    /**
     * The BetweenMatchmaker evaluates to true if the Property is between the given values (including the values).
     *
     * @author Martin Planer
     */
    public static final class BetweenMatchmaker implements Matchmaker {

        private static final long serialVersionUID = 1L;

        private final ComparableProperty property;
        private final Comparable<?> lowerBound;
        private final Comparable<?> upperBound;

        /**
         * @param property
         *            the property
         * @param lowerBound
         *            the lower bound
         * @param upperBound
         *            the upper bound
         */
        public BetweenMatchmaker(final ComparableProperty property, final Comparable<?> lowerBound,
                final Comparable<?> upperBound) {
            this.property = property;
            this.lowerBound = lowerBound;
            this.upperBound = upperBound;
        }

        /**
         * @return the property
         */
        public ComparableProperty getProperty() {
            return property;
        }

        /**
         * @return the lowerBound
         */
        public Comparable<?> getLowerBound() {
            return lowerBound;
        }

        /**
         * @return the upperBound
         */
        public Comparable<?> getUpperBound() {
            return upperBound;
        }

        @Override
        public String toString() {
            return "BetweenMatchmaker [property=" + property + ", lowerBound=" + lowerBound + ", upperBound="
                    + upperBound + "]";
        }

    }

    /**
     * The RegexMatchmaker evaluates to true if the string property matches the given regular expression.
     *
     * @author Martin Planer
     */
    public static final class RegexMatchmaker implements Matchmaker {

        private static final long serialVersionUID = 1L;

        private final ComparableProperty property;
        private final String regex;

        /**
         * @param property
         *            the property
         * @param regex
         *            the regular expression
         */
        public RegexMatchmaker(final ComparableProperty property, final String regex) {
            this.property = property;
            this.regex = regex;
        }

        /**
         * @return the property
         */
        public ComparableProperty getProperty() {
            return property;
        }

        /**
         * @return the value of the regex pattern string
         */
        public String getValue() {
            return regex;
        }

        @Override
        public String toString() {
            return "RegexMatchmaker [property=" + property + ", regex=" + regex + "]";
        }

    }

    /**
     * The GreaterThanMatchmaker evaluates to true if the Property is greater than the value.
     *
     * @author Martin Planer
     */
    public static final class AllGreaterThanMatchmaker extends AbstractValuePropertyMatchmaker {

        private static final long serialVersionUID = 1L;

        private final ComparableProperty property;
        private final Comparable<?> value;

        /**
         * @return the property
         */
        @Override
        public ComparableProperty getProperty() {
            return property;
        }

        /**
         * @return the value
         */
        @Override
        public Comparable<?> getValue() {
            return value;
        }

        private AllGreaterThanMatchmaker(final ComparableProperty property, final Comparable<?> value) {
            super(null);
            this.property = property;
            this.value = value;
        }

        private AllGreaterThanMatchmaker(final ComparableProperty property, final ComparableProperty valueProperty) {
            super(valueProperty);
            this.property = property;
            this.value = null;
        }

        @Override
        public String toString() {
            return "AllGreaterThanMatchmaker [property=" + property + ", value=" + value + ", valueProperty="
                    + getValueProperty() + "]";
        }

    }

    /**
     * The GreaterThanOrEqualToMatchmaker evaluates to true if the Property is greater than or equal the value.
     *
     * @author Martin Planer
     */
    public static final class AllGreaterThanOrEqualToMatchmaker extends AbstractValuePropertyMatchmaker {

        private static final long serialVersionUID = 1L;

        private final ComparableProperty property;
        private final Comparable<?> value;

        /**
         * @return the property
         */
        @Override
        public ComparableProperty getProperty() {
            return property;
        }

        /**
         * @return the value
         */
        @Override
        public Comparable<?> getValue() {
            return value;
        }

        private AllGreaterThanOrEqualToMatchmaker(final ComparableProperty property, final Comparable<?> value) {
            super(null);
            this.property = property;
            this.value = value;
        }

        private AllGreaterThanOrEqualToMatchmaker(final ComparableProperty property,
                final ComparableProperty valueProperty) {
            super(valueProperty);
            this.property = property;
            this.value = null;
        }

        @Override
        public String toString() {
            return "AllGreaterThanOrEqualToMatchmaker [property=" + property + ", value=" + value
                    + ", getValueProperty()=" + getValueProperty() + "]";
        }

    }

    /**
     * The LessThanMatchmaker evaluates to true if the Property is less than the value.
     *
     * @author Martin Planer
     */
    public static final class AllLessThanMatchmaker extends AbstractValuePropertyMatchmaker {

        private static final long serialVersionUID = 1L;

        private final ComparableProperty property;
        private final Comparable<?> value;

        /**
         * @return the property
         */
        @Override
        public ComparableProperty getProperty() {
            return property;
        }

        /**
         * @return the value
         */
        @Override
        public Comparable<?> getValue() {
            return value;
        }

        private AllLessThanMatchmaker(final ComparableProperty property, final Comparable<?> value) {
            super(null);
            this.property = property;
            this.value = value;
        }

        private AllLessThanMatchmaker(final ComparableProperty property, final ComparableProperty valueProperty) {
            super(valueProperty);
            this.property = property;
            this.value = null;
        }

        @Override
        public String toString() {
            return "AllLessThanMatchmaker [property=" + property + ", value=" + value + ", getValueProperty()="
                    + getValueProperty() + "]";
        }

    }

    /**
     * The LessThanOrEqualToMatchmaker evaluates to true if the Property is less than or equal to the value.
     *
     * @author Martin Planer
     */
    public static final class AllLessThanOrEqualToMatchmaker extends AbstractValuePropertyMatchmaker {

        private static final long serialVersionUID = 1L;

        private final ComparableProperty property;
        private final Comparable<?> value;

        /**
         * @return the property
         */
        @Override
        public ComparableProperty getProperty() {
            return property;
        }

        /**
         * @return the value
         */
        @Override
        public Comparable<?> getValue() {
            return value;
        }

        private AllLessThanOrEqualToMatchmaker(final ComparableProperty property, final Comparable<?> value) {
            super(null);
            this.property = property;
            this.value = value;
        }

        private AllLessThanOrEqualToMatchmaker(final ComparableProperty property,
                final ComparableProperty valueProperty) {
            super(valueProperty);
            this.property = property;
            this.value = null;
        }

        @Override
        public String toString() {
            return "AllLessThanOrEqualToMatchmaker [property=" + property + ", value=" + value
                    + ", getValueProperty()=" + getValueProperty() + "]";
        }

    }
}
