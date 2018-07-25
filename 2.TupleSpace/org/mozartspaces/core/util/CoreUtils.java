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
package org.mozartspaces.core.util;

import java.net.URI;

import net.jcip.annotations.ThreadSafe;

import org.mozartspaces.core.ContainerReference;
import org.mozartspaces.core.Reference;
import org.mozartspaces.core.RequestReference;
import org.mozartspaces.core.TransactionReference;
import org.mozartspaces.core.aspects.AspectReference;

/**
 * Various helper functions, not as static methods to avoid static dependencies
 * and improve testability.
 *
 * @author Tobias Doenz
 */
@ThreadSafe
public final class CoreUtils {

    private volatile URI spaceUri;

    /**
     * Constructs an instance.
     *
     * @param spaceUri
     *            the URI of this space
     */
    public CoreUtils(final URI spaceUri) {
        setSpaceUri(spaceUri);
    }

    /**
     * @param spaceUri
     *            the URI of this space
     */
    public void setSpaceUri(final URI spaceUri) {
        this.spaceUri = spaceUri;
        assert this.spaceUri != null;
    }

    /**
     * Tells whether a space URI identifies the embedded space. Note:
     * <code>true</code> is returned for the <code>null</code> URI.
     *
     * @param space
     *            a space URI, may be <code>null</code>
     * @return <code>true</code>, if the space URI identifies the embedded
     *         space.
     */
    public boolean isEmbeddedSpace(final URI space) {
        if (space == null || space.equals(spaceUri)) {
            return true;
        }
        return false;
    }

    /**
     * Creates a URI "clone" with the specified port.
     *
     * @param uri
     *            the URI to "clone"
     * @param port
     *            the port to use
     * @return the new URI
     */
    public static URI cloneUriWithNewPort(final URI uri, final int port) {
        String str = uri.toString();
        str = str.substring(0, str.lastIndexOf(':') + 1) + port;
        return URI.create(str);
    }

    /**
     * Constants representing the reference types in the converters.
     *
     * @author Tobias Doenz
     */
    private static enum ReferenceType {
        ASPECT, CONTAINER, REQUEST, TRANSACTION
    }

    private static final int ASPECT_PATH_PREFIX_LENGTH = AspectReference.PATH_PREFIX.length();
    private static final int CONTAINER_PATH_PREFIX_LENGTH = ContainerReference.PATH_PREFIX.length();
    private static final int REQUEST_PATH_PREFIX_LENGTH = RequestReference.PATH_PREFIX.length();
    private static final int TRANSACTION_PATH_PREFIX_LENGTH = TransactionReference.PATH_PREFIX.length();

    /**
     * Parses and creates an <code>AspectReference</code> from a string.
     *
     * @param str
     *            the string to parse
     * @return the created <code>AspectReference</code>
     */
    public static AspectReference parseAspectReference(final String str) {
        return (AspectReference) parseReference(str, ReferenceType.ASPECT, ASPECT_PATH_PREFIX_LENGTH);
    }

    /**
     * Parses and creates an <code>AspectReference</code> from a string.
     *
     * @param str
     *            the string to parse
     * @return the created <code>AspectReference</code>
     */
    public static AspectReference parseAspectReferenceWithoutPrefix(final String str) {
        return (AspectReference) parseReference(str, ReferenceType.ASPECT, 1);
    }

    /**
     * Parses and creates a <code>ContainerReference</code> from a string.
     *
     * @param str
     *            the string to parse
     * @return the created <code>ContainerReference</code>
     */
    public static ContainerReference parseContainerReference(final String str) {
        return (ContainerReference) parseReference(str, ReferenceType.CONTAINER, CONTAINER_PATH_PREFIX_LENGTH);
    }

    /**
     * Parses and creates a <code>ContainerReference</code> from a string.
     *
     * @param str
     *            the string to parse
     * @return the created <code>ContainerReference</code>
     */
    public static ContainerReference parseContainerReferenceWithoutPrefix(final String str) {
        return (ContainerReference) parseReference(str, ReferenceType.CONTAINER, 1);
    }

    /**
     * Parses and creates a <code>RequestReference</code> from a string.
     *
     * @param str
     *            the string to parse
     * @return the created <code>RequestReference</code>
     */
    public static RequestReference parseRequestReference(final String str) {
        return (RequestReference) parseReference(str, ReferenceType.REQUEST, REQUEST_PATH_PREFIX_LENGTH);
    }

    /**
     * Parses and creates a <code>RequestReference</code> from a string.
     *
     * @param str
     *            the string to parse
     * @return the created <code>RequestReference</code>
     */
    public static RequestReference parseRequestReferenceWithoutPrefix(final String str) {
        return (RequestReference) parseReference(str, ReferenceType.REQUEST, 1);
    }

    /**
     * Parses and creates a <code>TransactionReference</code> from a string.
     *
     * @param str
     *            the string to parse
     * @return the created <code>TransactionReference</code>
     */
    public static TransactionReference parseTransactionReference(final String str) {
        return (TransactionReference) parseReference(str, ReferenceType.TRANSACTION, TRANSACTION_PATH_PREFIX_LENGTH);
    }

    /**
     * Parses and creates a <code>TransactionReference</code> from a string.
     *
     * @param str
     *            the string to parse
     * @return the created <code>TransactionReference</code>
     */
    public static TransactionReference parseTransactionReferenceWithoutPrefix(final String str) {
        return (TransactionReference) parseReference(str, ReferenceType.TRANSACTION, 1);
    }

    private static Reference<?> parseReference(final String str, final ReferenceType refType,
            final int pathPrefixLength) {

        if (str == null) {
            return null;
        }

        int idStartIndex = str.lastIndexOf('/') + 1;
        if (idStartIndex < pathPrefixLength) {
            throw new IllegalArgumentException("Invalid reference string " + str);
        }
        String id = str.substring(idStartIndex);
        URI space = URI.create(str.substring(0, idStartIndex - pathPrefixLength));

        switch (refType) {
        case ASPECT:
            return new AspectReference(id, space);
        case CONTAINER:
            return new ContainerReference(id, space);
        case REQUEST:
            return new RequestReference(id, space);
        case TRANSACTION:
            return new TransactionReference(id, space);
        default:
            throw new AssertionError();
        }
    }

}
