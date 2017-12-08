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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import jssc.ISerialPort;
import jssc.SerialPortException;

/**
 *
 * @author Daniel Sendula
 */
public class JSSCSerialPortPrinterChannel implements PrinterChannel {

    private SerialPortsPrinterDiscovery parent;
    private String printerPath;
    private ISerialPort serialPort;
    private InputStream in;
    private OutputStream out;

    public JSSCSerialPortPrinterChannel(SerialPortsPrinterDiscovery parent, String printerPath, ISerialPort serialPort) throws IOException {
        this.parent = parent;
        this.printerPath = printerPath;
        this.serialPort = serialPort;

        in = new InputStream() {
            @Override public int read() throws IOException {
                try {
                    return JSSCSerialPortPrinterChannel.this.serialPort.readBytes(1)[0];
                } catch (SerialPortException e) {
                    throw new IOException(e);
                }
            }

            @Override public int read(byte b[], int off, int len) throws IOException {
                try {
                    byte[] bytes = JSSCSerialPortPrinterChannel.this.serialPort.readBytes(len);
                    System.arraycopy(bytes, 0, b, off, len);
                    return len;
                } catch (SerialPortException e) {
                    throw new IOException(e);
                }
            }
        };

        out = new OutputStream() {
            @Override public void write(int b) throws IOException {
                try {
                    JSSCSerialPortPrinterChannel.this.serialPort.writeByte((byte) b);
                } catch (SerialPortException e) {
                    throw new IOException(e);
                }
            }

            @Override public void write(byte b[], int off, int len) throws IOException {
                try {
                    if (off == 0 && b.length == len) {
                        JSSCSerialPortPrinterChannel.this.serialPort.writeBytes(b);
                    } else {
                        byte[] buffer = new byte[len];
                        System.arraycopy(b, off, buffer, 0, len);
                        JSSCSerialPortPrinterChannel.this.serialPort.writeBytes(buffer);
                    }
                } catch (SerialPortException e) {
                    throw new IOException(e);
                }
            }

        };
    }

    public String getPrinterPath() {
        return printerPath;
    }


    public InputStream getInputStream() {
        return in;
    }

    public OutputStream getOutputStream() {
        return out;
    }

    public void close() {
        try { in.close(); } catch (Exception ignore) {}
        try { out.close(); } catch (Exception ignore) {}
//        if (!System.getProperty("os.name").contains("Mac")) {
            try {
                serialPort.closePort();
            } catch (SerialPortException e) {
                e.printStackTrace();
            }
//        }
        parent.closed(this);
    }
}
