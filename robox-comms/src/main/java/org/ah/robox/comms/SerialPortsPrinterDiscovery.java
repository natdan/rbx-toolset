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

import gnu.io.CommPort;
import gnu.io.CommPortIdentifier;
import gnu.io.NoSuchPortException;
import gnu.io.PortInUseException;
import gnu.io.SerialPort;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 *
 * @author Daniel Sendula
 */
public class SerialPortsPrinterDiscovery implements PrinterDiscovery {

    {
        System.setProperty("java.library.path", "/usr/lib/jni");

        String pathToAdd = "/usr/lib/jni";

        try {
            Field usrPathsField = ClassLoader.class.getDeclaredField("usr_paths");
            usrPathsField.setAccessible(true);

            String[] paths = (String[]) usrPathsField.get(null);

            boolean found = false;
            for (String path : paths) {
                if (path.equals(pathToAdd)) {
                    found = true;
                }
            }

            if (!found) {
                String[] newPaths = Arrays.copyOf(paths, paths.length + 1);
                newPaths[newPaths.length - 1] = pathToAdd;
                usrPathsField.set(null, newPaths);
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialise native library " + pathToAdd, e);
        }
    }

    public SerialPortsPrinterDiscovery() {
    }

    public List<PrinterChannel> findPrinters() throws IOException {
        List<PrinterChannel> printerChannels = new ArrayList<PrinterChannel>();

        File devSerialById = new File("/dev/serial/by-id");

        if (devSerialById.exists()) {
            for (File devFile : devSerialById.listFiles()) {
                String devName = devFile.getName();
                if (devName.contains("Robox")) {
                    String id = devName;

                    if (Files.isSymbolicLink(devFile.toPath())) {
                        devName = new File(devFile.getParent(), Files.readSymbolicLink(devFile.toPath()).toString()).getCanonicalPath();
                    }

                    try {
                        CommPortIdentifier portIdentifier = CommPortIdentifier.getPortIdentifier(devName);

                        if (portIdentifier.isCurrentlyOwned()) {
                            System.err.println("Error: Port is currently in use");
                            System.exit(1);
                        } else {
                            int timeout = 2000;

                            CommPort commPort = portIdentifier.open("Robox", timeout);

                            if (commPort instanceof SerialPort) {
                                SerialPort serialPort = (SerialPort)commPort;

                                SerialPortPrinterChannel printerChannel = new SerialPortPrinterChannel(id, devName, serialPort);
                                printerChannels.add(printerChannel);
                            }
                        }
                    } catch (NoSuchPortException e) {
                        // throw new IOException(e);
                        // e.printStackTrace();
                    } catch (PortInUseException e) {
                        // throw new IOException(e);
                        // e.printStackTrace();
                    }

                }
            }
        }
        return printerChannels;
    }

}
