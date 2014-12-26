/*******************************************************************************
 * Copyright (c) 2014 Creative Sphere Limited.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *    Creative Sphere - initial API and implementation
 *
 *
 *******************************************************************************/
package org.ah.robox;

import java.util.List;

import org.ah.robox.comms.Printer;
import org.ah.robox.comms.response.StandardResponse;

/**
 *
 *
 * @author Daniel Sendula
 */
public class AbortPrintCommand {

    public static void execute(Printer printer, List<String> args) throws Exception {
        for (String a : args) {
            if ("-?".equals(a) || "-h".equals(a) || "--help".equals(a)) {
                printHelp();
                System.exit(0);
            } else {
                System.err.println("Unknown option: '" + a + "'");
                printHelp();
                System.exit(1);
            }
        }

        StandardResponse response = printer.abortPrint();
        Main.processStandardResponse(response);
    }

    public static void printHelp() {
        System.out.println("Usage: rbx [<general-options>] abort [<specific-options>]");
        System.out.println("");
        Main.printGeneralOptions();
        System.out.println("");
        Main.printSpecificOptions();
    }
}
