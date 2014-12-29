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
public class StartPrintJobCommand {
    public static void execute(Printer printer, List<String> args) throws Exception {
        String printJobId = null;
        for (String a : args) {
            if ("-?".equals(a) || "-h".equals(a) || "--help".equals(a)) {
                printHelp();
                System.exit(0);
            } else if (a.startsWith("-")) {
                System.err.println("Unknown option: '" + a + "'");
                printHelp();
                System.exit(1);
            } else if (printJobId == null) {
                printJobId = a;
            } else {
                System.err.println("Only one print job id argument is allowed.");
                System.exit(1);
            }
        }
        if (printJobId == null) {
            System.err.println("You must specify print job id parameter.");
            System.exit(1);
        }

        if (Main.verboseFlag) {
            System.out.println("Starting job " + printJobId);
        }
        StandardResponse response = printer.startPrint(printJobId);
        Main.processStandardResponse(printer, response);
    }

    public static void printHelp() {
        System.out.println("Usage: rbx [<general-options>] start [<specific-options>] <print-job-id>");
        System.out.println("");
        Main.printGeneralOptions();
        System.out.println("");
        Main.printSpecificOptions();
    }

}
