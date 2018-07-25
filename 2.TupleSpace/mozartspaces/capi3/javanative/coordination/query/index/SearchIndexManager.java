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
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.mozartspaces.capi3.Index;
import org.mozartspaces.capi3.Indexable;
import org.mozartspaces.capi3.LogItem;
import org.mozartspaces.capi3.QueryIndex;
import org.mozartspaces.capi3.QueryIndex.IndexType;
import org.mozartspaces.capi3.QueryIndexData;
import org.mozartspaces.capi3.QueryIndexes;
import org.mozartspaces.capi3.javanative.coordination.DefaultQueryCoordinator;
import org.mozartspaces.capi3.javanative.coordination.query.ReflectionUtils;
import org.mozartspaces.capi3.javanative.isolation.NativeSubTransaction;
import org.mozartspaces.capi3.javanative.operation.NativeEntry;
import org.mozartspaces.capi3.javanative.persistence.StoredMap;
import org.mozartspaces.util.AndroidHelperUtils;

/**
 * Manages (create, delete, retrieve) all corresponding search indexes.
 *
 * @author Martin Planer
 */
public final class SearchIndexManager {

    private final DefaultQueryCoordinator coordinator;
    private final Map<SearchIndexKey, SearchIndex> indexes = new ConcurrentHashMap<SearchIndexKey, SearchIndex>();

    private final Set<Class<? extends Serializable>> indexedEntryTypes = Collections
            .newSetFromMap(new ConcurrentHashMap<Class<? extends Serializable>, Boolean>());

    public SearchIndexManager(final DefaultQueryCoordinator defaultQueryCoordinator) {
        this.coordinator = defaultQueryCoordinator;
    }

    /**
     * Returns the matching index or <code>null</code> if a matching index could not be found.
     *
     * @param property
     *            the property of the index
     * @param indexType
     *            the type of the index
     * @return the matching index
     */
    public synchronized SearchIndex getIndex(final String[] path, final IndexType indexType) {

        if (indexType == IndexType.NONE) {
            return null;
        }

        SearchIndexKey key = new SearchIndexKey(path, indexType);

        SearchIndex index = indexes.get(key);

        // No such index available
        if (index == null) {
            return null;
        }

        return index;
    }

    /**
     * Removes all matching indexes from the manager.
     *
     * @param property
     *            the property of the index
     */
    public synchronized void removeIndex(final String[] path, final NativeSubTransaction stx) {
        if (stx != null) {
            stx.addLog(new LogItem() {

                @Override
                public void rollbackTransaction() {
                }

                @Override
                public void rollbackSubTransaction() {
                }

                @Override
                public void commitTransaction() {
                }

                @Override
                public void commitSubTransaction() {
                    indexes.remove(new SearchIndexKey(path, IndexType.BASIC));
                    indexes.remove(new SearchIndexKey(path, IndexType.EXTENDED));
                }
            });
        } else {
            indexes.remove(new SearchIndexKey(path, IndexType.BASIC));
            indexes.remove(new SearchIndexKey(path, IndexType.EXTENDED));
        }

    }

    /**
     * Creates a new index for the given property and with the given type in the manager. The method returns the created
     * index. If a matching index already exists, the existing index is returned instead of a newly created one.
     *
     * @param property
     *            the property of the index
     * @param indexType
     *            the type of the index
     * @param declaringType
     * @param stx
     * @return the newly created index or the matching existing one
     */
    public synchronized void createIndex(final String[] path, final IndexType indexType, final Class<?> declaringType,
            final NativeSubTransaction stx) {

        if (indexType == IndexType.NONE) {
            throw new IllegalArgumentException("IndexType.NONE not supported!");
        }

        // Create basic index no matter what type of index was requested
        SearchIndex index = getIndex(path, IndexType.BASIC);

        // Create new basic index and add if no appropriate index exists
        if (index == null) {

            index = createNewIndex(IndexType.BASIC, path);
            index.addClassToIndex(declaringType);

            updateIndex(index, stx);
            addIndex(new SearchIndexKey(path, IndexType.BASIC), index, stx);
        }

        // If extended index was requested, also create extended index
        if (indexType == IndexType.EXTENDED) {

            SearchIndex extendedIndex = getIndex(path, indexType);

            // Create new extended index and add if no appropriate index exists
            if (extendedIndex == null) {

                extendedIndex = createNewIndex(indexType, path);
                extendedIndex.addClassToIndex(declaringType);

                for (Class<?> indexedClass : index.getIndexedClasses()) {
                    extendedIndex.addClassToIndex(indexedClass);
                }

                updateIndex(extendedIndex, stx);
                addIndex(new SearchIndexKey(path, indexType), extendedIndex, stx);
            }
        }
    }

    public void createIndexesFromCoordData(final QueryIndexData[] indexData, final Class<? extends Serializable> clazz,
            final NativeSubTransaction stx) {
        if (indexData != null) {
            for (QueryIndexData data : indexData) {
                createIndex(data.getPath(), data.getIndexType(), clazz, stx);
            }
        }
    }

