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
package org.mozartspaces.core.aspects;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * The constants interception points a space aspect can use. They are referenced
 * in aspect requests and the AspectManager. Each constant has a corresponding
 * method in the interface {@link SpaceAspect} or the super interface
 * {@link ContainerAspect}.
 *
 * @author Tobias Doenz
 *
 * @see ContainerIPoint
 */
public enum SpaceIPoint implements InterceptionPoint {

    // entries requests
    /**
     * Before/After reading entries from a container.
     */
    PRE_READ, POST_READ,

    /**
     * Before/After testing for entries in a container.
     */
    PRE_TEST, POST_TEST,

    /**
     * Before/After taking entries from a container.
     */
    PRE_TAKE, POST_TAKE,

    /**
     * Before/After deleting entries from a container.
     */
    PRE_DELETE, POST_DELETE,

    /**
     * Before/After writing entries into a container.
     */
    PRE_WRITE, POST_WRITE,

    // transaction requests
    /**
     * Before/After creating an explicit transaction.
     */
    PRE_CREATE_TRANSACTION, POST_CREATE_TRANSACTION,

    /**
     * Before/After preparing a transaction for commit.
     */
    PRE_PREPARE_TRANSACTION, POST_PREPARE_TRANSACTION,

    /**
     * Before/After committing a transaction.
     */
    PRE_COMMIT_TRANSACTION, POST_COMMIT_TRANSACTION,

    /**
     * Before/After rollbacking a transaction.
     */
    PRE_ROLLBACK_TRANSACTION, POST_ROLLBACK_TRANSACTION,

    // container requests
    /**
     * Before/After creating a container.
     */
    PRE_CREATE_CONTAINER, POST_CREATE_CONTAINER,

    /**
     * Before/After destroying a container.
     */
    PRE_DESTROY_CONTAINER, POST_DESTROY_CONTAINER,

    /**
     * Before/After looking a container up.
     */
    PRE_LOOKUP_CONTAINER, POST_LOOKUP_CONTAINER,

    /**
     * Before/After setting a container lock.
     */
    PRE_LOCK_CONTAINER, POST_LOCK_CONTAINER,

    // aspect requests
    /**
     * Before/After adding a space or container aspect.
     */
    PRE_ADD_ASPECT, POST_ADD_ASPECT,

    /**
     * Before/After removing an aspect.
     */
    PRE_REMOVE_ASPECT, POST_REMOVE_ASPECT,

    // other requests
    /**
     * Before/After shutting the core down.
     */
    PRE_SHUTDOWN;

    /**
     * An unmodifiable set of all space interception points, that is, all values
     * of the enumeration.
     */
    public static final Set<SpaceIPoint> ALL_POINTS = Collections
            .unmodifiableSet(new HashSet<SpaceIPoint>(Arrays.asList(values())));

    /**
     * An unmodifiable set of all space <code>pre</code> interception points.
     */
    public static final Set<SpaceIPoint> ALL_PRE_POINTS = Collections
            .unmodifiableSet(new HashSet<SpaceIPoint>(Arrays.asList(new SpaceIPoint[] {
                    PRE_READ, PRE_TEST, PRE_TAKE, PRE_DELETE, PRE_WRITE,
                    PRE_CREATE_TRANSACTION, PRE_PREPARE_TRANSACTION, PRE_COMMIT_TRANSACTION, PRE_ROLLBACK_TRANSACTION,
                    PRE_CREATE_CONTAINER, PRE_DESTROY_CONTAINER, PRE_LOOKUP_CONTAINER, PRE_LOCK_CONTAINER,
                    PRE_ADD_ASPECT, PRE_REMOVE_ASPECT,
                    PRE_SHUTDOWN})));

    /**
     * An unmodifiable set of all space <code>post</code> interception points.
     */
    public static final Set<SpaceIPoint> ALL_POST_POINTS = Collections
            .unmodifiableSet(new HashSet<SpaceIPoint>(Arrays.asList(new SpaceIPoint[] {
                    POST_READ, POST_TEST, POST_TAKE, POST_DELETE, POST_WRITE,
                    POST_CREATE_TRANSACTION, POST_PREPARE_TRANSACTION,
                    POST_COMMIT_TRANSACTION, POST_ROLLBACK_TRANSACTION,
                    POST_CREATE_CONTAINER, POST_DESTROY_CONTAINER, POST_LOOKUP_CONTAINER, POST_LOCK_CONTAINER,
                    POST_ADD_ASPECT, POST_REMOVE_ASPECT})));
}
