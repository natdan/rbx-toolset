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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;

import org.ah.robox.Main;

/**
 *
 *
 * @author Daniel Sendula
 */
public class Image {

    private long lastInvoked;
    private int interval = 5;
    private boolean run = true;
    private String imageCommand;
    private byte[] image;
    private String lastError;

    public byte[] getImage() {
        long now = System.currentTimeMillis();
        if (image == null
                || now - lastInvoked > interval * 1000) {
            image = readImageCommand(imageCommand);
        }
        return image;
    }

    /**
     * @param imageCommand2
     */
    private synchronized byte[] readImageCommand(String imageCommand) {
        try {
            Process process = Runtime.getRuntime().exec(imageCommand);
            InputStream es = process.getErrorStream();
            InputStream is = process.getInputStream();
            ByteArrayOutputStream buf = new ByteArrayOutputStream();
            byte[] buffer = new byte[10240];

            int r = is.read(buffer);
            while (r > 0) {
                buf.write(buffer, 0, r);
                r = is.read(buffer);
            }
            is.close();

            ByteArrayOutputStream err = new ByteArrayOutputStream();

            r = es.read(buffer);
            while (r > 0) {
                buf.write(buffer, 0, r);
                r = es.read(buffer);
            }
            es.close();

            byte[] errBytes = err.toByteArray();
            if (errBytes.length > 0) {
                lastError = new String(errBytes, "US-ASCII");
            }

            return buf.toByteArray();
        } catch (IOException e) {
            if (Main.debugFlag) {
                e.printStackTrace(System.err);
            } else if (Main.verboseFlag) {
                System.err.println(e.getMessage());
            }
            StringWriter err = new StringWriter();
            PrintWriter printWriter = new PrintWriter(err);
            e.printStackTrace(printWriter);
            lastError = err.toString();
        }
        return null;
    }

    public void stop() {
        run = false;
    }

    public long getLastInvoked() {
        return lastInvoked;
    }

    public int getInterval() {
        return interval;
    }

    public void setInterval(int interval) {
        this.interval = interval;
    }

    public boolean isRunnable() {
        return run;
    }

    public String getImageCommand() {
        return imageCommand;
    }

    public void setImageCommand(String imageCommand) {
        this.imageCommand = imageCommand;
    }

    public String getLastError() {
        return lastError;
    }
}
