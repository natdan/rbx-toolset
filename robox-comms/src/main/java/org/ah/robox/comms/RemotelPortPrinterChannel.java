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
import java.net.Socket;

/**
 *
 * @author Daniel Sendula
 */
public class RemotelPortPrinterChannel implements PrinterChannel {

    private SerialPortsPrinterDiscovery parent;
    private String printerPath;
    private Socket socket;
    private InputStream in;
    private OutputStream out;

    public RemotelPortPrinterChannel(SerialPortsPrinterDiscovery parent, String printerPath, Socket socket) throws IOException {
        this.parent = parent;
        this.printerPath = printerPath;
        this.socket = socket;

        in = socket.getInputStream();
        out = socket.getOutputStream();
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
        try { socket.close(); } catch (Exception ignore) {}
        parent.closed(this);
    }
}
