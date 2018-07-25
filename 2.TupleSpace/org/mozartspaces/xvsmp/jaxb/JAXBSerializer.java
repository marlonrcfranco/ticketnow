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
package org.mozartspaces.xvsmp.jaxb;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.Charset;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import net.jcip.annotations.Immutable;

import org.mozartspaces.core.MzsCoreRuntimeException;
import org.mozartspaces.core.util.SerializationException;
import org.mozartspaces.core.util.Serializer;
import org.mozartspaces.util.LoggerFactory;
import org.mozartspaces.xvsmp.util.MarshalFactories;
import org.slf4j.Logger;
import org.xvsm.protocol.AbstractMessage;
import org.xvsm.protocol.ObjectFactory;

/**
 * Serializer from and to XML, uses the XML Schema for the Core and the classes
 * generated from it by JAXB RI 2.1.
 *
 * Can serialize and deserialize only instances of {@link AbstractMessage} and
 * can thus not be used for the entry copier.
 *
 * @author Tobias Doenz
 *
 * @see javax.xml.bind.JAXBContext
 */
// what if the (generated) entry objects are annotated with @XmlRootElement?
@Immutable
public final class JAXBSerializer implements Serializer {

    private static final Logger log = LoggerFactory.get();

    private static final ObjectFactory OBJ_FACTORY = new ObjectFactory();

    /*
     * according to the documentation of the JAXB RI, the JAXBContext is
     * thread-safe, but the marshallers and unmarshallers are not
     */
    private final JAXBContext context;
    private final ThreadLocal<Marshaller> marshaller;
    private final ThreadLocal<Unmarshaller> unmarshaller;
    private final MarshallerHelper marshallerHelper;
    private final UnmarshallerHelper unmarshallerHelper;

    /**
     * Constructs a <code>JAXBSerializer</code>.
     *
     * @throws JAXBException
     *             if the {@link JAXBContext} could not be created
     */
    public JAXBSerializer() throws JAXBException {
        JAXBContext temporary = null;
        try {
            temporary = JAXBContext.newInstance(AbstractMessage.class);
        } catch (JAXBException ex) {
            throw ex;
        }
        context = temporary;
        marshaller = new ThreadLocal<Marshaller>() {
            @Override
            protected Marshaller initialValue() {
                try {
                    return context.createMarshaller();
                } catch (JAXBException ex) {
                    String message = "Could not create JAXB Marshaller";
                    log.error(message, ex);
                    throw new MzsCoreRuntimeException(message, ex);
                }
            }
        };
        if (log.isTraceEnabled()) {
            // format output, but only for log level TRACE, when the XML messages are logged
            marshaller.get().setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
        }

        unmarshaller = new ThreadLocal<Unmarshaller>() {
            @Override
            protected Unmarshaller initialValue() {
                try {
                    return context.createUnmarshaller();
                } catch (JAXBException ex) {
                    String message = "Could not create JAXB Unmarshaller";
                    log.error(message, ex);
                    throw new MzsCoreRuntimeException(message, ex);
                }
            }
        };

        // TODO move marshal factories to configuration
        MarshalFactories factories = new MarshalFactories();
        marshallerHelper = new MarshallerHelper(factories);
        unmarshallerHelper = new UnmarshallerHelper(factories);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T copyObject(final T object) throws SerializationException {
        return (T) deserialize(serialize(object));
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T deserialize(final byte[] serializedObject) throws SerializationException {
        ByteArrayInputStream bais = new ByteArrayInputStream(serializedObject);
        T value = null;
        try {
            JAXBElement<AbstractMessage> jaxbElement = (JAXBElement<AbstractMessage>) unmarshaller.get().unmarshal(
                    bais);
            AbstractMessage xmlMessage = jaxbElement.getValue();
            value = (T) unmarshallerHelper.unmarshal(xmlMessage);
        } catch (Exception ex) {
            throw new SerializationException("Could not deserialize object", ex);
        }
        return value;
    }

    @Override
    public <T> byte[] serialize(final T object) throws SerializationException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            Object jaxbObject = marshallerHelper.marshal(object);
            JAXBElement<AbstractMessage> jaxbElement = OBJ_FACTORY.createMessage((AbstractMessage) jaxbObject);
            marshaller.get().marshal(jaxbElement, baos);
        } catch (Exception ex) {
            throw new SerializationException("Could not serialize object", ex);
        }
        log.trace("Serialized object:\n{}", new String(baos.toByteArray(), Charset.defaultCharset()));
        return baos.toByteArray();
    }

}
