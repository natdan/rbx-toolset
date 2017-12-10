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
public class PausePrinterCommand {
    private static final Logger logger = Logger.getLogger(PausePrinterCommand.class.getName());

    public static void execute(Printer printer, List<String> args) throws Exception {
        for (String a : args) {
            if ("-?".equals(a) || "-h".equals(a) || "--help".equals(a)) {
                printHelp();
                System.exit(0);
            } else {
                logger.severe("Unknown option: '" + a + "'");
                printHelp();
                System.exit(1);
            }
        }

        StandardResponse response = printer.pausePrinter();
        Main.processStandardResponse(printer, response);
    }

    public static void printHelp() {
        logger.info("Usage: rbx [<general-options>] pause [<specific-options>]");
        logger.info("");
        Main.printGeneralOptions();
        logger.info("");
        Main.printSpecificOptions();
    }
}
