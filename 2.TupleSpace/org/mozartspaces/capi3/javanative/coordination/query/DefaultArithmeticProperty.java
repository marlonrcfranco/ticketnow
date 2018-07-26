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
import java.util.ArrayList;
import java.util.List;

import org.mozartspaces.capi3.ArithmeticProperty.ArithmeticOperation;
import org.mozartspaces.util.LoggerFactory;
import org.slf4j.Logger;

public final class DefaultArithmeticProperty implements NativeProperty {

    private final ArithmeticOperation operation;
    private final NativeProperty property1;
    private final NativeProperty property2;
    private final Comparable<?> value2;
    private final Comparable<?> value1;

    private static final Logger log = LoggerFactory.get();

    private static final List<Class<?>> arithmethicTypes = new ArrayList<Class<?>>();

    static {
        // Character (?)
        arithmethicTypes.add(Byte.class);
        arithmethicTypes.add(Short.class);
        arithmethicTypes.add(Integer.class);
        arithmethicTypes.add(Long.class);
        arithmethicTypes.add(Float.class);
        arithmethicTypes.add(Double.class);
        // String
    }

    public DefaultArithmeticProperty(final ArithmeticOperation operation, final NativeProperty property1,
            final NativeProperty property2, final Comparable<?> value1, final Comparable<?> value2) {
        this.operation = operation;
        this.property1 = property1;
        this.property2 = property2;
        this.value1 = value1;
        this.value2 = value2;
    }

    @Override
    public Object getValue(final Serializable object) {

        Object arg1 = null;
        Object arg2 = null;

        if (property1 != null) {
            arg1 = property1.getValue(object);
        } else {
            arg1 = value1;
        }

        if (property2 != null) {
            arg2 = property2.getValue(object);
        } else {
            arg2 = value2;
        }

        try {
            switch (this.operation) {
            case ADD:
                return add(arg1, arg2);
            case SUBTRACT:
                return subtract(arg1, arg2);
            case MULTIPLY:
                return multiply(arg1, arg2);
            case DIVIDE:
                return divide(arg1, arg2);
            case CONCAT:
                return concat(arg1, arg2);
            default:
                throw new AssertionError("Unexpected operation " + this.operation);
            }
        } catch (IllegalArgumentException e) {
            log.warn("IllegalArgumentException in Arithmetic Expression");
        }

        return NoPathMatch.INSTANCE;
    }

    private Object add(final Object op1, final Object op2) {

        Class<?> resultType = getResultType(op1, op2);

        if (resultType == null) {
            throw new IllegalArgumentException();
        }

        if (resultType.equals(Character.class)) {
            return charValue(op1) + charValue(op2);
        }
        if (resultType.equals(Byte.class)) {
            return byteValue(op1) + byteValue(op2);
        }
        if (resultType.equals(Short.class)) {
            return shortValue(op1) + shortValue(op2);
        }
        if (resultType.equals(Integer.class)) {
            return intValue(op1) + intValue(op2);
        }
        if (resultType.equals(Long.class)) {
            return longValue(op1) + longValue(op2);
        }
        if (resultType.equals(Float.class)) {
            return floatValue(op1) + floatValue(op2);
        }
        if (resultType.equals(Double.class)) {
            return doubleValue(op1) + doubleValue(op2);
        }
        if (resultType.equals(String.class)) {
            return stringValue(op1) + stringValue(op2);
        }

        throw new IllegalArgumentException();
    }

    private Object subtract(final Object op1, final Object op2) {

        Class<?> resultType = getResultType(op1, op2);

        if (resultType == null) {
            throw new IllegalArgumentException();
        }

        if (resultType.equals(Character.class)) {
            return charValue(op1) - charValue(op2);
        }
        if (resultType.equals(Byte.class)) {
            return byteValue(op1) - byteValue(op2);
        }
        if (resultType.equals(Short.class)) {
            return shortValue(op1) - shortValue(op2);
        }
        if (resultType.equals(Integer.class)) {
            return intValue(op1) - intValue(op2);
        }
        if (resultType.equals(Long.class)) {
            return longValue(op1) - longValue(op2);
        }
        if (resultType.equals(Float.class)) {
            return floatValue(op1) - floatValue(op2);
        }
        if (resultType.equals(Double.class)) {
            return doubleValue(op1) - doubleValue(op2);
        }

        throw new IllegalArgumentException();
    }

