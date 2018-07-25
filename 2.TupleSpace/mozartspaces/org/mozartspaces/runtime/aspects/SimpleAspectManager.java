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

import java.net.URI;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

import net.jcip.annotations.GuardedBy;
import net.jcip.annotations.ThreadSafe;

import org.mozartspaces.capi3.Transaction;
import org.mozartspaces.core.ContainerReference;
import org.mozartspaces.core.aspects.AbstractContainerAspect;
import org.mozartspaces.core.aspects.AspectException;
import org.mozartspaces.core.aspects.AspectReference;
import org.mozartspaces.core.aspects.AspectRegistration;
import org.mozartspaces.core.aspects.ContainerAspect;
import org.mozartspaces.core.aspects.ContainerIPoint;
import org.mozartspaces.core.aspects.InterceptionPoint;
import org.mozartspaces.core.aspects.SpaceAspect;
import org.mozartspaces.core.aspects.SpaceIPoint;
import org.mozartspaces.util.LoggerFactory;
import org.slf4j.Logger;

/**
 * A simple <code>AspectManager</code> that does not support the transactional
 * adding and removing of aspects, that is, the transaction arguments are
 * ignored and its isolation level is <code>read uncommitted</code>.
 *
 * @author Tobias Doenz
 */
@ThreadSafe
public final class SimpleAspectManager implements AspectManager {

    private static final Logger log = LoggerFactory.get();

    @GuardedBy("this")
    private int aspectIdCounter;

    private final Map<AspectReference, ContainerAspect> aspects;
    private final Map<AspectReference, Set<InterceptionPoint>> aspectIPoints;
    private final Map<AspectReference, ContainerReference> aspectContainers;
    private final Map<SpaceIPoint, List<SpaceAspect>> spaceAspects;
    private final Map<ContainerReference, Map<ContainerIPoint, List<ContainerAspect>>> allContainerAspects;

    private final URI thisSpace;

    /**
     * Constructs a <code>SimpleAspectManager</code>.
     *
     * @param thisSpace
     *            the URI to identify this space, used to create the aspect
     *            references
     */
    public SimpleAspectManager(final URI thisSpace) {
        this.thisSpace = thisSpace;
        assert this.thisSpace != null;

        aspects = new HashMap<AspectReference, ContainerAspect>();
        aspectIPoints = new HashMap<AspectReference, Set<InterceptionPoint>>();
        aspectContainers = new HashMap<AspectReference, ContainerReference>();

        spaceAspects = new EnumMap<SpaceIPoint, List<SpaceAspect>>(SpaceIPoint.class);
        for (SpaceIPoint sip : SpaceIPoint.values()) {
            spaceAspects.put(sip, new CopyOnWriteArrayList<SpaceAspect>());
        }

        allContainerAspects = new HashMap<ContainerReference, Map<ContainerIPoint, List<ContainerAspect>>>();
    }

    @Override
    public synchronized AspectRegistration addSpaceAspect(final SpaceAspect aspect,
            final Set<? extends InterceptionPoint> iPoints, final Transaction tx) {

        assert aspect != null;
        assert iPoints != null;

        // create new set that is modifiable
        Set<InterceptionPoint> points = new HashSet<InterceptionPoint>(iPoints);
        for (InterceptionPoint point : points) {
            spaceAspects.get(point).add(aspect);
        }
        AspectReference aspectRef = createAspectRef(null);
        aspects.put(aspectRef, aspect);
        aspectIPoints.put(aspectRef, points);
        log.debug("Added space aspect {} for ipoints {}", aspectRef, points);

        return new AspectRegistration(aspectRef, aspect, Collections.unmodifiableSet(points));
    }

    @Override
    public synchronized AspectRegistration addContainerAspect(final ContainerAspect aspect,
            final ContainerReference container, final Set<? extends InterceptionPoint> iPoints, final Transaction tx) {

        assert aspect != null;
        assert container != null;
        assert iPoints != null;

        // get (or create) aspect map for this container
        Map<ContainerIPoint, List<ContainerAspect>> containerAspects = allContainerAspects.get(container);
        if (containerAspects == null) {
            containerAspects = new EnumMap<ContainerIPoint, List<ContainerAspect>>(
                    ContainerIPoint.class);
            for (ContainerIPoint cip : ContainerIPoint.values()) {
                containerAspects.put(cip, new CopyOnWriteArrayList<ContainerAspect>());
            }
            allContainerAspects.put(container, containerAspects);
        }

        // add aspect for specified ipoints
        // create new set that is modifiable
        Set<InterceptionPoint> points = new HashSet<InterceptionPoint>(iPoints);
        for (InterceptionPoint point : points) {
            containerAspects.get(point).add(aspect);
        }

        AspectReference aspectRef = createAspectRef(container.getId());
        aspects.put(aspectRef, aspect);
        aspectIPoints.put(aspectRef, points);
        aspectContainers.put(aspectRef, container);
        log.debug("Added container aspect {} for ipoints {}", aspectRef, points);

        return new AspectRegistration(aspectRef, aspect, Collections.unmodifiableSet(points));
    }

