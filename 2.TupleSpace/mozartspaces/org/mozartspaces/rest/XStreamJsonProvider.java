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
package org.mozartspaces.rest;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.servlet.ServletContext;
import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.Provider;

import org.atmosphere.annotation.Suspend;
import org.mozartspaces.core.util.Serializer;
import org.mozartspaces.xvsmp.xstream.AbstractXStreamSerializer;

import com.sun.jersey.core.provider.AbstractMessageReaderWriterProvider;
import com.thoughtworks.xstream.XStream;

/**
 * This class provides the Serialization with the serializer defined in the morzartspaces-configuration.
 *
 * For now it only works if an xstream-json serializer is configured.
 *
 * http://freddy33.blogspot.com/2008/07/using-jersey-for-exposing-rest-services.html
 *
 * @author Christian Proinger
 * */
// @Produces({"application/xml", "text/xml", "*/*"})
@Produces({ MediaType.APPLICATION_JSON })
// @Consumes({"application/xml", "text/xml", "*/*"})
@Consumes({ MediaType.APPLICATION_JSON })
@Provider
public final class XStreamJsonProvider extends AbstractMessageReaderWriterProvider<Object> {

    private final Set<Class<?>> processed = new HashSet<Class<?>>();

    // private static final XStream xstream = new XStreamJsonSerializer().getXStream();
    private final XStream xstream;

    private static final String DEFAULT_ENCODING = "utf-8";

    // doen't work for the client because it can't be provided.
    // @Context
    // private ServletContext servletContext;

    /**
     * This constructor is used by the jersey-container, it provides the servletContext.
     *
     * @param servletContext
     *            the servlet context
     */
    public XStreamJsonProvider(@Context final ServletContext servletContext) {
        @SuppressWarnings("unchecked")
        Map<String, Serializer> serializers = (Map<String, Serializer>) servletContext.getAttribute("serializers");
        AbstractXStreamSerializer ser = (AbstractXStreamSerializer) serializers.get("xstream-json");
        if (ser == null) {
            throw new IllegalStateException("xstream-json serializer not configured in the mozartspaces-config");
        }
        xstream = ser.getXStream();
        xstream.autodetectAnnotations(true);
    }

    /**
     * This constructor is used by the client which has to provide the serializer himself.
     *
     * @param xstream
     *            the XStream instance
     */
    public XStreamJsonProvider(final XStream xstream) {
        this.xstream = xstream;
    }

    @Override
    public boolean isReadable(final Class<?> type, final Type genericType, final Annotation[] annotations,
            final MediaType mediaType) {
        if (!MediaType.APPLICATION_JSON_TYPE.isCompatible(mediaType)) {
            return false;
        }
        // return type.getAnnotation(XStreamAlias.class) != null;
        // return xstream != null;
        return true;
    }

    @Override
    public boolean isWriteable(final Class<?> type, final Type genericType, final Annotation[] annotations,
            final MediaType mediaType) {

        // the client asks this class if it can write the object = octet-stream.
        if (!(MediaType.APPLICATION_JSON_TYPE.isCompatible(mediaType) || MediaType.APPLICATION_OCTET_STREAM_TYPE
                .equals(mediaType))) {
            return false;
        }
        // return type.getAnnotation(XStreamAlias.class) != null;
        // wenn man String auch mit dem provider schreibt dann gibts probleme mit Atmosphere
        // da wenn man ein return Broadcastable macht wird irgendwie "" zurückgegeben
        // scheinbar wird dabei auch die connection geschlossen was ein weiteres Problem ist
        // aber zunächst mal egal weil sowieso alle derzeitigen Methoden auf resume-on-broadcast
        // gesetzt sind. sollte sich das ändern gibts vermutlich ein problem.

        // um normale Strings nicht auch zu behandeln die von nicht @Suspend-Methoden
        // zurückgeliefert wurden prüfe ich hier noch zusätzlich darauf ob so eine Annotation vorhanden ist.
        if (String.class.equals(type)) {
            for (Annotation ano : annotations) {
                if (Suspend.class.equals(ano.annotationType())) {
                    return false;
                }
            }
        }
        // return xstream != null;
        return true;
    }

    protected static String getCharsetAsString(final MediaType m) {
        if (m == null) {
            return DEFAULT_ENCODING;
        }
        String result = m.getParameters().get("charset");
        return (result == null) ? DEFAULT_ENCODING : result;
    }

    /**
     * processes the class if it is new and returns xstream.
     *
     * @param type
     * @return
     */
    protected XStream getXStream(final Class<?> type) {
        synchronized (processed) {
            if (!processed.contains(type)) {
                xstream.processAnnotations(type);
                processed.add(type);
            }
        }
        return xstream;
    }

    @Override
    public Object readFrom(final Class<Object> aClass, final Type genericType, final Annotation[] annotations,
            final MediaType mediaType, final MultivaluedMap<String, String> map, final InputStream stream)
            throws IOException, WebApplicationException {
        String encoding = getCharsetAsString(mediaType);
        XStream xStream = getXStream(aClass);
        return xStream.fromXML(new InputStreamReader(stream, encoding));
    }

    @Override
    public void writeTo(final Object o, final Class<?> aClass, final Type type, final Annotation[] annotations,
            final MediaType mediaType, final MultivaluedMap<String, Object> map, final OutputStream stream)
            throws IOException, WebApplicationException {
        String encoding = getCharsetAsString(mediaType);
        XStream xStream = getXStream(o.getClass());
        // xStream.toXML(o, System.out);System.out.println();
        xStream.toXML(o, new OutputStreamWriter(stream, encoding));
    }
}
