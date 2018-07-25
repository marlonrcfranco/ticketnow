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
package org.mozartspaces.capi3.javanative.persistence.berkeley.profile;

import org.mozartspaces.capi3.javanative.persistence.berkeley.AbstractBerkeleyDB;

import com.sleepycat.je.Durability;

/**
 * A persistence profile based on Berkeley DB that uses {@link Durability#COMMIT_WRITE_NO_SYNC} durability.
 *
 * @author Jan Zarnikov
 */
public final class TransactionalBerkeleyDB extends AbstractBerkeleyDB {

    @Override
    public Durability getDurability() {
        return Durability.COMMIT_WRITE_NO_SYNC;
    }
}
