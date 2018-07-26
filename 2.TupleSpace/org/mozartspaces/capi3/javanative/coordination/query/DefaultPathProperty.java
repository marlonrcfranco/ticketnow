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
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.mozartspaces.capi3.Index;
import org.mozartspaces.capi3.Property;

/**
 * Defines a Property in an XVSM Query.
 *
 * @author Martin Barisits
 * @author Tobias Doenz
 * @author Martin Planer
 */
public final class DefaultPathProperty implements NativeProperty {

    private static final Pattern LIST_INDEX_PATTERN = Pattern.compile("^\\[(\\d+)\\]$");
    private static final Pattern SUBLIST_PATTERN = Pattern.compile("^\\[(\\d+)\\.\\.(\\d+)\\]$");

    @SuppressWarnings("unchecked")
    private static final Set<Class<?>> WRAPPER_TYPES = new HashSet<Class<?>>(Arrays.asList(Boolean.class,
            Character.class, Byte.class, Short.class, Integer.class, Long.class, Float.class, Double.class,
            Void.class));

    private final String[] path;
    private final Class<?> entryClass;
    private final Class<?> propertyClass;

    private final boolean returnSize;

    private final HashSet<Object> visited = new HashSet<Object>();

    /**
     * Creates a NativeProperty.
     *
     * @param property
     *            to base the NativeProperty on
     */
    public DefaultPathProperty(final Property property) {
        this.path = property.getPath();
        this.entryClass = property.getEntryClazz();
        this.propertyClass = property.getPropertyClass();

        this.returnSize = property.isReturnSize();
    }

    /**
     * Evaluate if a given path matches a field.
     *
     * @param field
     *            to be evaluated
     * @param remainingPath
     *            to match against
     * @return true if the path matches the field, false otherwise
     */
    private boolean pathMatchesField(final Field field, final List<String> remainingPath) {
        if (remainingPath.get(0).equals(Property.PATH_WILDCARD)) {
            return true;
        }
        Index indexAnnotation = field.getAnnotation(Index.class);
        if (indexAnnotation != null) {
            if (indexAnnotation.label().equals(remainingPath.get(0))) {
                return true;
            }
        }
        if (field.getName().equals(remainingPath.get(0))) {
            return true;
        }
        return false;
    }

    /**
     * Investigate an Object recursively and return the path-matching object.
     *
     * @param object
     *            to investigate
     * @param remainingPath
     *            to match
     * @return the matching object or <code>NoMatch</code> otherwise
     * @throws IllegalAccessException
     * @throws IllegalArgumentException
     */
    private Object investigateObject(final Object object, final List<String> remainingPath)
            throws IllegalAccessException {

        if (remainingPath.isEmpty()) {
            return object;
        } else if (object == null) {
            return NoPathMatch.INSTANCE;
        }

        String firstPathElement = remainingPath.get(0);

        // Cycle detection against circular references when deep traversing object
        if (firstPathElement.equals(Property.PATH_DEEPWILDCARD) && !visited.add(object)) {
            return NoPathMatch.INSTANCE;
        }

        // Special List/Collection return values
        List<String> nextPath = remainingPath.subList(1, remainingPath.size());
        if (firstPathElement.startsWith("[")) {
            Object atIndex = atIndex(object, firstPathElement);
            if(atIndex != null) {
                return investigateObject(atIndex, nextPath);
            }
            
            List<?> subList = subList(object, firstPathElement);
            if (subList == null) {
                return NoPathMatch.INSTANCE;
            } else {
                return investigateObject(subList, nextPath);
            }
        }

        if (Collection.class.isAssignableFrom(object.getClass())) {
            ArrayList<Object> results = new ResultList();
            /* This is a Sequence and has to be evaluated accordingly */
            for (Object item : (Collection<?>) object) {

                // TODO Check if this was an - until now - undiscovered bug!?
                // List<String> nextPath = remainingPath.subList(1, remainingPath.size());
                // Object result = this.investigateObject(item, nextPath);

                Object result = this.investigateObject(item, remainingPath);
                if (result != NoPathMatch.INSTANCE) {
                    if (result.getClass().equals(ResultList.class)) {
                        results.addAll((ResultList) result);
                    } else {
                        results.add(result);
                    }
                }
            }
            if (results.isEmpty()) {
                return NoPathMatch.INSTANCE;
            } else {
                return results;
            }
        }

        ArrayList<Object> results = new ResultList();
        for (Field field : ReflectionUtils.getDeclaredFieldsIncludingSuperclasses(object.getClass())) {
            if (!field.isAccessible()) {
                field.setAccessible(true);
            }
            if (this.pathMatchesField(field, remainingPath)) {
                results.add(this.investigateObject(field.get(object), remainingPath.subList(1, remainingPath.size())));
            } else if (firstPathElement.equals(Property.PATH_DEEPWILDCARD) && remainingPath.size() >= 2) {

                Object fieldObject = field.get(object);
                if (!isPrimitiveOrWrapper(fieldObject)) {
                    // add recursive deep path resolving to result
                    Object result = this.investigateObject(fieldObject, remainingPath);
                    if (result != NoPathMatch.INSTANCE) {
                        if (result.getClass().equals(ResultList.class)) {
                            results.addAll((ResultList) result);
                        } else {
                            results.add(result);
                        }
                    }
                }

                // check if path after deep wildcard matches
                if (this.pathMatchesField(field, remainingPath.subList(1, remainingPath.size()))) {
                    results.add(this.investigateObject(field.get(object),
                            remainingPath.subList(2, remainingPath.size())));
                }
            }

        }

        if (results.isEmpty()) {
            return NoPathMatch.INSTANCE;
        }
        if (results.size() == 1) {
            return results.get(0);
        }
        return results;
    }

