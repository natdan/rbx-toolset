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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Daniel Sendula
 */
public abstract class BasePrinterDiscovery implements PrinterDiscovery {
    protected static final Logger logger = Logger.getLogger(BasePrinterDiscovery.class.getName());

    protected Map<String, PrinterChannel> channels = new HashMap<String, PrinterChannel>();
    protected Map<PrinterChannel, Printer> printers = new HashMap<PrinterChannel, Printer>();
    protected boolean isWindows = System.getProperty("os.name").contains("Windows");
    protected boolean isLinux = System.getProperty("os.name").contains("Linux");
    protected boolean isOSX = System.getProperty("os.name").contains("Mac");

    public BasePrinterDiscovery() {
    }

    @Override
    public List<Printer> findAllPrinters() throws IOException {
        List<Printer> resultPrinters = new ArrayList<Printer>();
        List<PrinterChannel> channels = findAllPrinterChannels();
        for (PrinterChannel channel : channels) {
            Printer printer = getPrinterForChannel(channel);
            printers.put(channel, printer);
            resultPrinters.add(printer);
        }

        return resultPrinters;
    }

    @Override
    public Printer getPrinterForChannel(PrinterChannel printerChannel) throws IOException {
        Printer printer = printers.get(printerChannel);
        if (printer == null) {

            if (!printerChannel.isOpen()) {
                printerChannel.open();
            }

            RoboxPrinter p = new RoboxPrinter(printerChannel);
            try {
                p.init();
                printer = p;
                printers.put(printerChannel, printer);
            } catch (IOException e) {
                logger.warning("Failed to initialise printer on path " + printerChannel.getPrinterPath());
                logger.fine("  " + e.getMessage());
                logger.log(Level.FINER, "  Error:" + printerChannel.getPrinterPath(), e);
            }
        }
        return printer;
    }

    @Override
    public boolean hasChannel(PrinterChannel printerChannel) {
        return channels.containsKey(printerChannel);
    }
}
