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

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

import org.ah.robox.comms.Printer;
import org.ah.robox.comms.TransmitCallback;
import org.ah.robox.comms.response.StandardResponse;

/**
 *
 *
 * @author Daniel Sendula
 */
public class SendPrintJobCommand {

    public static void execute(final Printer printer, List<String> args) throws Exception {
        boolean printJobFlag = false;
        boolean fileFlag = false;
        boolean initiatePrintFlag = false;

        File file = null;
        String printJobId = null;
        for (String a : args) {
            if (printJobFlag) {
                printJobFlag = false;
                printJobId = a;
            } else if (fileFlag) {
                fileFlag = false;
                file = new File(a);
            } else if ("-?".equals(a) || "-h".equals(a) || "--help".equals(a)) {
                printHelp();
                System.exit(0);
            } else if ("-id".equals(a) || "--print-job-id".equals(a)) {
                printJobFlag = true;
            } else if ("-f".equals(a) || "--file".equals(a)) {
                fileFlag = true;
            } else if ("-p".equals(a) || "--initiate-print".equals(a)) {
                initiatePrintFlag = true;
            } else {
                System.err.println("Unknown option: '" + a + "'");
                printHelp();
                System.exit(1);
            }
        }

        if (printJobId == null) {
            printJobId = UUID.randomUUID().toString().replaceAll("-", "").substring(0, 16);
        }

        if (file == null) {
            System.err.println("No file specified");
            System.exit(1);
        }

        if (!file.exists()) {
            System.err.println("File does not exist; " + file.getAbsolutePath());
            System.exit(1);
        }

        StandardResponse response = null;

        FileReader reader = new FileReader(file);
        try {
            System.out.println("Job id: " + printJobId);
            if (Main.verboseFlag) {
                System.out.print("Sending data (each dot - 8Kb): ");
            }

            final boolean initialPrintFlagFinal = initiatePrintFlag;
            final String printJobIdFinal = printJobId;

            response = printer.transmitPrintJob(printJobId, reader, new TransmitCallback() {
                boolean initiatePrintFlag = initialPrintFlagFinal;
                int bytes = 0;

                public void transmitted(int sequenceNumber, int totalBytes) throws IOException {
                    if (totalBytes > 10240 && initiatePrintFlag) {
                        @SuppressWarnings("unused")
                        StandardResponse response = printer.startPrint(printJobIdFinal);
                        // TODO do something with response
                        initiatePrintFlag = false;
                    }
                    if (Main.verboseFlag) {
                        bytes = bytes + 512;
                        if (bytes >= 8192) {
                            System.out.print(".");
                            bytes = 0;
                        }
                    }

                }
            });
        } finally {
            reader.close();
        }
        if (Main.verboseFlag) {
            System.out.println(" done.");
        }
        Main.processStandardResponse(printer, response);
    }

    public static void printHelp() {
        System.out.println("Usage: rbx [<general-options>] send [<specific-options>]");
        System.out.println("");
        Main.printGeneralOptions();
        System.out.println("");
        Main.printSpecificOptions();
        System.out.println("  -f | --file          - gcode file. Mandatory option.");
        System.out.println("  -id | --print-job-id - job id. If not specified random one");
        System.out.println("                         is going to be generated.");
        System.out.println("  -p | --initiate-print - print is going to be started as well,");
        System.out.println("                          like start command is invoked.");
    }

}
