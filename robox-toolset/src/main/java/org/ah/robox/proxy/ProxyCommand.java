package org.ah.robox.proxy;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.ah.robox.comms.BasePrinterDiscovery;
import org.ah.robox.comms.PrinterChannel;
import org.ah.robox.comms.PrinterDiscovery;

public class ProxyCommand {

    private static Logger logger = Logger.getLogger(ProxyCommand.class.getName());
    private static final Logger discoveryLogger = Logger.getLogger(BasePrinterDiscovery.class.getName());

    public static void execute(PrinterDiscovery localDiscovery, List<String> args) throws Exception {
        ProxyCommand client = new ProxyCommand(localDiscovery);
        client.start();
    }

    private PrinterDiscovery localDiscovery;
    private Map<PrinterChannel, PrinterConnection> discovered = new HashMap<>();
    private boolean run = true;

    public ProxyCommand(PrinterDiscovery localDiscovery) {
        this.localDiscovery = localDiscovery;
    }

    public void start() {
        try {
            final DatagramSocket datagramSocket = new DatagramSocket(4080);
            datagramSocket.setBroadcast(true);

            Thread thread = new Thread(new LocalProxyResponder(datagramSocket, discovered));
            thread.setDaemon(true);
            thread.start();
        } catch (SocketException e1) {
            logger.severe("Cannot open UDP socket at 4080");
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

    private class PrinterConnection implements Runnable {
        private ProxyCommand proxy;
        private ServerSocket serverSocket;
        private boolean stopped;
        private PrinterChannel channel;
        private Thread thread;
        private InputStream printerIn;
        private OutputStream printerOut;

        PrinterConnection(ProxyCommand proxy, PrinterChannel channel) throws IOException {
            this.proxy = proxy;
            this.channel = channel;
            serverSocket = new ServerSocket(0);
            serverSocket.setSoTimeout(500);
        }

        public void closeConnection() {
            stopped = true;
            try {
                printerIn.close();
            } catch (Exception ignore) {
            }
            try {
                printerOut.close();
            } catch (Exception ignore) {
            }
            try {
                channel.close();
            } catch (Exception ignore) {
            }
            proxy.closePrinterConnection(this);
        }

        public void start() {
            try {
                if (!channel.isOpen()) {
                    channel.open();
                }

                printerIn = channel.getInputStream();
                printerOut = channel.getOutputStream();
            } catch (IOException e) {
                closeConnection();
                return;
            }

            thread = new Thread(this);
            thread.setDaemon(true);
            thread.start();
        }

        public int getLocalPort() {
            return serverSocket.getLocalPort();
        }

        @Override
        public void run() {
            while (!stopped) {
                try {
                    Socket socket = serverSocket.accept();
                    Client client = new Client(this, socket, printerIn, printerOut);
                    client.start();
                } catch (SocketTimeoutException ignore) {
                } catch (IOException e) {
                    closeConnection();
                    e.printStackTrace();
                }
            }
        }
    }

    private class Client {

        private PrinterConnection printerConnection;
        private Thread readerThread;
        private Thread writerThread;
        private Socket socket;
        private InputStream socketIn;
        private OutputStream socketOut;
        private InputStream printerIn;
        private OutputStream printerOut;

        private boolean startWriterThread = true;

        public Client(PrinterConnection printerConnection, Socket socket, InputStream printerIn, OutputStream printerOut) {
            this.printerConnection = printerConnection;
            this.socket = socket;
            this.printerIn = printerIn;
            this.printerOut = printerOut;
        }

        public void start() {
            try {
                socketIn = socket.getInputStream();
                socketOut = socket.getOutputStream();
            } catch (IOException e) {
                close();
                return;
            }

            logger.fine("Opened connection " + socket.getRemoteSocketAddress());

            readerThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    String socketDesc = socket.toString();
                    logger.finest("Started read thread for  " + socketDesc);
                    while (socket != null && !socket.isClosed()) {
                        try {
                            int r = socketIn.read();

                            if (r > 0) {
                                if (startWriterThread) {
                                    writerThread.start();
                                    startWriterThread = false;
                                }
                                int a = socketIn.available();
                                logger.finer("<(" + a + ")");
                                byte[] readBuffer = new byte[a + 1];
                                readBuffer[0] = (byte) r;
                                int total = 1;
                                if (a > 0) {
                                    int actual = socketIn.read(readBuffer, 1, a);
                                    total = total + actual;
                                }

                                try {
                                    printerOut.write(readBuffer, 0, total);
                                } catch (IOException e) {
                                    close();
                                    printerConnection.closeConnection();
                                }
                            } else {
                                logger.finer("<(EOF)");
                                close();
                            }
                        } catch (IOException e) {
                            close();
                        }
                    }
                    logger.finest("Closed read thread for  " + socketDesc);
                }
            });

            writerThread = new Thread(new Runnable() {

                @Override
                public void run() {
                    String socketDesc = socket.toString();
                    logger.finest("Started write thread for  " + socketDesc);
                    while (socket != null && !socket.isClosed()) {
                        try {
                            int bytes = printerIn.available();
                            if (bytes > 0) {
                                logger.finer(">(" + bytes + ")");
                                byte[] buffer = new byte[bytes];

                                int actual = printerIn.read(buffer, 0, bytes);

                                try {
                                    socketOut.write(buffer, 0, actual);
                                } catch (IOException e) {
                                    close();
                                }
                            } else {
                                try {
                                    Thread.sleep(100);
                                } catch (InterruptedException ignore) { }
                            }
                        } catch (IOException e) {
                            close();
                            printerConnection.closeConnection();
                        }
                    }
                    logger.finest("Closed write thread for  " + socketDesc);
                }
            });

            readerThread.start();
            // writerThread.start();
        }

