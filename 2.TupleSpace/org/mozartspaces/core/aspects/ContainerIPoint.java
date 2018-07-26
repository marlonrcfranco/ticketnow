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
 * The constants for the possible interception points of a container aspect.
 * They are used in aspect requests and the AspectManager. Each constant has a
 * corresponding method in the interface {@link ContainerAspect}.
 *
 * @author Tobias Doenz
 *
 * @see SpaceIPoint
 */
public enum ContainerIPoint implements InterceptionPoint {

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

    // container requests
    /**
     * Before/After destroying a container.
     */
    PRE_DESTROY_CONTAINER, POST_DESTROY_CONTAINER,

    /**
     * After looking a container up.
     */
    POST_LOOKUP_CONTAINER,

    /**
     * Before/After setting a container lock.
     */
    PRE_LOCK_CONTAINER, POST_LOCK_CONTAINER,

    // aspect requests
    /**
     * Before/After adding a container aspect.
     */
    PRE_ADD_ASPECT, POST_ADD_ASPECT,

    /**
     * Before/After removing a container aspect.
     */
    PRE_REMOVE_ASPECT, POST_REMOVE_ASPECT;

    /**
     * An unmodifiable set of all container interception points, that is, all values
     * of the enumeration.
     */
    public static final Set<ContainerIPoint> ALL_POINTS = Collections
            .unmodifiableSet(new HashSet<ContainerIPoint>(Arrays.asList(values())));

    /**
     * An unmodifiable set of all container <code>pre</code> interception points.
     */
    public static final Set<ContainerIPoint> ALL_PRE_POINTS = Collections
            .unmodifiableSet(new HashSet<ContainerIPoint>(Arrays.asList(new ContainerIPoint[] {
                    PRE_READ, PRE_TEST, PRE_TAKE, PRE_DELETE, PRE_WRITE,
                    PRE_DESTROY_CONTAINER, PRE_LOCK_CONTAINER,
                    PRE_ADD_ASPECT, PRE_REMOVE_ASPECT})));

    /**
     * An unmodifiable set of all container <code>post</code> interception points.
     */
    public static final Set<ContainerIPoint> ALL_POST_POINTS = Collections
            .unmodifiableSet(new HashSet<ContainerIPoint>(Arrays.asList(new ContainerIPoint[] {
                    POST_READ, POST_TEST, POST_TAKE, POST_DELETE, POST_WRITE,
                    POST_DESTROY_CONTAINER, POST_LOOKUP_CONTAINER, POST_LOCK_CONTAINER,
                    POST_ADD_ASPECT, POST_REMOVE_ASPECT})));
}
