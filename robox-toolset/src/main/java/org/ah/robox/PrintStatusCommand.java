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

import org.ah.robox.comms.Printer;
import org.ah.robox.comms.response.PrinterStatusResponse;

/**
 *
 *
 * @author Daniel Sendula
 */
public class PrintStatusCommand {

    public static int ESTIMATE_MIN_LINES = 100; // 100 is arbirtary number that is greater than setup: heating heads/bed, home, short purge, etc...

    public static void execute(Printer printer, List<String> args) throws Exception {

        boolean estimateFlag = false;
        boolean shortFlag = false;
        boolean allFlag = false;
        boolean jobFlag = false;
        boolean busyFlag = false;
        boolean pauseStatusFlag = false;
        boolean currentLineFlag = false;
        boolean totalLineFlag = false;
        for (String a : args) {
            if ("-s".equals(a) || "--short".equals(a)) {
                shortFlag = true;
            } else if ("-a".equals(a) || "--all".equals(a)) {
                allFlag = true;
            } else if ("-e".equals(a) || "--estimate".equals(a)) {
                estimateFlag = true;
            } else if ("-j".equals(a) || "--job".equals(a)) {
                jobFlag = true;
            } else if ("-b".equals(a) || "--busy".equals(a)) {
                jobFlag = true;
            } else if ("-ps".equals(a) || "--pause-status".equals(a)) {
                pauseStatusFlag = true;
            } else if ("-cl".equals(a) || "--current-line".equals(a)) {
                currentLineFlag = true;
            } else if ("-tl".equals(a) || "--total-lines".equals(a)) {
                totalLineFlag = true;
            } else if ("-h".equals(a) || "--help".equals(a) || "-?".equals(a)) {
                printHelp();
                System.exit(0);
            } else {
                System.err.println("Unknown option: '" + a + "'");
                printHelp();
                System.exit(1);
            }
        }

        PrinterStatusResponse printStatus = printer.getPrinterStatus();

        String printJob = printStatus.getPrintJob();

        boolean hasRunningJobs = printJob != null && printJob.length() > 0;

        UploadCommand.cleanupConfigDir(printJob);
        if (printStatus.getLineNumber() > ESTIMATE_MIN_LINES) {
            createEstimateFile(printJob, printStatus.getLineNumber());
        }

        if (!shortFlag) {
            System.out.println("There a print in progress @ " + printer.getPrinterName() + "(" + printer.getPrinterChannel().getPrinterPath() + ")");
        }
        if (jobFlag || allFlag) {
            if (shortFlag) {
                System.out.println(printJob);
            } else {
                System.out.println("    Job id      : '" + printJob + "'");
            }
        }
        if (currentLineFlag || allFlag) {
            if (shortFlag) {
                System.out.println(printStatus.getLineNumber());
            } else {
                System.out.println("    Current line: " + printStatus.getLineNumber());
            }
        }
        if (totalLineFlag || allFlag) {
            File configDir = UploadCommand.ensureConfigDir();
            File linesFile = new File(configDir, printJob + ".lines");
            if (!linesFile.exists()) {
                throw new IOException("Cannot return total number of lines as there is no .gcode file specified.\n"
                        + "Please use rbx status -f <robox.gcode file> with this job's file (job name '" + printJob + "')\n"
                        + "in order for number of lines to be calculated.");
            }
            int totalLines = readNumberFromFile(configDir, linesFile);

            if (shortFlag) {
                System.out.println(Integer.toString(totalLines));
            } else {
                System.out.println("    Total # line:  " + Integer.toString(totalLines));
            }
        }

        if (pauseStatusFlag || allFlag) {
            if (shortFlag) {
                System.out.println(printStatus.getPause().getText());
            } else {
                System.out.println("    Status      : " + printStatus.getPause().getText());
            }
        }
        if (busyFlag || allFlag) {
            if (shortFlag) {
                System.out.println(printStatus.isBusy());
            } else {
                System.out.println("    Busy        : " + printStatus.isBusy());
            }
        }
        if (hasRunningJobs) {
            if (estimateFlag) {
                try {
                    Estimate estimate = calculateEstimate(printJob, printStatus.getLineNumber(), System.currentTimeMillis());
                    String estimateTime = estimate.toString();
                    if (shortFlag) {
                        System.out.println(estimateFlag);
                    } else {
                        System.out.println("    Estimate    : " + estimateTime);
                    }
                } catch (IOException e) {
                    System.err.println(e.getMessage());
                    System.exit(1);
                }
            }
        }
    }

    public static void printHelp() {
        System.out.println("Usage: rbx [<general-options>] status [<specific-options>]");
        System.out.println("");
        Main.printGeneralOptions();
        System.out.println("");
        Main.printSpecificOptions();
        System.out.println("");
        System.out.println("  -h | --help | -?     - this page");
        System.out.println("  -a | --all           - displays all status information");
        System.out.println("  -s | --short         - displays values only");
        System.out.println("                         It is machine readable format.");

        System.out.println("  -e | --estimate      - displays estimate time until job completion.");
        System.out.println("                         See -f/--file option for more details.");
        System.out.println("  -j | --job           - displays job id");
        System.out.println("  -b | --busy          - displays busy flag");
        System.out.println("  -ps | --pause-status - displays pause status");
        System.out.println("  -cl | --current-line - displays current line number");
        System.out.println("  -tl | --total-lines  - displays total line number. Only if file was supplied.");

        System.out.println("");
        System.out.println("For estimate to work, this utility needs original xxx_robox.gcode file.");
        System.out.println("See rbx upload command.");
        System.out.println("");
        System.out.println("More time passed, estimate might be more correct. Estimate is calculated");
        System.out.println("by amount of lines processed per amount of time starting from when");
        System.out.println("<jobid>.estimate file is created.");
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
            if (currentLine > ESTIMATE_MIN_LINES) { // 100 is arbirtary number that is greater than setup: heating heads/bed, home, short purge, etc...
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
