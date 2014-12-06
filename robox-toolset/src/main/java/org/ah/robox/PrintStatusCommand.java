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
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import org.ah.robox.comms.Printer;
import org.ah.robox.comms.PrinterChannel;
import org.ah.robox.comms.RoboxPrinter;
import org.ah.robox.comms.response.PrinterStatus;

/**
 *
 *
 * @author Daniel Sendula
 */
public class PrintStatusCommand {

    public static void execute(PrinterChannel selectedChannel, List<String> args) throws Exception {

        String fileName = null;
        boolean fileFlag = false;
        boolean estimateFlag = false;
        boolean shortFlag = false;
        boolean allFlag = false;

        boolean jobFlag = false;
        boolean busyFlag = false;
        boolean pauseStatusFlag = false;
        boolean currentLineFlag = false;
        boolean totalLineFlag = false;
        for (String a : args) {
            if (fileFlag) {
                fileName = a;
            } else if ("-f".equals(a) || "--file".equals(a)) {
                fileFlag = true;
            } else if ("-s".equals(a) || "--short".equals(a)) {
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
            } else {
                System.err.println("Unknown option: '" + a + "'");
                printHelp();
                System.exit(1);
            }
        }

        Printer printer = new RoboxPrinter(selectedChannel);

        PrinterStatus printStatus = printer.getPrinterStatus();

        String printJob = printStatus.getPrintJob();

        boolean hasRunningJobs = printJob != null && printJob.length() > 0;
        if (hasRunningJobs) {

            cleanupConfigDir(printJob);
            if (printStatus.getLineNumber() > 100) { // 100 is arbirtary number that is greater than setup: heating heads/bed, home, short purge, etc...
                createEstimateFile(printJob, printStatus.getLineNumber());
            }

            if (!shortFlag) {
                System.out.println("There a print in progress @ " + selectedChannel.getPrinterDeviceId() + "(" + selectedChannel.getPrinterPath() + ")");
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
                File configDir = ensureConfigDir();
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
            if (fileName != null) {
                String warnings = copyJobFile(printJob, fileName);
                if (warnings != null) {
                    System.err.println(warnings);
                }
            }
        } else {
            cleanupConfigDir("xxxxxxxxxxxxxx");
            System.out.println("There is no current jobs on the printer @ " + selectedChannel.getPrinterDeviceId() + "(" + selectedChannel.getPrinterPath() + ")");
            if (fileName != null) {
                System.err.println("Didn't set job file as there are not running jobs on the printer!");
                System.exit(1);
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
                    String estimateTime = calculateEstimate(printJob, printStatus.getLineNumber(), System.currentTimeMillis());
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
        System.out.println("  General options are one of these:");
        System.out.println("  -v | --verbose   - increases voutput erbosity level");
        System.out.println("  -d | --debug     - increases debug level");
        System.out.println("  -p | --printer   - if more than one printer is connected to your");
        System.out.println("                     computer you must select which one command is");
        System.out.println("                     going to be applied on. You can get list of");
        System.out.println("                     available printers using 'list' command");
        System.out.println("");
        System.out.println("  Specific options are:");
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

        System.out.println("  -f | --file          - gcode file needed for estimate.");
        System.out.println("");
        System.out.println("For estimate to work, this utility needs original xxx_robox.gcode file.");
        System.out.println("It will store file in ~/.robox/ dir, along with two more files:");
        System.out.println("  <jobid>.lines    - file that contains number of non-empty .gcode lines");
        System.out.println("  <jobid>.estimate - file with line number from job file that is higher");
        System.out.println("                     that 100 (warming bed and head). Also, that' file's");
        System.out.println("                     last modified date is going to serve for estimate");
        System.out.println("                     calculation.");
        System.out.println("You don't need to specify -f each time to obtain status - only once");
        System.out.println("at the beginning. <jobid>.estimate file is going to be create no matter");
        System.out.println("if estimate is going to succeed or fail (due to lack of <jobid> file");
        System.out.println("previously specified). It is going to be written only once per detected");
        System.out.println("job.");
        System.out.println("");
        System.out.println("Also, the moment new job is detected, all other files from previous jobs");
        System.out.println("are going to be removed.");
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
        File configDir = ensureConfigDir();
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

    private static void createLinesFile(String printJob, int numberOfLines) throws IOException {
        File configDir = ensureConfigDir();
        File linesFile = new File(configDir, printJob + ".lines");
        if (!linesFile.exists()) {
            try {
                FileWriter fileWriter = new FileWriter(linesFile);
                try {
                    fileWriter.write(Integer.toString(numberOfLines));
                } finally {
                    fileWriter.close();
                }
            } catch (IOException e) {
                throw new IOException("Cannot create lines file " + linesFile.getAbsolutePath(), e);
            }
        }
    }

    private static void cleanupConfigDir(String jobName) {
        File configDir = new File(new File(System.getProperty("user.home")), ".robox");
        if (configDir.exists()) {
            for (File f : configDir.listFiles()) {
                String fileName = f.getName();
                if (!fileName.startsWith(".") && !fileName.startsWith(jobName)) {
                    f.delete();
                }
            }
        }
    }

    private static String calculateEstimate(String printJob, int currentLine, long now) throws IOException {
        File configDir = ensureConfigDir();
        File estimateFile = new File(configDir, printJob + ".estimate");
        File linesFile = new File(configDir, printJob + ".lines");
        if (!linesFile.exists()) {
            throw new IOException("Cannot calcualte estimate as there is no .gcode file specified.\n"
                    + "Please use rbx status -f <robox.gcode file> with this job's file (job name '" + printJob + "')\n"
                    + "in order for number of lines to be calculated.");
        }
        long created = linesFile.lastModified();

        int totalLines = readNumberFromFile(configDir, linesFile);
        int startLine = readNumberFromFile(configDir, estimateFile);

        int currentDifference = currentLine - startLine;
        long currentDifferenceMillis = now - created;

        int remainingDifference = totalLines - currentLine;

        long remainingDifferenceMillis = currentDifferenceMillis * remainingDifference / currentDifference;

        //        System.out.println("currentDifference      = " + currentDifference);
        //        System.out.println("currentDifferenceMillis= " + currentDifferenceMillis);
        //        System.out.println("remainingDifference    = " + remainingDifference);

        StringBuilder res = new StringBuilder();

        // long millis = remainingDifferenceMillis % 1000;
        remainingDifferenceMillis = remainingDifferenceMillis / 1000;

        long seconds = remainingDifferenceMillis % 60;
        remainingDifferenceMillis = remainingDifferenceMillis / 60;

        long minutes = remainingDifferenceMillis % 60;
        remainingDifferenceMillis = remainingDifferenceMillis / 60;

        long hours = remainingDifferenceMillis % 60;

        res.append(Long.toString(hours));
        res.append(":");
        if (minutes < 10) {
            res.append("0");
        }
        res.append(Long.toString(minutes));
        res.append(":");

        if (seconds < 10) {
            res.append("0");
        }
        res.append(Long.toString(seconds));

        return res.toString();
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

    /**
     * @param printJob
     * @param fileName
     * @return warning string or null if no warnings.
     */
    private static String copyJobFile(String printJob, String fileName) throws IOException {
        String warning = null;
        File configDir = ensureConfigDir();
        File inJobFile = new File(fileName);
        if (!inJobFile.exists()) {
            throw new IOException("Cannot read file " + inJobFile.getAbsolutePath());
        }
        String inJobFileName = inJobFile.getName();
        if (!inJobFileName.endsWith(".gcode")) {
            warning = "Warning: Filename " + fileName + " doesn't end with '.gcode'";
        }
        File destJobFile = new File(configDir, printJob.toLowerCase() + "_robox.gcode");
        if (destJobFile.exists()) {
            warning = "Warning: Job file is already uploaded. Overwriting it.";
        }
        int numberOfLines = copyFileAndCalculateLines(inJobFile, destJobFile);
        createLinesFile(printJob, numberOfLines);
        return warning;
    }

    private static final int BEGINNING_OF_LINE = 0;
    private static final int DETECTED_LINE = 1;
    private static final int COMMENT = 2;

    /**
     * @param inJobFile
     * @param destJobFile
     * @return
     */
    private static int copyFileAndCalculateLines(File inJobFile, File destJobFile) throws IOException {
        int numberOfLines = 0;
        int state = BEGINNING_OF_LINE;
        byte[] buffer = new byte[10240];

        try {
            FileInputStream in = null;
            try {
                in = new FileInputStream(inJobFile);
            } catch (IOException e) {
                throw new IOException("Error opening file " + inJobFile.getAbsolutePath(), e);
            }
            try {
                FileOutputStream out = null;
                try {
                    out = new FileOutputStream(destJobFile);
                } catch (IOException e) {
                    throw new IOException("Error creating file " + inJobFile.getAbsolutePath(), e);
                }
                try {
                    int r = in.read(buffer);
                    while (r > 0) {
                        for (int i = 0; i < r; i++) {
                            byte b = buffer[i];
                            if (state == BEGINNING_OF_LINE) {
                                if (b == ';') {
                                    state = COMMENT;
                                } else if (b > ' ') {
                                    numberOfLines = numberOfLines + 1;
                                    state = DETECTED_LINE;
                                }
                            } else if (state == DETECTED_LINE) {
                                if (b == '\n') {
                                    state = BEGINNING_OF_LINE;
                                }
                            } else if (state == COMMENT) {
                                if (b == '\n') {
                                    state = BEGINNING_OF_LINE;
                                }
                            }
                        }
                        out.write(buffer, 0, r);
                        r = in.read(buffer);
                    }
                } finally {
                    out.close();
                }
            } finally {
                in.close();
            }
        } catch (IOException e) {
            throw new IOException("Error copying file from " + inJobFile.getAbsolutePath() + " to  " + destJobFile.getAbsolutePath(), e);
        }

        return numberOfLines;
    }

    // TODO factor out this method to some helper file
    public static void copyFile(File inJobFile, File destJobFile) throws IOException {
        byte[] buffer = new byte[10240];

        try {
            FileInputStream in = null;
            try {
                in = new FileInputStream(inJobFile);
            } catch (IOException e) {
                throw new IOException("Error opening file " + inJobFile.getAbsolutePath(), e);
            }
            try {
                FileOutputStream out = null;
                try {
                    out = new FileOutputStream(destJobFile);
                } catch (IOException e) {
                    throw new IOException("Error creating file " + inJobFile.getAbsolutePath(), e);
                }
                try {
                    int r = in.read(buffer);
                    while (r > 0) {
                        out.write(buffer, 0, r);
                        r = in.read(buffer);
                    }
                } finally {
                    out.close();
                }
            } finally {
                in.close();
            }
        } catch (IOException e) {
            throw new IOException("Error copying file from " + inJobFile.getAbsolutePath() + " to  " + destJobFile.getAbsolutePath(), e);
        }
    }

    private static File ensureConfigDir() throws IOException {
        File configDir = new File(new File(System.getProperty("user.home")), ".robox");
        if (!configDir.exists()) {
            if (!configDir.mkdirs()) {
                throw new IOException("Cannot create config dir " + configDir.getAbsolutePath());
            }
        }
        return configDir;
    }

    public static void main(String[] args) throws Exception {
//        String warnings = copyJobFile("test-job", System.getProperty("user.home") + "/CEL Robox/PrintJobs/06f86307f6db4e50/06f86307f6db4e50_robox.gcode");
//        if (warnings != null) {
//            System.err.println(warnings);
//        }
//        createEstimateFile("test-job", 300);

        String estimate = calculateEstimate("test-joba", 30000, System.currentTimeMillis());
        System.out.println("Current estimate: " + estimate);
    }

}
