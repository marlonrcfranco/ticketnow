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
package org.mozartspaces.core.metamodel;

/**
 * An object that provides meta data.
 *
 * @author Tobias Doenz
 */
public interface MetaDataProvider {

    /**
     * Gets the meta data for this meta model node or sub-tree.
     *
     * @param depth
     *            the tree depth up to which the meta model sub-tree should be traversed and meta data returned
     * @return the meta model sub-tree.
     */
    Object getMetaData(int depth);

    /**
     * Sets a meta data property on this meta model node.
     *
     * @param key
     *            the key of the property
     * @param value
     *            the value of the property
     */
    void setMetaDataProperty(String key, Object value);

}
