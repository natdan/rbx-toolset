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

import org.ah.robox.comms.PrinterDiscovery;
import org.ah.robox.web.WebServer;

/**
 *
 *
 * @author Daniel Sendula
 */
public class WebCommand {

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
                    System.err.println("Bad port number '" + a + "'");
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
                    System.err.println("Bad number for status refresh interval '" + a + "'");
                    System.exit(1);
                }
                refreshIntervalFlag = false;
            } else if (imageRefreshIntervalFlag) {
                try {
                    webServer.setImageRefreshInterval(Integer.parseInt(a));
                } catch (NumberFormatException e) {
                    System.err.println("Bad number for image refresh interval '" + a + "'");
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
                    System.err.println("Bad number for automatic refresh '" + a + "'");
                    System.exit(1);
                }
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
            } else if ("start".equals(a)) {
                if (statusFlag || stopFlag) {
                    System.err.println("start/stop/status are mutually exclusive commands. Supply only one at the time.");
                    System.exit(1);
                }
                startFlag = true;
            } else if ("stop".equals(a)) {
                if (statusFlag || startFlag) {
                    System.err.println("start/stop/status are mutually exclusive commands. Supply only one at the time.");
                    System.exit(1);
                }
                stopFlag = true;
            } else if ("status".equals(a)) {
                if (startFlag || stopFlag) {
                    System.err.println("start/stop/status are mutually exclusive commands. Supply only one at the time.");
                    System.exit(1);
                }
                statusFlag = true;
            } else if ("-h".equals(a) || "--help".equals(a) || "-?".equals(a)) {
                printHelp();
                System.exit(0);
            } else {
                System.err.println("Unknown option: '" + a + "'");
                printHelp();
                System.exit(1);
            }
        }
        if (postRefreshCommandFlag) {
            System.err.println("Missing post refresh command.");
            System.exit(1);
        } else if (imageCommandFlag) {
            System.err.println("Missing image command inverval.");
            System.exit(1);
        } else if (imageRefreshIntervalFlag) {
            System.err.println("Missing image refresh inverval.");
            System.exit(1);
        } else if (refreshIntervalFlag) {
            System.err.println("Missing refresh interval.");
            System.exit(1);
        } else if (portFlag) {
            System.err.println("Missing port.");
            System.exit(1);
        } else if (refreshCommandFormatFlag) {
            System.err.println("Missing refresh command format.");
            System.exit(1);
        } else if (templateFileFlag) {
            System.err.println("Missing template file format.");
            System.exit(1);
        } else if (staticDirFlag) {
            System.err.println("Missing static directory.");
            System.exit(1);
        } else if (automaticRefrehsFlag) {
            System.err.println("Missing automatic refresh value.");
            System.exit(1);
        }

        if (templateFileName != null) {
            webServer.setTemplateFile(new File(templateFileName));
            if (!webServer.getTemplateFile().exists()) {
                System.err.println("Template file does not exist '" + templateFileName + "'.");
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
                        System.err.println("Cannot delete lock file " + portFile.getAbsolutePath());
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

                    System.out.println("Started web server at " + webServer.getAddress().getHostName() + ":" + webServer.getPort());
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
                    System.err.println("Cannot create lock file " + portFile.getAbsolutePath());
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
                                System.out.println("Server is working.");
                                error = false;
                            } else {
                                System.err.println("Server appears to be working but got wrong response.");
                            }
                        } catch (Exception e) {
                            System.err.println("Server appears to be working but doesn't respond to stop requests.");
                        }
                    } catch (Exception e) {
                        System.err.println("Server appears to be working but cannot contact it.");
                    }
                } finally {
                    socket.close();
                }
            } catch (Exception e) {
                System.err.println("Server seems not to be working. Deleting stale port lock file.");
                File f = serverPortFile();
                if (f.exists()) {
                    f.delete();
                }
            }
        } else {
            System.err.println("Server is not working.");
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
        System.out.println("ARGS: " + runtimeMxBean.getInputArguments());
        System.out.println("Command: " + System.getProperty("sun.java.command"));
        System.out.println("x: " + ManagementFactory.getRuntimeMXBean().getName());

        System.err.println("This function is still not implemented.");
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
                                System.out.println("Server confirmed it stopped.");
                                error = false;
                            } else {
                                System.err.println("Server appears to be working but got wrong response.");
                            }
                        } catch (Exception e) {
                            System.err.println("Server appears to be working but doesn't respond to stop requests.");
                        }
                    } catch (Exception e) {
                        System.err.println("Server appears to be working but cannot contact it.");
                    }
                } finally {
                    socket.close();
                }
            } catch (Exception e) {
                System.err.println("Server seems not to be working");
                File f = serverPortFile();
                if (f.exists()) {
                    f.delete();
                }
            }
        } else {
            System.err.println("Cannot find port lock file. Server appears not to be working.");
        }
        if (error) {
            System.exit(-1);
        }
    }

    public static void printHelp() {
        System.out.println("Usage: rbx [<general-options>] web [<specific-options>] [<command>]");
        System.out.println("");
        System.out.println("  General options are one of these:");
        System.out.println("  -v | --verbose   - increases voutput erbosity level");
        System.out.println("  -d | --debug     - increases debug level");
        System.out.println("");
        Main.printSpecificOptions();
        System.out.println("");
        System.out.println("  -h | --help | -?     - this page");
        System.out.println("  -a | --all           - displays all status information");
        System.out.println("  -s | --short         - displays values only");
        System.out.println("                         It is machine readable format.");

        System.out.println("  -p | --port                     - port to start web server.");
        System.out.println("  -rs | --refresh-status-interval <value-in-seconds>");
        System.out.println("        Refresh status interval in seconds. It is how often printer is going");
        System.out.println("        to be queried for the status. Default is 15 seconds.");
        System.out.println("  -ri | --refresh-image-interval <value-in-seconds>");
        System.out.println("        Refresh image interval in seconds. It is how image is fetched is going");
        System.out.println("        to be queried for the status. Also, if on RPi and raspistill is detected");
        System.out.println("        it will automatically be used. Default is 5 seconds");
        System.out.println("  -ic | --image-command <shell-command>");
        System.out.println("        Imaage command. This is shell command to be used to fetch image.");
        System.out.println("        Command should send image data in jpg format to stdout.");
        System.out.println("  -pc | --post-refresh-command <shell-command>");
        System.out.println("        Comamnd to be called after printer status was fetched.");
        System.out.println("        It will be called with estimage format string as first parameter.");
        System.out.println("  -cf | --post-refresh-command-format <format-string>");
        System.out.println("        Format post refresh command is going to get estimate in.");
        System.out.println("        Placeholders are %c - command, %h -hours, %m - minutes (in.");
        System.out.println("        two digit format), %s - seconds (in two digit format).");
        System.out.println("        Default format is %c: %h:%m");
        System.out.println("  -ac | --allow-commands ");
        System.out.println("        If set web pages will allow commaindg printer:");
        System.out.println("        sending pause, resume and abort commands.");
        System.out.println("  -t  | --template-file <file>");
        System.out.println("        Template html file for status file. See");
        System.out.println("        -sf/--static-files switch for extra resources like css/images...");
        System.out.println("  -sf | --static-files <directory>");
        System.out.println("        Directory where static files are stored.");
        System.out.println("        They are going to be served from root of web app ('/').");
        System.out.println("  -ar | --automatic-refresh <value-in-seconds>");
        System.out.println("        Enables internal template to create html page refresh. External templates");
        System.out.println("        can utilise it by adding ${automatic-refresh} placeholder in html head part.");
        System.out.println("");
        System.out.println("Commands are optional. If none specified then web server will start in current process.");
        System.out.println("");
        System.out.println("  start  - starts the server in background. Not implemented yet.");
        System.out.println("  status - displays status of the server.");
        System.out.println("  stop   - stops the server.");
        System.out.println("");
        System.out.println("");
        System.out.println("Template file should have following placeholders:");
        System.out.println("");
        System.out.println("${status}   - printing status (\"Unknown\", \"Working\", \"Pausing\", \"Paused\", \"Resuming\")");
        System.out.println("${busy}     - is printer busy or not (\"true\", \"false\"). Can be used directly in javascript.");
        System.out.println("${job_id}   - printer job id.");
        System.out.println("${error_msg}      - previous request error message");
        System.out.println("${estimate}       - estimate time in %h:%m:%s format");
        System.out.println("${estimate_hours} - estimate time hours");
        System.out.println("${estimate_mins}  - estimate time minutes (with leading zero)");
        System.out.println("${estimate_secs}  - estimate time seconds (with leading zero)");
        System.out.println("${current_line}   - current line");
        System.out.println("${total_lines}    - total lines");
        System.out.println("${current_line_and_total_line} - current line followed, optionally, with '/' and total lines");
        System.out.println("${all_printers_link}           - link (or empty) to url with list of all printers.");
        System.out.println("                                 It is set to empty if one or no printers available.");
        System.out.println("${capture_image_tag}           - capture image tag. It is set to <img src=\"/capture.jpg\"/>");
        System.out.println("                                 when capture is enabled or empty string if not.");
        System.out.println("${capture_image_css_display}   - css for display attribute for capture image section.");
        System.out.println("                                 It is set to inline when capture is enabled or none if not.");
        System.out.println("${commands_css_display}        - css for display attribute for commands section.");
        System.out.println("                                 It is set to inline when commands are enabled or none if not.");
        System.out.println("${printers_list}               - applicable only to printers page - list of <li> tags");
        System.out.println("                                 with links to known printers. Not connected printers will");
        System.out.println("                                 have no links associanted.");
        System.out.println("${automatic-refresh}           - tag for html head. It will be empty if -ar|--automatic-refresh");
        System.out.println("                                 option is not added. ");

        System.out.println("${x_limit}       - x limit switch (on/off)");
        System.out.println("${y_limit}       - y limit switch (on/off)");
        System.out.println("${z_limit}       - z limit switch (on/off)");

        System.out.println("${filament_1}    - filament 1 switch (on/off)");
        System.out.println("${filament_2}    - filament 2 switch (on/off)");

        System.out.println("${nozzle_switch} - nozzle switch (on/off)");
        System.out.println("${door_open}   - door closed switch (on/off)");
        System.out.println("${reel_button}   - reel button switch (on/off)");

        System.out.println("${nozzle_temp}           - nozzle temperature");
        System.out.println("${nozzle_set_temp}       - nozzle set temperature");
        System.out.println("${nozzle_temp_combined}  - nozzle + nozzle set temperature divided by '/'");

        System.out.println("${bed_temp}              - bed temperature");
        System.out.println("${bed_set_temp}          - bed set temperature");
        System.out.println("${nozzle_temp_combined}  - bed + bed set temperature divided by '/'");

        System.out.println("${ambient_temp}          - ambient temperature");
        System.out.println("${ambient_set_temp}      - ambient set temperature");
        System.out.println("${ambient_temp_combined} - ambient temperature + ambient set temperature divided by '/");

        System.out.println("${fan}           - fan (on/off)");
        System.out.println("${head_Fan}      - head fan (on/off)");

        System.out.println("${x_position}    - x position");
        System.out.println("${y_position}    - y position");
        System.out.println("${z_position}    - z position");

        System.out.println("${filament_nultiplier}  - filament multiplier");
        System.out.println("${feed_rate_nultiplier} - feed rate multiplier");

        System.out.println("${temp_state}    - temperature state ('working', 'cooling', 'heating bed' or 'heating nozzles')");

        System.out.println("");

    }
}
