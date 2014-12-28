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

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.ah.robox.comms.utils.SharedLibraries;

/**
 *
 * @author Daniel Sendula
 */
public class SerialPortsPrinterDiscovery implements PrinterDiscovery {

    {
        SharedLibraries.load("rxtxSerial");
    }

    private boolean verbose = false;
    private boolean debug = false;
    private Map<String, SerialPortPrinterChannel> channels = new HashMap<String, SerialPortPrinterChannel>();
    private Map<PrinterChannel, Printer> printers = new HashMap<PrinterChannel, Printer>();

    protected void closed(SerialPortPrinterChannel channel) {
        channels.remove(channel);
    }

    public void setVerbose(boolean verbose) {
        this.verbose = verbose;
    }

    public void setDebug(boolean debug) {
        this.debug = debug;
    }


    public SerialPortsPrinterDiscovery() {
    }

    public List<PrinterChannel> findAllPrinterChannels() throws IOException {

        boolean isWindows = System.getProperty("os.name").contains("Windows");
        boolean isLinux = System.getProperty("os.name").contains("Linux");
        boolean isOSX = System.getProperty("os.name").contains("Mac");
        if (isWindows) {
            System.err.println("Windwos operating system is not yet supported. Check later");
            System.exit(1);
        } else if (isLinux) {
            return detectLinuxPrinters();
        } else if (isOSX) {
            return detectOSXPrinters();
        } else {
            System.err.println("Operating system " + System.getProperty("os.name") + " not supported");
            System.exit(1);
        }

        return new ArrayList<PrinterChannel>();
    }

    public List<PrinterChannel> detectOSXPrinters() throws IOException {
        if (verbose) {
            System.out.println("Scanning for Robox printers");
        }
        if (debug) {
            System.out.println("Executing 'ioreg -p IOService -n Robox -rl'");
        }
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
                        openSerialDevice(printerChannels, devicePath);
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
        if (verbose) {
            System.out.println("Scanning " + devSerialById.getAbsolutePath() + " for Robox printers");
        }

        if (devSerialById != null && devSerialById.exists()) {
            for (File devFile : devSerialById.listFiles()) {
                String devName = devFile.getName();
                if (devName.contains("Robox")) {

                    if (Files.isSymbolicLink(devFile.toPath())) {
                        if (debug) {
                            System.out.println("Device " + devFile.getAbsolutePath() + " is symbolic link. Resolving it.");
                        }
                        devName = new File(devFile.getParent(), Files.readSymbolicLink(devFile.toPath()).toString()).getCanonicalPath();
                        if (debug) {
                            System.out.println("Device " + devFile.getAbsolutePath() + " symbolic link is resolved to " + devName);
                        }
                    }

                    openSerialDevice(printerChannels, devName);
                }
            }
        }
        return printerChannels;
    }

    private void openSerialDevice(List<PrinterChannel> printerChannels, String devName) throws IOException {
        try {
            if (verbose) {
                System.out.println("Trying to open device " + devName);
            }
            SerialPortPrinterChannel channel = channels.get(devName);
            if (channel != null) {
                printerChannels.add(channel);
            } else {

                CommPortIdentifier portIdentifier = CommPortIdentifier.getPortIdentifier(devName);

                if (portIdentifier.isCurrentlyOwned()) {
                    System.err.println("Error: Port is currently in use");
                    System.exit(1);
                } else {
                    int timeout = 2000;

                    CommPort commPort = portIdentifier.open(devName, timeout);

                    if (commPort instanceof SerialPort) {
                        SerialPort serialPort = (SerialPort)commPort;


                        SerialPortPrinterChannel printerChannel = new SerialPortPrinterChannel(this, devName, serialPort);

                        channels.put(devName, printerChannel);
                        printerChannels.add(printerChannel);
                    } else {
                        if (verbose) {
                            System.err.println("Device " + devName + " is not serial port");
                        }
                    }
                }
            }
        } catch (NoSuchPortException e) {
            if (debug) {
                e.printStackTrace();
            } else if (verbose) {
                System.err.println(e.getMessage());
            }
        } catch (PortInUseException e) {
            if (debug) {
                e.printStackTrace();
            } else if (verbose) {
                System.err.println(e.getMessage());
            }
        } catch (IOException e) {
            if (debug) {
                e.printStackTrace();
            } else if (verbose) {
                System.err.println(e.getMessage());
            }
            throw e;
        }
    }

    public List<Printer> findAllPrinters() throws IOException {
        List<Printer> resultPrinters = new ArrayList<Printer>();
        List<PrinterChannel> channels = findAllPrinterChannels();
        for (PrinterChannel channel : channels) {
            Printer printer = getPrinterForChannel(channel);
            printers.put(channel, printer);
            resultPrinters.add(printer);
        }
        return resultPrinters;
    }

    public Printer getPrinterForChannel(PrinterChannel printerChannel) {
        Printer printer = printers.get(printerChannel);
        if (printer == null) {
            RoboxPrinter p = new RoboxPrinter(printerChannel);
            try {
                p.init();
                printer = p;
                printers.put(printerChannel, printer);
            } catch (IOException e) {
                System.err.println("Failed to initialise printer on path " + printerChannel.getPrinterPath());
            }
        }
        return printer;
    }

}
