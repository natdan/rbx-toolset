package org.ah.robox.comms;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class PriorityPrinterDiscovery implements PrinterDiscovery {

    private BasePrinterDiscovery firstDiscovery;
    private BasePrinterDiscovery secondDiscovery;

    public PriorityPrinterDiscovery(BasePrinterDiscovery firstDiscovery, BasePrinterDiscovery secondDiscovery) {
        this.firstDiscovery = firstDiscovery;
        this.secondDiscovery = secondDiscovery;
    }

    @Override
    public List<PrinterChannel> findAllPrinterChannels() throws IOException {
        List<PrinterChannel> result = new ArrayList<>();
        result.addAll(firstDiscovery.findAllPrinterChannels());

        if (result.size() == 0) {
            result.addAll(secondDiscovery.findAllPrinterChannels());
        }

        return result;
    }

    @Override
    public List<Printer> findAllPrinters() throws IOException {
        List<Printer> result = new ArrayList<>();
        result.addAll(firstDiscovery.findAllPrinters());
        result.addAll(secondDiscovery.findAllPrinters());

        return result;
    }

    @Override
    public Printer getPrinterForChannel(PrinterChannel printerChannel) throws IOException {
        if (firstDiscovery.hasChannel(printerChannel)) {
            return firstDiscovery.getPrinterForChannel(printerChannel);
        }
        if (secondDiscovery.hasChannel(printerChannel)) {
            return secondDiscovery.getPrinterForChannel(printerChannel);
        }

        throw new IOException("Unknown channel for device: " + printerChannel.getPrinterPath());
    }

    @Override
    public boolean hasChannel(PrinterChannel printerChannel) {
        return firstDiscovery.hasChannel(printerChannel)
                || secondDiscovery.hasChannel(printerChannel);
    }

}
