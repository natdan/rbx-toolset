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

import org.ah.robox.comms.response.PrinterPause;
import org.ah.robox.comms.response.PrinterStatusResponse;
import org.ah.robox.web.WebServer;

/**
 *
 *
 * @author Daniel Sendula
 */
public class TwoDummyPrintersWebServer {

    public static void main(String[] args) throws Exception {
        DummyTestPrinterChannel dummyTestPrinterChannel1 = new DummyTestPrinterChannel();
        DummyTestPrinter dummyTestPrinter1 = new DummyTestPrinter(dummyTestPrinterChannel1, "DummyPrinter1", "1234567890");
        dummyTestPrinter1.initDefaults();

        DummyTestPrinterChannel dummyTestPrinterChannel2 = new DummyTestPrinterChannel();
        DummyTestPrinter dummyTestPrinter2 = new DummyTestPrinter(dummyTestPrinterChannel2, "DummyPrinter2", "9999999999");
        //dummyTestPrinter2.initDefaults();
        dummyTestPrinter2.getPrinterStatus().setPause(PrinterPause.NOT_PAUSED);
        dummyTestPrinter2.getPrinterStatus().setPrintJob("");

        DummyTestPrinterTestDiscovery printerDiscovery = new DummyTestPrinterTestDiscovery();
        printerDiscovery.getPrinterChannels().add(dummyTestPrinterChannel1);
        printerDiscovery.getPrinterChannels().add(dummyTestPrinterChannel2);
        printerDiscovery.getPrinters().add(dummyTestPrinter1);
        printerDiscovery.getPrinters().add(dummyTestPrinter2);

        PrinterStatusResponse printerStatusResponse1 = dummyTestPrinter1.getPrinterStatusResponse();
//        PrinterStatusResponse printerStatusResponse2 = dummyTestPrinter2.getPrinterStatusResponse();

        WebServer webServer = new WebServer(printerDiscovery);
        webServer.setAllowCommandsFlag(true);
        webServer.setAutomaticRefresh(10);
        webServer.setImageCommand("cat /Users/daniel/image.jpg");
        webServer.setPort(8100);
        webServer.init();
        webServer.start();

        while (true) {
            Thread.sleep(500);
            printerStatusResponse1.setLineNumber(printerStatusResponse1.getLineNumber() + 1);
        }
    }

}
