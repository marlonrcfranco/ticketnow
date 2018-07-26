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
 * Provides basic arithmetic functions for Properties.
 *
 * @author Martin Planer
 */
public final class ArithmeticProperty extends ComparableProperty {

    private static final long serialVersionUID = 1L;

    private final ArithmeticOperation operation;

    private final Property property1;
    private final Property property2;
    private final Comparable<?> value2;
    private final Comparable<?> value1;

    ArithmeticProperty(final ArithmeticOperation operation, final Property property1, final Property property2) {
        super(null, null, null);
        this.operation = operation;
        this.property1 = property1;
        this.property2 = property2;

        this.value1 = null;
        this.value2 = null;
    }

    ArithmeticProperty(final ArithmeticOperation operation, final Property property, final Comparable<?> operand) {
        super(null, null, null);
        this.operation = operation;
        this.property1 = property;
        this.value2 = operand;

        this.value1 = null;
        this.property2 = null;
    }

    ArithmeticProperty(final ArithmeticOperation operation, final Comparable<?> operand, final Property property) {
        super(null, null, null);
        this.operation = operation;
        this.value1 = operand;
        this.property2 = property;

        this.property1 = null;
        this.value2 = null;
    }

    /**
     * @return the operation
     */
    public ArithmeticOperation getOperation() {
        return operation;
    }

    /**
     * @return the property1
     */
    public Property getProperty1() {
        return property1;
    }

    /**
     * @return the property2
     */
    public Property getProperty2() {
        return property2;
    }

    /**
     * @return the value1
     */
    public Comparable<?> getValue1() {
        return value1;
    }

    /**
     * @return the value2
     */
    public Comparable<?> getValue2() {
        return value2;
    }

    /**
     * The possible types of an arithmetic operations.
     */
    public static enum ArithmeticOperation {
        /**
         * Addition.
         */
        ADD,
        /**
         * Subtraction.
         */
        SUBTRACT,
        /**
         * Multiplication.
         */
        MULTIPLY,
        /**
         * Division.
         */
        DIVIDE,
        /**
         * Concatenation.
         */
        CONCAT
    }
}
