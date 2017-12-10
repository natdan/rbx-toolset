/*******************************************************************************
 * Copyright (c) 2014-2017 Creative Sphere Limited.
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
import java.net.Socket;

/**
 *
 * @author Daniel Sendula
 */
public class RemotelPortPrinterChannel implements PrinterChannel {


    private String printerPath;
    private Socket socket;
    private InputStream in;
    private OutputStream out;

    public RemotelPortPrinterChannel(String printerPath) throws IOException {
        this.printerPath = printerPath;
    }

    @Override
    public String getPrinterPath() {
        return printerPath;
    }

    @Override
    public void open() throws IOException {
        String[] split = getPrinterPath().split(":");
        String address = split[0];
        int port = Integer.parseInt(split[1]);

        socket = new Socket(address, port);

        in = socket.getInputStream();
        out = socket.getOutputStream();
    }


    @Override
    public boolean isOpen() {
        return socket != null;
    }

    @Override
    public InputStream getInputStream() throws IOException {
        if (socket == null) {
            throw new IOException("Channel is not open; " +  getPrinterPath());
        }
        return in;
    }

    @Override
    public OutputStream getOutputStream() throws IOException {
        if (socket == null) {
            throw new IOException("Channel is not open; " +  getPrinterPath());
        }
        return out;
    }

    @Override
    public void close() {
        if (socket != null) {
            try { in.close(); } catch (Exception ignore) {}
            try { out.close(); } catch (Exception ignore) {}
            try { socket.close(); } catch (Exception ignore) {}
        }
        socket = null;
    }
}