    private void addIndex(final SearchIndexKey searchIndexKey, final SearchIndex index, final NativeSubTransaction stx) {
        indexes.put(searchIndexKey, index);

        if (stx != null) {
            stx.addLog(new LogItem() {

                @Override
                public void rollbackTransaction() {
                }

                @Override
                public void rollbackSubTransaction() {
                    indexes.remove(searchIndexKey);
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

    /**
     * Updates all indexes with the given entry.
     *
     * @param entry
     *            entry to be inserted into the indexes
     * @param stx
     */
    public synchronized void updateIndexes(final NativeEntry entry, final NativeSubTransaction stx) {

        // Create indexes if not present
        final Class<? extends Serializable> clazz = entry.getData().getClass();
        if (!indexedEntryTypes.contains(clazz)) {
            createNecessaryIndexes(entry, stx);

            indexedEntryTypes.add(clazz);

            if (stx != null) {
                stx.addLog(new LogItem() {

                    @Override
                    public void rollbackTransaction() {
                    }

                    @Override
                    public void rollbackSubTransaction() {
                        indexedEntryTypes.remove(clazz);
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

        for (SearchIndex index : indexes.values()) {
            index.index(entry, stx);
        }
    }

    /**
     * Remove a {@link NativeEntry} from all existing indexes.
     *
     * @param entry
     *            the entry to be removed
     * @param stx
     */
    public synchronized void removeFromIndexes(final NativeEntry entry, final NativeSubTransaction stx) {
        for (SearchIndex index : indexes.values()) {
            index.remove(entry, stx);
        }
    }

    private void createNecessaryIndexes(final NativeEntry entry, final NativeSubTransaction stx) {

        Class<? extends Serializable> clazz = entry.getData().getClass();

        Indexable indexable = clazz.getAnnotation(Indexable.class);
        if (indexable == null) {
            return;
        }

        // Create indexes defined on the type
        QueryIndexes typeIndexes = clazz.getAnnotation(QueryIndexes.class);
        if (typeIndexes != null) {
            createIndexes(typeIndexes, null, clazz, stx);
        }

        // Add indexes defined on the fields
        for (Field field : ReflectionUtils.getDeclaredFieldsIncludingSuperclasses(clazz)) {
            // Get path prefix (from label if present)
            String prefixPath = field.getName();
            Index fieldLabel = field.getAnnotation(Index.class);
            if (fieldLabel != null) {
                prefixPath = fieldLabel.label();
            }

            QueryIndex fieldIndex = field.getAnnotation(QueryIndex.class);
            if (fieldIndex != null) {
                createSingleIndex(fieldIndex, prefixPath, field.getDeclaringClass(), stx);
            }

            QueryIndexes fieldIndexes = field.getAnnotation(QueryIndexes.class);
            if (fieldIndexes != null) {
                createIndexes(fieldIndexes, prefixPath, field.getDeclaringClass(), stx);
            }
        }
    }

    private void createIndexes(final QueryIndexes typeIndexes, final String prefixPath, final Class<?> declaringClass,
            final NativeSubTransaction stx) {
        QueryIndex[] values = typeIndexes.value();

        if (values == null) {
            return;
        }

        for (QueryIndex queryIndex : values) {
            createSingleIndex(queryIndex, prefixPath, declaringClass, stx);
        }
    }

    private void createSingleIndex(final QueryIndex fieldIndex, final String prefixPath, final Class<?> declaringClass,
            final NativeSubTransaction stx) {
        IndexType indexType = fieldIndex.type();
        String[] path = fieldIndex.path();

        if (prefixPath != null && !AndroidHelperUtils.isEmpty(prefixPath)) {
            String[] prefixedPath = new String[path.length + 1];
            prefixedPath[0] = prefixPath;
            System.arraycopy(path, 0, prefixedPath, 1, path.length);

            createIndex(prefixedPath, indexType, declaringClass, stx);
        } else {
            createIndex(path, indexType, declaringClass, stx);
        }
    }

    private void updateIndex(final SearchIndex index, final NativeSubTransaction stx) {
        StoredMap<NativeEntry, NativeEntry> entries = coordinator.getEntries();

        if (entries == null) {
            return;
        }

        for (NativeEntry entry : entries.keySet()) {
            index.index(entry, stx);
        }
    }

    private SearchIndex createNewIndex(final IndexType indexType, final String[] path) {

        switch (indexType) {
        case BASIC:
            return new DefaultSearchIndex(path);

        case EXTENDED:
            return new DefaultExtendedSearchIndex(path);

        default:
            return null;
        }
    }

    private static class SearchIndexKey {
        private final String[] path;
        private final IndexType indexType;
        private final int hashCode;

        public SearchIndexKey(final String[] path, final IndexType indexType) {
            this.path = path;
            this.indexType = indexType;

            // pre-calculate and cache hash code
            int hashCode = 17;
            int hashMultiplier = 59;
            hashCode = hashCode * hashMultiplier + ((path == null) ? 0 : Arrays.hashCode(path));
            hashCode = hashCode * hashMultiplier + ((indexType == null) ? 0 : indexType.hashCode());
            this.hashCode = hashCode;
        }

        @Override
        public int hashCode() {
            return hashCode;
        }

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

            SearchIndexKey otherKey = (SearchIndexKey) obj;

            if (indexType != otherKey.indexType) {
                return false;
            }

            if (!Arrays.equals(path, otherKey.path)) {
                return false;
            }

            return true;
        }

        @Override
        public String toString() {
            return "SearchIndexKey [path=" + Arrays.toString(path) + ", indexType=" + indexType + ", hashCode="
                    + hashCode + "]";
        }

    }
}
