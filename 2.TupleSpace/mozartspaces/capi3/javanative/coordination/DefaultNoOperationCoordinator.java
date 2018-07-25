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
package org.mozartspaces.capi3.javanative.coordination;

import java.util.ArrayList;
import java.util.List;

import org.mozartspaces.capi3.AccessDeniedException;
import org.mozartspaces.capi3.Capi3Exception;
import org.mozartspaces.capi3.CoordinationData;
import org.mozartspaces.capi3.CoordinatorLockedException;
import org.mozartspaces.capi3.CountNotMetException;
import org.mozartspaces.capi3.EntryLockedException;
import org.mozartspaces.capi3.IsolationLevel;
import org.mozartspaces.capi3.OperationType;
import org.mozartspaces.capi3.javanative.authorization.AuthorizationResult;
import org.mozartspaces.capi3.javanative.isolation.NativeSubTransaction;
import org.mozartspaces.capi3.javanative.operation.NativeContainer;
import org.mozartspaces.capi3.javanative.operation.NativeEntry;
import org.mozartspaces.core.MzsCoreRuntimeException;
import org.mozartspaces.core.RequestContext;

/**
 * No Operation Coordinator for Test Purposes.
 *
 * @author Martin Barisits
 * @author Stefan Crass
 */
public final class DefaultNoOperationCoordinator extends AbstractDefaultCoordinator {

    private static final long serialVersionUID = -1693759530075066073L;

    /**
     * Creates a DefaultNoOperationCoordinator.
     *
     * @param name
     *            name of the Coordinator
     */
    public DefaultNoOperationCoordinator(final String name) {
        super(name);
    }

    @Override
    public void init(final NativeContainer container, final NativeSubTransaction stx, final RequestContext context)
            throws MzsCoreRuntimeException {
    }

    @Override
    public void close() throws MzsCoreRuntimeException {
    }

    @Override
    public void prepareEntryRemoval(final NativeSubTransaction stx, final NativeEntry entry,
            final RequestContext context) throws CoordinatorLockedException {
        return;
    }

    @Override
    public boolean registerEntry(final NativeSubTransaction stx, final CoordinationData coordData,
            final NativeEntry entry, final RequestContext context) throws Capi3Exception {
        return true;
    }

    @Override
    public boolean unregisterEntry(final NativeEntry entry, final RequestContext context,
            final NativeSubTransaction stx) {
        return true;
    }

    /**
     * The NoOperationSelector.
     *
     * @author Martin Barisits
     * @author Stefan Crass
     */
    public static final class DefaultNoOperationSelector extends AbstractNativeSelector<DefaultNoOperationCoordinator> {

        private static final long serialVersionUID = 7247632216488530383L;

        DefaultNoOperationSelector(final String name, final int count) {
            super(name, count);
        }

        @Override
        public List<NativeEntry> getAll(final IsolationLevel isolationLevel, final AuthorizationResult auth,
                final NativeSubTransaction stx, final OperationType opType, final RequestContext context)
                throws CountNotMetException, EntryLockedException, AccessDeniedException {
            return new ArrayList<NativeEntry>();
        }

        @Override
        public NativeEntry getNext(final IsolationLevel isolationLevel, final AuthorizationResult auth,
                final NativeSubTransaction stx, final OperationType opType, final RequestContext context)
                throws CountNotMetException, EntryLockedException, AccessDeniedException {
            return null;
        }

        @Override
        public void linkEntries(final DefaultNoOperationCoordinator coordinator) {
            return;
        }
    }

}
