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

    public String getPrinterDeviceId() {
        return "dummy-robox";
    }

    public String getPrinterPath() {
        return "/dev/dummy-robox";
    }

    public InputStream getInputStream() {
        return null;
    }

    public OutputStream getOutputStream() {
        return null;
    }

    public void close() {
    }

}