    private boolean isPrimitiveOrWrapper(final Object object) {

        if (object == null) {
            return false;
        }

        Class<?> clazz = object.getClass();
        return clazz.isPrimitive() || WRAPPER_TYPES.contains(clazz);
    }

    @Override
    public Object getValue(final Serializable object) {

        // check type if set in property
        if (this.entryClass != null) {
            if (!this.entryClass.isInstance(object)) {
                return NoPathMatch.INSTANCE;
            }
        }

        // create list this way, the one returned by Arrays.asList does not support "remove"
        List<String> currentPath = new ArrayList<String>();
        for (String label : path) {
            currentPath.add(label);
        }

        // ignore wildcard at the path beginning
        if (currentPath.size() > 0 && currentPath.get(0).equals(Property.PATH_WILDCARD)) {
            currentPath.remove(0);
        }

        // ignore THIS operator at the path beginning
        if (currentPath.size() > 0 && currentPath.get(0).equals(Property.PATH_THIS)) {
            currentPath.remove(0);
        }

        try {
            Object value = this.investigateObject(object, currentPath);

            if (isReturnSize()) {
                value = getSize(value);
            }

            if (!checkType(value, this.propertyClass)) {
                return NoPathMatch.INSTANCE;
            }

            return value;

        } catch (IllegalAccessException e) {
            return NoPathMatch.INSTANCE;
        }
    }

    private boolean checkType(final Object object, final Class<?> clazz) {

        if (clazz == null) {
            return true;
        }

        return clazz.isInstance(object);
    }

    private Object atIndex(final Object object, final String pathElement) {

        if (!List.class.isAssignableFrom(object.getClass())) {
            return null;
        }

        List<?> list = (List<?>) object;

        Matcher indexMatcher = LIST_INDEX_PATTERN.matcher(pathElement);

        if (indexMatcher.matches()) {
            int index = Integer.valueOf(indexMatcher.group(1));
            return (index < list.size()) ? list.get(index) : null;
        }

        return null;
    }

    private List<?> subList(final Object object, final String pathElement) {

        if (!List.class.isAssignableFrom(object.getClass())) {
            return null;
        }

        List<?> list = (List<?>) object;

        int lowerIndex = -1;
        int upperIndex = -1;

        Matcher subListMatcher = SUBLIST_PATTERN.matcher(pathElement);

        if (subListMatcher.matches()) {
            lowerIndex = Integer.valueOf(subListMatcher.group(1));
            upperIndex = Integer.valueOf(subListMatcher.group(2));
        }

        if (lowerIndex < 0 || upperIndex < 0) {
            return null;
        }

        if (upperIndex > list.size() - 1) {
            return null;
        }
        // TODO Define semantics if list is not long enough
        // upperIndex = list.size() - 1;

        if (upperIndex < lowerIndex) {
            return null;
        }

        return list.subList(lowerIndex, upperIndex + 1);
    }

    private Object getSize(final Object object) {
        if (Collection.class.isAssignableFrom(object.getClass())) {
            return ((Collection<?>) object).size();
        } else {
            return 1;
        }
    }

    @Override
    public String toString() {
        return "PROPERTY (path=" + Arrays.toString(path) + ", clazz=" + entryClass + ")";
    }

    private static final class ResultList extends ArrayList<Object> {
        private static final long serialVersionUID = 1L;
    };

    @Override
    public boolean equals(final Object obj) {

        if (obj == null) {
            return false;
        }
        if (this == obj) {
            return true;
        }
        if (obj.getClass() != this.getClass()) {
            return false;
        }

        DefaultPathProperty p = (DefaultPathProperty) obj;

        if (path == null) {
            if (p.getPath() != null) {
                return false;
            }
        } else {
            if (!Arrays.equals(path, p.getPath())) {
                return false;
            }
        }

        if (entryClass == null) {
            if (p.getEntryClass() != null) {
                return false;
            }
        } else {
            if (entryClass.equals(p.getEntryClass()) == false) {
                return false;
            }
        }

        if (propertyClass == null) {
            if (p.getPropertyClass() != null) {
                return false;
            }
        } else {
            if (propertyClass.equals(p.getPropertyClass()) == false) {
                return false;
            }
        }

        return isReturnSize() == p.isReturnSize();
    }

    @Override
    public int hashCode() {
        int hashCode = 17;
        int hashMultiplier = 59;

        hashCode = hashCode * hashMultiplier + ((path == null) ? 0 : Arrays.hashCode(path));
        hashCode = hashCode * hashMultiplier + ((entryClass == null) ? 0 : entryClass.hashCode());
        hashCode = hashCode * hashMultiplier + ((propertyClass == null) ? 0 : propertyClass.hashCode());
        hashCode = hashCode * hashMultiplier + ((returnSize) ? 1 : 0);

        return hashCode;
    }

    /**
     * @return the path
     */
    public String[] getPath() {
        return path;
    }

    /**
     * @return the entryClass
     */
    public Class<?> getEntryClass() {
        return entryClass;
    }

    /**
     * @return the propertyClass
     */
    public Class<?> getPropertyClass() {
        return propertyClass;
    }

    /**
     * @return the returnSize
     */
    public boolean isReturnSize() {
        return returnSize;
    }

}
