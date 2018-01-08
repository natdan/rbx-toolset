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

import static org.ah.robox.comms.LocalProxyPrinterDiscovery.LOCAL_PROXY_DEFAULT_PORT;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import org.ah.robox.comms.utils.NotASerialPort;
import org.ah.robox.comms.utils.SerialPortAlreadyInUse;

/**
 *
 * @author Daniel Sendula
 */
public class RemotePrinterDiscovery extends BasePrinterDiscovery {

    public static final int SOCKET_TIMEOUT = 250; // 1/4 sec
    public static final int TOTAL_TIMEOUT = 2000; // 2 sec

    public RemotePrinterDiscovery() {
    }

    @Override
    public List<PrinterChannel> findAllPrinterChannels() throws IOException {
        List<PrinterChannel> printerChannels = new ArrayList<>();

        final DatagramSocket datagramSocket = new DatagramSocket();
        try {
            datagramSocket.setBroadcast(true);

            byte[] data = new byte[2048];
            DatagramPacket packet = new DatagramPacket(data, data.length);

            int port = datagramSocket.getLocalPort();
            logger.finest("Local port: " + port + " is bound: " + datagramSocket.isBound());

            byte[] buf = ("ROBOX_PROXY_DISCOVER#255.255.255.255:" + port).getBytes();
            DatagramPacket response = new DatagramPacket(buf, buf.length,
                    InetAddress.getByName("255.255.255.255"), LOCAL_PROXY_DEFAULT_PORT);
            datagramSocket.send(response);

            datagramSocket.setSoTimeout((SOCKET_TIMEOUT));

            long currentTimeout = TOTAL_TIMEOUT;
            long started = System.currentTimeMillis();
            while (System.currentTimeMillis() - started < currentTimeout) {
                try {
                    datagramSocket.receive(packet);
                    String s = new String(data, 0, packet.getLength());
                    if (s.startsWith("serialproxy://")) {
                        String devName = s.substring(14);

                        PrinterChannel channel = channels.get(devName);
                        if (channel != null) {
                            if (!containsChannel(printerChannels, channel)) {
                                printerChannels.add(channel);
                            }
                        } else {
                            PrinterChannel printerChannel = new RemotelPortPrinterChannel(devName);
                            try {
                                printerChannel.open();
                                printerChannel.close();

                                channels.put(devName, printerChannel);
                                printerChannels.add(printerChannel);
                            } catch (NotASerialPort e) {
                                logger.log(Level.FINER, "", e);
                            } catch (SerialPortAlreadyInUse e) {
                                logger.log(Level.FINER, "", e);
                            }
                        }
                    }
                    // After first printer discovered drop down to only 250ms after for each new...
                    started = System.currentTimeMillis();
                    currentTimeout = SOCKET_TIMEOUT;
                } catch (IOException ignore) {
                }
            }
        } finally {
            datagramSocket.close();
        }

        return printerChannels;
    }

    private boolean containsChannel(List<PrinterChannel> printerChannels, PrinterChannel channel) {
        for (PrinterChannel c : printerChannels) {
            if (c.getPrinterPath().equals(channel.getPrinterPath())) {
                return true;
            }
        }
        return false;
    }
}
