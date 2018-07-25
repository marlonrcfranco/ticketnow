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
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.mozartspaces.capi3.javanative.operation.NativeEntry;

/**
 * Default implementation of {@link ClassToEntriesMapper}.
 *
 * @author Martin Planer
 */
public final class DefaultClassToEntriesMapper implements ClassToEntriesMapper {

    private final Map<Class<?>, Set<NativeEntry>> entries = new ConcurrentHashMap<Class<?>, Set<NativeEntry>>();
    private final Map<Class<?>, Set<Class<?>>> subtypes = new ConcurrentHashMap<Class<?>, Set<Class<?>>>();
    private final Map<NativeEntry, Set<NativeEntry>> savedLocation =
            new ConcurrentHashMap<NativeEntry, Set<NativeEntry>>();

    @Override
    public void put(final NativeEntry entry) {
        if (entry == null) {
            return;
        }

        Class<? extends Serializable> clazz = entry.getData().getClass();

        Set<NativeEntry> entrySet = entries.get(clazz);
        boolean didAlreadyExist = true;

        if (entrySet == null) {
            didAlreadyExist = false;
            entrySet = Collections.synchronizedSet(new HashSet<NativeEntry>());
            entries.put(clazz, entrySet);
        }

        entrySet.add(entry);
        savedLocation.put(entry, entrySet);

        if (didAlreadyExist == false) {
            mapSubtypes(clazz);
        }
    }

    private void mapSubtypes(final Class<? extends Serializable> clazz) {

        List<Class<?>> superTypes = new ArrayList<Class<?>>();
        //Collections.addAll(superTypes, clazz.getInterfaces());
        // not used for Android compatibility
        for (Class<?> type : clazz.getInterfaces()) {
            superTypes.add(type);
        }
        superTypes.add(clazz.getSuperclass());

        for (Class<?> superClazz : superTypes) {
            mapSubtype(clazz, superClazz);
        }

    }

    private void mapSubtype(final Class<? extends Serializable> clazz, final Class<?> superClazz) {

        Class<?> currentClass = superClazz;

        while (currentClass != Object.class && currentClass != null) {

            Set<Class<?>> types = subtypes.get(currentClass);

            if (types == null) {
                types = Collections.synchronizedSet(new HashSet<Class<?>>());
                subtypes.put(currentClass, types);
            }

            types.add(clazz);

            // Map all superinterfaces
            for (Class<?> c : currentClass.getInterfaces()) {
                mapSubtype(clazz, c);
            }

            currentClass = currentClass.getSuperclass();
        }
    }

    @Override
    public List<NativeEntry> get(final Class<?> clazz) {

        if (clazz == Object.class) {
            return new ArrayList<NativeEntry>(savedLocation.keySet());
        }

        Set<NativeEntry> entrySet = entries.get(clazz);

        if (entrySet == null) {
            entrySet = new HashSet<NativeEntry>();
        }

        Set<Class<?>> types = subtypes.get(clazz);

        if (types != null) {
            for (Class<?> subtype : types) {
                for (NativeEntry entry : entries.get(subtype)) {
                    entrySet.add(entry);
                }
            }
        }

        return new ArrayList<NativeEntry>(entrySet);
    }

    @Override
    public void delete(final NativeEntry entry) {

        Set<NativeEntry> set = savedLocation.get(entry);

        if (set == null) {
            return;
        }

        set.remove(entry);
        savedLocation.remove(entry);
    }

}
