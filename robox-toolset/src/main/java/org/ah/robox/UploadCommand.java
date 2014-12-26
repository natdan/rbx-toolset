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

/**
 *
 *
 * @author Daniel Sendula
 */
public class UploadCommand {

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
            } else {
                System.err.println("Unknown option: '" + a + "'");
                printHelp();
                System.exit(1);
            }
        }
        if (fileFlag) {
            System.err.println("File name is missing.");
            System.exit(1);
        }
        if (jobIdFlag) {
            System.err.println("Job id is missing.");
            System.exit(1);
        }

        if (fileName != null) {
            File f = new File(fileName);
            if (!f.exists()) {
                System.err.println("File '" + fileName + "' does not exist");
                System.exit(1);
            }

            if (jobId == null) {
                String fn = f.getName();
                if (fn.endsWith("_robox.gcode")) {
                    jobId = fn.substring(0, fn.length() - 12);
                } else if (fn.endsWith(".gcode")) {
                    jobId = fn.substring(0, fn.length() - 6);
                } else {
                    System.err.println("Cannot deduct print job from file name '" + jobId + "'");
                    System.exit(1);
                }
            }
            String warnings = copyJobFile(jobId, fileName);
            if (warnings != null) {
                System.err.println(warnings);
            }
        }

        cleanupConfigDir("xxxxxxxxxxxxxx");
    }

    public static void printHelp() {
        System.out.println("Usage: rbx [<general-options>] update [<specific-options>]");
        System.out.println("");
        Main.printGeneralOptions();
        System.out.println("");
        Main.printSpecificOptions();
        System.out.println("");
        System.out.println("  -h | --help | -?     - this page");
        System.out.println("  -f | --file          - gcode file needed for estimate");
        System.out.println("  -i | --job-id        - job id");
        System.out.println("");
        System.out.println("Note: if -f/--file option is not set, this command will try to");
        System.out.println("read job file from the stdin. But for it to work you must specify");
        System.out.println("-i/--job-id option with job-id. In case of -f/--file option you");
        System.out.println("may omit -i/--job-id option if file name comes form AutoMaker and");
        System.out.println("is in form xxxxxxxx_robox.gcode or xxxxxxxx.gcode where xxxxxxxx");
        System.out.println("is job id.");
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
