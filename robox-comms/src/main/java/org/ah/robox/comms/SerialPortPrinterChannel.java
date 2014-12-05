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

import gnu.io.SerialPort;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 *
 * @author Daniel Sendula
 */
public class SerialPortPrinterChannel implements PrinterChannel {

    private String printerDeviceId;
    private String printerPath;
    private SerialPort serialPort;
    private InputStream in;
    private OutputStream out;

    public SerialPortPrinterChannel(String deviceId, String printerPath, SerialPort serialPort) throws IOException {
        this.printerDeviceId = deviceId;
        this.printerPath = printerPath;
        this.serialPort = serialPort;

        in = serialPort.getInputStream();
        out = serialPort.getOutputStream();
    }

    public String getPrinterDeviceId() {
        return printerDeviceId;
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
        serialPort.close();
    }

}
