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
package org.ah.robox.toolset;

import java.io.InputStream;
import java.io.OutputStream;

import org.ah.robox.comms.PrinterChannel;

/**
 *
 *
 * @author Daniel Sendula
 */
public class DummyTestPrinterChannel implements PrinterChannel {

    private boolean open = false;

    public String getPrinterDeviceId() {
        return "dummy-robox";
    }

    @Override
    public String getPrinterPath() {
        return "/dev/dummy-robox";
    }

    @Override
    public InputStream getInputStream() {
        return null;
    }

    @Override
    public OutputStream getOutputStream() {
        return null;
    }

    @Override
    public void open() {
        open = true;
    }

    @Override
    public boolean isOpen() {
        return open;
    }

    @Override
    public void close() {
        open = false;
    }

}
