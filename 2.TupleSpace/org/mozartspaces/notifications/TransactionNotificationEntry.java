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
package org.mozartspaces.notifications;

import java.io.Serializable;

import net.jcip.annotations.Immutable;

/**
 * Contains the information how the transaction ended, with a commit or a rollback.
 *
 * @author Tobias Doenz
 */
@Immutable
public final class TransactionNotificationEntry implements Serializable {

    private static final long serialVersionUID = 1L;

    private final TransactionEndOperation operation;

    /**
     * @param operation
     *            the operation that ended the transaction
     */
    public TransactionNotificationEntry(final TransactionEndOperation operation) {
        this.operation = operation;
        assert this.operation != null;
    }

    /**
     * @return the transaction end operation
     */
    public TransactionEndOperation getOperation() {
        return operation;
    }

}
