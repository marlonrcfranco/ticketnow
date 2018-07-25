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

import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import net.jcip.annotations.ThreadSafe;

import org.mozartspaces.capi3.Capi3;
import org.mozartspaces.capi3.Capi3AspectPort;
import org.mozartspaces.capi3.EntryOperationResult;
import org.mozartspaces.capi3.IsolationLevel;
import org.mozartspaces.capi3.LocalContainerReference;
import org.mozartspaces.capi3.OperationResult;
import org.mozartspaces.capi3.OperationStatus;
import org.mozartspaces.capi3.Selector;
import org.mozartspaces.capi3.SubTransaction;
import org.mozartspaces.core.ContainerReference;
import org.mozartspaces.core.Entry;
import org.mozartspaces.core.RequestContext;
import org.mozartspaces.runtime.blocking.WaitForCategory;

/**
 * Provides a CAPI-3 interface limited to operations on one specific container. This is used for the aspects.
 *
 * @author Martin Barisits
 * @author Tobias Doenz
 */
// TODO move interface to core-common or runtime
// allow use of arbitrary container; allow container operations too
// rename to ContainerCapi3 (interface) and DefaultContainerCapi3?
@ThreadSafe
public final class DefaultCapi3AspectPort implements Capi3AspectPort {

    private final ContainerReference container;
    private final LocalContainerReference cref;
    private final Capi3 capi3;

    /*
     * This set can contain events for stx that were created AND rollbacked inside an aspect and should actually not be
     * triggered. But we ignore this, better to many than to few events.
     */
    private volatile Set<WaitForCategory> eventCategories;

    /**
     * Creates a new DefaultCapi3AspectPort.
     *
     * @param container
     *            ContainerReference of this AspectPort
     * @param capi3
     *            Capi3 to channel the operations to
     */
    public DefaultCapi3AspectPort(final ContainerReference container, final Capi3 capi3) {
        this.container = container;
        assert this.container != null;
        this.capi3 = capi3;
        assert this.capi3 != null;

        cref = new LocalContainerReference(container.getId());
    }

    private void initEventCategoriesSet() {
        if (eventCategories == null) {
            eventCategories = Collections.synchronizedSet(EnumSet.of(WaitForCategory.UNLOCK_ST));
        }
    }

    /**
     * Returns the event categories of the executed operations. The set is not copied for performance reasons.
     *
     * @return the event categories
     */
    public Set<WaitForCategory> getEventCategories() {
        return eventCategories;
    }

    @Override
    public EntryOperationResult executeReadOperation(final List<? extends Selector> selectors,
            final IsolationLevel isolationLevel, final SubTransaction stx, final RequestContext context) {
        EntryOperationResult result = capi3.executeReadOperation(this.cref, selectors, isolationLevel, stx, context);
        initEventCategoriesSet();
        return result;
    }

    @Override
    public EntryOperationResult executeTakeOperation(final List<? extends Selector> selectors,
            final IsolationLevel isolationLevel, final SubTransaction stx, final RequestContext context) {
        EntryOperationResult result = capi3.executeTakeOperation(this.cref, selectors, isolationLevel, stx, context);
        initEventCategoriesSet();
        if (result.getStatus() == OperationStatus.OK) {
            eventCategories.add(WaitForCategory.REMOVE);
        }
        return result;
    }

    @Override
    public OperationResult executeWriteOperation(final Entry entry, final IsolationLevel isolationLevel,
            final SubTransaction stx, final RequestContext context) {
        OperationResult result = capi3.executeWriteOperation(this.cref, entry, isolationLevel, stx, context);
        initEventCategoriesSet();
        if (result.getStatus() == OperationStatus.OK) {
            eventCategories.add(WaitForCategory.INSERT);
        }
        return result;
    }

}
