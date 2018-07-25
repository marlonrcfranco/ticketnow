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
package org.mozartspaces.core;

import org.mozartspaces.capi3.IsolationLevel;
import org.mozartspaces.core.authorization.AuthorizationLevel;

/**
 * This utility class contains the constants for the MozartSpaces Core that can
 * be used in Core API calls.
 *
 * @author Tobias Doenz, Martin Barisits, Stefan Crass
 */
public final class MzsConstants {

    private MzsConstants() {
    }

    /**
     * The default isolation level.
     */
    public static final IsolationLevel DEFAULT_ISOLATION = IsolationLevel.REPEATABLE_READ;

    /**
     * The default authorization level.
     */
    public static final AuthorizationLevel DEFAULT_AUTHORIZATION = AuthorizationLevel.NONE;

    /**
     * Name of the policy container for authorization.
     */
    public static final String POLICY_CONTAINER_NAME = "__policyC";

    /**
     * Name of the request container for request-based authorization.
     */
    public static final String REQUEST_CONTAINER_NAME = "__requestC";

    /**
     * Constants that can be used when a Container is created.
     */
    public static final class Container {

        private Container() {
        }

        /**
         * The container size is not limited.
         */
        public static final int UNBOUNDED = 0;

        /**
         * The default container size.
         */
        public static final int DEFAULT_SIZE = UNBOUNDED;

        /**
         * The container has no explicit name and is therefore unnamed. The
         * container can be accessed only with the
         * <code>ContainerReference</code> that is returned when the container
         * is created. It is not possible to lookup the container.
         */
        public static final String UNNAMED = null;

        /**
         * By default the container uses the persistence profile configured in mozartspaces.xml.
         */
        public static final boolean DEFAULT_FORCE_IN_MEMORY = false;
    }

    /**
     * Constants that can be used with {@link org.mozartspaces.capi3.Selector
     * Selector}s.
     */
    public static final class Selecting {

        private Selecting() {
        }

        /**
         * To select all available entries matching the specific selector.
         */
        public static final int COUNT_MAX = -1;

        /**
         * To select all entries matching the specific selector. If at least one
         * entry is not available, <code>EntryLockedException</code> is
         * returned.
         */
        public static final int COUNT_ALL = -2;

        /**
         * The default entry count, if not explicitly specified.
         */
        public static final int DEFAULT_COUNT = 1;

        /**
         * Checks whether a count value is a special value.
         *
         * @param count
         *            the count to check
         * @return {@code true} if the count is a special value, {@code false}
         *         otherwise
         */
        public static boolean isSpecialCountConstant(final int count) {
            if (count == COUNT_ALL || count == COUNT_MAX) {
                return true;
            }
            return false;
        }

        /**
         * Checks that a count value is valid.
         *
         * @param count
         *            the count to check
         * @throws IllegalArgumentException
         *             if the count is not valid
         */
        public static void checkCount(final int count) {
            if (count < COUNT_ALL) {
                throw new IllegalArgumentException("count " + count);
            }
        }

    }

    /**
     * Constants that can be used to define special timeout behavior of
     * requests. These constants can be used instead of a positive number that
     * specifies the timeout in milliseconds.
     */
    public static final class RequestTimeout {

        private RequestTimeout() {
        }

        /**
         * The request will be processed once in the Core Processor and,
         * regardless of the return status, then discarded and a response will
         * be sent immediately.
         *
         * A request with this timeout value can never be rescheduled, as it is
         * not written to the Wait Container. If the CAPI3 operation status is
         * not <code>OK</code>, the request will fail and an exception sent as
         * response.
         */
        public static final long ZERO = 0;

        /**
         * The request will never expire and should always be rescheduled when
         * the operation status from CAPI3 is <code>LOCKED</code> or
         * <code>DELAYABLE</code>.
         */
        public static final long INFINITE = -1;

        /**
         * The request will be processed once on the actual data in CAPI3. That
         * is, the request is rescheduled after an operation status
         * <code>LOCKED</code>, but not after <code>DELAYABLE</code>.
         */
        public static final long TRY_ONCE = -2;

        /**
         * The default timeout value used in requests, if not explicitly
         * specified.
         */
        public static final long DEFAULT = TRY_ONCE;
    }

    /**
     * Constants that can be used to define special timeout behavior of a
     * transaction. These constants can be used instead of a positive number
     * that specifies the timeout in milliseconds.
     */
    public static final class TransactionTimeout {

        private TransactionTimeout() {
        }

        /**
         * The transaction will never expire.
         */
        public static final long INFINITE = -1;

    }
}
