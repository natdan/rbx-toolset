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
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.ah.robox.comms.Printer;
import org.ah.robox.comms.TransmitCallback;
import org.ah.robox.comms.response.PrinterPause;
import org.ah.robox.comms.response.PrinterStatusResponse;
import org.ah.robox.comms.response.StandardResponse;
import org.ah.robox.ui.MonitorWindow;
import org.ah.robox.util.Detach;

/**
 *
 *
 * @author Daniel Sendula
 */
public class SendPrintJobCommand {

    private static final Logger logger = Logger.getLogger(SendPrintJobCommand.class.getName());

    private static MonitorWindow monitorWindow;

    private static boolean continueUploading = true;

    private static List<IOAction> actions = new ArrayList<IOAction>();

    public static void execute(final Printer printer, List<String> args) throws Exception {
        boolean printJobFlag = false;
        boolean fileFlag = false;
        boolean initiatePrintFlag = false;
        boolean detachFlag = false;
        boolean uiFlag = false;

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
            } else if ("-d".equals(a) || "--detach".equals(a)) {
                detachFlag = true;
            } else if ("-m".equals(a) || "--monitor".equals(a)) {
                uiFlag = true;
            } else {
                logger.severe("Unknown option: '" + a + "'");
                printHelp();
                System.exit(1);
            }
        }

        if (file == null) {
            logger.severe("No file specified");
            System.exit(1);
        }

        if (!file.exists()) {
            logger.severe("File does not exist; " + file.getAbsolutePath());
            System.exit(1);
        }

        if (detachFlag && !Main.detachedFlag) {
            Detach.detach(Main.class.getName());
        } else {
            final boolean monitor = uiFlag;
            if (monitor) {
                monitorWindow = new MonitorWindow();
                monitorWindow.setUploadFileName(file.getName());
                monitorWindow.setUploading(true);
                monitorWindow.setVisible(true);
                monitorWindow.setAbortUploadingAction(() -> {
                    continueUploading = false;
                });

                monitorWindow.setAbortPrintingAction(() -> {
                    executePrinterCommand(monitorWindow, () -> {
                        try {
                            StandardResponse response = printer.abortPrint();
                            processStandardResponse(monitorWindow, response);
                            if (Main.processStandardResponse(printer, response)) {
                                GCodeCommand.sendGCode(printer, AbortPrintCommand.FINISH_PRINT_GCODE);
                            }
                        } catch (IOException e) { }
                        System.exit(0);
                    });
                });

                monitorWindow.setPausePrintingAction(() -> {
                    executePrinterCommand(monitorWindow, () -> {
                        PrinterStatusResponse printerStatus = printer.getPrinterStatus();

                        PrinterPause status = printerStatus.getCombinedStatus();
                        monitorWindow.setStatus(status);
                        if (status == PrinterPause.PAUSED) {
                            StandardResponse response = printer.resumePrinter();
                            processStandardResponse(monitorWindow, response);
                        } else if (status == PrinterPause.WORKING) {
                            StandardResponse response = printer.pausePrinter();
                            processStandardResponse(monitorWindow, response);
                        }
                    });
                });
            }

            if (printJobId == null) {
                printJobId = UUID.randomUUID().toString().replaceAll("-", "").substring(0, 16);
            }

            StandardResponse response = null;

            if (monitor) {
                try {
                    int totalLines = UploadCommand.countLines(file);
                    monitorWindow.setLinesTotal(totalLines);
                } catch (IOException e) {
                    monitorWindow.setExceptionError("Exception reading file " + file.getAbsolutePath(), e);
                }
            }

            FileReader reader = new FileReader(file);
            try {
                if (!monitor) {
                    logger.info("Job id: " + printJobId);
                    logger.fine("Sending data (each dot - 8Kb): ");
                } else {
                    monitorWindow.setUploadTotal((int)file.length());
                }
                final boolean initialPrintFlagFinal = initiatePrintFlag;
                final String printJobIdFinal = printJobId;

                response = printer.transmitPrintJob(printJobId, reader, new TransmitCallback() {
                    boolean initiatePrintFlag = initialPrintFlagFinal;
                    int bytes = 0;

                    @Override
                    public boolean transmitted(int sequenceNumber, int totalBytes) throws IOException {
                        if (totalBytes > 0 && initiatePrintFlag) {
                            @SuppressWarnings("unused")
                            StandardResponse response = printer.startPrint(printJobIdFinal);
                            // TODO do something with response
                            initiatePrintFlag = false;
                        } else if (monitor) {
                            monitorWindow.setUploadProgress(totalBytes);
                            synchronized (actions) {
                                while (actions.size() > 0) {
                                    executePrinterCommandImmediate(monitorWindow, actions.get(0));
                                    actions.remove(0);
                                    actions.notifyAll();
                                }
                                if (System.currentTimeMillis() - MonitorCommand.lastStatus > MonitorCommand.STATUS_TIMER) {
                                    executePrinterCommandImmediate(monitorWindow, () -> {
                                        PrinterStatusResponse printStatus = printer.getPrinterStatus();
                                        MonitorCommand.processPrinterStatus(monitorWindow, printStatus);
                                    });
                                    MonitorCommand.lastStatus = System.currentTimeMillis();
                                }
                            }
                        } else {
                            if (Main.logLevel.intValue() <= Level.FINE.intValue()) {
                                bytes = bytes + 512;
                                if (bytes >= 8192) {
                                    System.out.print(".");
                                    bytes = 0;
                                }
                            }
                        }
                        return continueUploading;
                    }
                });

                if (monitor) {
                    monitorWindow.setUploading(false);
                    synchronized (actions) {
                        while (true) {
                            if (actions.size() > 0) {
                                executePrinterCommandImmediate(monitorWindow, actions.get(0));
                                actions.remove(0);
                                actions.notifyAll();
                            }
                            try {
                                actions.wait(1000);
                            } catch (InterruptedException ignore) { }
                            if (System.currentTimeMillis() - MonitorCommand.lastStatus > MonitorCommand.STATUS_TIMER) {
                                executePrinterCommandImmediate(monitorWindow, () -> {
                                    PrinterStatusResponse printStatus = printer.getPrinterStatus();
                                    MonitorCommand.processPrinterStatus(monitorWindow, printStatus);
                                });
                                MonitorCommand.lastStatus = System.currentTimeMillis();
                            }
                        }
                    }
                }

            } finally {
                reader.close();
            }

            if (!monitor) {
                logger.fine(" done.");
                Main.processStandardResponse(printer, response);
            }
        }
    }

    private static void executePrinterCommandImmediate(MonitorWindow monitorWindow, IOAction ioAction) {
        try {
            ioAction.execute();
        } catch (IOException e) {
            monitorWindow.setExceptionError("Exception executing action", e);
        }
    }

    private static void executePrinterCommand(MonitorWindow monitorWindow, IOAction action) {
        synchronized (actions) {
            actions.add(action);
            actions.notifyAll();
            while (actions.size() > 0) {
                try {
                    actions.wait(500);
                } catch (InterruptedException ignore) { }
            }
        }
    }

    private static void processStandardResponse(MonitorWindow monitorWindow, StandardResponse response) {
        // TODO Auto-generated method stub

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

    public static interface IOAction {
        void execute() throws IOException;
    }

}
