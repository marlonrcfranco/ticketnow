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
package org.mozartspaces.xvsmp.xstream;

import org.mozartspaces.core.ContainerReference;
import org.mozartspaces.core.RequestReference;
import org.mozartspaces.core.TransactionReference;
import org.mozartspaces.core.aspects.AspectReference;
import org.mozartspaces.core.util.CoreUtils;

import com.thoughtworks.xstream.converters.SingleValueConverter;

/**
 * Converters for the various references used in the MozartSpaces core.
 *
 * @author Tobias Doenz
 */
public final class ReferenceConverters {

    /**
     * Converts an {@link AspectReference} to and from a string.
     *
     * @author Tobias Doenz
     */
    static class AspectReferenceConverter implements SingleValueConverter {

        @Override
        public boolean canConvert(@SuppressWarnings("rawtypes") final Class type) {
            return type == AspectReference.class;
        }

        @Override
        public String toString(final Object obj) {
            return obj == null ? null : ((AspectReference) obj).getStringRepresentation();
        }

        @Override
        public Object fromString(final String str) {
            return CoreUtils.parseAspectReferenceWithoutPrefix(str);
        }

    }

    /**
     * Converts a {@link ContainerReference} to and from a string.
     *
     * @author Tobias Doenz
     */
    static class ContainerReferenceConverter implements SingleValueConverter {

        @Override
        public boolean canConvert(@SuppressWarnings("rawtypes") final Class type) {
            return type == ContainerReference.class;
        }

        @Override
        public String toString(final Object obj) {
            return obj == null ? null : ((ContainerReference) obj).getStringRepresentation();
        }

        @Override
        public Object fromString(final String str) {
            return CoreUtils.parseContainerReferenceWithoutPrefix(str);
        }
    }

    /**
     * Converts a {@link RequestReference} to and from a string.
     *
     * @author Tobias Doenz
     */
    static class RequestReferenceConverter implements SingleValueConverter {

        @Override
        public boolean canConvert(@SuppressWarnings("rawtypes") final Class type) {
            return type == RequestReference.class;
        }

        @Override
        public String toString(final Object obj) {
            return obj == null ? null : ((RequestReference) obj).getStringRepresentation();
        }

        @Override
        public Object fromString(final String str) {
            return CoreUtils.parseRequestReferenceWithoutPrefix(str);
        }
    }

    /**
     * Converts a {@link TransactionReference} to and from a string.
     *
     * @author Tobias Doenz
     */
    static class TransactionReferenceConverter implements SingleValueConverter {

        @Override
        public boolean canConvert(@SuppressWarnings("rawtypes") final Class type) {
            return type == TransactionReference.class;
        }

        @Override
        public String toString(final Object obj) {
            return obj == null ? null : ((TransactionReference) obj).getStringRepresentation();
        }

        @Override
        public Object fromString(final String str) {
            return CoreUtils.parseTransactionReferenceWithoutPrefix(str);
        }
    }

    private ReferenceConverters() {
    }
}
