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
package org.mozartspaces.core;

import java.io.Serializable;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import net.jcip.annotations.ThreadSafe;

import org.mozartspaces.util.MzsCloneable;

/**
 * Store for meta information, properties (key-value pairs) that can be used by
 * Aspects, Coordinators and the Runtime itself. The meta information is
 * organized in four different categories - Request, Aspect, System, and
 * Coordinator - with corresponding getter and setter methods. The categories
 * should indicate where the information has been written. This should help to
 * avoid conflicts caused by the multiple use of the same key. For Aspects and
 * Coordinators further precautions like prefixes might be necessary to avoid
 * such conflicting properties.
 *
 * @author Tobias Doenz
 * @author Stefan Crass
 */
@ThreadSafe
public final class RequestContext implements MzsCloneable, Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * Prefix for properties in the Request category.
     */
    public static final String REQUEST_PREFIX = "REQ.";

    /**
     * Prefix for properties in the Aspect category.
     */
    public static final String ASPECT_PREFIX = "ASP.";

    /**
     * Prefix for properties in the System category.
     */
    public static final String SYSTEM_PREFIX = "SYS.";

    /**
     * Prefix for properties in the Coordinator category.
     */
    public static final String COORDINATOR_PREFIX = "COO.";

    private final Map<String, Object> properties;

    /**
     * Constructs an empty <code>RequestContext</code>.
     */
    public RequestContext() {
        properties = new ConcurrentHashMap<String, Object>();
    }

    /**
     * Private constructor for cloning.
     */
    private RequestContext(final Map<String, Object> properties) {
        this.properties = properties;
        assert this.properties != null;
    }

    /**
     * Gets a property without fixed prefix.
     *
     * Note that this method does not copy write-protected entries before returning them,
     * so use only immutable objects to prevent unexpected modifications.
     *
     * @param key
     *            the key
     * @return the value for the specified key
     */
    public Object getProperty(final String key) {
        return properties.get(key);
    }

    /**
     * Returns a set of all property keys, backed by the internally used map.
     * This method is used for marshalling the request context and should not
     * be used elsewhere.
     *
     * @return the key set
     */
    public Set<String> getPropertyKeys() {
        return properties.keySet();
    }

    /**
     * Sets a property without fixed prefix.
     *
     * @param key
     *            the key
     * @param value
     *            the value for the specified key
     * @throws IllegalArgumentException
     *            if key starts with the prefix for write-protected entries
     */
    public void setProperty(final String key, final Object value) {
        properties.put(key, value);
    }

    /**
     * Returns <code>true</code> if a value for the specified key exists.
     *
     * @param key
     *            the key
     * @return <code>true</code> if a value for the specified key exists,
     *         <code>false</code> otherwise
     */
    public boolean containsProperty(final String key) {
        return properties.containsKey(key);
    }

    /**
     * Removes a property without fixed prefix.
     *
     * @param key
     *            the key
     * @return the value that was removed, or <code>null</code> if there was no
     *            value for the specified key
     * @throws IllegalArgumentException
     *            if key starts with the prefix for write-protected entries
     */
    public Object removeProperty(final String key) {
        return properties.remove(key);
    }

    /**
     * Gets a Request property.
     *
     * @param key
     *            the key
     * @return the value for the specified key
     */
    public Object getRequestProperty(final String key) {
        return properties.get(REQUEST_PREFIX + key);
    }

    /**
     * Sets a Request property.
     *
     * @param key
     *            the key
     * @param value
     *            the value for the specified key
     */
    public void setRequestProperty(final String key, final Object value) {
        properties.put(REQUEST_PREFIX + key, value);
    }

    /**
     * Gets an Aspect property.
     *
     * @param key
     *            the key
     * @return the value for the specified key
     */
    public Object getAspectProperty(final String key) {
        return properties.get(ASPECT_PREFIX + key);
    }

    /**
     * Sets an Aspect property.
     *
     * @param key
     *            the key
     * @param value
     *            the value for the specified key
     */
    public void setAspectProperty(final String key, final Object value) {
        properties.put(ASPECT_PREFIX + key, value);
    }

    /**
     * Gets a System property.
     *
     * @param key
     *            the key
     * @return the value for the specified key
     */
    public Object getSystemProperty(final String key) {
        return properties.get(SYSTEM_PREFIX + key);
    }

    /**
     * Sets a System property.
     *
     * @param key
     *            the key
     * @param value
     *            the value for the specified key
     */
    public void setSystemProperty(final String key, final Object value) {
        properties.put(SYSTEM_PREFIX + key, value);
    }

    /**
     * Gets a Coordinator property.
     *
     * @param key
     *            the key
     * @return the value for the specified key
     */
    public Object getCoordinatorProperty(final String key) {
        return properties.get(COORDINATOR_PREFIX + key);
    }

    /**
     * Sets a Coordinator property.
     *
     * @param key
     *            the key
     * @param value
     *            the value for the specified key
     */
    public void setCoordinatorProperty(final String key, final Object value) {
        properties.put(COORDINATOR_PREFIX + key, value);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((properties == null) ? 0 : properties.hashCode());
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        RequestContext other = (RequestContext) obj;
        if (properties == null) {
            if (other.properties != null) {
                return false;
            }
        } else if (!properties.equals(other.properties)) {
            return false;
        }
        return true;
    }

    @Override
    public RequestContext clone() throws CloneNotSupportedException {
        Map<String, Object> clonedProperties = new ConcurrentHashMap<String, Object>(properties.size());
        for (Map.Entry<String, Object> entry : properties.entrySet()) {
            Object value = entry.getValue();
            try {
                Object clonedValue = ((MzsCloneable) value).clone();
                clonedProperties.put(entry.getKey(), clonedValue);
            } catch (ClassCastException ex) {
                throw new CloneNotSupportedException(ex.toString());
            }
        }
        return new RequestContext(clonedProperties);
    }

    @Override
    public String toString() {
        return "RequestContext [properties=" + properties + "]";
    }

}
