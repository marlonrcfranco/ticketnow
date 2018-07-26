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
package org.mozartspaces.runtime.util;

import java.net.URI;

import net.jcip.annotations.Immutable;

import org.mozartspaces.capi3.LocalContainerReference;
import org.mozartspaces.capi3.Transaction;
import org.mozartspaces.core.ContainerReference;
import org.mozartspaces.core.TransactionReference;

/**
 * Various helper functions, not as static methods to avoid static dependencies
 * and improve testability.
 *
 * @author Tobias Doenz
 */
@Immutable
public final class RuntimeUtils {

    private final URI thisSpace;

    /**
     * Constructs an instance.
     *
     * @param thisSpace
     *            the URI of this space
     */
    public RuntimeUtils(final URI thisSpace) {
        this.thisSpace = thisSpace;
        assert this.thisSpace != null;
    }

    /**
     * Creates a globally unique reference for a transaction from an internal
     * transaction object.
     *
     * @param tx
     *            the internal transaction object
     * @return the created transaction reference
     */
    public TransactionReference createTransactionReference(final Transaction tx) {
        return new TransactionReference(tx.getId(), thisSpace);
    }

    /**
     * Creates a globally unique reference for a container from an internal
     * container reference.
     *
     * @param localCRef
     *            the internal container reference
     * @return the created global container reference
     */
    public ContainerReference createContainerReference(final LocalContainerReference localCRef) {
        return new ContainerReference(localCRef.getId(), thisSpace);
    }

}
