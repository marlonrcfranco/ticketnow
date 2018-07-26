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
package org.mozartspaces.core;

import java.io.Serializable;
import java.net.URI;

/**
 * An object that is used for communication and transport of requests and
 * responses. Messages are used internally in the Runtime and for the
 * communication between different XVSM cores.
 *
 * @author Tobias Doenz
 *
 * @param <T>
 *            the type of the message content object
 */
public interface Message<T> extends Serializable {

    /**
     * @return the reference of the request this message corresponds to
     */
    RequestReference getRequestReference();

    /**
     * @return the message content
     */
    T getContent();

    /**
     * @return the information to write an answer to a non-virtual container,
     *         may be <code>null</code>
     */
    AnswerContainerInfo getAnswerContainerInfo();

    /**
     * @return the Space URI where this message should be sent to
     */
    URI getDestinationSpace();

}
