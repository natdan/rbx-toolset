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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.ah.robox.comms.Printer;
import org.ah.robox.comms.SerialPortsPrinterDiscovery;
import org.ah.robox.comms.response.ResponseFactory;
import org.ah.robox.comms.response.StandardResponse;

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
            } else if ("-p".equals(arg) || "--printer".equals(arg)) {
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
            } else if ("upload".equals(arg)) {
                command = "upload";
                furtherArgsFlag = true;
            } else if ("pause".equals(arg)) {
                command = "pause";
                furtherArgsFlag = true;
            } else if ("resume".equals(arg)) {
                command = "resume";
                furtherArgsFlag = true;
            } else if ("abort".equals(arg)) {
                command = "abort";
                furtherArgsFlag = true;
            } else if ("web".equals(arg)) {
                command = "web";
                furtherArgsFlag = true;
            } else if ("gcode".equals(arg)) {
                command = "gcode";
                furtherArgsFlag = true;
            } else if ("send".equals(arg)) {
                command = "send";
                furtherArgsFlag = true;
            } else if ("jobs".equals(arg)) {
                command = "jobs";
                furtherArgsFlag = true;
            } else if ("start".equals(arg)) {
                command = "start";
                furtherArgsFlag = true;
            } else {
                System.err.println("Unknown option: '" + arg + "'");
                printHelp();
                System.exit(1);
            }
        }

        ResponseFactory.DEBUG = debugFlag;

        if ("help".equals(command) || command == null) {
            printHelp();
            System.exit(0);
        } else {
            SerialPortsPrinterDiscovery discovery = new SerialPortsPrinterDiscovery();
            discovery.setVerbose(verboseFlag);
            discovery.setDebug(debugFlag);

            List<Printer> printers = null;
            if (!"upload".equals(command) && !"web".equals(command)) {
                printers = discovery.findAllPrinters();
            }

            if ("list".equals(command)) {
                ListCommand.execute(printers);
            } else if ("upload".equals(command)) {
                UploadCommand.execute(furtherArgs);
            } else if ("web".equals(command)) {
                WebCommand.execute(discovery, printerId, furtherArgs);
                //UploadCommand.execute(furtherArgs);
            } else {
                Printer selectedPrinter = null;
                boolean helpInvocation = furtherArgs.contains("-h") || furtherArgs.contains("--help") || furtherArgs.contains("-?");

                if (printerId == null) {
                    if (printers.size() == 1) {
                        selectedPrinter = printers.get(0);
                    } else if (printers.size() == 0 && !helpInvocation) {
                        System.err.println("There are not detected printers.");
                        System.exit(1);
                    } else if (!helpInvocation) {
                        System.err.println("There are more detected printers:");
                        int i = 1;
                        for (Printer printer : printers) {
                            printer.close();
                            System.err.println("    " + i + ":" + printer.getPrinterName());
                            i++;
                        }
                        System.err.println("This tool currently doesn't support multiple printers.");
                        System.exit(1);
                    }
                } else {
                    for (Printer printer : printers) {
                        if (printerId.equalsIgnoreCase(printer.getPrinterId())
                                || printerId.equalsIgnoreCase(printer.getPrinterChannel().getPrinterPath())
                                || printerId.equalsIgnoreCase(printer.getPrinterName())) {
                            selectedPrinter = printer;
                        } else {
                            printer.close();
                        }
                    }
                    if (selectedPrinter == null) {
                        System.err.println("No printer with id or path '" + printerId + "' detected. Try list command.");
                        System.exit(1);
                    }
                }

                try {
                    if ("status".equals(command)) {
                        PrintStatusCommand.execute(selectedPrinter, furtherArgs);
                    } else if ("pause".equals(command)) {
                        PausePrinterCommand.execute(selectedPrinter, furtherArgs);
                    } else if ("resume".equals(command)) {
                        ResumePrinterCommand.execute(selectedPrinter, furtherArgs);
                    } else if ("abort".equals(command)) {
                        AbortPrintCommand.execute(selectedPrinter, furtherArgs);
                    } else if ("gcode".equals(command)) {
                        GCodeCommand.execute(selectedPrinter, furtherArgs);
                    } else if ("send".equals(command)) {
                        SendPrintJobCommand.execute(selectedPrinter, furtherArgs);
                    } else if ("jobs".equals(command)) {
                        GetPrintJobsCommand.execute(selectedPrinter, furtherArgs);
                    } else if ("start".equals(command)) {
                        StartPrintJobCommand.execute(selectedPrinter, furtherArgs);
                    }
                } finally {
                    if (selectedPrinter != null) {
                        selectedPrinter.close();
                    }
                }
            }
        }
    }

    public static void printGeneralOptions() {
        System.out.println("  General options are one of these:");
        System.out.println("  -v | --verbose   - increases voutput erbosity level");
        System.out.println("  -d | --debug     - increases debug level");
        System.out.println("  -p | --printer   - if more than one printer is connected to your");
        System.out.println("                     computer you must select which one command is");
        System.out.println("                     going to be applied on. You can get list of");
        System.out.println("                     available printers using 'list' command");
    }

    public static void printSpecificOptions() {
        System.out.println("  Specific options are:");
        System.out.println("");
        System.out.println("  -h | --help | -?     - this page");
    }

    public static void printHelp() {
        System.out.println("Usage: rbx [<general-options>] <command> [<specific-options>]");
        System.out.println("");
        System.out.println("  General options are one of these:");
        System.out.println("  -h | --help | -? - this page");
        printGeneralOptions();
        System.out.println("");
        System.out.println("  Supported commands are:");
        System.out.println("");
        System.out.println("  list     - lists attached printers");
        System.out.println("  status   - displays printer's status");
        System.out.println("  pause    - pauses current print if there's one");
        System.out.println("  resume   - resumes current print if there's one");
        System.out.println("  abort    - aborts current print if there's one");
        System.out.println("  upload   - sets print file for status command");
        System.out.println("  gcode    - sends gcode command to the printer");
        System.out.println("  send     - creates a print job and send gcode (file) it to the printer");
        System.out.println("  start    - starts print job that is already in the printer");
        System.out.println("  jobs     - lists jobs stored on ther printer");
        System.out.println("");
        System.out.println("  Tip: further help can be obtained if '-h'/'-?'/'--help; is specified");
        System.out.println("  after commmand. Example: ");
        System.out.println("");
        System.out.println("  rbx status --help");
    }

    /**
     * @param printer TODO
     * @param response
     * @throws IOException
     */
    public static void processStandardResponse(Printer printer, StandardResponse response) throws IOException {
        if (response.isBufferOverFlow()) {
            System.err.println("ERR: Buffer overflow.");
        } else if (response.isTooLongLine()) {
            System.err.println("ERR: Line too long.");
        } else if (response.isUnknownCommand()) {
            System.err.println("ERR: Unknown command.");
        } else if (response.isSequenceError()) {
            System.err.println("ERR: Sequence error.");
        } else if (response.isFileTooLargeError()) {
            System.err.println("ERR: File too large error.");
        } else if (response.isError1()) {
            System.err.println("ERR: error code 1.");
        } else if (response.isError5()) {
            System.err.println("ERR: error code 5.");
        } else if (response.isError6()) {
            System.err.println("ERR: error code 6.");
        } else if (response.isError8()) {
            System.err.println("ERR: error code 8.");
        } else if (response.isError9()) {
            System.err.println("ERR: error code 9.");
        } else if (response.isError10()) {
            System.err.println("ERR: error code 10.");
        } else if (response.isError12()) {
            System.err.println("ERR: error code 12.");
        } else if (response.isError13()) {
            System.err.println("ERR: error code 13.");
        } else if (response.isError14()) {
            System.err.println("ERR: error code 14.");
        } else if (response.isError15()) {
            System.err.println("ERR: error code 15.");
        } else if (response.isError16()) {
            System.err.println("ERR: error code 16.");
        } else if (response.isError17()) {
            System.err.println("ERR: error code 17.");
        } else if (response.isError18()) {
            System.err.println("ERR: error code 18.");
        } else if (response.isError19()) {
            System.err.println("ERR: error code 19.");
        } else if (response.isError20()) {
            System.err.println("ERR: error code 20.");
        }
        if (response.isError()) {
            StandardResponse standardResponse = printer.resetErrors();
            if (standardResponse.isError()) {
                System.err.println("*** Cannot clear error!");
            }
        }
    }
}
