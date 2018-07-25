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
package org.mozartspaces.runtime.aspects;

import java.util.List;
import java.util.Set;

import org.mozartspaces.capi3.Transaction;
import org.mozartspaces.core.ContainerReference;
import org.mozartspaces.core.aspects.AspectException;
import org.mozartspaces.core.aspects.AspectReference;
import org.mozartspaces.core.aspects.AspectRegistration;
import org.mozartspaces.core.aspects.ContainerAspect;
import org.mozartspaces.core.aspects.ContainerIPoint;
import org.mozartspaces.core.aspects.InterceptionPoint;
import org.mozartspaces.core.aspects.SpaceAspect;
import org.mozartspaces.core.aspects.SpaceIPoint;

/**
 * Interface with the methods to add and remove container and space aspects as
 * well as methods to get the aspects for a specific interception point,
 * transaction, and container.
 *
 * @author Tobias Doenz
 */
public interface AspectManager {

    /**
     * Adds a space aspect for the specified interception points.
     *
     * @param aspect
     *            the space aspect to add
     * @param iPoints
     *            the container interception points where this aspect should be
     *            added
     * @param tx
     *            the transaction, may be <code>null</code>
     * @return the aspect registration
     */
    AspectRegistration addSpaceAspect(SpaceAspect aspect, Set<? extends InterceptionPoint> iPoints, Transaction tx);

    /**
     * Adds a container aspect for the specified interception points.
     *
     * @param aspect
     *            the container aspect to add
     * @param container
     *            the container where the aspect should be added
     * @param iPoints
     *            the container interception points where this aspect should be
     *            added
     * @param tx
     *            the transaction, may be <code>null</code>
     * @return the aspect registration
     */
    AspectRegistration addContainerAspect(ContainerAspect aspect, ContainerReference container,
            Set<? extends InterceptionPoint> iPoints, Transaction tx);

    /**
     * Removes an aspect from the specified interception points.
     *
     * @param aspectRef
     *            the reference of the aspect to remove
     * @param iPoints
     *            the interception points where this aspect should be removed
     * @param tx
     *            the transaction, may be <code>null</code>
     * @throws org.mozartspaces.core.aspects.AspectException
     *             if the aspect is not registered and thus cannot be removed
     * @return the aspect registration
     */
    AspectRegistration removeAspect(AspectReference aspectRef, Set<? extends InterceptionPoint> iPoints, Transaction tx)
            throws AspectException;

    /**
     * Removes a container from the aspect map, used when it has been destroyed.
     * This removes also all aspects that are registered on this container.
     *
     * @param container
     *            the container that should be removed from the aspect map
     * @param tx
     *            the transaction, may be <code>null</code>
     */
    void removeContainer(final ContainerReference container, final Transaction tx);

    /**
     * Gets the reference of the container where an aspect is registered.
     *
     * @param aspectRef
     *            the reference of the aspect
     * @param tx
     *            the transaction, may be <code>null</code>
     * @return the reference of the container where the aspect is registered, or
     *         <code>null</code> if the aspect is a space aspect or not
     *         registered at all
     */
    ContainerReference getContainerWhereAspectIsRegistered(AspectReference aspectRef, final Transaction tx);

    /**
     * Gets the list of the registered container aspects for a container,
     * interception point, and transaction.
     *
     * @param container
     *            the reference of the container
     * @param iPoint
     *            the interception point
     * @param tx
     *            the transaction, may be <code>null</code>
     * @return the list of the registered container aspects for this container,
     *         interception point, and transaction.
     */
    List<ContainerAspect> getContainerAspects(ContainerReference container, ContainerIPoint iPoint,
            Transaction tx);

    /**
     * Gets the list of the registered space aspects for an interception point,
     * and transaction.
     *
     * @param iPoint
     *            the interception point
     * @param tx
     *            the transaction, may be <code>null</code>
     * @return the list of the registered space aspects for this interception
     *         point and transaction.
     */
    List<SpaceAspect> getSpaceAspects(SpaceIPoint iPoint, Transaction tx);

}
