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
package org.mozartspaces.capi3.javanative.coordination.query.index;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.mozartspaces.capi3.LogItem;
import org.mozartspaces.capi3.Property;
import org.mozartspaces.capi3.javanative.coordination.query.DefaultPathProperty;
import org.mozartspaces.capi3.javanative.coordination.query.NativeProperty;
import org.mozartspaces.capi3.javanative.coordination.query.NativeProperty.NoPathMatch;
import org.mozartspaces.capi3.javanative.coordination.query.ReflectionUtils;
import org.mozartspaces.capi3.javanative.isolation.NativeSubTransaction;
import org.mozartspaces.capi3.javanative.operation.NativeEntry;

/**
 * Default implementation of a {@link SearchIndex}.
 *
 * @author Martin Planer
 */
public final class DefaultSearchIndex implements SearchIndex {

    private final NativeProperty property;
    private final Map<Object, Set<NativeEntry>> index = new ConcurrentHashMap<Object, Set<NativeEntry>>();
    private final Set<Class<?>> indexedClasses = Collections.newSetFromMap(new ConcurrentHashMap<Class<?>, Boolean>());

    public DefaultSearchIndex(final String[] path) {
        this.property = new DefaultPathProperty(Property.forName(path));
    }

    @Override
    public synchronized void index(final NativeEntry entry, final NativeSubTransaction stx) {
        Object value = property.getValue(entry.getData());

        if (value == NoPathMatch.INSTANCE) {
            return;
        }
        if (!classShouldBeIndexed(entry.getData().getClass())) {
            return;
        }

        if (Collection.class.isAssignableFrom(value.getClass())) {
            for (Object val : (Collection<?>) value) {
                Set<NativeEntry> entrySet = index.get(val);

                if (entrySet == null) {
                    entrySet = Collections.newSetFromMap(new ConcurrentHashMap<NativeEntry, Boolean>());
                    index.put(val, entrySet);
                }

                entrySet.add(entry);
            }
        } else {
            Set<NativeEntry> entrySet = index.get(value);

            if (entrySet == null) {
                entrySet = Collections.newSetFromMap(new ConcurrentHashMap<NativeEntry, Boolean>());
                index.put(value, entrySet);
            }

            entrySet.add(entry);
        }

        if (stx != null) {
            stx.addLog(new LogItem() {

                @Override
                public void rollbackTransaction() {
                }

                @Override
                public void rollbackSubTransaction() {
                    remove(entry, null);
                }

                @Override
                public void commitTransaction() {
                }

                @Override
                public void commitSubTransaction() {
                }
            });
        }
    }

    @Override
    public synchronized Set<NativeEntry> lookupEqualTo(final Object value, final Class<?> restrictClass) {
        Set<NativeEntry> results = new HashSet<NativeEntry>();

        Set<NativeEntry> entrySet = index.get(value);

        if (entrySet == null) {
            return results;
        }

        for (NativeEntry entry : entrySet) {
            Serializable data = entry.getData();
            if (ReflectionUtils.classIsValidTypeFor(data.getClass(), restrictClass)) {
                results.add(entry);
            }
        }

        return results;
    }

    @Override
    public synchronized void remove(final NativeEntry entry, final NativeSubTransaction stx) {
        for (Set<NativeEntry> entrySet : index.values()) {
            entrySet.remove(entry);
        }

        if (stx != null) {
            stx.addLog(new LogItem() {

                @Override
                public void rollbackTransaction() {
                }

                @Override
                public void rollbackSubTransaction() {
                    index(entry, null);
                }

                @Override
                public void commitTransaction() {
                }

                @Override
                public void commitSubTransaction() {
                }
            });
        }
    }

    @Override
    public synchronized void addClassToIndex(final Class<?> clazz) {
        indexedClasses.add(clazz);
    }

    @Override
    public synchronized Set<Class<?>> getIndexedClasses() {
        return indexedClasses;
    }

    private boolean classShouldBeIndexed(final Class<? extends Object> clazz) {

        // Don't index if no class is specified
        // if (indexedClasses.isEmpty())
        // return true;

        if (indexedClasses.contains(clazz)) {
            return true;
        }

        for (Class<?> c : indexedClasses) {
            if (ReflectionUtils.classIsValidTypeFor(clazz, c)) {
                return true;
            }
        }

        return false;
    }
}
