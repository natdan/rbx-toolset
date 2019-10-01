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
import java.io.FileReader;
import java.io.FileWriter;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.List;
import java.util.logging.Logger;

import org.ah.robox.comms.PrinterDiscovery;
import org.ah.robox.web.WebServer;

/**
 *
 *
 * @author Daniel Sendula
 */
public class WebCommand {
    private static final Logger logger = Logger.getLogger(WebCommand.class.getName());

    public static void execute(PrinterDiscovery printerDiscovery, String printerId, List<String> args) throws Exception {

        boolean portFlag = false;
        boolean imageCommandFlag = false;
        boolean refreshIntervalFlag = false;
        boolean imageRefreshIntervalFlag = false;
        boolean postRefreshCommandFlag = false;
        boolean staticDirFlag = false;
        boolean templateFileFlag = false;
        boolean refreshCommandFormatFlag = false;
        boolean automaticRefrehsFlag = false;
        boolean authenticationFlag = false;
        boolean startFlag = false;
        boolean stopFlag = false;
        boolean statusFlag = false;

        WebServer webServer = new WebServer(printerDiscovery);
        String templateFileName = null;

        boolean raspberryPi = System.getProperty("os.name").contains("Linux") || System.getProperty("os.arch").equalsIgnoreCase("arm");
        if (raspberryPi) {
            if (new File("/usr/bin/raspistill").exists()) {
                webServer.setImageCommand("raspistill -e jpg -w 800 -h 600 -o -");
            }
        }

        for (String a : args) {
            if (portFlag) {
                try {
                    webServer.setPort(Integer.parseInt(a));
                } catch (NumberFormatException e) {
                    logger.severe("Bad port number '" + a + "'");
                    System.exit(1);
                }
                portFlag = false;
            } else if (imageCommandFlag) {
                if ("".equals(a)) {
                    webServer.setImageCommand(null);
                } else {
                    webServer.setImageCommand(a);
                }
                imageCommandFlag = false;
            } else if (refreshIntervalFlag) {
                try {
                    webServer.setRefreshInterval(Integer.parseInt(a));
                } catch (NumberFormatException e) {
                    logger.severe("Bad number for status refresh interval '" + a + "'");
                    System.exit(1);
                }
                refreshIntervalFlag = false;
            } else if (imageRefreshIntervalFlag) {
                try {
                    webServer.setImageRefreshInterval(Integer.parseInt(a));
                } catch (NumberFormatException e) {
                    logger.severe("Bad number for image refresh interval '" + a + "'");
                    System.exit(1);
                }
                imageRefreshIntervalFlag = false;
            } else if (postRefreshCommandFlag) {
                if ("".equals(a)) {
                    webServer.setPostRefreshCommand(null);
                } else {
                    webServer.setPostRefreshCommand(a);
                }
                postRefreshCommandFlag = false;
            } else if (refreshCommandFormatFlag) {
                webServer.setRefreshCommandFormat(a);
                refreshCommandFormatFlag = false;
            } else if (templateFileFlag) {
                templateFileName = a;
                templateFileFlag = false;
            } else if (staticDirFlag) {
                webServer.setStaticDir(a);
                staticDirFlag = false;
            } else if (automaticRefrehsFlag) {
                try {
                    webServer.setAutomaticRefresh(Integer.parseInt(a));
                    automaticRefrehsFlag = false;
                } catch (NumberFormatException e) {
                    logger.severe("Bad number for automatic refresh '" + a + "'");
                    System.exit(1);
                }
            } else if (authenticationFlag) {
                int i = a.indexOf(':');
                if (i < 0) {
                    webServer.setUsername("");
                    webServer.setPassword(a);
                } else {
                    webServer.setUsername(a.substring(0, i));
                    webServer.setPassword(a.substring(i + 1));
                }
                authenticationFlag = false;
            } else if ("-p".equals(a) || "--port".equals(a)) {
                portFlag = true;
            } else if ("-rs".equals(a) || "--refresh-status-interval".equals(a)) {
                refreshIntervalFlag = true;
            } else if ("-ri".equals(a) || "--refresh-image-interval".equals(a)) {
                imageRefreshIntervalFlag = true;
            } else if ("-ic".equals(a) || "--image-command".equals(a)) {
                imageCommandFlag = true;
            } else if ("-pc".equals(a) || "--post-refresh-command".equals(a)) {
                postRefreshCommandFlag = true;
            } else if ("-cf".equals(a) || "--post-refresh-comamnd-format".equals(a)) {
                refreshCommandFormatFlag = true;
            } else if ("-ac".equals(a) || "--allow-commands".equals(a)) {
                webServer.setAllowCommandsFlag(true);
            } else if ("-t".equals(a) || "--template-file".equals(a)) {
                templateFileFlag = true;
            } else if ("-sf".equals(a) || "--static-files".equals(a)) {
                staticDirFlag = true;
            } else if ("-ar".equals(a) || "--automatic-refresh".equals(a)) {
                automaticRefrehsFlag = true;
            } else if ("-ah".equals(a) || "--authentication".equals(a)) {
                authenticationFlag = true;
            } else if ("start".equals(a)) {
                if (statusFlag || stopFlag) {
                    logger.severe("start/stop/status are mutually exclusive commands. Supply only one at the time.");
                    System.exit(1);
                }
                startFlag = true;
            } else if ("stop".equals(a)) {
                if (statusFlag || startFlag) {
                    logger.severe("start/stop/status are mutually exclusive commands. Supply only one at the time.");
                    System.exit(1);
                }
                stopFlag = true;
            } else if ("status".equals(a)) {
                if (startFlag || stopFlag) {
                    logger.severe("start/stop/status are mutually exclusive commands. Supply only one at the time.");
                    System.exit(1);
                }
                statusFlag = true;
            } else if ("-h".equals(a) || "--help".equals(a) || "-?".equals(a)) {
                printHelp();
                System.exit(0);
            } else {
                logger.severe("Unknown option: '" + a + "'");
                printHelp();
                System.exit(1);
            }
        }
        if (postRefreshCommandFlag) {
            logger.severe("Missing post refresh command.");
            System.exit(1);
        } else if (imageCommandFlag) {
            logger.severe("Missing image command inverval.");
            System.exit(1);
        } else if (imageRefreshIntervalFlag) {
            logger.severe("Missing image refresh inverval.");
            System.exit(1);
        } else if (refreshIntervalFlag) {
            logger.severe("Missing refresh interval.");
            System.exit(1);
        } else if (portFlag) {
            logger.severe("Missing port.");
            System.exit(1);
        } else if (refreshCommandFormatFlag) {
            logger.severe("Missing refresh command format.");
            System.exit(1);
        } else if (templateFileFlag) {
            logger.severe("Missing template file format.");
            System.exit(1);
        } else if (staticDirFlag) {
            logger.severe("Missing static directory.");
            System.exit(1);
        } else if (automaticRefrehsFlag) {
            logger.severe("Missing automatic refresh value.");
            System.exit(1);
        }

        if (templateFileName != null) {
            webServer.setTemplateFile(new File(templateFileName));
            if (!webServer.getTemplateFile().exists()) {
                logger.severe("Template file does not exist '" + templateFileName + "'.");
                System.exit(1);
            }
        }

        // Printer printer = new RoboxPrinter(selectedChannel);

        boolean error = false;
        if (statusFlag) {
            doStatus();
        } else if (stopFlag) {
            doStopWebServer();
        } else if (startFlag) {
            doStartWebServerInSeparateProcess();
        } else {
            ServerSocket serverSocket = new ServerSocket(0);
            try {
                int port = serverSocket.getLocalPort();
                serverSocket.setSoTimeout(500);

                webServer.setPreferredPrinterId(printerId);
                webServer.init();
                webServer.start();

                File portFile = serverPortFile();
                if (portFile.exists()) {
                    if (!portFile.delete()) {
                        logger.severe("Cannot delete lock file " + portFile.getAbsolutePath());
                        System.exit(-1);
                    }
                }

                try {
                    FileWriter fw = new FileWriter(portFile);
                    try {
                        fw.write(Integer.toString(port));
                    } finally {
                        fw.close();
                    }

                    logger.info("Started web server at " + webServer.getAddress().getHostName() + ":" + webServer.getPort());
                    boolean stop = false;
                    while (!stop) {
                        try {
                            Socket socket = serverSocket.accept();
                            try {
                                byte[] buf = new byte[4];
                                int r = socket.getInputStream().read(buf);
                                if (r == 4) {
                                    if ("PING".equals(new String(buf, "US-ASCII"))) {
                                        socket.getOutputStream().write("PONG\n".getBytes("US-ASCII"));
                                    } else if ("STOP".equals(new String(buf, "US-ASCII"))) {
                                        stop = true;
                                        webServer.stopAndWaitForStopped();
                                        socket.getOutputStream().write("OK\n".getBytes("US-ASCII"));
                                    }
                                }
                            } finally {
                                socket.close();
                            }
                        } catch (SocketTimeoutException ignore) {
                        } catch (Exception ignore) {
                            error = true;
                        }
                    }
                } catch (Exception e) {
                    logger.severe("Cannot create lock file " + portFile.getAbsolutePath());
                    error = true;
                }
            } finally {
                serverSocket.close();
            }
        }

        if (error) {
            System.exit(-1);
        } else {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ignore) { }
            System.exit(0);
        }
    }

    private static File serverPortFile() {
        File configDir = new File(new File(System.getProperty("user.home")), ".robox");
        File serverPort = new File(configDir, ".port");
        return serverPort;
    }

    private static int getPortNumber() {
        int port = -1;
        File serverPort = serverPortFile();
        if (serverPort.exists()) {
            try {
                char[] portNumberChars = new char[(int)serverPort.length()];
                FileReader fr = new FileReader(serverPort);
                try {
                    fr.read(portNumberChars);
                } finally {
                    fr.close();
                }
                try {
                    port = Integer.parseInt(new String(portNumberChars));
                } catch (NumberFormatException e) { }
            } catch (Throwable ignore) { }
        }
        return port;
    }

    /**
     *
     */
    private static void doStatus() {
        boolean error = true;
        int port = getPortNumber();
        if (port > 0) {
            try {
                Socket socket = new Socket("127.0.0.1", port);
                try {
                    socket.setSoTimeout(5000);
                    try {
                        socket.getOutputStream().write("PING".getBytes("US-ASCII"));
                        socket.getOutputStream().flush();
                        try {
                            byte[] response = new byte[5];
                            int r = socket.getInputStream().read(response);
                            if (r == 5 && "PONG\n".equals(new String(response, "US-ASCII"))) {
                                logger.info("Server is working.");
                                error = false;
                            } else {
                                logger.severe("Server appears to be working but got wrong response.");
                            }
                        } catch (Exception e) {
                            logger.severe("Server appears to be working but doesn't respond to stop requests.");
                        }
                    } catch (Exception e) {
                        logger.severe("Server appears to be working but cannot contact it.");
                    }
                } finally {
                    socket.close();
                }
            } catch (Exception e) {
                logger.severe("Server seems not to be working. Deleting stale port lock file.");
                File f = serverPortFile();
                if (f.exists()) {
                    f.delete();
                }
            }
        } else {
            logger.severe("Server is not working.");
        }
        if (error) {
            System.exit(-1);
        }
    }

    /**
     *
     */
    private static void doStartWebServerInSeparateProcess() {
        RuntimeMXBean runtimeMxBean = ManagementFactory.getRuntimeMXBean();
        logger.info("ARGS: " + runtimeMxBean.getInputArguments());
        logger.info("Command: " + System.getProperty("sun.java.command"));
        logger.info("x: " + ManagementFactory.getRuntimeMXBean().getName());

        logger.severe("This function is still not implemented.");
        System.exit(-1);
    }

    /**
     *
     */
    private static void doStopWebServer() {
        boolean error = true;
        int port = getPortNumber();
        if (port > 0) {
            try {
                Socket socket = new Socket("127.0.0.1", port);
                try {
                    socket.setSoTimeout(5000);
                    try {
                        socket.getOutputStream().write("STOP".getBytes("US-ASCII"));
                        socket.getOutputStream().flush();
                        try {
                            byte[] response = new byte[3];
                            int r = socket.getInputStream().read(response);
                            if (r == 3 && "OK\n".equals(new String(response, "US-ASCII"))) {
                                logger.info("Server confirmed it stopped.");
                                error = false;
                            } else {
                                logger.severe("Server appears to be working but got wrong response.");
                            }
                        } catch (Exception e) {
                            logger.severe("Server appears to be working but doesn't respond to stop requests.");
                        }
                    } catch (Exception e) {
                        logger.severe("Server appears to be working but cannot contact it.");
                    }
                } finally {
                    socket.close();
                }
            } catch (Exception e) {
                logger.severe("Server seems not to be working");
                File f = serverPortFile();
                if (f.exists()) {
                    f.delete();
                }
            }
        } else {
            logger.severe("Cannot find port lock file. Server appears not to be working.");
        }
        if (error) {
            System.exit(-1);
        }
    }

    public static void printHelp() {
        logger.info("Usage: rbx [<general-options>] web [<specific-options>] [<command>]");
        logger.info("");
        logger.info("  General options are one of these:");
        logger.info("  -v | --verbose   - increases voutput erbosity level");
        logger.info("  -d | --debug     - increases debug level");
        logger.info("");
        Main.printSpecificOptions();
        logger.info("");
        logger.info("  -h | --help | -?     - this page");
        logger.info("  -a | --all           - displays all status information");
        logger.info("  -s | --short         - displays values only");
        logger.info("                         It is machine readable format.");

        logger.info("  -p | --port                     - port to start web server.");
        logger.info("  -rs | --refresh-status-interval <value-in-seconds>");
        logger.info("        Refresh status interval in seconds. It is how often printer is going");
        logger.info("        to be queried for the status. Default is 15 seconds.");
        logger.info("  -ri | --refresh-image-interval <value-in-seconds>");
        logger.info("        Refresh image interval in seconds. It is how image is fetched is going");
        logger.info("        to be queried for the status. Also, if on RPi and raspistill is detected");
        logger.info("        it will automatically be used. Default is 5 seconds");
        logger.info("  -ic | --image-command <shell-command>");
        logger.info("        Imaage command. This is shell command to be used to fetch image.");
        logger.info("        Command should send image data in jpg format to stdout.");
        logger.info("  -pc | --post-refresh-command <shell-command>");
        logger.info("        Comamnd to be called after printer status was fetched.");
        logger.info("        It will be called with estimage format string as first parameter.");
        logger.info("  -cf | --post-refresh-command-format <format-string>");
        logger.info("        Format post refresh command is going to get estimate in.");
        logger.info("        Placeholders are %c - command, %h -hours, %m - minutes (in.");
        logger.info("        two digit format), %s - seconds (in two digit format).");
        logger.info("        Default format is %c: %h:%m");
        logger.info("  -ac | --allow-commands ");
        logger.info("        If set web pages will allow commaindg printer:");
        logger.info("        sending pause, resume and abort commands.");
        logger.info("  -t  | --template-file <file>");
        logger.info("        Template html file for status file. See");
        logger.info("        -sf/--static-files switch for extra resources like css/images...");
        logger.info("  -sf | --static-files <directory>");
        logger.info("        Directory where static files are stored.");
        logger.info("        They are going to be served from root of web app ('/').");
        logger.info("  -ar | --automatic-refresh <value-in-seconds>");
        logger.info("        Enables internal template to create html page refresh. External templates");
        logger.info("        can utilise it by adding ${automatic-refresh} placeholder in html head part.");
        logger.info("  -ah | --authentication <user:password>");
        logger.info("        Enables basic HTTP authentication. Username and password specified");
        logger.info("        are to be separated by ':'. If no ':' specified then username is left empty.");

        logger.info("");
        logger.info("Commands are optional. If none specified then web server will start in current process.");
        logger.info("");
        logger.info("  start  - starts the server in background. Not implemented yet.");
        logger.info("  status - displays status of the server.");
        logger.info("  stop   - stops the server.");
        logger.info("");
        logger.info("");
        logger.info("Template file should have following placeholders:");
        logger.info("");
        logger.info("${status}     - printing status (\"Unknown\", \"Working\", \"Pausing\", \"Paused\", \"Resuming\")");
        logger.info("${busy}       - is printer busy or not (\"true\", \"false\"). Can be used directly in javascript.");
        logger.info("${job_id}     - printer job id.");
        logger.info("${printer_id} - currently selected printer id. Used in building URLs.");
        logger.info("${error_msg}      - previous request error message");
        logger.info("${estimate}       - estimate time in %h:%m:%s format");
        logger.info("${estimate_hours} - estimate time hours");
        logger.info("${estimate_mins}  - estimate time minutes (with leading zero)");
        logger.info("${estimate_secs}  - estimate time seconds (with leading zero)");
        logger.info("${current_line}   - current line");
        logger.info("${total_lines}    - total lines");
        logger.info("${current_line_and_total_line} - current line followed, optionally, with '/' and total lines");
        logger.info("${all_printers_link}           - link (or empty) to url with list of all printers.");
        logger.info("                                 It is set to empty if one or no printers available.");
        logger.info("${capture_image_tag}           - capture image tag. It is set to <img src=\"/capture.jpg\"/>");
        logger.info("                                 when capture is enabled or empty string if not.");
        logger.info("${capture_image_css_display}   - css for display attribute for capture image section.");
        logger.info("                                 It is set to inline when capture is enabled or none if not.");
        logger.info("${commands_css_display}        - css for display attribute for commands section.");
        logger.info("                                 It is set to inline when commands are enabled or none if not.");
        logger.info("${printers_list}               - applicable only to printers page - list of <li> tags");
        logger.info("                                 with links to known printers. Not connected printers will");
        logger.info("                                 have no links associanted.");
        logger.info("${automatic-refresh}           - tag for html head. It will be empty if -ar|--automatic-refresh");
        logger.info("                                 option is not added. ");

        logger.info("${x_limit}       - x limit switch (on/off)");
        logger.info("${y_limit}       - y limit switch (on/off)");
        logger.info("${z_limit}       - z limit switch (on/off)");

        logger.info("${filament_1}    - filament 1 switch (on/off)");
        logger.info("${filament_2}    - filament 2 switch (on/off)");

        logger.info("${nozzle_switch} - nozzle switch (on/off)");
        logger.info("${door_open}   - door closed switch (on/off)");
        logger.info("${reel_button}   - reel button switch (on/off)");

        logger.info("${nozzle_temp}           - nozzle temperature");
        logger.info("${nozzle_set_temp}       - nozzle set temperature");
        logger.info("${nozzle_temp_combined}  - nozzle + nozzle set temperature divided by '/'");

        logger.info("${bed_temp}              - bed temperature");
        logger.info("${bed_set_temp}          - bed set temperature");
        logger.info("${nozzle_temp_combined}  - bed + bed set temperature divided by '/'");

        logger.info("${ambient_temp}          - ambient temperature");
        logger.info("${ambient_set_temp}      - ambient set temperature");
        logger.info("${ambient_temp_combined} - ambient temperature + ambient set temperature divided by '/");

        logger.info("${fan}           - fan (on/off)");
        logger.info("${head_Fan}      - head fan (on/off)");

        logger.info("${x_position}    - x position");
        logger.info("${y_position}    - y position");
        logger.info("${z_position}    - z position");

        logger.info("${filament_nultiplier}  - filament multiplier");
        logger.info("${feed_rate_nultiplier} - feed rate multiplier");

        logger.info("${temp_state}    - temperature state ('working', 'cooling', 'heating bed' or 'heating nozzles')");

        logger.info("");
    }
}