    private AspectReference createAspectRef(final String containerId) {
        String id = Integer.toString(++aspectIdCounter);
        if (containerId != null) {
            // TODO keep this hack? check for underscore used in XVSMP Unmarshaller
            id += "_" + containerId;
        }
        return new AspectReference(id, thisSpace);
    }

    @Override
    public synchronized AspectRegistration removeAspect(final AspectReference aspectRef,
            final Set<? extends InterceptionPoint> iPoints, final Transaction tx) throws AspectException {

        assert aspectRef != null;

        ContainerAspect aspect = aspects.get(aspectRef);
        if (aspect == null) {
            throw new AspectException("Aspect " + aspectRef + " not registered");
        }
        ContainerReference container = aspectContainers.get(aspectRef);
        Set<? extends InterceptionPoint> removePoints = null;
        if (container == null) {
            // remove space aspect
            if (iPoints == null) {
                removePoints = SpaceIPoint.ALL_POINTS;
            } else {
                removePoints = iPoints;
            }
            for (InterceptionPoint point : removePoints) {
                spaceAspects.get(point).remove(aspect);
            }
            log.debug("Removed space aspect {} from ipoints {}", aspectRef, removePoints);
        } else {
            // remove container aspect
            if (iPoints == null) {
                removePoints = ContainerIPoint.ALL_POINTS;
            } else {
                removePoints = iPoints;
            }
            Map<ContainerIPoint, List<ContainerAspect>> containerAspects = allContainerAspects
                    .get(container);
            for (InterceptionPoint point : removePoints) {
                containerAspects.get(point).remove(aspect);
            }
            log.debug("Removed container aspect {} from ipoints {}", aspectRef, removePoints);
        }

        // remove aspect, if it has been removed from all ipoints
        Set<InterceptionPoint> registeredPoints = aspectIPoints.get(aspectRef);
        registeredPoints.removeAll(removePoints);
        if (registeredPoints.isEmpty()) {
            aspects.remove(aspectRef);
            aspectIPoints.remove(aspectRef);
            aspectContainers.remove(aspectRef);
            log.debug("Completely removed aspect (removed from all ipoints)");
        }
        return new AspectRegistration(aspectRef, aspect, Collections.unmodifiableSet(registeredPoints));
    }

    @Override
    public synchronized void removeContainer(final ContainerReference container, final Transaction tx) {
        assert container != null;
        Map<ContainerIPoint, List<ContainerAspect>> containerAspects = allContainerAspects.remove(container);
        if (containerAspects == null) {
            return;
        }
        Iterator<AspectReference> aspectsIt = aspectContainers.keySet().iterator();
        while (aspectsIt.hasNext()) {
            AspectReference aspectRef = aspectsIt.next();
            // there is a different aspect reference for each container (addAspect call)!
            if (aspectContainers.get(aspectRef).equals(container)) {
                aspects.remove(aspectRef);
                aspectIPoints.remove(aspectRef);
                aspectsIt.remove();
            }
            ContainerAspect aspect = aspects.get(aspectRef);
            if (aspect instanceof AbstractContainerAspect) {
                AbstractContainerAspect aspectInstance = (AbstractContainerAspect) aspect;
                try {
                    aspectInstance.aspectRemoved(new AspectRegistration(aspectRef, aspect,
                            Collections.<InterceptionPoint>emptySet()));
                } catch (RuntimeException ex) {
                    log.warn("Exception in aspectRemoved method of {}", aspectRef, ex);
                }
            }
        }
    }

    @Override
    public synchronized ContainerReference getContainerWhereAspectIsRegistered(final AspectReference aspectRef,
            final Transaction tx) {
        assert aspectRef != null;
        return aspectContainers.get(aspectRef);
    }

    @Override
    public synchronized List<ContainerAspect> getContainerAspects(final ContainerReference container,
            final ContainerIPoint point, final Transaction tx) {

        Map<ContainerIPoint, List<ContainerAspect>> containerAspects = allContainerAspects.get(container);
        if (containerAspects == null) {
            // log.debug("No aspects for container {}", container);
            return Collections.emptyList();
        }
        List<ContainerAspect> registeredContainerAspects = containerAspects.get(point);
        return registeredContainerAspects;
    }

    @Override
    public synchronized List<SpaceAspect> getSpaceAspects(final SpaceIPoint point, final Transaction tx) {
        return spaceAspects.get(point);
    }

}
