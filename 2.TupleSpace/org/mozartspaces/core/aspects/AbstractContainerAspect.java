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

import java.io.Serializable;
import java.util.List;

import net.jcip.annotations.ThreadSafe;

import org.mozartspaces.capi3.Capi3AspectPort;
import org.mozartspaces.capi3.SubTransaction;
import org.mozartspaces.capi3.Transaction;
import org.mozartspaces.core.ContainerReference;
import org.mozartspaces.core.MzsCore;
import org.mozartspaces.core.requests.AddAspectRequest;
import org.mozartspaces.core.requests.DeleteEntriesRequest;
import org.mozartspaces.core.requests.DestroyContainerRequest;
import org.mozartspaces.core.requests.LockContainerRequest;
import org.mozartspaces.core.requests.LookupContainerRequest;
import org.mozartspaces.core.requests.ReadEntriesRequest;
import org.mozartspaces.core.requests.RemoveAspectRequest;
import org.mozartspaces.core.requests.TakeEntriesRequest;
import org.mozartspaces.core.requests.TestEntriesRequest;
import org.mozartspaces.core.requests.WriteEntriesRequest;

/**
 * Container aspect with stub implementations for all interception point
 * methods. The purpose of this class is to ease the implementation of aspects
 * that are invoked for only a few interception points. The stub implementations
 * return <code>null</code>, which is equivalent to an <code>AspectResult</code>
 * with {@link AspectStatus} <code>OK</code>. Additionally, a reference to the
 * core where this aspect is executed can be obtained by calling
 * {@link #getCore()}, after an instance of this class has been added to a
 * container.
 *
 * @author Tobias Doenz
 *
 * @see AbstractSpaceAspect
 */
@ThreadSafe
public abstract class AbstractContainerAspect implements ContainerAspect {

    private static final long serialVersionUID = 1L;

    private transient volatile MzsCore core;

    /**
     * Gets a reference to the core where this aspect is executed, after the aspect has been added.
     *
     * @return the core
     */
    protected final MzsCore getCore() {
        return core;
    }

    /**
     * Sets the core instance. Do not invoke from outside the core! This method is intended to set the reference of the
     * core on which the aspect is added. It is called in the Runtime when a
     * {@link org.mozartspaces.core.requests.AddAspectRequest AddAspectRequest} is processed. The corresponding field is
     * <code>transient</code>.
     *
     * @param core
     *            the core instance
     */
    public final void setCore(final MzsCore core) {
        this.core = core;
    }

    /**
     * Method for initialization of the aspect. Do not invoke from outside the core! This method is called in the
     * Runtime after the aspect has been added.
     *
     * @param core
     *            the core instance where the aspect is registered
     * @param registration
     *            the aspect registration
     */
    public void aspectAdded(final MzsCore core, final AspectRegistration registration) {
    }

    /**
     * Method for cleanup of resources in the aspect. Do not invoke from outside the core! This method is called in the
     * Runtime after the aspect has been removed from some or all aspect points. This method is also called when a
     * container is destroyed and this aspect has been registered at it.
     *
     * @param registration
     *            the aspect registration
     */
    public void aspectRemoved(final AspectRegistration registration) {
    }

    // TODO move aspectAdded and aspectRemoved methods to ContainerAspect interface (would be a breaking API change)?

    // CHECKSTYLE:OFF (deactivate warnings that methods are not "final")

    @Override
    public AspectResult postAddAspect(final AddAspectRequest request, final Transaction tx,
            final SubTransaction stx, final Capi3AspectPort capi3, final int executionCount,
            final AspectReference aspect) {
        return null;
    }

    @Override
    public AspectResult postDelete(final DeleteEntriesRequest request, final Transaction tx, final SubTransaction stx,
            final Capi3AspectPort capi3, final int executionCount, final List<Serializable> entries) {
        return null;
    }

    @Override
    public AspectResult postDestroyContainer(final DestroyContainerRequest request, final Transaction tx,
            final SubTransaction stx, final int executionCount) {
        return null;
    }

    @Override
    public AspectResult postLookupContainer(final LookupContainerRequest request, final Transaction tx,
            final SubTransaction stx, final Capi3AspectPort capi3, final int executionCount,
            final ContainerReference container) {
        return null;
    }

    @Override
    public AspectResult postRead(final ReadEntriesRequest<?> request, final Transaction tx, final SubTransaction stx,
            final Capi3AspectPort capi3, final int executionCount, final List<Serializable> entries) {
        return null;
    }

    @Override
    public AspectResult postRemoveAspect(final RemoveAspectRequest request, final Transaction tx,
            final SubTransaction stx, final ContainerReference cRef, final Capi3AspectPort capi3,
            final int executionCount) {
        return null;
    }

    @Override
    public AspectResult postLockContainer(final LockContainerRequest request, final Transaction tx,
            final SubTransaction stx, final Capi3AspectPort capi3, final int executionCount) {
        return null;
    }

    @Override
    public AspectResult postTake(final TakeEntriesRequest<?> request, final Transaction tx, final SubTransaction stx,
            final Capi3AspectPort capi3, final int executionCount, final List<Serializable> entries) {
        return null;
    }

    @Override
    public AspectResult postTest(final TestEntriesRequest request, final Transaction tx, final SubTransaction stx,
            final Capi3AspectPort capi3, final int executionCount, final List<Serializable> entries) {
        return null;
    }

    @Override
    public AspectResult postWrite(final WriteEntriesRequest request, final Transaction tx, final SubTransaction stx,
            final Capi3AspectPort capi3, final int executionCount) {
        return null;
    }

    @Override
    public AspectResult preAddAspect(final AddAspectRequest request, final Transaction tx,
            final SubTransaction stx, final Capi3AspectPort capi3, final int executionCount) {
        return null;
    }

    @Override
    public AspectResult preDelete(final DeleteEntriesRequest request, final Transaction tx, final SubTransaction stx,
            final Capi3AspectPort capi3, final int executionCount) {
        return null;
    }

    @Override
    public AspectResult preDestroyContainer(final DestroyContainerRequest request, final Transaction tx,
            final SubTransaction stx, final Capi3AspectPort capi3, final int executionCount) {
        return null;
    }

    @Override
    public AspectResult preRead(final ReadEntriesRequest<?> request, final Transaction tx, final SubTransaction stx,
            final Capi3AspectPort capi3, final int executionCount) {
        return null;
    }

    @Override
    public AspectResult preRemoveAspect(final RemoveAspectRequest request, final Transaction tx,
            final SubTransaction stx, final ContainerReference cRef, final Capi3AspectPort capi3,
            final int executionCount) {
        return null;
    }

    @Override
    public AspectResult preLockContainer(final LockContainerRequest request, final Transaction tx,
            final SubTransaction stx, final Capi3AspectPort capi3, final int executionCount) {
        return null;
    }

    @Override
    public AspectResult preTake(final TakeEntriesRequest<?> request, final Transaction tx, final SubTransaction stx,
            final Capi3AspectPort capi3, final int executionCount) {
        return null;
    }

    @Override
    public AspectResult preTest(final TestEntriesRequest request, final Transaction tx, final SubTransaction stx,
            final Capi3AspectPort capi3, final int executionCount) {
        return null;
    }

    @Override
    public AspectResult preWrite(final WriteEntriesRequest request, final Transaction tx, final SubTransaction stx,
            final Capi3AspectPort capi3, final int executionCount) {
        return null;
    }

}
