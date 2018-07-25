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

import net.jcip.annotations.Immutable;

import org.mozartspaces.core.AnswerContainerInfo;
import org.mozartspaces.core.GenericResponse;
import org.mozartspaces.core.RequestReference;
import org.mozartspaces.core.ResponseMessage;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.SingleValueConverter;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

/**
 * Converts {@link ResponseMessage} to and from XML. Added to get rid of the
 * class attribute for the response element.
 *
 * @author Tobias Doenz
 */
// TODO test with answer container info
@Immutable
public final class ResponseMessageConverter implements Converter {

    private final XStream xstream;

    /**
     * Constructs a <code>ResponseMessageConverter</code>.
     *
     * @param xstream
     *            the XStream instance where this converter is used, needed to
     *            lookup converters
     */
    public ResponseMessageConverter(final XStream xstream) {
        this.xstream = xstream;
    }

    @Override
    public void marshal(final Object source, final HierarchicalStreamWriter writer, final MarshallingContext context) {
        ResponseMessage message = (ResponseMessage) source;
//        writer.addAttribute("xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance");
//        writer.addAttribute("xsi:schemaLocation", "http://www.xvsm.org xvsm.xsd");

        writer.addAttribute("requestRef", message.getRequestReference().getStringRepresentation());
        writer.startNode("Response");
        context.convertAnother(message.getContent());
        writer.endNode();
        if (message.getAnswerContainerInfo() != null) {
            writer.startNode("AnswerContainerInfo");
            context.convertAnother(message.getAnswerContainerInfo());
            writer.endNode();
        }
    }

    @Override
    public Object unmarshal(final HierarchicalStreamReader reader, final UnmarshallingContext context) {
        String refString = reader.getAttribute("requestRef");
        SingleValueConverter converter = (SingleValueConverter) xstream.getConverterLookup().lookupConverterForType(
                RequestReference.class);
        RequestReference requestRef = (RequestReference) converter.fromString(refString);
        reader.moveDown();
        GenericResponse<?> response = (GenericResponse<?>) context.convertAnother(null, GenericResponse.class);
        reader.moveUp();
        AnswerContainerInfo answerContainerInfo = null;
        if (reader.hasMoreChildren()) {
            reader.moveDown();
            answerContainerInfo = (AnswerContainerInfo) context.convertAnother(null, AnswerContainerInfo.class);
            reader.moveUp();
        }
        ResponseMessage message = new ResponseMessage(requestRef, response, answerContainerInfo);
        return message;
    }

    @Override
    public boolean canConvert(@SuppressWarnings("rawtypes") final Class type) {
        return type == ResponseMessage.class;
    }

}
