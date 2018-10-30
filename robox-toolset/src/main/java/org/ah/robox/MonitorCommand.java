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
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.logging.Logger;

import org.ah.robox.comms.Printer;
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
public class MonitorCommand {

    private static final Logger logger = Logger.getLogger(MonitorCommand.class.getName());

    public static void execute(Printer printer, List<String> args) throws Exception {

        boolean detachFlag = false;
        boolean fileFlag = false;
        File file = null;
        for (String a : args) {
            if (fileFlag) {
                fileFlag = false;
                file = new File(a);
            } else if ("-?".equals(a) || "-h".equals(a) || "--help".equals(a)) {
                printHelp();
                System.exit(0);
            } else if ("-f".equals(a) || "--file".equals(a)) {
                fileFlag = true;
            } else if ("-d".equals(a) || "--detach".equals(a)) {
                detachFlag = true;
            } else {
                logger.severe("Unknown option: '" + a + "'");
                printHelp();
                System.exit(1);
            }
        }

        if (detachFlag && !Main.detachedFlag) {
            Detach.detach(Main.class.getName());
        } else {

            MonitorWindow monitorWindow = new MonitorWindow();
            monitorWindow.setUploading(false);
            monitorWindow.setVisible(true);

            monitorWindow.setAbortPrintingAction(() -> {
                monitorWindow.executePrinterCommand(() -> {
                    try {
                        StandardResponse response = printer.abortPrint();
                        monitorWindow.processStandardResponse(response);
                        if (Main.processStandardResponse(printer, response)) {
                            GCodeCommand.sendGCode(printer, AbortPrintCommand.FINISH_PRINT_GCODE);
                        }
                    } catch (IOException e) { }
                    System.exit(0);
                });
            });

            monitorWindow.setPausePrintingAction(() -> {
                monitorWindow.executePrinterCommand(() -> {
                    PrinterStatusResponse printerStatus = printer.getPrinterStatus();

                    PrinterPause status = printerStatus.getCombinedStatus();
                    monitorWindow.setStatus(status);
                    if (status == PrinterPause.PAUSED) {
                        StandardResponse response = printer.resumePrinter();
                        monitorWindow.processStandardResponse(response);
                    } else if (status == PrinterPause.WORKING) {
                        StandardResponse response = printer.pausePrinter();
                        monitorWindow.processStandardResponse(response);
                    }
                });
            });

            if (file != null) {
                try {
                    int totalLines = UploadCommand.countLines(file);
                    monitorWindow.setLinesTotal(totalLines);
                } catch (IOException e) {
                    monitorWindow.setExceptionError("Exception reading file " + file.getAbsolutePath(), e);
                }
            }

            PrinterStatusResponse printStatus = printer.getPrinterStatus();

            String printJob = printStatus.getPrintJob();

            UploadCommand.cleanupConfigDir(printJob);
            if (printStatus.getLineNumber() > PrintStatusCommand.ESTIMATE_MIN_LINES) {
                createEstimateFile(printJob, printStatus.getLineNumber());
            }

            while (true) {
                monitorWindow.executePrinterActions(printer, 1000);
            }
        }
    }

    public static void printHelp() {
        logger.info("Usage: rbx [<general-options>] status [<specific-options>]");
        logger.info("");
        Main.printGeneralOptions();
        logger.info("");
        Main.printSpecificOptions();
        logger.info("");
        logger.info("  -h | --help | -?     - this page");
        logger.info("  -a | --all           - displays all status information");
        logger.info("  -s | --short         - displays values only");
        logger.info("                         It is machine readable format.");

        logger.info("  -f | --file           - file of the job - xxx_robox.gcode file (for estimations).");
        logger.info("  -d | --detach         - detach this process from current shell;");
        logger.info("                          normally used with monitor.");

        logger.info("");
        logger.info("For estimate to work, this utility needs original xxx_robox.gcode file.");
        logger.info("See rbx upload command.");
        logger.info("");
        logger.info("More time passed, estimate might be more correct. Estimate is calculated");
        logger.info("by amount of lines processed per amount of time starting from when");
        logger.info("<jobid>.estimate file is created.");
    }

    /**
     * @param printJob
     * @param lineNumber
     */
    private static void createEstimateFile(String printJob, int lineNumber) throws IOException {
        File configDir = UploadCommand.ensureConfigDir();
        File estimateFile = new File(configDir, printJob + ".estimate");
        if (!estimateFile.exists()) {
            try {
                FileWriter fileWriter = new FileWriter(estimateFile);
                try {
                    fileWriter.write(Integer.toString(lineNumber));
                } finally {
                    fileWriter.close();
                }
            } catch (IOException e) {
                throw new IOException("Cannot create estimate file " + estimateFile.getAbsolutePath(), e);
            }
        }
    }

