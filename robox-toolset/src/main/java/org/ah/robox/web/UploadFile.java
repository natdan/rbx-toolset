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
package org.ah.robox.web;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.ah.robox.UploadCommand;

/**
 *
 *
 * @author Daniel Sendula
 */
public class UploadFile {

    public static Result uploadFile(InputStream inputStream) {
        try {
            InputStreamReader isr = new InputStreamReader(inputStream);

            BufferedReader in = new BufferedReader(isr);
            String line = in.readLine();
            if (line == null) {
                return Result.BAD_INPUT;
            }
            String divider = line + "--";
            String filename = null;

            // Process headers


            line = in.readLine();
            while (line != null && !"".equals(line)) {
                if (line.startsWith("Content-Disposition:")) {
                    int i = line.indexOf("filename=\"");
                    int e = line.indexOf("\"", i + 10);
                    filename = line.substring(i + 10, e);
//                } else if (line.startsWith("Content-Type: ")) {
//                    String typ = line.substring(14);
//                    if (!typ.startsWith("text/")) {
//                        return Result.NOT_TEXT;
//                    }
                }
                line = in.readLine();
            }
            if (line == null) {
                return Result.BAD_INPUT;
            }

            if (filename == null) {
                return Result.NO_FILENAME;
            }

            String jobName = null;
            boolean gcode = false;
            // boolean roboxGCode = false;

            if (filename.endsWith(".gcode")) {
                gcode = true;
                if (filename.endsWith("_robox.gcode")) {
                    // roboxGCode = true;
                    jobName = filename.substring(0, filename.length() - 12);
                } else {
                    jobName = filename.substring(0, filename.length() - 6);
                }
            } else if (filename.endsWith(".statistics")) {
                jobName = filename.substring(0, filename.length() - 11);
            }


            File configDir = UploadCommand.ensureConfigDir();
            File file = new File(configDir, filename);

            int numberOfLines = 0;
            FileWriter out = new FileWriter(file);
            try {
                line = in.readLine();
                while (line != null && !line.equals(divider)) {
                    int i = line.indexOf(';');
                    if (i >= 0) {
                        line = line.substring(0, i);
                    }
                    line = line.trim();
                    if (line.length() > 0) {
                        numberOfLines = numberOfLines + 1;
                        out.write(line);
                        out.write(File.separator);
                    }
                    line = in.readLine();
                }
            } finally {
                out.close();
            }

            if (gcode) {
                UploadCommand.createLinesFile(jobName, numberOfLines);
            }
        } catch (IOException e) {

        } finally {
            try {
                inputStream.close();
            } catch (IOException ignore) { }
        }

        return Result.UPLOADED;
    }

    public static enum Result {
        UPLOADED,
        NOT_TEXT,
        BAD_INPUT,
        NO_FILENAME;
    }
}
