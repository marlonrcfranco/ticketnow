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

/**
 * Needs to be implemented by objects that want to be notified about entry operation events.
 *
 * @author Tobias Doenz
 */
public interface NotificationListener {

    /**
     * Notifies about the successful execution of an entry operation on an observed container.
     *
     * @param source
     *            the notification that fired the event
     * @param operation
     *            the operation that was executed
     * @param entries
     *            the entries that were affected by the operation, that is, read, tested, taken, deleted or written; for
     *            a {@code WRITE} operation this is the list of {@link org.mozartspaces.core.Entry Entry} objects
     *            (including the application object and the coordination data for each {@code Entry}) that is written to
     *            a container; for all other objects this is a list of the application objects (without the coordination
     *            data)
     */
    void entryOperationFinished(Notification source, Operation operation, List<? extends Serializable> entries);

}
