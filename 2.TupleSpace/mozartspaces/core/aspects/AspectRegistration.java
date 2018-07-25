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

import java.util.Set;


/**
 * Properties of an aspect registration that contains the aspect instance, its reference and the ipoints where it is
 * registered.
 *
 * @author Tobias Doenz
 */
public final class AspectRegistration {

    private final AspectReference aspectRef;
    private final ContainerAspect aspect;
    private final Set<InterceptionPoint> ipoints;

    /**
     * @param aspectRef
     *            the aspect reference
     * @param aspect
     *            the aspect instance
     * @param ipoints
     *            the ipoints where the aspect is (still) registered
     */
    public AspectRegistration(final AspectReference aspectRef, final ContainerAspect aspect,
            final Set<InterceptionPoint> ipoints) {
        this.aspectRef = aspectRef;
        this.aspect = aspect;
        this.ipoints = ipoints;
    }

    /**
     * @return the aspect reference
     */
    public AspectReference getAspectReference() {
        return aspectRef;
    }

    /**
     * @return the aspect instance
     */
    public ContainerAspect getAspect() {
        return aspect;
    }

    /**
     * @return the ipoints where the aspect is (still) registered
     */
    public Set<InterceptionPoint> getIPoints() {
        return ipoints;
    }

}
