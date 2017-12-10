package org.ah.robox.comms;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class AggregatedPrinterDiscovery implements PrinterDiscovery {

    private BasePrinterDiscovery localDiscovery;
    private BasePrinterDiscovery remoteDiscovery;

    public AggregatedPrinterDiscovery(BasePrinterDiscovery localDiscovery, BasePrinterDiscovery remoteDiscovery) {
        this.localDiscovery = localDiscovery;
        this.remoteDiscovery = remoteDiscovery;
    }

    @Override
    public List<PrinterChannel> findAllPrinterChannels() throws IOException {
        List<PrinterChannel> result = new ArrayList<>();
        result.addAll(localDiscovery.findAllPrinterChannels());
        result.addAll(remoteDiscovery.findAllPrinterChannels());

        return result;
    }

    @Override
    public List<Printer> findAllPrinters() throws IOException {
        List<Printer> result = new ArrayList<>();
        result.addAll(localDiscovery.findAllPrinters());
        result.addAll(remoteDiscovery.findAllPrinters());

        return result;
    }

    @Override
    public Printer getPrinterForChannel(PrinterChannel printerChannel) throws IOException {
        if (localDiscovery.hasChannel(printerChannel)) {
            return localDiscovery.getPrinterForChannel(printerChannel);
        }
        if (remoteDiscovery.hasChannel(printerChannel)) {
            return remoteDiscovery.getPrinterForChannel(printerChannel);
        }

        throw new IOException("Unknown channel for device: " + printerChannel.getPrinterPath());
    }

    @Override
    public boolean hasChannel(PrinterChannel printerChannel) {
        return localDiscovery.hasChannel(printerChannel)
                || remoteDiscovery.hasChannel(printerChannel);
    }

}