        protected synchronized void close() {
            if (socket != null) {
                try {
                    socket.close();
                    logger.fine("Closing connection " + socket.getRemoteSocketAddress());
                } catch (Exception ignore) {
                }

                try {
                    readerThread.interrupt();
                } catch (Exception ignore) {
                }
                try {
                    writerThread.interrupt();
                } catch (Exception ignore) {
                }

                readerThread = null;
                writerThread = null;

                socket = null;
            }
        }
    }

    private static class LocalProxyResponder implements Runnable {

        private DatagramSocket datagramSocket;
        private Map<PrinterChannel, PrinterConnection> discovered = new HashMap<>();

        public LocalProxyResponder(DatagramSocket datagramSocket, Map<PrinterChannel, PrinterConnection> discovered) {
            this.datagramSocket = datagramSocket;
            this.discovered = discovered;
        }

        @Override
        public void run() {
            byte[] data = new byte[2048];
            DatagramPacket packet = new DatagramPacket(data, data.length);

            while (true) {
                try {
                    datagramSocket.receive(packet);
                    String s = new String(data, 0, packet.getLength());
                    logger.finer("Received datagram: " + s);

                    List<PrinterConnection> currentPrinters = new ArrayList<>();
                    synchronized (discovered) {
                        for (PrinterConnection client : discovered.values()) {
                            currentPrinters.add(client);
                        }
                    }

                    if (s.startsWith("DISCOVER_ROVER") && currentPrinters.size() > 0) {
                        String[] split = s.split("#");

                        Enumeration<NetworkInterface> networkInterfaceEnumerator = NetworkInterface.getNetworkInterfaces();
                        while (networkInterfaceEnumerator.hasMoreElements()) {
                            NetworkInterface networkInterface = networkInterfaceEnumerator.nextElement();
                            Enumeration<InetAddress> inetAddressEnumerator = networkInterface.getInetAddresses();
                            while (inetAddressEnumerator.hasMoreElements()) {
                                InetAddress address = inetAddressEnumerator.nextElement();
                                String hostAddress = address.getHostAddress();

                                logger.finest("Interface on: " + hostAddress + ", lo:" + address.isLoopbackAddress() + ", ll:" + address.isLinkLocalAddress()
                                        + ", al:" + address.isAnyLocalAddress() + ", sl:" + address.isSiteLocalAddress());

                                if (!address.isLoopbackAddress() && !address.isLinkLocalAddress()) {

                                    for (PrinterConnection connection : currentPrinters) {
                                        int port = -1;
                                        synchronized (connection) {
                                            port = connection.getLocalPort();
                                        }
                                        if (port > 0) {
                                            String printerPath = "serialproxy://" + hostAddress + ":" + port;
                                            byte[] buf = printerPath.getBytes();

                                            String returnAddress = "255.255.255.255";
                                            int returnPort = 4080;

                                            if (split.length > 1) {
                                                String[] split2 = split[1].split(":");
                                                if (split2.length > 1) {
                                                    returnAddress = split2[0];
                                                    try {
                                                        returnPort = Integer.parseInt(split2[1]);
                                                    } catch (NumberFormatException ignore) {
                                                    }
                                                }
                                            }

                                            logger.finer("Sending back to " + returnAddress + ":" + returnPort + " : " + printerPath);

                                            DatagramPacket response = new DatagramPacket(buf, buf.length, InetAddress.getByName(returnAddress), returnPort);
                                            datagramSocket.send(response);
                                        }
                                    }
                                }
                            }
                        }
                    }
                } catch (IOException ignore) {
                }
            }
        }
    }
}
