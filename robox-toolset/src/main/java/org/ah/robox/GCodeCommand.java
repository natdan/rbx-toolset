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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

import org.ah.robox.comms.Printer;
import org.ah.robox.comms.response.GCodeResponse;
import org.ah.robox.comms.response.StandardResponse;

/**
 *
 *
 * @author Daniel Sendula
 */
public class GCodeCommand {

    public static void execute(Printer printer, List<String> args) throws Exception {

        boolean interactiveFlag = false;

        int argNo = 1;

        for (String a : args) {
            if ("-?".equals(a) || "-h".equals(a) || "--help".equals(a)) {
                printHelp();
                System.exit(0);
            } else if ("-i".equals(a) || "--interactive".equals(a)) {
                interactiveFlag = true;
            } else if (a.startsWith("-")) {
                System.err.println("Unknown option: '" + a + "'");
                printHelp();
                System.exit(1);
            } else {
                if (Main.verboseFlag) {
                    System.out.println("Sending gcode command: '" + a + "'");
                }
                String response = sendGCode(printer, a);
                System.out.println("A" + argNo + ": " +response);

                argNo = argNo + 1;
            }
        }

        if (interactiveFlag) {
            int lineNo = 1;
            try {
                InputStreamReader isr = new InputStreamReader(System.in);
                BufferedReader in = new BufferedReader(isr);
                String line = in.readLine();
                while (line != null) {
                    try {
                        int i = line.indexOf(';');
                        if (i >= 0) {
                            line = line.substring(0, i);
                        }
                        line = line.trim();
                        if (line.length() > 0) {
                            String response = sendGCode(printer, line);
                            System.out.println("L" + lineNo + ": " + response);
                        }
                        lineNo = lineNo + 1;
                    } catch (IOException e) {
                        System.err.println("Error sending gcode command to printer; " + e.getMessage());
                        System.exit(1);
                    }

                    line = in.readLine();
                }
            } catch (IOException ignore) {}
        }
    }

    public static void sendGCode(Printer printer, String[] gcode) throws IOException {
        for (String line : gcode) {
            sendGCode(printer, line);
        }
    }

    public static String sendGCode(Printer printer, String gcode) throws IOException {
        GCodeResponse response = printer.sendGCode(gcode);

        StandardResponse standardResponse = printer.reportErrors();
        while (standardResponse.isBufferOverFlow()) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ignore) { }

            standardResponse = printer.resetErrors();

            response = printer.sendGCode(gcode);
            standardResponse = printer.reportErrors();
        }

        return response.getResponse();
    }

    public static void printHelp() {
        System.out.println("Usage: rbx [<general-options>] gcode [<specific-options>] [<gcode-commands>]");
        System.out.println("");
        Main.printGeneralOptions();
        System.out.println("");
        Main.printSpecificOptions();
        System.out.println("");
        System.out.println("All arguments that do not start with '-' will be processed as gcode commands");
        System.out.println("and sent to the printer. Also, all sysin will be processed line by line and");
        System.out.println("sent to the printer. Resposes are prefixed with 'A' + number of argument + ': '");
        System.out.println("sent out or 'L' + number of line from sysin + ': ' (note trailing space).");
    }
}
