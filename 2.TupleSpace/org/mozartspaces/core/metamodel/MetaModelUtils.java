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
package org.mozartspaces.core.metamodel;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import org.mozartspaces.util.AndroidHelperUtils;
import org.mozartspaces.util.LoggerFactory;
import org.slf4j.Logger;

/**
 * Helper methods for the meta model.
 *
 * @author Tobias Doenz
 */
public final class MetaModelUtils {

    private static final Logger log = LoggerFactory.get();

    /**
     * Method object for {@link AtomicInteger#get()}.
     */
    public static final Method ATOMIC_INTEGER_GET_METHOD;

    /**
     * Method object for {@link AtomicLong#get()}.
     */
    public static final Method ATOMIC_LONG_GET_METHOD;

    /**
     * Method object for {@link ConcurrentHashMap#size()}.
     */
    public static final Method CONCURRENT_HASH_MAP_SIZE_METHOD;

    static {
        try {
            ATOMIC_INTEGER_GET_METHOD = AtomicInteger.class.getMethod("get", (Class<?>[]) null);
            ATOMIC_LONG_GET_METHOD = AtomicLong.class.getMethod("get", (Class<?>[]) null);
            CONCURRENT_HASH_MAP_SIZE_METHOD = ConcurrentHashMap.class.getMethod("size", (Class<?>[]) null);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    private MetaModelUtils() {
    }

    /**
     * Helper class for a getter method and the object where it should be invoked.
     *
     * @author Tobias Doenz
     */
    public static final class MethodTuple {

        private final Method method;
        private final Object object;

        /**
         * @param method
         *            the method
         * @param object
         *            the object where the method should be invoked
         */
        public MethodTuple(final Method method, final Object object) {
            this.method = method;
            this.object = object;
        }

        /**
         * Invokes the getter method.
         *
         * @return the return value of the getter method.
         */
        public Object get() {
            try {
                if (!method.isAccessible()) {
                    method.setAccessible(true);
                }
                return method.invoke(object, (Object[]) null);
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        }
    }

    /**
     * Helper class for a field and an object where this field is.
     *
     * @author Tobias Doenz
     */
    public static final class FieldTuple {

        private final Field field;
        private final Object object;

        /**
         * @param field
         *            the field
         * @param object
         *            the object where the field is
         */
        public FieldTuple(final Field field, final Object object) {
            this.field = field;
            this.object = object;
        }

        /**
         * @return the value of the field in the object
         */
        public Object get() {
            try {
                if (!field.isAccessible()) {
                    field.setAccessible(true);
                }
                return field.get(object);
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        }
    }

    /**
     * Gets meta model data.
     *
     * @param depth
     *            the tree depth up to which the meta model sub-tree should be traversed and meta data returned
     * @param metaModel
     *            the meta model of the node
     * @return the meta model data tree with the specified depth
     */
    public static Object getData(final int depth, final Map<String, Object> metaModel) {
        if (depth < 0) {
            return null;
        }
        Map<String, Object> metaData = new HashMap<String, Object>();
        for (Map.Entry<String, Object> entry : metaModel.entrySet()) {
            Object metaModelValue = entry.getValue();
            Object metaDataValue = getDataValue(depth, metaModelValue);
            metaData.put(entry.getKey(), metaDataValue);
        }
        return metaData;
    }

    /**
     * Gets meta model data.
     *
     * @param depth
     *            the tree depth up to which the meta model sub-tree should be traversed and meta data returned
     * @param metaModelValue
     *            the meta model value
     * @return the meta model data tree with the specified depth
     */
    public static Object getDataValue(final int depth, final Object metaModelValue) {
        if (metaModelValue instanceof MetaDataProvider) {
            MetaDataProvider provider = (MetaDataProvider) metaModelValue;
            return provider.getMetaData(depth - 1);
        }
        if (metaModelValue instanceof FieldTuple) {
            return ((FieldTuple) metaModelValue).get();
        }
        if (metaModelValue instanceof MethodTuple) {
            return ((MethodTuple) metaModelValue).get();
        }
        if (metaModelValue instanceof Map<?, ?>) {
            @SuppressWarnings("unchecked")
            Map<String, Object> map = (Map<String, Object>) metaModelValue;
            return getData(depth - 1, map);
        }
        if (metaModelValue instanceof List<?>) {
            @SuppressWarnings("unchecked")
            List<Object> list = (List<Object>) metaModelValue;
            Map<String, Object> metaData = new HashMap<String, Object>();
            for (int i = 0; i < list.size(); i++) {
                metaData.put(Integer.toString(i), getDataValue(depth - 1, list.get(i)));
            }
            return metaData;
        }
        if (metaModelValue != null && metaModelValue.getClass().isArray()) {
            Object[] array = (Object[]) metaModelValue;
            Map<String, Object> metaData = new HashMap<String, Object>();
            for (int i = 0; i < array.length; i++) {
                metaData.put(Integer.toString(i), getDataValue(depth - 1, array[i]));
            }
            return metaData;
        }
        return metaModelValue;
    }

    /**
     * Navigates from this node downwards in the meta model tree, along the specified path.
     *
     * @param path
     *            the path to navigate to
     * @param object
     *            the current object
     * @param metaModel
     *            the meta model for the current object
     * @return the meta model node at the the path
     */
    public static Object navigate(final String path, final Object object, final Map<String, Object> metaModel) {
        if (path == null) {
            return object;
        }
        String myPath = path;
        if (myPath.startsWith(MetaModelKeys.PATH_DELIMITER)) {
            myPath = myPath.substring(1);
        }
        if (AndroidHelperUtils.isEmpty(myPath)) {
            return object;
        }
        String[] matches = myPath.split(MetaModelKeys.PATH_DELIMITER, 2);
        // System.out.println(Arrays.toString(matches));
        String key = matches[0];
        if (key == null || AndroidHelperUtils.isEmpty(key)) {
            throw new IllegalArgumentException();
        }
        String remainingPath = matches.length == 1 ? null : matches[1];
        Object value = metaModel.get(key);
        if (value instanceof Navigable) {
            return ((Navigable) value).navigate(remainingPath);
        }
        if (value instanceof FieldTuple) {
            return ((FieldTuple) value).get();
        }
        if (value instanceof MethodTuple) {
            return ((MethodTuple) value).get();
        }
        if (value instanceof Map<?, ?>) {
            @SuppressWarnings("unchecked")
            Map<String, Object> map = (Map<String, Object>) value;
            log.debug("Navigating into map");
            return navigate(remainingPath, map, map);
        }
        if (value instanceof List<?>) {
            @SuppressWarnings("unchecked")
            List<Object> list = (List<Object>) value;
            // TODO optimize? map creation on the fly is slow (also for array below)
            Map<String, Object> map = new HashMap<String, Object>();
            for (int i = 0; i < list.size(); i++) {
                map.put(Integer.toString(i), list.get(i));
            }
            log.debug("Navigating into list");
            return navigate(remainingPath, map, map);
        }
        if (value != null && value.getClass().isArray()) {
            Object[] array = (Object[]) value;
            Map<String, Object> map = new HashMap<String, Object>();
            for (int i = 0; i < array.length; i++) {
                map.put(Integer.toString(i), array[i]);
            }
            log.debug("Navigating into array");
            return navigate(remainingPath, map, map);
        }
        return value;
    }

}
