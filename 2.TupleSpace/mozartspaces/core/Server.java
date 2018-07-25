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

/**
 * Starts a MozartSpaces peer with an embedded space in the stand-alone mode.
 * The space is remotely accessible at the port that is passed as first argument
 * or the port specified in the configuration.
 *
 * @author Tobias Doenz
 */
public final class Server {

    private Server() {
    }

    /**
     * Starts MozartSpaces with an embedded space in the stand-alone mode.
     *
     * @param args
     *            the port number for the first TCP socket receiver. If exactly
     *            one argument is provided, the first argument is parsed as
     *            integer value and set as the port number of the first TCP
     *            socket receiver in the transports that are read from the
     *            configuration.
     */
    public static void main(final String[] args) {
        System.out.println("Starting MozartSpaces standalone peer");
        final MzsCore core;
        if (args.length == 1) {
            int port = Integer.parseInt(args[0]);
            System.out.println("Using port " + port + " for default TCP socket receiver");
            core = DefaultMzsCore.newInstance(port);
        } else {
            core = DefaultMzsCore.newInstance();
        }
        System.out.println("Core is running and ready for requests");
        System.out.println("Press Ctrl+C to exit");
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                core.shutdown(true);
            }
        });
    }

}
