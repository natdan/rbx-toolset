package org.ah.robox.proxy;

import org.ah.robox.Main;
import org.ah.robox.comms.BasePrinterDiscovery;
import org.ah.robox.comms.PrinterChannel;
import org.ah.robox.comms.PrinterDiscovery;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.ah.robox.comms.LocalProxyPrinterDiscovery.LOCAL_PROXY_DEFAULT_PORT;
import static org.ah.robox.comms.LocalProxyPrinterDiscovery.isLocalProxyRunning;

import static java.lang.String.join;

public class ProxyCommand {

    public static final int KILL_TIMEOUT = 4000; // 4sec
    public static final int KILL_SLEEP = 500; // 1/2 sec

    static Logger logger = Logger.getLogger(ProxyCommand.class.getName());
    private static final Logger discoveryLogger = Logger.getLogger(BasePrinterDiscovery.class.getName());

    public static void execute(PrinterDiscovery localDiscovery, List<String> args) throws Exception {
        String command = "run";
        for (String a : args) {
            if ("-?".equals(a) || "-h".equals(a) || "--help".equals(a)) {
                printHelp();
                System.exit(0);
            } else if (a.equals("run")) {
                command = a;
            } else if (a.equals("install")) {
                command = a;
            } else if (a.equals("status")) {
                command = a;
            } else if (a.equals("start")) {
                command = a;
            } else if (a.equals("stop")) {
                command = a;
            } else if (a.equals("kill")) {
                command = a;
            } else {
                logger.severe("Unknown option: '" + a + "'");
                printHelp();
                System.exit(1);
            }
        }

        if ("run".equals(command)) {
            ProxyCommand client = new ProxyCommand(localDiscovery);
            client.start();
        } else if ("install".equals(command)) {
            install();
        } else if ("kill".equals(command)) {
            kill();
        } else if ("status".equals(command)) {
            status();
        } else if ("start".equals(command)) {
            startService();
        } else if ("stop".equals(command)) {
            stopService();
        }
    }

    private static void status() {
        if (isLocalProxyRunning()) {
            logger.info("Proxy service is running");
            System.exit(0);
        } else {
            logger.info("No proxy service is running");
            System.exit(-1);
        }
    }

    private static void install() {
        if (!System.getProperty("os.name").contains("Linux")) {
            logger.severe("Cannot install on any other OS but Linux.");
            System.exit(-1);
        }
        File rbxProxyFile = new File("/etc/systemd/system/rbx-proxy.service");

        InputStream is = ProxyCommand.class.getResourceAsStream("/rbx-proxy.service");
        try {
            try {
                FileOutputStream fos = new FileOutputStream(rbxProxyFile);
                try {
                    byte[] buf = new byte[50000];

                    int r = is.read(buf);
                    fos.write(buf, 0, r);

                } catch (IOException e) {
                    logger.log(Level.SEVERE, "Failed to create /etc/systemd/system/rbx-proxy.service", e);
                } finally {
                    try {
                        fos.close();
                    } catch (IOException ignore) { }
                }
            } catch (FileNotFoundException e) {
                String msg = e.getMessage();
                if (msg.endsWith("(Permission denied)")) {
                    logger.severe("Cannot create file /etc/systemd/system/rbx-proxy.service due to permissions.");
                    logger.severe("Please try running it with sudo:");
                    logger.severe("");
                    logger.severe("sudo rbx proxy install");
                    System.exit(-1);
                } else {
                    logger.log(Level.SEVERE, "Failed to create /etc/systemd/system/rbx-proxy.service", e);
                }
            }
        } finally {
            try {
                is.close();
            } catch (IOException ignore) { }
        }

        try {
            rbxProxyFile.setExecutable(true, false);
        } catch (SecurityException e) {
            logger.severe("Failed to set executable permissions to /etc/init.d/rbx-proxy. Try running 'sudo chmod a+x /etc/init.d/rbx-proxy'");
            System.exit(-1);
        }

//        if (!execute("systemctl", "daemon-reload")) {
//            logger.severe("Failed to run 'systemctl daemon-reload'. Please try running rbx with sudo.");
//            System.exit(-1);
//        }
        if (!execute("systemctl", "enable", "rbx-proxy.service")) {
            logger.severe("Failed to run 'systemctl enable'. Please try running rbx with sudo.");
            System.exit(-1);
        }
        logger.info("Service rbx-proxy successfully installed");
        logger.info("");
        logger.info("You can start service with:");
        logger.info("    sudo service rbx-proxy start");
    }

    private static void startService() {
        if (execute("service", "start")) {
            logger.severe("Failed to run 'service start'. Please try running rbx with sudo.");
            System.exit(-1);
        }
        logger.info("Service rbx-proxy successfully started");
    }

    private static void stopService() {
        if (execute("service", "stop")) {
            logger.severe("Failed to run 'service stop'. Please try running rbx with sudo.");
            System.exit(-1);
        }
        logger.info("Service rbx-proxy successfully stopped");
    }

    private static boolean execute(String... command) {
        try {
            ProcessBuilder pb = new ProcessBuilder(command);
            Process p = pb.start();
            InputStream is = p.getInputStream();
            int a = is.available();
            while (a > 0 || p.isAlive()) {
                if (a > 0) {
                    byte[] buf = new byte[10240];

                    int r = is.read(buf, 0, a);
                    System.out.write(buf, 0, r);
                } else {
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException ignore) {}
                }
                a = is.available();
            }
            int exitValue = p.waitFor();
            if (exitValue != 0) {
                logger.severe("Got exit code " + exitValue + " while running '" + join(" ", command) + "'");
                return false;
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Got exception while running '" + command + "'", e);
            return false;
        }
        return true;
    }

