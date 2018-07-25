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
package org.mozartspaces.rest.resources;

import java.net.URI;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

import com.sun.jersey.server.linking.Binding;
import com.sun.jersey.server.linking.Ref;
import com.sun.jersey.server.linking.Ref.Style;

/**
 * a representations for a Mzs-Container which can contain a link to the container itself or a lookup-link.
 *
 * @author Christian Proinger
 */
@XmlRootElement(name = "container")
// (name = "link", namespace = "http://www.w3.org/2005/Atom")
@XmlAccessorType(XmlAccessType.NONE)
// @XStreamAlias("containers") // to make this work. the sender has to process this. TODO alias-provider?
public class RESTContainerResponse {

    // @see: http://www.xfront.com/REST-Web-Services.html
    // <?xml version="1.0"?>
    // <p:Parts xmlns:p="http://www.parts-depot.com"
    // xmlns:xlink="http://www.w3.org/1999/xlink">
    // <Part id="00345" xlink:href="http://www.parts-depot.com/parts/00345"/>
    // <Part id="00346" xlink:href="http://www.parts-depot.com/parts/00346"/>
    // <Part id="00347" xlink:href="http://www.parts-depot.com/parts/00347"/>
    // <Part id="00348" xlink:href="http://www.parts-depot.com/parts/00348"/>
    // </p:Parts>

    // <container id="1" xlink:href="server/containers/1" />

    @XmlAttribute
    private String id;

    /**
     * xlink: http://de.wikipedia.org/wiki/XLink_%28Syntax%29
     */
    @Ref(resource = ContainerResource.class, style = Style.RELATIVE_PATH,
            bindings = @Binding(name = "id", value = "${resource.id}"))
    @XmlAttribute(name = "href", namespace = "http://www.w3.org/1999/xlink")
    private URI link;

    public RESTContainerResponse() {

    }

    public RESTContainerResponse(final String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public URI getLink() {
        return link;
    }

    public void setId(final String id) {
        this.id = id;
    }

    public void setLink(final URI res) {
        this.link = res;
    }

    @Override
    public String toString() {
        return "RESTContainerResponse [id=" + id + ", link=" + link + "]";
    }
}
