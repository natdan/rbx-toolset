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
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.ConsoleHandler;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import org.ah.robox.comms.AggregatedPrinterDiscovery;
import org.ah.robox.comms.BasePrinterDiscovery;
import org.ah.robox.comms.JSSCPrinterDiscovery;
import org.ah.robox.comms.LocalProxyPrinterDiscovery;
import org.ah.robox.comms.Printer;
import org.ah.robox.comms.PrinterDiscovery;
import org.ah.robox.comms.PriorityPrinterDiscovery;
import org.ah.robox.comms.RemotePrinterDiscovery;
import org.ah.robox.comms.RxTxPrinterDiscovery;
import org.ah.robox.comms.response.StandardResponse;
import org.ah.robox.proxy.ProxyCommand;

/**
 *
 *
 * @author Daniel Sendula
 */
public class Main {

    public static Level logLevel = Level.INFO;
    public static boolean remoteFlag = false;
    public static boolean detachedFlag = false;
    public static String[] originalArgs;

    public static Logger logger;

    public static void main(String[] args) throws Exception {

        originalArgs = args;

        Formatter formatter = new LocalFormatter();

        Handler consoleHandler = new ConsoleHandler() {
            { setOutputStream(System.out); }
        };
        consoleHandler.setFormatter(formatter);
        consoleHandler.setLevel(Level.FINEST);

        Logger rootLogger = LogManager.getLogManager().getLogger("");

        for (Handler h : rootLogger.getHandlers()) {
            rootLogger.removeHandler(h);
        }
        rootLogger.addHandler(consoleHandler);

        for (String loggerName : new String[] { "javax", "java", "sun" }) {
            Logger logger = Logger.getLogger(loggerName);
            logger.setLevel(Level.INFO);
        }
        Logger.getAnonymousLogger().addHandler(consoleHandler);

        logger = Logger.getLogger("NetworkSerialPort");

        String command = null;

        boolean furtherArgsFlag = false;
        boolean printerFlag = false;
        boolean jsscFlag = true;
        boolean rxtxFlag = false;
        String printerId = null;

        List<String> furtherArgs = new ArrayList<String>();
        for (String arg : args) {
            if (furtherArgsFlag) {
                furtherArgs.add(arg);
            } else if (printerFlag) {
                printerId = arg;
                printerFlag = false;
            } else if ("---detached".equals(arg)) {
                detachedFlag = true;
            } else if ("-j".equals(arg) || "--jssc".equals(arg)) {
                jsscFlag = true;
                rxtxFlag = true;
            } else if ("-t".equals(arg) || "--rxtx".equals(arg)) {
                jsscFlag = false;
                rxtxFlag = true;
            } else if ("-p".equals(arg) || "--printer".equals(arg)) {
                printerFlag = true;
            } else if ("-v".equals(arg) || "--verbose".equals(arg)) {
                logLevel = Level.FINE;
            } else if ("-vv".equals(arg)) {
                logLevel = Level.FINER;
            } else if ("-vvv".equals(arg)) {
                logLevel = Level.FINEST;
            } else if ("-r".equals(arg) || "--remote".equals(arg)) {
                remoteFlag = true;
            } else if ("-?".equals(arg) || "-h".equals(arg) || "--help".equals(arg)) {
                command = "help";
            } else if ("list".equals(arg)) {
                command = "list";
                furtherArgsFlag = true;
            } else if ("install".equals(arg)) {
                command = "install";
                furtherArgsFlag = true;
            } else if ("proxy".equals(arg)) {
                command = "proxy";
                furtherArgsFlag = true;
            } else if ("status".equals(arg)) {
                command = "status";
                furtherArgsFlag = true;
            } else if ("readreel".equals(arg)) {
                command = "readreel";
                furtherArgsFlag = true;
            } else if ("readhead".equals(arg)) {
                command = "readhead";
                furtherArgsFlag = true;
            } else if ("calibrate".equals(arg)) {
                command = "calibrate";
                furtherArgsFlag = true;
            } else if ("monitor".equals(arg)) {
                command = "monitor";
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
                logger.warning("Unknown option: '" + arg + "'");
                printHelp();
                System.exit(1);
            }
        }
        rootLogger.setLevel(logLevel);
        logger.setLevel(logLevel);

        if ("help".equals(command) || command == null) {
            printHelp();
            System.exit(0);
        } else {
            BasePrinterDiscovery localDiscovery;
            if (jsscFlag) {
                localDiscovery = new JSSCPrinterDiscovery();
            } else if (rxtxFlag) {
                localDiscovery = new RxTxPrinterDiscovery();
            } else {
                localDiscovery = new JSSCPrinterDiscovery();
            }

            PrinterDiscovery discovery;
            if (remoteFlag) {
                BasePrinterDiscovery remoteDiscovery = new RemotePrinterDiscovery();

                discovery = new AggregatedPrinterDiscovery(localDiscovery, remoteDiscovery);
            } else {
                LocalProxyPrinterDiscovery localProxyDiscovery = new LocalProxyPrinterDiscovery();
                discovery = new PriorityPrinterDiscovery(localProxyDiscovery, localDiscovery);
            }

            List<Printer> printers = null;
            if (!"upload".equals(command)
                    && !"install".equals(command)
                    && !"proxy".equals(command)
                    && !"web".equals(command)) {
                printers = discovery.findAllPrinters();
            }

            if ("list".equals(command)) {
                ListCommand.execute(printers);
            } else if ("install".equals(command)) {
                InstallCommand.execute(furtherArgs);
            } else if ("proxy".equals(command)) {
                ProxyCommand.execute(localDiscovery, furtherArgs);
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
                        logger.severe("There are no detected printers.");
                        System.exit(1);
                    } else if (!helpInvocation) {
                        logger.severe("There are more detected printers:");
                        int i = 1;
                        for (Printer printer : printers) {
                            printer.close();
                            logger.severe("    " + i + ":" + printer.getPrinterName());
                            i++;
                        }
                        logger.severe("This tool currently doesn't support multiple printers.");
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
                        logger.severe("No printer with id or path '" + printerId + "' detected. Try list command.");
                        System.exit(1);
                    }
                }

                try {
                    if ("status".equals(command)) {
                        PrintStatusCommand.execute(selectedPrinter, furtherArgs);
                    } else if ("readreel".equals(command)) {
                        ReadReelCommand.execute(selectedPrinter, furtherArgs);
                    } else if ("readhead".equals(command)) {
                        ReadHeadCommand.execute(selectedPrinter, furtherArgs);
                    } else if ("calibrate".equals(command)) {
                        CalibrateHeadCommand.execute(selectedPrinter, furtherArgs);
                    } else if ("monitor".equals(command)) {
                        MonitorCommand.execute(selectedPrinter, furtherArgs);
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
                } catch (Exception e) {
                    logger.info("Error: " + e.getMessage());
                    logger.log(Level.FINE, "Got exception", e);
                } finally {
                    if (selectedPrinter != null) {
                        selectedPrinter.close();
                    }
                }
            }
        }
    }

    public static void printGeneralOptions() {
        logger.info("  General options are one of these:");
        logger.info("  -v | --verbose   - increases voutput erbosity level");
        logger.info("  -vv              - increases debug level");
        logger.info("  -vvv             - maximm debug output level");
        logger.info("  -r | --remote    - include remote printers in discovery");
        logger.info("  -p | --printer   - if more than one printer is connected to your");
        logger.info("                     computer you must select which one command is");
        logger.info("                     going to be applied on. You can get list of");
        logger.info("                     available printers using 'list' command");
    }

    public static void printSpecificOptions() {
        logger.info("  Specific options are:");
        logger.info("");
        logger.info("  -h | --help | -?     - this page");
    }

    public static void printHelp() {
        logger.info("Usage: rbx [<general-options>] <command> [<specific-options>]");
        logger.info("");
        logger.info("  General options are one of these:");
        logger.info("  -h | --help | -? - this page");
        printGeneralOptions();
        logger.info("");
        logger.info("  Supported commands are:");
        logger.info("");
        logger.info("  install   - installs in local (linux) system");
        logger.info("  list      - lists attached printers");
        logger.info("  status    - displays printer's status");
        logger.info("  monitor   - starts GUI to display current status and progress");
        logger.info("  pause     - pauses current print if there's one");
        logger.info("  resume    - resumes current print if there's one");
        logger.info("  abort     - aborts current print if there's one");
        logger.info("  upload    - sets print file for status command");
        logger.info("  gcode     - sends gcode command to the printer");
        logger.info("  send      - creates a print job and send gcode (file) it to the printer");
        logger.info("  start     - starts print job that is already in the printer");
        logger.info("  jobs      - lists jobs stored on ther printer");
        logger.info("  readreel  - reads reel eeprom");
        logger.info("  readhead  - reads head eeprom");
        logger.info("  calibrate - calibrates - writes head eeprom");
        logger.info("  proxy     - sets up serial proxy for the printer");
        logger.info("");
        logger.info("  Tip: further help can be obtained if '-h'/'-?'/'--help; is specified");
        logger.info("  after commmand. Example: ");
        logger.info("");
        logger.info("  rbx status --help");
    }

    /**
     * @param printer TODO
     * @param response
     * @throws IOException
     */
    public static boolean processStandardResponse(Printer printer, StandardResponse response) throws IOException {
        String hrs = StandardResponseHelper.processStandardResponse(printer, response);
        if (hrs != null) {
            logger.warning(hrs);
        }
        return hrs == null;
    }

    private static class LocalFormatter extends Formatter {
        private final Date dat = new Date();

        @Override
        public synchronized String format(LogRecord record) {
            dat.setTime(record.getMillis());
            String source;
            if (record.getSourceClassName() != null) {
                source = record.getSourceClassName();
                if (record.getSourceMethodName() != null) {
                   source += " " + record.getSourceMethodName();
                }
            } else {
                source = record.getLoggerName();
            }
            String message = formatMessage(record);
            String throwable = "";
            if (record.getThrown() != null) {
                StringWriter sw = new StringWriter();
                PrintWriter pw = new PrintWriter(sw);
                pw.println();
                record.getThrown().printStackTrace(pw);
                pw.close();
                throwable = sw.toString();
            }

            if (record.getLevel().intValue() < Level.FINE.intValue()) {
                return String.format("%1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS %4$6s %2$s %5$s%6$s%n",
                        dat,
                        source,
                        record.getLoggerName(),
                        record.getLevel(),
                        message,
                        throwable);
            } else {
                return String.format("%5$s%6$s%n",
                        dat,
                        source,
                        record.getLoggerName(),
                        record.getLevel(),
                        message,
                        throwable);
            }
        }
    }
}
