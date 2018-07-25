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

import org.mozartspaces.capi3.ArithmeticProperty.ArithmeticOperation;

/**
 * Helper methods for creating pre-defined arithmetic property operations.
 *
 * @author Martin Planer
 */
public final class Arithmetic {

    /**
     * Performs an addition (+) on the 2 given properties/values. The direct values or the values specified by the
     * properties have to be one of the following types: {@link Character}, {@link Byte}, {@link Short}, {@link Integer}
     * , {@link Long}, {@link Float}, {@link Double}.
     *
     * The resulting type is defined by the larger type of the 2 operands.
     *
     * This method also takes operands of the type {@link String}. The result will be the concatenated string form of
     * the operands.
     *
     * @param arg0
     *            the first property operand
     * @param arg1
     *            the second property operand
     * @return the operation result
     */
    public static ComparableProperty add(final Property arg0, final Property arg1) {
        return new ArithmeticProperty(ArithmeticOperation.ADD, arg0, arg1);
    }

    /**
     * Performs an addition (+) on the 2 given properties/values. The direct values or the values specified by the
     * properties have to be one of the following types: {@link Character}, {@link Byte}, {@link Short}, {@link Integer}
     * , {@link Long}, {@link Float}, {@link Double}.
     *
     * The resulting type is defined by the larger type of the 2 operands.
     *
     * This method also takes operands of the type {@link String}. The result will be the concatenated string form of
     * the operands.
     *
     * @param arg0
     *            the first property operand
     * @param arg1
     *            the second value operand
     * @return the operation result
     */
    public static ComparableProperty add(final Property arg0, final Comparable<?> arg1) {
        return new ArithmeticProperty(ArithmeticOperation.ADD, arg0, arg1);
    }

    /**
     * Performs an addition (+) on the 2 given properties/values. The direct values or the values specified by the
     * properties have to be one of the following types: {@link Character}, {@link Byte}, {@link Short}, {@link Integer}
     * , {@link Long}, {@link Float}, {@link Double}.
     *
     * The resulting type is defined by the larger type of the 2 operands.
     *
     * This method also takes operands of the type {@link String}. The result will be the concatenated string form of
     * the operands.
     *
     * @param arg0
     *            the first value operand
     * @param arg1
     *            the second property operand
     * @return the operation result
     */
    public static ComparableProperty add(final Comparable<?> arg0, final Property arg1) {
        return new ArithmeticProperty(ArithmeticOperation.ADD, arg0, arg1);
    }

    /**
     * Performs an subtraction (-) on the 2 given properties/values. The direct values or the values specified by the
     * properties have to be one of the following types: {@link Character}, {@link Byte}, {@link Short}, {@link Integer}
     * , {@link Long}, {@link Float}, {@link Double}.
     *
     * The resulting type is defined by the larger type of the 2 operands.
     *
     * @param arg0
     *            the first property operand
     * @param arg1
     *            the second property operand
     * @return the operation result
     */
    public static ComparableProperty subtract(final Property arg0, final Property arg1) {
        return new ArithmeticProperty(ArithmeticOperation.SUBTRACT, arg0, arg1);
    }

    /**
     * Performs an subtraction (-) on the 2 given properties/values. The direct values or the values specified by the
     * properties have to be one of the following types: {@link Character}, {@link Byte}, {@link Short}, {@link Integer}
     * , {@link Long}, {@link Float}, {@link Double}.
     *
     * The resulting type is defined by the larger type of the 2 operands.
     *
     * @param arg0
     *            the first property operand
     * @param arg1
     *            the second value operand
     * @return the operation result
     */
    public static ComparableProperty subtract(final Property arg0, final Comparable<?> arg1) {
        return new ArithmeticProperty(ArithmeticOperation.SUBTRACT, arg0, arg1);
    }

    /**
     * Performs an subtraction (-) on the 2 given properties/values. The direct values or the values specified by the
     * properties have to be one of the following types: {@link Character}, {@link Byte}, {@link Short}, {@link Integer}
     * , {@link Long}, {@link Float}, {@link Double}.
     *
     * The resulting type is defined by the larger type of the 2 operands.
     *
     * @param arg0
     *            the first value operand
     * @param arg1
     *            the second property operand
     * @return the operation result
     */
    public static ComparableProperty subtract(final Comparable<?> arg0, final Property arg1) {
        return new ArithmeticProperty(ArithmeticOperation.SUBTRACT, arg0, arg1);
    }

    /**
     * Performs an multiplication (*) on the 2 given properties/values. The direct values or the values specified by the
     * properties have to be one of the following types: {@link Character}, {@link Byte}, {@link Short}, {@link Integer}
     * , {@link Long}, {@link Float}, {@link Double}.
     *
     * The resulting type is defined by the larger type of the 2 operands.
     *
     * @param arg0
     *            the first property operand
     * @param arg1
     *            the second property operand
     * @return the operation result
     */
    public static ComparableProperty multiply(final Property arg0, final Property arg1) {
        return new ArithmeticProperty(ArithmeticOperation.MULTIPLY, arg0, arg1);
    }

