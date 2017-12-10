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
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.logging.Logger;

/**
 *
 *
 * @author Daniel Sendula
 */
public class UploadCommand {
    private static final Logger logger = Logger.getLogger(UploadCommand.class.getName());

    public static void execute(List<String> args) throws Exception {

        String fileName = null;
        boolean fileFlag = false;
        String jobId = null;
        boolean jobIdFlag = false;

        for (String a : args) {
            if (fileFlag) {
                fileName = a;
                fileFlag = false;
            } else if (jobIdFlag) {
                jobId = a;
                jobIdFlag = false;
            } else if ("-f".equals(a) || "--file".equals(a)) {
                fileFlag = true;
            } else if ("-i".equals(a) || "--job-id".equals(a)) {
                jobIdFlag = true;
            } else if ("-h".equals(a) || "--help".equals(a) || "-?".equals(a)) {
                printHelp();
                System.exit(0);
            } else {
                logger.severe("Unknown option: '" + a + "'");
                printHelp();
                System.exit(1);
            }
        }
        if (fileFlag) {
            logger.severe("File name is missing.");
            System.exit(1);
        }
        if (jobIdFlag) {
            logger.severe("Job id is missing.");
            System.exit(1);
        }

        if (fileName != null) {
            File f = new File(fileName);
            if (!f.exists()) {
                logger.severe("File '" + fileName + "' does not exist");
                System.exit(1);
            }

            if (jobId == null) {
                String fn = f.getName();
                if (fn.endsWith("_robox.gcode")) {
                    jobId = fn.substring(0, fn.length() - 12);
                } else if (fn.endsWith(".gcode")) {
                    jobId = fn.substring(0, fn.length() - 6);
                } else {
                    logger.severe("Cannot deduct print job from file name '" + jobId + "'");
                    System.exit(1);
                }
            }
            String warnings = copyJobFile(jobId, fileName);
            if (warnings != null) {
                logger.warning(warnings);
            }
        }

        cleanupConfigDir("xxxxxxxxxxxxxx");
    }

    public static void printHelp() {
        logger.info("Usage: rbx [<general-options>] update [<specific-options>]");
        logger.info("");
        logger.info("  General options are one of these:");
        logger.info("  -v | --verbose   - increases voutput erbosity level");
        logger.info("  -d | --debug     - increases debug level");
        logger.info("");
        Main.printSpecificOptions();
        logger.info("");
        logger.info("  -f | --file          - gcode file needed for estimate");
        logger.info("  -i | --job-id        - job id");
        logger.info("");
        logger.info("Note: if -f/--file option is not set, this command will try to");
        logger.info("read job file from the stdin. But for it to work you must specify");
        logger.info("-i/--job-id option with job-id. In case of -f/--file option you");
        logger.info("may omit -i/--job-id option if file name comes form AutoMaker and");
        logger.info("is in form xxxxxxxx_robox.gcode or xxxxxxxx.gcode where xxxxxxxx");
        logger.info("is job id.");
        logger.info("");
        logger.info("For estimate to work, this utility needs original xxx_robox.gcode file.");
        logger.info("It will store file in ~/.robox/ dir, along with two more files:");
        logger.info("  <jobid>.lines    - file that contains number of non-empty .gcode lines");
        logger.info("  <jobid>.estimate - file with line number from job file that is higher");
        logger.info("                     that 100 (warming bed and head). Also, that' file's");
        logger.info("                     last modified date is going to serve for estimate");
        logger.info("                     calculation.");
        logger.info("You don't need to specify -f each time to obtain status - only once");
        logger.info("at the beginning. <jobid>.estimate file is going to be create no matter");
        logger.info("if estimate is going to succeed or fail (due to lack of <jobid> file");
        logger.info("previously specified). It is going to be written only once per detected");
        logger.info("job.");
        logger.info("");
        logger.info("Also, the moment new job is detected, all other files from previous jobs");
        logger.info("are going to be removed.");
        logger.info("");
        logger.info("More time passed, estimate might be more correct. Estimate is calculated");
        logger.info("by amount of lines processed per amount of time starting from when");
        logger.info("<jobid>.estimate file is created.");
    }

    public static void createLinesFile(String printJob, int numberOfLines) throws IOException {
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

    public static void cleanupConfigDir(String jobName) {
        long now = System.currentTimeMillis();
        long twoDaysAgo = now - (1000l * 60l * 60l * 24l * 2l); // Two days ago

        File configDir = new File(new File(System.getProperty("user.home")), ".robox");
        if (configDir.exists()) {
            for (File f : configDir.listFiles()) {
                String fileName = f.getName();
                if (!fileName.startsWith(".") && !fileName.startsWith(jobName)) {
                    long created = f.lastModified();
                    if (created < twoDaysAgo) {
                        f.delete();
                    }
                }
            }
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

    public static File ensureConfigDir() throws IOException {
        File configDir = new File(new File(System.getProperty("user.home")), ".robox");
        if (!configDir.exists()) {
            if (!configDir.mkdirs()) {
                throw new IOException("Cannot create config dir " + configDir.getAbsolutePath());
            }
        }
        return configDir;
    }
}
