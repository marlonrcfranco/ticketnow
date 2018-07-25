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

import java.net.URI;

import net.jcip.annotations.Immutable;

/**
 * A <code>Message</code> that contains a <code>Request</code>.
 *
 * @author Tobias Doenz
 */
@Immutable
public final class RequestMessage extends AbstractMessage<Request<?>> {

    private static final long serialVersionUID = 1L;

    private final URI destinationSpace;

    private static final Request<?> DUMMY_REQUEST = new Request<String>() {
        private static final long serialVersionUID = 1L;
    };

    /**
     * Constructs a <code>RequestMessage</code>.
     *
     * @param requestRef
     *            the request reference
     * @param request
     *            the request
     * @param destinationSpace
     *            the space to which the request should be sent, you can use
     *            <code>null</code> for the embedded space
     * @param answerContainerInfo
     *            the answer container information, may be <code>null</code>
     */
    public RequestMessage(final RequestReference requestRef, final Request<?> request, final URI destinationSpace,
            final AnswerContainerInfo answerContainerInfo) {
        super(requestRef, request, answerContainerInfo);
        this.destinationSpace = destinationSpace;
    }

    private RequestMessage() {
        super(RequestReference.DUMMY, DUMMY_REQUEST, null);
        this.destinationSpace = null;
    }

    @Override
    public URI getDestinationSpace() {
        return destinationSpace;
    }
}
