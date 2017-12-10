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
import java.util.logging.Logger;

import org.ah.robox.comms.Printer;
import org.ah.robox.comms.response.StandardResponse;

/**
 *
 *
 * @author Daniel Sendula
 */
public class StartPrintJobCommand {
    private static final Logger logger = Logger.getLogger(StartPrintJobCommand.class.getName());

    public static void execute(Printer printer, List<String> args) throws Exception {
        String printJobId = null;
        for (String a : args) {
            if ("-?".equals(a) || "-h".equals(a) || "--help".equals(a)) {
                printHelp();
                System.exit(0);
            } else if (a.startsWith("-")) {
                logger.severe("Unknown option: '" + a + "'");
                printHelp();
                System.exit(1);
            } else if (printJobId == null) {
                printJobId = a;
            } else {
                logger.severe("Only one print job id argument is allowed.");
                System.exit(1);
            }
        }
        if (printJobId == null) {
            logger.severe("You must specify print job id parameter.");
            System.exit(1);
        }

        logger.fine("Starting job " + printJobId);
        StandardResponse response = printer.startPrint(printJobId);
        Main.processStandardResponse(printer, response);
    }

    public static void printHelp() {
        logger.info("Usage: rbx [<general-options>] start [<specific-options>] <print-job-id>");
        logger.info("");
        Main.printGeneralOptions();
        logger.info("");
        Main.printSpecificOptions();
    }

}