    /**
     * <p>Returns estimate in a form of a string. For estiamte to work previous estimate file needs to exist
     * and lines file with total number of lines.</p>
     *
     * @param printJob print job
     * @param currentLine current line
     * @param now number of milliseconds - System.currentMillis()
     * @return
     * @throws IOException
     */
    public static Estimate calculateEstimate(String printJob, int currentLine, long now) throws IOException {
        File configDir = UploadCommand.ensureConfigDir();
        File estimateFile = new File(configDir, printJob + ".estimate");
        if (!estimateFile.exists()) {
            if (currentLine > PrintStatusCommand.ESTIMATE_MIN_LINES) { // 100 is arbirtary number that is greater than setup: heating heads/bed, home, short purge, etc...
                createEstimateFile(printJob, currentLine);
            } else {
                return new Estimate(0, 0, 0, -1, EstimateState.PREPARING);
            }
        }
        File linesFile = new File(configDir, printJob + ".lines");
        if (!linesFile.exists()) {
            return new Estimate(0, 0, 0, -1, EstimateState.NO_LINES);
        }
        long created = linesFile.lastModified();

        int totalLines = readNumberFromFile(configDir, linesFile);
        int startLine = readNumberFromFile(configDir, estimateFile);

        int currentDifference = currentLine - startLine;
        long currentDifferenceMillis = now - created;

        int remainingDifference = totalLines - currentLine;

        long remainingDifferenceMillis = 0;

        if (currentDifference != 0) {
            remainingDifferenceMillis = currentDifferenceMillis * remainingDifference / currentDifference;
        }

        // long millis = remainingDifferenceMillis % 1000;
        remainingDifferenceMillis = remainingDifferenceMillis / 1000;

        long seconds = remainingDifferenceMillis % 60;
        remainingDifferenceMillis = remainingDifferenceMillis / 60;

        long minutes = remainingDifferenceMillis % 60;
        remainingDifferenceMillis = remainingDifferenceMillis / 60;

        long hours = remainingDifferenceMillis % 60;

        return new Estimate((int)hours, (int)minutes, (int)seconds, totalLines, EstimateState.PRINTING);
    }

    private static int readNumberFromFile(File configDir, File file) throws IOException {
        try {
            FileReader reader = new FileReader(file);
            try {
                char[] buffer = new char[10240];
                int r = reader.read(buffer);
                if (r > 0) {
                    String str = new String(buffer, 0, r);
                    try {
                        int number = Integer.parseInt(str);
                        return number;
                    } catch (NumberFormatException e) {
                        throw new IOException("File doesn't contain number: '" + str + "'; " + file.getAbsolutePath());
                    }
                } else {
                    throw new IOException("Lines file is empty. Cannot calculate estimate.");
                }
            } finally {
                reader.close();
            }
        } catch (IOException e) {
            throw new IOException("Cannot read file " + file.getAbsolutePath());
        }
    }

    public enum EstimateState {

        PRINTING("Printing"), PREPARING("Preparing"), NO_LINES("No gcode file available"), IDLE("Idle");

        private String text;

        private EstimateState(String text) { this.text = text; }

        public String getText() { return text; }
    }

    public static class Estimate {
        private int totalLines;
        private int hours;
        private int minutes;
        private int seconds;
        private EstimateState printState;

        public Estimate(int hours, int minutes, int seconds, int totalLines, EstimateState printState) {
            this.hours = hours;
            this.minutes = minutes;
            this.seconds = seconds;
            this.totalLines = totalLines;
            this.printState = printState;
        }

        public String getHours() {
            return Integer.toString(hours);
        }

        public String getMinutes() {
            String res = Integer.toString(minutes);
            if (res.length() < 2) {
                res = "0" + res;
            }
            return res;
        }

        public String getSeconds() {
            String res = Integer.toString(seconds);
            if (res.length() < 2) {
                res = "0" + res;
            }
            return res;
        }

        public EstimateState getPrintStatus() {
            return printState;
        }

        public String toString(String format) {
            String res = format.replace("%h", getHours()).replace("%m", getMinutes()).replace("%s", getSeconds());
            return res;
        }

        @Override
        public String toString() {
            if (printState == EstimateState.PRINTING) {
                return toString("%h:%m:%s");
            } else {
                return printState.getText();
            }
        }

        public int getTotalLines() {
            return totalLines;
        }
    }
}
