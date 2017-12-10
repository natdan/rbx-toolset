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

import org.ah.robox.comms.utils.NotASerialPort;
import org.ah.robox.comms.utils.SerialPortAlreadyInUse;

import gnu.io.CommPort;
import gnu.io.CommPortIdentifier;
import gnu.io.NoSuchPortException;
import gnu.io.PortInUseException;
import gnu.io.SerialPort;

/**
 *
 * @author Daniel Sendula
 */
public class RxTxSerialPortPrinterChannel implements PrinterChannel {

    public static final int TIMEOUT = 2000;

    private String printerPath;
    private SerialPort serialPort;
    private InputStream in;
    private OutputStream out;

    public RxTxSerialPortPrinterChannel(String printerPath) throws IOException {
        this.printerPath = printerPath;

        in = serialPort.getInputStream();
        out = serialPort.getOutputStream();
    }

    @Override
    public String getPrinterPath() {
        return printerPath;
    }

    @Override
    public void open() throws IOException {
        try {
            CommPortIdentifier portIdentifier = CommPortIdentifier.getPortIdentifier(getPrinterPath());
            if (portIdentifier.isCurrentlyOwned()) {
                throw new SerialPortAlreadyInUse("Error: Port is currently in use");
            } else {
                CommPort commPort = portIdentifier.open(getPrinterPath(), TIMEOUT);

                if (commPort instanceof SerialPort) {
                    serialPort = (SerialPort)commPort;
                } else {
                    throw new NotASerialPort("Device " + getPrinterPath() + " is not serial port");
                }
            }
        } catch (NoSuchPortException e) {
            throw new NotASerialPort("Device " + getPrinterPath() + " no such serial port", e);
        } catch (PortInUseException e) {
            throw new SerialPortAlreadyInUse("Device " + getPrinterPath() + " already in use", e);
        }
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
            if (!System.getProperty("os.name").contains("Mac")) {
                serialPort.close(); // Bad hack until versions of rxtx is sorted.
            }
        }
        serialPort = null;
    }
}
