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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.Charset;

import org.mozartspaces.core.util.SerializationException;
import org.mozartspaces.core.util.Serializer;
import org.mozartspaces.util.LoggerFactory;
import org.slf4j.Logger;

import com.thoughtworks.xstream.XStream;

/**
 * Superclass of the XStream JSON and XML serializer, actually implements the
 * method in the MozartSpaces serializer interface.
 *
 * @author Tobias Doenz
 */
// TODO check that encoding is interoperable (explicitly set charset?)
public abstract class AbstractXStreamSerializer implements Serializer {

    private static final Logger log = LoggerFactory.get();

    /**
     * @return the XStream facade
     */
    public abstract XStream getXStream();

    // TODO catch and rethrow XStream exceptions
    @Override
    @SuppressWarnings("unchecked")
    public final <T> T copyObject(final T object) throws SerializationException {
        return (T) deserialize(serialize(object));
    }

    @Override
    public final <T> T deserialize(final byte[] serializedObject) throws SerializationException {
        ByteArrayInputStream bais = new ByteArrayInputStream(serializedObject);
        @SuppressWarnings("unchecked")
        T value = (T) getXStream().fromXML(bais);
        return value;
    }

    @Override
    public final <T> byte[] serialize(final T object) throws SerializationException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        getXStream().toXML(object, baos);
        log.trace("Serialized object:\n{}", new String(baos.toByteArray(), Charset.defaultCharset()));
        return baos.toByteArray();
    }

}
