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

import org.mozartspaces.capi3.Capi3AspectPort;
import org.mozartspaces.capi3.SubTransaction;
import org.mozartspaces.capi3.Transaction;
import org.mozartspaces.core.ContainerReference;
import org.mozartspaces.core.TransactionReference;
import org.mozartspaces.core.requests.CommitTransactionRequest;
import org.mozartspaces.core.requests.CreateContainerRequest;
import org.mozartspaces.core.requests.CreateTransactionRequest;
import org.mozartspaces.core.requests.LookupContainerRequest;
import org.mozartspaces.core.requests.PrepareTransactionRequest;
import org.mozartspaces.core.requests.RollbackTransactionRequest;
import org.mozartspaces.core.requests.ShutdownRequest;

/**
 * Space aspect with stub implementations for all interception point methods.
 * The purpose of this class is to ease the implementation of aspects that are
 * invoked for only a few interception points. The stub implementations return
 * <code>null</code>, which is equivalent to an <code>AspectResult</code> with
 * {@link AspectStatus} <code>OK</code>. Additionally, a reference to the core
 * where this aspect is executed can be obtained by calling {@link #getCore()},
 * after an instance of this class has been added to a space.
 *
 * @author Tobias Doenz
 *
 * @see AbstractContainerAspect
 */
public abstract class AbstractSpaceAspect extends AbstractContainerAspect implements SpaceAspect {

    private static final long serialVersionUID = 1L;

    // CHECKSTYLE:OFF (deactivate warnings that methods are not "final")

    @Override
    public AspectResult postCommitTransaction(final CommitTransactionRequest request, final Transaction tx) {
        return null;
    }

    @Override
    public AspectResult postCreateContainer(final CreateContainerRequest request, final Transaction tx,
            final SubTransaction stx, final Capi3AspectPort capi3, final int executionCount,
            final ContainerReference container) {
        return null;
    }

    @Override
    public AspectResult postCreateTransaction(final CreateTransactionRequest request,
            final TransactionReference txRef, final Transaction tx) {
        return null;
    }

    @Override
    public AspectResult postPrepareTransaction(final PrepareTransactionRequest request, final Transaction tx) {
        return null;
    }

    @Override
    public AspectResult postRollbackTransaction(final RollbackTransactionRequest request, final Transaction tx) {
        return null;
    }

    @Override
    public AspectResult preCommitTransaction(final CommitTransactionRequest request, final Transaction tx) {
        return null;
    }

    @Override
    public AspectResult preCreateContainer(final CreateContainerRequest request, final Transaction tx,
            final SubTransaction stx, final int executionCount) {
        return null;
    }

    @Override
    public AspectResult preCreateTransaction(final CreateTransactionRequest request) {
        return null;
    }

    @Override
    public AspectResult preLookupContainer(final LookupContainerRequest request, final Transaction tx,
            final SubTransaction stx, final int executionCount) {
        return null;
    }

    @Override
    public AspectResult prePrepareTransaction(final PrepareTransactionRequest request, final Transaction tx) {
        return null;
    }

    @Override
    public AspectResult preRollbackTransaction(final RollbackTransactionRequest request, final Transaction tx) {
        return null;
    }

    @Override
    public AspectResult preShutdown(final ShutdownRequest request) {
        return null;
    }

}
