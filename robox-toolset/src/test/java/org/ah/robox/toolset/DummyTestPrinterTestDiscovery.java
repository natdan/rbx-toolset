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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.ah.robox.comms.Printer;
import org.ah.robox.comms.PrinterChannel;
import org.ah.robox.comms.PrinterDiscovery;

/**
 *
 *
 * @author Daniel Sendula
 */
public class DummyTestPrinterTestDiscovery implements PrinterDiscovery {

    private List<PrinterChannel> testPrinterChannels = new ArrayList<PrinterChannel>();
    private List<Printer> testPrinters = new ArrayList<Printer>();

    @Override
    public List<PrinterChannel> findAllPrinterChannels() throws IOException {
        return testPrinterChannels;
    }

    @Override
    public List<Printer> findAllPrinters() throws IOException {
       List<Printer> resultPrinters = new ArrayList<Printer>();
        List<PrinterChannel> channels = findAllPrinterChannels();
        for (PrinterChannel channel : channels) {
            Printer printer = getPrinterForChannel(channel);
            resultPrinters.add(printer);
        }
       return resultPrinters;
    }

    @Override
    public Printer getPrinterForChannel(PrinterChannel printerChannel) {
        for (Printer printer : testPrinters) {
            if (printerChannel == printer.getPrinterChannel()) {
                return printer;
            }
        }
        return null;
    }

    public List<PrinterChannel> getPrinterChannels() {
        return testPrinterChannels;
    }

    public List<Printer> getPrinters() {
        return testPrinters;
    }

    @Override
    public boolean hasChannel(PrinterChannel printerChannel) {
        return testPrinterChannels.contains(printerChannel);
    }
}
