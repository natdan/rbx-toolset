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
import jssc.LocalSerialPort;
import jssc.SerialPortException;

/**
 *
 * @author Daniel Sendula
 */
public class JSSCSerialPortPrinterChannel implements PrinterChannel {

    private String printerPath;
    private ISerialPort serialPort;
    private InputStream in;
    private OutputStream out;

    public JSSCSerialPortPrinterChannel(String printerPath) throws IOException {
        this.printerPath = printerPath;
    }

    @Override
    public String getPrinterPath() {
        return printerPath;
    }

    @Override
    public void open() throws IOException {
        try {
            serialPort = new LocalSerialPort(getPrinterPath());
            serialPort.openPort();
            serialPort.setParams(115200, 8, 1, 0);
        } catch (SerialPortException e) {
            throw new IOException(e);
        }

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

            @Override public int available() {
                try {
                    return JSSCSerialPortPrinterChannel.this.serialPort.getInputBufferBytesCount();
                } catch (SerialPortException ignore) { }
                return 0;
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

    @Override
    public boolean isOpen() {
        return serialPort != null;
    }

    @Override
    public InputStream getInputStream() throws IOException {
        if (serialPort == null) {
            throw new IOException("Channel is not open; " +  getPrinterPath());
        }
        return in;
    }

    @Override
    public OutputStream getOutputStream() throws IOException {
        if (serialPort == null) {
            throw new IOException("Channel is not open; " +  getPrinterPath());
        }
        return out;
    }

    @Override
    public void close() {
        if (serialPort != null) {
            try { in.close(); } catch (Exception ignore) {}
            try { out.close(); } catch (Exception ignore) {}
            try { serialPort.closePort(); } catch (SerialPortException ignore) { }
        }
        serialPort = null;
    }
}