    /**
     * Performs an multiplication (*) on the 2 given properties/values. The direct values or the values specified by the
     * properties have to be one of the following types: {@link Character}, {@link Byte}, {@link Short}, {@link Integer}
     * , {@link Long}, {@link Float}, {@link Double}.
     *
     * The resulting type is defined by the larger type of the 2 operands.
     *
     * @param arg0
     *            the first property operand
     * @param arg1
     *            the second value operand
     * @return the operation result
     */
    public static ComparableProperty multiply(final Property arg0, final Comparable<?> arg1) {
        return new ArithmeticProperty(ArithmeticOperation.MULTIPLY, arg0, arg1);
    }

    /**
     * Performs an multiplication (*) on the 2 given properties/values. The direct values or the values specified by the
     * properties have to be one of the following types: {@link Character}, {@link Byte}, {@link Short}, {@link Integer}
     * , {@link Long}, {@link Float}, {@link Double}.
     *
     * The resulting type is defined by the larger type of the 2 operands.
     *
     * @param arg0
     *            the first value operand
     * @param arg1
     *            the second property operand
     * @return the operation result
     */
    public static ComparableProperty multiply(final Comparable<?> arg0, final Property arg1) {
        return new ArithmeticProperty(ArithmeticOperation.MULTIPLY, arg0, arg1);
    }

    /**
     * Performs an division (/) on the 2 given properties/values. The direct values or the values specified by the
     * properties have to be one of the following types: {@link Character}, {@link Byte}, {@link Short}, {@link Integer}
     * , {@link Long}, {@link Float}, {@link Double}.
     *
     * The resulting type is defined by the larger type of the 2 operands.
     *
     * @param arg0
     *            the first property operand
     * @param arg1
     *            the second property operand
     * @return the operation result
     */
    public static ComparableProperty divide(final Property arg0, final Property arg1) {
        return new ArithmeticProperty(ArithmeticOperation.DIVIDE, arg0, arg1);
    }

    /**
     * Performs an division (/) on the 2 given properties/values. The direct values or the values specified by the
     * properties have to be one of the following types: {@link Character}, {@link Byte}, {@link Short}, {@link Integer}
     * , {@link Long}, {@link Float}, {@link Double}.
     *
     * The resulting type is defined by the larger type of the 2 operands.
     *
     * @param arg0
     *            the first property operand
     * @param arg1
     *            the second value operand
     * @return the operation result
     */
    public static ComparableProperty divide(final Property arg0, final Comparable<?> arg1) {
        return new ArithmeticProperty(ArithmeticOperation.DIVIDE, arg0, arg1);
    }

    /**
     * Performs an division (/) on the 2 given properties/values. The direct values or the values specified by the
     * properties have to be one of the following types: {@link Character}, {@link Byte}, {@link Short}, {@link Integer}
     * , {@link Long}, {@link Float}, {@link Double}.
     *
     * The resulting type is defined by the larger type of the 2 operands.
     *
     * @param arg0
     *            the first value operand
     * @param arg1
     *            the second property operand
     * @return the operation result
     */
    public static ComparableProperty divide(final Comparable<?> arg0, final Property arg1) {
        return new ArithmeticProperty(ArithmeticOperation.DIVIDE, arg0, arg1);
    }

    /**
     * Performs an concatenation on the string representation of the 2 given properties/values. The direct values or the
     * values specified by the properties have to support the toString() method.
     *
     * The resulting type is string.
     *
     * @param arg0
     *            the first property operand
     * @param arg1
     *            the second property operand
     * @return the operation result
     */
    public static ComparableProperty concat(final Property arg0, final Property arg1) {
        return new ArithmeticProperty(ArithmeticOperation.CONCAT, arg0, arg1);
    }

    /**
     * Performs an concatenation on the string representation of the 2 given properties/values. The direct values or the
     * values specified by the properties have to support the toString() method.
     *
     * The resulting type is string.
     *
     * @param arg0
     *            the first property operand
     * @param arg1
     *            the second value operand
     * @return the operation result
     */
    public static ComparableProperty concat(final Property arg0, final Comparable<?> arg1) {
        return new ArithmeticProperty(ArithmeticOperation.CONCAT, arg0, arg1);
    }

    /**
     * Performs an concatenation on the string representation of the 2 given properties/values. The direct values or the
     * values specified by the properties have to support the toString() method.
     *
     * The resulting type is string.
     *
     * @param arg0
     *            the first value operand
     * @param arg1
     *            the second property operand
     * @return the operation result
     */
    public static ComparableProperty concat(final Comparable<?> arg0, final Property arg1) {
        return new ArithmeticProperty(ArithmeticOperation.CONCAT, arg0, arg1);
    }

    private Arithmetic() {
    }
}