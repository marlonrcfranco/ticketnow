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
import java.util.List;

import net.jcip.annotations.Immutable;

/**
 * Contains the information of a notification event, that is, the operation type and the corresponding entries.
 * Instances of this class are created in {@link NotificationAspect} and written to the notification container where
 * they are taken by a {@link Notification}.
 *
 * @author Tobias Doenz
 */
@Immutable
final class NotificationEntry implements Serializable {

    private static final long serialVersionUID = 1L;

    private final Operation operation;
    private final List<? extends Serializable> entries;

    /**
     * Constructs a <code>NotificationEntry</code>.
     *
     * @param operation
     *            the operation
     * @param entries
     *            the entries, returned like by the corresponding request
     */
    NotificationEntry(final Operation operation, final List<? extends Serializable> entries) {
        this.operation = operation;
        assert this.operation != null;
        this.entries = entries;
        assert this.entries != null;
    }

    /**
     * @return the operation
     */
    public Operation getOperation() {
        return operation;
    }

    /**
     * @return the entries
     */
    public List<? extends Serializable> getEntries() {
        return entries;
    }
}
