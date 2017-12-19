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
package org.ah.robox.comms;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import org.ah.robox.comms.utils.NotASerialPort;
import org.ah.robox.comms.utils.SerialPortAlreadyInUse;

/**
 *
 * @author Daniel Sendula
 */
public class JSSCPrinterDiscovery extends BasePrinterDiscovery {

    public JSSCPrinterDiscovery() {
    }

    @Override
    public List<PrinterChannel> findAllPrinterChannels() throws IOException {
        if (isWindows) {
            logger.severe("Windwos operating system is not yet supported. Check later");
            System.exit(1);
        } else if (isLinux) {
            return detectLinuxPrinters();
        } else if (isOSX) {
            return detectOSXPrinters();
        } else {
            logger.severe("Operating system " + System.getProperty("os.name") + " not supported");
            System.exit(1);
        }

        return new ArrayList<PrinterChannel>();
    }

    public List<PrinterChannel> detectOSXPrinters() throws IOException {
        logger.fine("Scanning for Robox printers");

        logger.finer("Executing 'ioreg -p IOService -n Robox -rl'");
        List<PrinterChannel> printerChannels = new ArrayList<PrinterChannel>();

        Process process = Runtime.getRuntime().exec("ioreg -p IOService -n Robox -rl");

        try {
            InputStream is = process.getInputStream();
            InputStreamReader isr = new InputStreamReader(is);
            BufferedReader in = new BufferedReader(isr);
            String line = in.readLine();
            while (line != null) {
                if (line.contains("IOCalloutDevice")) {
                    int i = line.indexOf("\" = \"");
                    if (i > 0) {
                        String devicePath = line.substring(i + 5, line.length() - 1);
                        discoverSerialDevice(printerChannels, devicePath);
                    }
                }

                line = in.readLine();
            }
        } catch (Exception e) {
        }

        return printerChannels;
    }

    public List<PrinterChannel> detectLinuxPrinters() throws IOException {
        List<PrinterChannel> printerChannels = new ArrayList<PrinterChannel>();
        File devSerialById = null;
        devSerialById = new File("/dev/serial/by-id");
        logger.fine("Scanning " + devSerialById.getAbsolutePath() + " for Robox printers");

        if (devSerialById != null && devSerialById.exists()) {
            for (File devFile : devSerialById.listFiles()) {
                String devName = devFile.getName();
                if (devName.contains("Robox")) {

                    if (Files.isSymbolicLink(devFile.toPath())) {
                        logger.finer("Device " + devFile.getAbsolutePath() + " is symbolic link. Resolving it.");
                        devName = new File(devFile.getParent(), Files.readSymbolicLink(devFile.toPath()).toString()).getCanonicalPath();
                        logger.finer("Device " + devFile.getAbsolutePath() + " symbolic link is resolved to " + devName);
                    }

                    try {
                        discoverSerialDevice(printerChannels, devName);
                    } catch (IOException ignore) { }
                }
            }
        }
        return printerChannels;
    }

    private void discoverSerialDevice(List<PrinterChannel> printerChannels, String devName) throws IOException {
        try {
            logger.fine("Trying to open device " + devName);
            PrinterChannel channel = channels.get(devName);
            if (channel != null) {
                printerChannels.add(channel);
            } else {

                PrinterChannel printerChannel = new JSSCSerialPortPrinterChannel(devName);

                try {
                    printerChannel.open();
                    printerChannel.close();

                    channels.put(devName, printerChannel);
                    printerChannels.add(printerChannel);
                } catch (NotASerialPort e) {
                    logger.log(Level.FINER, "", e);
                } catch (SerialPortAlreadyInUse e) {
                    logger.log(Level.FINER, "", e);
                }
            }
        } catch (IOException e) {
            logger.log(Level.FINER, "", e);
            throw e;
        }
    }
}
