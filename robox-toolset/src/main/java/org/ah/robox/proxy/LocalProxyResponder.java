package org.ah.robox.proxy;

import static org.ah.robox.comms.LocalProxyPrinterDiscovery.LOCAL_PROXY_DEFAULT_PORT;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.ah.robox.comms.PrinterChannel;

class LocalProxyResponder implements Runnable {

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
                ProxyCommand.logger.finer("Received datagram: " + s);

                List<PrinterConnection> currentPrinters = new ArrayList<>();
                synchronized (discovered) {
                    for (PrinterConnection client : discovered.values()) {
                        currentPrinters.add(client);
                    }
                }

                if (s.startsWith("ROBOX_PROXY_STOP")) {
                    ProxyCommand.logger.info("Stopping proxy");
                    System.exit(0);
                } else if (s.startsWith("ROBOX_PROXY_DISCOVER") && currentPrinters.size() > 0) {
                    String[] split = s.split("#");

                    Enumeration<NetworkInterface> networkInterfaceEnumerator = NetworkInterface.getNetworkInterfaces();
                    while (networkInterfaceEnumerator.hasMoreElements()) {
                        NetworkInterface networkInterface = networkInterfaceEnumerator.nextElement();
                        Enumeration<InetAddress> inetAddressEnumerator = networkInterface.getInetAddresses();
                        while (inetAddressEnumerator.hasMoreElements()) {
                            InetAddress address = inetAddressEnumerator.nextElement();
                            String hostAddress = address.getHostAddress();

                            ProxyCommand.logger.finest("Interface on: " + hostAddress + ", lo:" + address.isLoopbackAddress() + ", ll:" + address.isLinkLocalAddress()
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
                                        int returnPort = LOCAL_PROXY_DEFAULT_PORT;

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

                                        ProxyCommand.logger.finer("Sending back to " + returnAddress + ":" + returnPort + " : " + printerPath);

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