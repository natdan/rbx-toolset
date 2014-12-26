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

import org.ah.robox.comms.response.PrinterStatusResponse;
import org.ah.robox.web.WebServer;

/**
 *
 *
 * @author Daniel Sendula
 */
public class DummyPrinterWebServer {

    public static void main(String[] args) throws Exception {
        DummyTestPrinterChannel dummyTestPrinterChannel = new DummyTestPrinterChannel();
        DummyTestPrinter dummyTestPrinter = new DummyTestPrinter(dummyTestPrinterChannel, "DummyPrinter1", "1234567890");
        dummyTestPrinter.initDefaults();

        DummyTestPrinterTestDiscovery printerDiscovery = new DummyTestPrinterTestDiscovery();
        printerDiscovery.getPrinterChannels().add(dummyTestPrinterChannel);
        printerDiscovery.getPrinters().add(dummyTestPrinter);

        PrinterStatusResponse printerStatusResponse = dummyTestPrinter.getPrinterStatusResponse();

        WebServer webServer = new WebServer(printerDiscovery);
        webServer.setAllowCommandsFlag(true);
        webServer.setPort(8100);
        webServer.init();
        webServer.start();

        while (true) {
            Thread.sleep(500);
            printerStatusResponse.setLineNumber(printerStatusResponse.getLineNumber() + 1);
        }
    }

}