    private static void kill() {
        if (isLocalProxyRunning()) {
            try {
                final DatagramSocket datagramSocket = new DatagramSocket();
                try {
                    datagramSocket.setBroadcast(true);

                    byte[] buf = ("ROBOX_PROXY_STOP").getBytes();
                    DatagramPacket response = new DatagramPacket(buf, buf.length, InetAddress.getByName("127.0.0.1"), LOCAL_PROXY_DEFAULT_PORT);

                    datagramSocket.send(response);
                } finally {
                    datagramSocket.close();
                }
            } catch (Exception ignore) {}

            long now = System.currentTimeMillis();
            boolean running = isLocalProxyRunning();
            while (running && System.currentTimeMillis() - now < KILL_TIMEOUT) {
                try {
                    Thread.sleep(KILL_SLEEP);
                } catch (InterruptedException e) {}
                running = isLocalProxyRunning();
            }

            if (running) {
                logger.info("Failed to stop proxy service");
                System.exit(-1);
            } else {
                logger.info("Proxy service is stopped");
                System.exit(0);
            }
        } else {
            logger.info("No proxy service is running");
            System.exit(-1);
        }
    }

    private PrinterDiscovery localDiscovery;
    private Map<PrinterChannel, PrinterConnection> discovered = new HashMap<>();
    private boolean run = true;

    public ProxyCommand(PrinterDiscovery localDiscovery) {
        this.localDiscovery = localDiscovery;
    }

    public void start() {
        try {
            final DatagramSocket datagramSocket = new DatagramSocket(LOCAL_PROXY_DEFAULT_PORT);
            datagramSocket.setBroadcast(true);

            Thread thread = new Thread(new LocalProxyResponder(datagramSocket, discovered));
            thread.setDaemon(true);
            thread.start();
        } catch (SocketException e1) {
            logger.severe("Cannot open UDP socket at " + LOCAL_PROXY_DEFAULT_PORT);
            System.exit(-1);
        }

        while (run) {

            try {
                List<PrinterChannel> discoveredPrinterChannels;
                Level discoveryLoggerLevel = discoveryLogger.getLevel();
                discoveryLogger.setLevel(Level.INFO);
                try {
                    discoveredPrinterChannels = localDiscovery.findAllPrinterChannels();
                } finally {
                    discoveryLogger.setLevel(discoveryLoggerLevel);
                }

                synchronized (discovered) {
                    for (PrinterChannel printerChannel : discoveredPrinterChannels) {
                        if (!discovered.containsKey(printerChannel)) {
                            try {
                                PrinterConnection printerConnection = new PrinterConnection(this, printerChannel);
                                printerConnection.start();
                                discovered.put(printerChannel, printerConnection);
                            } catch (IOException e2) {
                                logger.log(Level.INFO, "Cannot setup printer connection for " + printerChannel.getPrinterPath(), e2);
                            }
                        }
                    }
                    Iterator<Map.Entry<PrinterChannel, PrinterConnection>> channelsIterator = discovered.entrySet().iterator();
                    while (channelsIterator.hasNext()) {
                        Map.Entry<PrinterChannel, PrinterConnection> entry = channelsIterator.next();
                        if (!discoveredPrinterChannels.contains(entry.getKey())) {
                            channelsIterator.remove();
                            entry.getValue().closeConnection();
                        }
                    }
                }
            } catch (IOException e) {
                logger.log(Level.INFO, "Cannot discover printers", e);
            }

            try {
                Thread.sleep(1000);
            } catch (InterruptedException ignore) {
            }

        }
    }

    protected synchronized void closePrinterConnection(PrinterConnection printerConnection) {
        synchronized (discovered) {
            Iterator<Map.Entry<PrinterChannel, PrinterConnection>> it = discovered.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry<PrinterChannel, PrinterConnection> entry = it.next();

                if (entry.getValue() == printerConnection) {
                    it.remove();
                }
            }
        }
    }

    public static void printHelp() {
        logger.info("Usage: rbx [<general-options>] proxy [<specific-options>] [run|install|status|kill|stop|start]");
        logger.info("");
        Main.printGeneralOptions();
        logger.info("");
        Main.printSpecificOptions();
        logger.info("");
        logger.info("If invoked without any extra command proxy will start proxy server on current machine");
        logger.info("and expose attached printers through autodiscovery service. In order to invoke any");
        logger.info("other command on a remote printer (like one with proxy service started) use");
        logger.info("option -r. See general options.");
        logger.info("");
        logger.info("Following extra commands are available, too:");
        logger.info(" run           - starts proxy in the current shell. Can be omitted.");
        logger.info(" install       - sets up a linux service in /etc/init.d");
        logger.info(" status        - checks if proxy is already running on a local computer");
        logger.info(" kill          - kills currently running proxy. It is not to be used directly");
        logger.info("                 but by the service to stop the proxy process.");
        logger.info(" stop          - stops currently running proxy. This command uses linux service.");
        logger.info(" start         - starts the proxy. This command uses linux service.");
        logger.info("");
        logger.info("Note: service can only be installed on linux computer (as started or stopped).");
        logger.info("Internally commands just invoke 'service start' or 'service stop' commands");
        logger.info("and, usually, that means that rbx needs to be, then, invoked with sudo.");
        logger.info("");
        logger.info("Examples:");
        logger.info(" rbx proxy               - starts proxy in current shell");
        logger.info(" rbx -vv proxy           - starts proxy with detailed verbose outout");
        logger.info(" rbx proxy status        - shows if the proxy is already running on the current computer");
        logger.info(" sudo rbx proxy install  - creates /etc/systemd/system/rbx-proxy.service file and sets it up as a daemon");
        logger.info(" sudo rbx proxy start    - starts proxy daemon");
        logger.info(" sudo rbx proxy stop     - stops proxy daemon");
    }
}
