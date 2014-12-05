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

import java.util.ArrayList;
import java.util.List;

import org.ah.robox.comms.PrinterChannel;
import org.ah.robox.comms.SerialPortsPrinterDiscovery;
import org.ah.robox.comms.response.ResponseFactory;

/**
 *
 *
 * @author Daniel Sendula
 */
public class Main {

    public static boolean verboseFlag = false;
    public static boolean debugFlag = false;

    public static void main(String[] args) throws Exception {
        String command = null;

        boolean furtherArgsFlag = false;
        boolean printerFlag = false;
        String printerId = null;

        List<String> furtherArgs = new ArrayList<String>();
        for (String arg : args) {
            if (furtherArgsFlag) {
                furtherArgs.add(arg);
            } else if (printerFlag) {
                printerId = arg;
                printerFlag = false;
            } else {
                if ("-p".equals(arg) || "--printer".equals(arg)) {
                    printerFlag = true;
                } else if ("-v".equals(arg) || "--verbose".equals(arg)) {
                    verboseFlag = true;
                } else if ("-d".equals(arg) || "--debug".equals(arg)) {
                    debugFlag = true;
                } else if ("-?".equals(arg) || "-h".equals(arg) || "--help".equals(arg)) {
                    command = "help";
                } else if ("list".equals(arg)) {
                    command = "list";
                    furtherArgsFlag = true;
                } else if ("status".equals(arg)) {
                    command = "status";
                    furtherArgsFlag = true;
                } else if ("pause".equals(arg)) {
                    command = "pause";
                    furtherArgsFlag = true;
                } else if ("resume".equals(arg)) {
                    command = "resume";
                    furtherArgsFlag = true;
                }
            }
        }

        if ("help".equals(command) || command == null) {
            printHelp();
            System.exit(0);
        } else {

            SerialPortsPrinterDiscovery discovery = new SerialPortsPrinterDiscovery();
            List<PrinterChannel> printerChannels = discovery.findPrinters();

            if ("list".equals(command)) {
                ListCommand.execute(printerChannels);
            } else {
                PrinterChannel selectedChannel = null;

                if (printerId == null) {
                    if (printerChannels.size() == 1) {
                        selectedChannel = printerChannels.get(0);
                    } else if (printerChannels.size() == 0) {
                        System.err.println("There are not detected printers.");
                        System.exit(1);
                    } else {
                        System.err.println("There are more detected printers:");
                        int i = 1;
                        for (PrinterChannel channel : printerChannels) {
                            channel.close();
                            System.err.println("    " + i + ":" + channel.getPrinterDeviceId());
                            i++;
                        }
                        System.err.println("This tool currently doesn't support multiple printers.");
                        System.exit(1);
                    }
                } else {
                    for (PrinterChannel printerChannel : printerChannels) {
                        if (printerId.equalsIgnoreCase(printerChannel.getPrinterDeviceId())
                                || printerId.equalsIgnoreCase(printerChannel.getPrinterPath())) {
                            selectedChannel = printerChannel;
                        } else {
                            printerChannel.close();
                        }
                    }
                    if (selectedChannel == null) {
                        System.err.println("No printer with id or path '" + printerId + "' detected. Try list command.");
                        System.exit(1);
                    }
                }

                ResponseFactory.DEBUG = debugFlag;

                try {
                    if ("status".equals(command)) {
                        PrintStatusCommand.execute(selectedChannel, furtherArgs);
                    } else if ("pause".equals(command)) {
                        PauseCommand.execute(selectedChannel, furtherArgs);
                    } else if ("resume".equals(command)) {
                        ResumeCommand.execute(selectedChannel, furtherArgs);
                    }
                } finally {
                    selectedChannel.close();
                }
            }
        }
    }

    public static void printHelp() {
        System.out.println("Usage: ...");
    }
}
