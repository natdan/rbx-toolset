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
public class AbortPrintCommand {
    private static final Logger logger = Logger.getLogger(AbortPrintCommand.class.getName());

    public static final String[] FINISH_PRINT_GCODE = new String[]{
        "M103 S0", // Nozzle heater off
        "M140 S0", // Bed heater off

        "G1 E-3 F400", // Depressurise the head


        // Finish/Abort Print
        "M106", // Fan on full
        "G0 B0", // Close Nozzle
        "G91", // Relative positioning
        "G0 Z5", // Move up 5mm
        "G90", // Absolute positioning
        "G0 X15 Y0", // Move to back corner

        // Open Door
        "G37 S", // Unlock door (S: don't wait for safe temp)

        "M170 S0", // Ambient control off
        "M107", // Fan off
        "M128", // Head Light off
        "M84", // Motors of
    };


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

        StandardResponse response = printer.abortPrint();
        if (Main.processStandardResponse(printer, response)) {
            GCodeCommand.sendGCode(printer, FINISH_PRINT_GCODE);
        }

    }

    public static void printHelp() {
        logger.info("Usage: rbx [<general-options>] abort [<specific-options>]");
        logger.info("");
        Main.printGeneralOptions();
        logger.info("");
        Main.printSpecificOptions();
    }
}
