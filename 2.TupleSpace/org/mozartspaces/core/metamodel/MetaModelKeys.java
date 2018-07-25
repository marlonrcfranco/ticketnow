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

/**
 * @author Tobias Doenz
 */
public final class MetaModelKeys {

    /**
     * The delimiter of keys in the meta model path.
     */
    public static final String PATH_DELIMITER = "/";

    private MetaModelKeys() {
    }

    /**
     * The core configuration.
     */
    public static final String CONFIG = "config";

    /**
     * The container manager.
     */
    public static final String CONTAINERS = "containers";

    /**
     * The runtime data.
     */
    public static final String RUNTIME = "runtime";

    /**
     * The system data.
     */
    public static final String SYSTEM = "system";

    /**
     * @author Tobias Doenz
     */
    public static final class Containers {

        /**
         * The container map.
         */
        public static final String CONTAINER = "container";

        /**
         * The container names.
         */
        public static final String NAMES = "names";

        /**
         * The number of containers in the space.
         */
        public static final String COUNT = "count";

        /**
         * The counter properties.
         */
        public static final String COUNTERS = "counters";

        /**
         * @author Tobias Doenz
         */
        public static final class Counters {
            /**
             * The number of operations to create a container.
             */
            public static final String CREATE_CONTAINER_OP_COUNT = "createContainerOpCount";

            /**
             * The number of successful operations to create a container.
             */
            public static final String SUCCESSFUL_CREATE_CONTAINER_OP_COUNT = "successfulCreateContainerOpCount";

            /**
             * The number of operations to destroy a container.
             */
            public static final String DESTROY_CONTAINER_OP_COUNT = "destroyContainerOpCount";

            /**
             * The number of successful operations to destroy a container.
             */
            public static final String SUCCESSFUL_DESTROY_CONTAINER_OP_COUNT = "successfulDestroyContainerOpCount";

            /**
             * The number of operations to lock a container.
             */
            public static final String LOCK_CONTAINER_OP_COUNT = "lockContainerOpCount";

            /**
             * The number of successful operations to lock a container.
             */
            public static final String SUCCESSFUL_LOCK_CONTAINER_OP_COUNT = "successfulLockContainerOpCount";

            /**
             * The number of operations to lookup a container.
             */
            public static final String LOOKUP_CONTAINER_OP_COUNT = "lookupContainerOpCount";

            /**
             * The number of successful operations to lookup a container.
             */
            public static final String SUCCESSFUL_LOOKUP_CONTAINER_OP_COUNT = "successfulLookupContainerOpCount";

            private Counters() {
            }
        }

        private Containers() {
        }

        /**
         * @author Tobias Doenz
         */
        public static final class Container {

            /**
             * The container name.
             */
            public static final String NAME = "name";

            /**
             * The maximal number of entries in the container.
             */
            public static final String MAXSIZE = "maxSize";

            /**
             * The current number of entries in the container.
             */
            public static final String SIZE = "size";

            /**
             * The authorization level of the container.
             */
            public static final String AUTH_LEVEL = "authLevel";

            /**
             * The entries in the container.
             */
            public static final String ENTRIES = "entries";

            /**
             * The obligatory coordinator arguments used to create the container.
             */
            public static final String OBLIGATORY_COORDINATOR_ARGS = "obligatoryCoordArgs";

            /**
             * The obligatory coordinators of the container (implementation classes).
             */
            public static final String OBLIGATORY_COORDINATORS = "obligatoryCoords";

            /**
             * The optional coordinator arguments used to create the container.
             */
            public static final String OPTIONAL_COORDINATOR_ARGS = "optionalCoordArgs";

            /**
             * The optional coordinators of the container (implementation classes).
             */
            public static final String OPTIONAL_COORDINATORS = "optionalCoords";

            /**
             * The counter properties.
             */
            public static final String COUNTERS = "counters";

            /**
             * @author Tobias Doenz
             */
            public static final class Counters {
                /**
                 * The number of write operations on the container.
                 */
                public static final String WRITE_OP_COUNT = "writeOpCount";

                /**
                 * The number of successful write operations on the container.
                 */
                public static final String SUCCESSFUL_WRITE_OP_COUNT = "successfulWriteOpCount";

                /**
                 * The number of read operations on the container.
                 */
                public static final String READ_OP_COUNT = "readOperationCount";

                /**
                 * The number of successful read operations on the container.
                 */
                public static final String SUCCESSFUL_READ_OP_COUNT = "successfulReadOpCount";

                /**
                 * The number of take operations on the container.
                 */
                public static final String TAKE_OP_COUNT = "takeOpCount";

                /**
                 * The number of successful take operations on the container.
                 */
                public static final String SUCCESSFUL_TAKE_OP_COUNT = "successfulTakeOpCount";

                private Counters() {
                }
            }

            private Container() {
            }

            /**
             * @author Tobias Doenz
             */
            public static final class Coordinators {

                /**
                 * The coordinator name.
                 */
                public static final String NAME = "name";

                /**
                 * The class name of the coordinator implementation.
                 */
                public static final String CLASS = "class";

                /**
                 * The number of entries in the coordinator.
                 */
                public static final String ENTRYCOUNT = "entryCount";

                /**
                 * The entries in the coordinator. The value is
                 * coordinator-specific!
                 */
                public static final String ENTRIES = "entries";

                private Coordinators() {
                }

                /**
                 * @author Tobias Doenz
                 */
                public static final class KeyCoordinatorMeta {

                    /**
                     * The keys of the entries.
                     */
                    public static final String KEYS = "keys";

                    private KeyCoordinatorMeta() {
                    }

                }

                /**
                 * @author Tobias Doenz
                 */
                public static final class LabelCoordinatorMeta {

                    /**
                     * The labels of the entries.
                     */
                    public static final String LABELS = "labels";

                    private LabelCoordinatorMeta() {
                    }
                }

            }
        }
    }

    /**
     * @author Tobias Doenz
     */
    public static final class Runtime {

    }

    /**
     * @author Tobias Doenz
     */
    public static final class System {

        /**
         * Return value of {@link java.lang.Runtime#availableProcessors()}.
         */
        public static final String AVAILABLE_PROCESSORS = "availableProcessors";

        /**
         * Return value of {@link java.lang.Runtime#freeMemory()}.
         */
        public static final String FREE_MEMORY = "freeMemory";

        /**
         * Return value of {@link java.lang.Runtime#totalMemory()}.
         */
        public static final String TOTAL_MEMORY = "totalMemory";

        /**
         * Return value of {@link java.lang.Runtime#maxMemory()}.
         */
        public static final String MAX_MEMORY = "maxMemory";

        private System() {
        }
    }
}
