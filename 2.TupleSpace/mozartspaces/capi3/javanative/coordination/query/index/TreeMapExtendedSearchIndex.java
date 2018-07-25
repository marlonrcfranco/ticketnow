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
import java.util.Comparator;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NavigableMap;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.mozartspaces.capi3.Property;
import org.mozartspaces.capi3.javanative.coordination.query.DefaultPathProperty;
import org.mozartspaces.capi3.javanative.coordination.query.NativeProperty.NoPathMatch;
import org.mozartspaces.capi3.javanative.coordination.query.ReflectionUtils;
import org.mozartspaces.capi3.javanative.isolation.NativeSubTransaction;
import org.mozartspaces.capi3.javanative.operation.NativeEntry;

/**
 * TODO
 *
 * @author Martin Planer
 */
public final class TreeMapExtendedSearchIndex implements ExtendedSearchIndex {

    private final ReadWriteLock lock = new ReentrantReadWriteLock();
    private final Lock readLock = lock.readLock();
    private final Lock writeLock = lock.writeLock();

    private final DefaultPathProperty property;

    private final NavigableMap<Object, Set<NativeEntry>> data = new TreeMap<Object, Set<NativeEntry>>(
            new IndexComparator());

    private final Set<Class<?>> indexedClasses = Collections.newSetFromMap(new ConcurrentHashMap<Class<?>, Boolean>());

    public TreeMapExtendedSearchIndex(final String[] path) {
        this.property = new DefaultPathProperty(Property.forName(path));
    }

    @Override
    public void index(final NativeEntry entry, final NativeSubTransaction stx) {

        if (!classShouldBeIndexed(entry.getData().getClass())) {
            return;
        }

        Object value = property.getValue(entry.getData());

        if (value == NoPathMatch.INSTANCE) {
            return;
        }

        writeLock.lock();

        try {
            if (Collection.class.isAssignableFrom(value.getClass())) {
                for (Object val : (Collection<?>) value) {
                    Set<NativeEntry> entrySet = data.get(val);

                    if (entrySet == null) {
                        entrySet = Collections.newSetFromMap(new ConcurrentHashMap<NativeEntry, Boolean>());
                        data.put(val, entrySet);
                    }

                    entrySet.add(entry);
                }
            } else {
                Set<NativeEntry> entrySet = data.get(value);

                if (entrySet == null) {
                    entrySet = Collections.newSetFromMap(new ConcurrentHashMap<NativeEntry, Boolean>());
                    data.put(value, entrySet);
                }

                entrySet.add(entry);
            }
        } finally {
            writeLock.unlock();
        }
    }

    @Override
    public Set<NativeEntry> lookupEqualTo(final Object value, final Class<?> restrictClass) {

        Set<NativeEntry> results = new HashSet<NativeEntry>();

        readLock.lock();

        try {
            Set<NativeEntry> entrySet = data.get(value);

            if (entrySet == null) {
                return results;
            }

            for (NativeEntry entry : data.get(value)) {
                Serializable entryData = entry.getData();
                if (ReflectionUtils.classIsValidTypeFor(entryData.getClass(), restrictClass)) {
                    results.add(entry);
                }
            }
        } finally {
            readLock.unlock();
        }

        return results;
    }

    @Override
    public void remove(final NativeEntry entry, final NativeSubTransaction stx) {

        writeLock.lock();

        try {
            for (Set<NativeEntry> entrySet : data.values()) {
                entrySet.remove(entry);
            }
        } finally {
            writeLock.unlock();
        }
    }

    @Override
    public void addClassToIndex(final Class<?> clazz) {
        indexedClasses.add(clazz);
    }

    @Override
    public Set<Class<?>> getIndexedClasses() {
        return indexedClasses;
    }

    @Override
    public Set<NativeEntry> lookupLessThan(final Comparable<?> value, final Class<?> restrictClass) {
        readLock.lock();
        try {
            Map<Object, Set<NativeEntry>> resultMap = data.headMap(value);
            return concatResultMaps(resultMap, value.getClass(), restrictClass);
        } finally {
            readLock.unlock();
        }
    }

