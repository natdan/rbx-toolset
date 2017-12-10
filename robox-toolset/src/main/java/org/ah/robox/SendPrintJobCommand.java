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
import java.util.logging.Level;
import java.util.logging.Logger;

import org.ah.robox.comms.Printer;
import org.ah.robox.comms.TransmitCallback;
import org.ah.robox.comms.response.StandardResponse;

/**
 *
 *
 * @author Daniel Sendula
 */
public class SendPrintJobCommand {

    private static final Logger logger = Logger.getLogger(SendPrintJobCommand.class.getName());

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
                logger.severe("Unknown option: '" + a + "'");
                printHelp();
                System.exit(1);
            }
        }

        if (printJobId == null) {
            printJobId = UUID.randomUUID().toString().replaceAll("-", "").substring(0, 16);
        }

        if (file == null) {
            logger.severe("No file specified");
            System.exit(1);
        }

        if (!file.exists()) {
            logger.severe("File does not exist; " + file.getAbsolutePath());
            System.exit(1);
        }

        StandardResponse response = null;

        FileReader reader = new FileReader(file);
        try {
            logger.info("Job id: " + printJobId);
            logger.fine("Sending data (each dot - 8Kb): ");

            final boolean initialPrintFlagFinal = initiatePrintFlag;
            final String printJobIdFinal = printJobId;

            response = printer.transmitPrintJob(printJobId, reader, new TransmitCallback() {
                boolean initiatePrintFlag = initialPrintFlagFinal;
                int bytes = 0;

                @Override
                public void transmitted(int sequenceNumber, int totalBytes) throws IOException {
                    if (totalBytes > 10240 && initiatePrintFlag) {
                        @SuppressWarnings("unused")
                        StandardResponse response = printer.startPrint(printJobIdFinal);
                        // TODO do something with response
                        initiatePrintFlag = false;
                    }
                    if (Main.logLevel.intValue() <= Level.FINE.intValue()) {
                        bytes = bytes + 512;
                        if (bytes >= 8192) {
                            logger.fine(".");
                            bytes = 0;
                        }
                    }

                }
            });
        } finally {
            reader.close();
        }

        logger.fine(" done.");
        Main.processStandardResponse(printer, response);
    }

    public static void printHelp() {
        logger.info("Usage: rbx [<general-options>] send [<specific-options>]");
        logger.info("");
        Main.printGeneralOptions();
        logger.info("");
        Main.printSpecificOptions();
        logger.info("  -f | --file          - gcode file. Mandatory option.");
        logger.info("  -id | --print-job-id - job id. If not specified random one");
        logger.info("                         is going to be generated.");
        logger.info("  -p | --initiate-print - print is going to be started as well,");
        logger.info("                          like start command is invoked.");
    }

}
