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

/**
 * Resource-Constants used in path/param/... Annotations
 *
 * @author Christian Proinger
 */
public abstract class ResourceConstants {

    public final static String TRANSACTIONS = "/transactions/transaction";
    public final static String TRANSACTION = "/transactions/transaction/{txid}";
    public final static String CONTAINERS = "/containers/container";
    public final static String CONTAINERS_LOOKUP = "/containers/container/lookup";
    public final static String CONTAINER = "/containers/container/{cid}";
    public final static String CONTAINER_ANSWER = "/containers/container/{cid}/{op}/answer";
    public final static String CONTAINER2 = "containers/container/{cid:[0-9]+}";
    public final static String TAKE = "/take";
    public static final String READ = "/read";
    public static final String TEST = "/test";
    public final static String TAKE_OP = "take";
    public static final String READ_OP = "read";
    public static final String TEST_OP = "test";
    public static final String WRITE = "/write";
    public static final String DELETE = "/delete";

    public final static String MESSAGES = "/messages";
    public static final String X_REQUEST_ID = "X-requestId";
    public static final String X_ISOLATION_LEVEL = "X-isolationLevel";
    public static final String ASPECTS_ASPECT = "/aspects/aspect";
}
