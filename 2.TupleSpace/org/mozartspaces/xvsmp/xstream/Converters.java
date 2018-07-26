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

import java.net.URI;

import org.mozartspaces.core.util.Nothing;

import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.converters.basic.AbstractSingleValueConverter;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

/**
 * Miscellaneous XStream converters.
 *
 * @author Tobias Doenz
 */
public final class Converters {

    /**
     * Converts a {@link URI} to and from a string.
     *
     * @author Tobias Doenz
     */
    static class URIConverter extends AbstractSingleValueConverter {

        @Override
        public boolean canConvert(@SuppressWarnings("rawtypes") final Class type) {
            return type == URI.class;
        }

        @Override
        public Object fromString(final String str) {
            return URI.create(str);
        }

    }

    /**
     * Converts {@link Nothing} to and from XML.
     *
     * @author Tobias Doenz
     */
    static class NothingConverter implements Converter {

        @Override
        public void marshal(final Object source, final HierarchicalStreamWriter writer,
                final MarshallingContext context) {
            // nothing to do, just one possible value that can be ignored
        }

        @Override
        public Object unmarshal(final HierarchicalStreamReader reader, final UnmarshallingContext context) {
            return Nothing.INSTANCE;
        }

        @Override
        public boolean canConvert(@SuppressWarnings("rawtypes") final Class type) {
            return type == Nothing.class;
        }

    }

    private Converters() {
    }
}