    private Object multiply(final Object op1, final Object op2) {

        Class<?> resultType = getResultType(op1, op2);

        if (resultType == null) {
            throw new IllegalArgumentException();
        }

        if (resultType.equals(Character.class)) {
            return charValue(op1) * charValue(op2);
        }
        if (resultType.equals(Byte.class)) {
            return byteValue(op1) * byteValue(op2);
        }
        if (resultType.equals(Short.class)) {
            return shortValue(op1) * shortValue(op2);
        }
        if (resultType.equals(Integer.class)) {
            return intValue(op1) * intValue(op2);
        }
        if (resultType.equals(Long.class)) {
            return longValue(op1) * longValue(op2);
        }
        if (resultType.equals(Float.class)) {
            return floatValue(op1) * floatValue(op2);
        }
        if (resultType.equals(Double.class)) {
            return doubleValue(op1) * doubleValue(op2);
        }

        throw new IllegalArgumentException();
    }

    private Object divide(final Object op1, final Object op2) {

        Class<?> resultType = getResultType(op1, op2);

        if (resultType == null) {
            throw new IllegalArgumentException();
        }

        if (resultType.equals(Character.class)) {
            return charValue(op1) / charValue(op2);
        }
        if (resultType.equals(Byte.class)) {
            return byteValue(op1) / byteValue(op2);
        }
        if (resultType.equals(Short.class)) {
            return shortValue(op1) / shortValue(op2);
        }
        if (resultType.equals(Integer.class)) {
            return intValue(op1) / intValue(op2);
        }
        if (resultType.equals(Long.class)) {
            return longValue(op1) / longValue(op2);
        }
        if (resultType.equals(Float.class)) {
            return floatValue(op1) / floatValue(op2);
        }
        if (resultType.equals(Double.class)) {
            return doubleValue(op1) / doubleValue(op2);
        }

        throw new IllegalArgumentException();
    }

    private Object concat(final Object op1, final Object op2) {
        return stringValue(op1) + stringValue(op2);
    }

    private Class<?> getResultType(final Object o1, final Object o2) {

        if (o1 == null || o2 == null) {
            return null;
        }

        if (o1.getClass().equals(String.class) || o2.getClass().equals(String.class)) {
            return String.class;
        }

        int type1 = arithmethicTypes.indexOf(o1.getClass());
        int type2 = arithmethicTypes.indexOf(o2.getClass());

        if (type1 < 0 && o1.getClass().equals(Character.class) == false) {
            return null;
        }

        if (type2 < 0 && o2.getClass().equals(Character.class) == false) {
            return null;
        }

        return arithmethicTypes.get(Math.max(type1, type2));
    }

    private Character charValue(final Object o) {

        if (o.getClass().equals(Character.class)) {
            return (Character) o;
        }

        throw new IllegalArgumentException();
    }

    private Byte byteValue(final Object o) {

        if (o.getClass().equals(Character.class)) {
            return ((Integer) ((int) ((Character) o))).byteValue();
        // OLD VERSION: return ((Integer) Character.digit((Character) o, 10)).byteValue();
        }

        if (arithmethicTypes.contains(o.getClass())) {
            return ((Number) o).byteValue();
        }

        throw new IllegalArgumentException();
    }

    private Short shortValue(final Object o) {

        if (o.getClass().equals(Character.class)) {
            return ((Integer) ((int) ((Character) o))).shortValue();
        }

        if (arithmethicTypes.contains(o.getClass())) {
            return ((Number) o).shortValue();
        }

        throw new IllegalArgumentException();
    }

    private Integer intValue(final Object o) {

        if (o.getClass().equals(Character.class)) {
            return (((int) ((Character) o)));
        }

        if (arithmethicTypes.contains(o.getClass())) {
            return ((Number) o).intValue();
        }

        throw new IllegalArgumentException();
    }

    private Long longValue(final Object o) {

        if (o.getClass().equals(Character.class)) {
            return ((Integer) ((int) ((Character) o))).longValue();
        }

        if (arithmethicTypes.contains(o.getClass())) {
            return ((Number) o).longValue();
        }

        throw new IllegalArgumentException();
    }

    private Float floatValue(final Object o) {

        if (o.getClass().equals(Character.class)) {
            return ((Integer) ((int) ((Character) o))).floatValue();
        }

        if (arithmethicTypes.contains(o.getClass())) {
            return ((Number) o).floatValue();
        }

        throw new IllegalArgumentException();
    }

    private Double doubleValue(final Object o) {

        if (o.getClass().equals(Character.class)) {
            return ((Integer) ((int) ((Character) o))).doubleValue();
        }

        if (arithmethicTypes.contains(o.getClass())) {
            return ((Number) o).doubleValue();
        }

        throw new IllegalArgumentException();
    }

    private String stringValue(final Object o) {

        return o.toString();
    }

}