    @Override
    public Set<NativeEntry> lookupLessThanOrEqualTo(final Comparable<?> value, final Class<?> restrictClass) {
        readLock.lock();
        try {
            Map<Object, Set<NativeEntry>> resultMap = data.headMap(value, true);
            return concatResultMaps(resultMap, value.getClass(), restrictClass);
        } finally {
            readLock.unlock();
        }
    }

    @Override
    public Set<NativeEntry> lookupGreaterThan(final Comparable<?> value, final Class<?> restrictClass) {
        readLock.lock();
        try {
            Map<Object, Set<NativeEntry>> resultMap = data.tailMap(value, false);
            return concatResultMaps(resultMap, value.getClass(), restrictClass);
        } finally {
            readLock.unlock();
        }
    }

    @Override
    public Set<NativeEntry> lookupGreaterThanOrEqualTo(final Comparable<?> value, final Class<?> restrictClass) {
        readLock.lock();
        try {
            Map<Object, Set<NativeEntry>> resultMap = data.tailMap(value, true);
            return concatResultMaps(resultMap, value.getClass(), restrictClass);
        } finally {
            readLock.unlock();
        }
    }

    @Override
    public Set<NativeEntry> lookupBetween(final Comparable<?> lowerBound, final Comparable<?> upperBound,
            final Class<?> restrictClass) {
        readLock.lock();
        try {
            NavigableMap<Object, Set<NativeEntry>> resultMap = data.subMap(lowerBound, true, upperBound, true);
            return concatResultMaps(resultMap, lowerBound.getClass(), restrictClass);
        } finally {
            readLock.unlock();
        }
    }

    private boolean classShouldBeIndexed(final Class<? extends Object> clazz) {
        return indexedClasses.contains(clazz);
    }

    private Set<NativeEntry> concatResultMaps(final Map<Object, Set<NativeEntry>> resultMap, final Class<?> valueClass,
            final Class<?> restrictClass) {

        Set<NativeEntry> results = new HashSet<NativeEntry>();

        Set<Entry<Object, Set<NativeEntry>>> resultMapEntries = resultMap.entrySet();

        for (Entry<Object, Set<NativeEntry>> resultMapEntry : resultMapEntries) {
            if (ReflectionUtils.classIsValidTypeFor(resultMapEntry.getKey().getClass(), valueClass)) {
                for (NativeEntry entry : resultMapEntry.getValue()) {
                    if (ReflectionUtils.classIsValidTypeFor(entry.getData().getClass(), restrictClass)) {
                        results.add(entry);
                    }
                }
            }
        }

        return results;
    }

    /**
     * Comparator for sorting the entries in this index. NOTE: All entries, regardless of their type, will be sorted.
     * The order of the different types is based on their class name and can lead to strange results when using extended
     * index queries. Be sure to limit index entries to the desired type only to avoid these problems.
     *
     * @author Martin Planer
     *
     */
    private static class IndexComparator implements Comparator<Object> {

        @Override
        @SuppressWarnings("unchecked")
        public int compare(final Object o1, final Object o2) {

            if (o1.getClass().equals(o2.getClass())) {
                if (Comparable.class.isAssignableFrom(o1.getClass())
                        && Comparable.class.isAssignableFrom(o2.getClass())) {
                    return ((Comparable<Object>) o1).compareTo(o2);
                }

                if (o1.equals(o2)) {
                    return 0;
                }
            }

            int compareClasses = o1.getClass().getName().compareTo(o2.getClass().getName());

            if (compareClasses == 0) {
                int hashCode1 = o1.hashCode();
                int hashCode2 = o2.hashCode();
                return (hashCode1 < hashCode2) ? -1 : ((hashCode1 == hashCode2) ? 0 : 1);
            }

            return compareClasses;
        }
    }
}
