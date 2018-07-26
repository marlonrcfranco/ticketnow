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
package org.mozartspaces.xvsmp.kryo;

import java.net.URI;

import net.jcip.annotations.ThreadSafe;

import org.mozartspaces.core.ContainerReference;
import org.mozartspaces.core.RequestReference;
import org.mozartspaces.core.TransactionReference;
import org.mozartspaces.core.aspects.AspectReference;
import org.mozartspaces.core.util.SerializationException;
import org.mozartspaces.core.util.Serializer;
import org.objenesis.strategy.StdInstantiatorStrategy;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

/**
 * Binary serializer that uses the kryo serialization library.
 *
 * @author Tobias Doenz
 */
@ThreadSafe
public final class KryoSerializer implements Serializer {

    private final Kryo kryo;

    private final Output output;

    /**
     * Constructs a {@code KryoSerializer}.
     */
    public KryoSerializer() {
        kryo = new Kryo();

        // strategy that does not require non-argument constructor (problem with serializing e.g. exceptions otherwise)
        kryo.setInstantiatorStrategy(new StdInstantiatorStrategy());
        // TODO evaluate performance impact of this special instantiator strategy

        output = new Output(1024, 1024 * 1024);

        // TODO register all (known) API classes?
        // kryo.register(Entry.class);

        // serializer for reference classes, necessary because of transient spaceURI field
        kryo.register(RequestReference.class, new com.esotericsoftware.kryo.Serializer<RequestReference>() {
            @Override
            public RequestReference read(final Kryo kryo, final Input input, final Class<RequestReference> type) {
                String id = input.readString();
                String space = input.readString();
                return new RequestReference(id, URI.create(space));
            }

            @Override
            public void write(final Kryo kryo, final Output output, final RequestReference ref) {
                output.writeString(ref.getId());
                output.writeString(ref.getSpace().toString());
            }
        });
        kryo.register(ContainerReference.class, new com.esotericsoftware.kryo.Serializer<ContainerReference>() {
            @Override
            public ContainerReference read(final Kryo kryo, final Input input, final Class<ContainerReference> type) {
                String id = input.readString();
                String space = input.readString();
                return new ContainerReference(id, URI.create(space));
            }

            @Override
            public void write(final Kryo kryo, final Output output, final ContainerReference ref) {
                output.writeString(ref.getId());
                output.writeString(ref.getSpace().toString());
            }
        });
        kryo.register(TransactionReference.class, new com.esotericsoftware.kryo.Serializer<TransactionReference>() {
            @Override
            public TransactionReference read(final Kryo kryo, final Input input,
                    final Class<TransactionReference> type) {
                String id = input.readString();
                String space = input.readString();
                return new TransactionReference(id, URI.create(space));
            }

            @Override
            public void write(final Kryo kryo, final Output output, final TransactionReference ref) {
                output.writeString(ref.getId());
                output.writeString(ref.getSpace().toString());
            }
        });
        kryo.register(AspectReference.class, new com.esotericsoftware.kryo.Serializer<AspectReference>() {
            @Override
            public AspectReference read(final Kryo kryo, final Input input, final Class<AspectReference> type) {
                String id = input.readString();
                String space = input.readString();
                return new AspectReference(id, URI.create(space));
            }

            @Override
            public void write(final Kryo kryo, final Output output, final AspectReference ref) {
                output.writeString(ref.getId());
                output.writeString(ref.getSpace().toString());
            }
        });

    }

    /**
     * @return the Kryo instance (not thread-safe and this method is not synchronized!)
     */
    public Kryo getKryo() {
        return kryo;
    }

    @Override
    public synchronized <T> byte[] serialize(final T object) throws SerializationException {
        output.clear();
        kryo.writeClassAndObject(output, object);
        return output.getBuffer();
    }

    @SuppressWarnings("unchecked")
    @Override
    public synchronized <T> T deserialize(final byte[] serializedObject) throws SerializationException {
        Input input = new Input(serializedObject);
        return (T) kryo.readClassAndObject(input);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T copyObject(final T object) throws SerializationException {
        return (T) deserialize(serialize(object));
    }

}
