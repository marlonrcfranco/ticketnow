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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import net.jcip.annotations.Immutable;

/**
 * A <code>Serializer</code> that uses the built-in binary serialization.
 *
 * @author Tobias Doenz
 */
@Immutable
public final class JavaBuiltinSerializer implements Serializer {

    @Override
    public <T> byte[] serialize(final T object) throws SerializationException {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(baos);
            oos.writeObject(object);
            oos.flush();
            // streams deliberately not closed (not necessary)
            return baos.toByteArray();
        } catch (IOException ex) {
            throw new SerializationException("Could not serialize object", ex);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T deserialize(final byte[] serializedObject) throws SerializationException {
        T copy = null;
        try {
            ByteArrayInputStream bais = new ByteArrayInputStream(serializedObject);
            ObjectInputStream ois = new ObjectInputStream(bais);
            copy = (T) ois.readObject();
            // streams deliberately not closed (not necessary)
        } catch (Exception ex) {
            throw new SerializationException("Could not deserialize object", ex);
        }
        return copy;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T copyObject(final T object) throws SerializationException {
        return (T) deserialize(serialize(object));
        // the cast is necessary for javac, but not the eclipse compiler
    }

}
