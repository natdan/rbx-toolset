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
package org.ah.robox.web;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.ah.robox.ExtendedPrinterStatus;
import org.ah.robox.Main;
import org.ah.robox.OverallPrinterStatus;
import org.ah.robox.PrintStatusCommand;
import org.ah.robox.PrintStatusCommand.Estimate;
import org.ah.robox.comms.Printer;
import org.ah.robox.comms.PrinterDiscovery;
import org.ah.robox.comms.response.PrinterStatusResponse;

/**
 *
 *
 * @author Daniel Sendula
 */
public class StatusManager implements Runnable {

    private String preferredPrinterId;
    private long lastInvoked;
    private int interval = 15;
    private boolean run = true;
    private boolean running = false;
    private String postRefreshCommand;
    private String param;
    private String format = "%c: %h:%m:%s";
    private Thread statusThread;
    private PrinterDiscovery printerDiscovery;
    private Map<String, Printer> printers = new HashMap<String, Printer>();
    private Map<String, ExtendedPrinterStatus> statuses = new HashMap<String, ExtendedPrinterStatus>();

    public StatusManager(PrinterDiscovery printerDiscovery, String preferredPrinterId) {
        this.preferredPrinterId = preferredPrinterId;
        this.printerDiscovery = printerDiscovery;
    }

    public void start() {
        Thread statusThread = new Thread(this);
        statusThread.setDaemon(true);
        statusThread.start();
    }

    public void stop() {
        run = false;
    }

    public void run() {
        try {
            if (Main.debugFlag) {
                System.out.println("Starting printer status checking thread");
            }
            if (Main.verboseFlag && postRefreshCommand != null) {
                System.out.println("Post refresh command set to: " + postRefreshCommand);
            }
            while (run) {
                try {
                    running = true;
                    try {
                        updatePrinters();

                        Set<String> knownButNotPresentPrinterIds = new HashSet<String>(statuses.keySet());
                        for (Printer printer : printers.values()) {
                            try {
                                synchronized (printer) {
                                    PrinterStatusResponse printerStatus = printer.getPrinterStatus();
                                    String printerId = printer.getPrinterId();
                                    knownButNotPresentPrinterIds.remove(printerId);
                                    ExtendedPrinterStatus extendedPrinterStatus = statuses.get(printerId);
                                    if (extendedPrinterStatus == null) {
                                        extendedPrinterStatus = new ExtendedPrinterStatus(printer);
                                        statuses.put(printerId, extendedPrinterStatus);
                                    }
                                    extendedPrinterStatus.setPrinterStatus(printerStatus);
                                    if (extendedPrinterStatus.getPrinterStatus() != null
                                            && extendedPrinterStatus.getPrinterStatus().getPrintJob() != null
                                            && extendedPrinterStatus.getPrinterStatus().getPrintJob().length() > 0) {

                                        String printJob = extendedPrinterStatus.getPrinterStatus().getPrintJob();
                                        if (Main.verboseFlag) {
                                            System.out.println("Got print job: " + printJob);
                                        }
                                        try {
                                            Estimate estimateTime = PrintStatusCommand.calculateEstimate(printJob, extendedPrinterStatus.getPrinterStatus().getLineNumber(), System.currentTimeMillis());
                                            extendedPrinterStatus.setEstimate(estimateTime);

                                            OverallPrinterStatus overallStatus = extendedPrinterStatus.getOverallPrinterStatus();
                                            param = estimateTime.toString(format).replace("%c", overallStatus.name());

                                        } catch (IOException e) {
                                            String emptyTime = format.replace("%h", "").replace("%m", "").replace("%s", "");
                                            param = emptyTime.replace("%c", "ERROR:IOException"  + e.getMessage());
                                        }
                                    } else {
                                        if (Main.verboseFlag) {
                                            System.out.println("No print job.");
                                        }
                                        String emptyTime = format.replace("%h", "").replace("%m", "").replace("%s", "");
                                        param = emptyTime.replace("%c", "Idle");
                                    }
                                    if (run && postRefreshCommand != null && extendedPrinterStatus != null) {
                                        // TODO - this needs re-working
                                        invokeCommand(extendedPrinterStatus);
                                    }
                                }
                            } catch (IOException e) {
                                if (Main.debugFlag) {
                                    System.err.println("Failed to obtain printer status: " + e.getMessage());
                                }
                                printer = null;
                            }
                        }

                        for (String knowPrinterId : knownButNotPresentPrinterIds) {
                            ExtendedPrinterStatus status = statuses.get(knowPrinterId);
                            status.setPrinterStatus(null);
                        }
                    } finally {
                        running = false;
                    }

                    if (run) {
                        if (Main.debugFlag) {
                            System.out.println("Printer status waiting for " + (interval * 1000));
                        }
                        Thread.sleep(interval * 1000);
                        lastInvoked = System.currentTimeMillis();
                    }
                } catch (InterruptedException e) {
                }
            }
            if (Main.debugFlag) {
                System.out.println("Leaving printer status checking thread");
            }
        } catch (Throwable t) {
            t.printStackTrace(System.err);
            System.exit(1);
        }
    }

    /**
     * This methd returns only status we have, or one that correspocts to preferredPrinterId,
     * or only printer status that's not in ERROR or UNKONWN or NOT_CONNECTED state.
     * If tehre is more than one that nothing is going to be selected.
     *
     * @return
     */
    public ExtendedPrinterStatus selectOnePrinter() {
        if (statuses.size() == 1) {
            return statuses.values().iterator().next();
        }

        boolean oneSelected = false;
        ExtendedPrinterStatus selectedStatus = null;

        for (Map.Entry<String, ExtendedPrinterStatus> entry : statuses.entrySet()) {
            if (preferredPrinterId != null && preferredPrinterId.equals(entry.getKey())) {
                return entry.getValue();
            }
            ExtendedPrinterStatus status = entry.getValue();

            OverallPrinterStatus opStatus = status.getOverallPrinterStatus();
            if (opStatus != OverallPrinterStatus.NOT_CONNECTED
                    && opStatus != OverallPrinterStatus.ERROR
                    && opStatus != OverallPrinterStatus.UNKNOWN) {

                if (oneSelected) {
                    // If one is already selected - then deselect it and never select another
                    selectedStatus = null;
                } else {
                    // If one is not alreayd selected - then use this one if it is only...
                    oneSelected = true;
                    selectedStatus = status;
                }
            }
        }

        return selectedStatus;
    }

    public Map<String, Printer> getPrinters() {
        return printers;
    }

    public ExtendedPrinterStatus getPrinterStatus(String printerId) {
        return statuses.get(printerId);
    }

    private void updatePrinters() throws IOException {
        List<Printer> allPrinters = printerDiscovery.findAllPrinters();
        for (Printer printer : allPrinters) {
            String printerId = printer.getPrinterId();
            Printer existingPrinter = printers.get(printerId);
            if (existingPrinter == null) {
                printers.put(printerId, printer);
            }
        }
    }

    private void invokeCommand(ExtendedPrinterStatus printStatus) {
        String command = postRefreshCommand + " " + param + "";
        if (Main.debugFlag) {
            System.out.println("Invoking command for printer status '" + command + "'");
        }
        try {
            Process process = Runtime.getRuntime().exec(command);
            if (Main.debugFlag) {
                try {
                    byte[] buffer = new byte[1024];
                    int r = process.getErrorStream().read(buffer);
                    while (r > 0) {
                        System.err.print(new String(buffer, 0, r));
                        r = process.getErrorStream().read(buffer);
                    }
                    r = process.getInputStream().read(buffer);
                    while (r > 0) {
                        System.err.print(new String(buffer, 0, r));
                        r = process.getInputStream().read(buffer);
                    }
                } catch (IOException e) {

                }
            }
        } catch (Throwable t) {
            if (Main.debugFlag) {
                t.printStackTrace(System.err);
            } else if (Main.verboseFlag) {
                System.err.print(t.getMessage());
            }
        }
    }

    protected Thread getThread() {
        return statusThread;
    }

    public long getLastInvoked() {
        return lastInvoked;
    }

    public int getInterval() {
        return interval;
    }

    public void setInterval(int interval) {
        this.interval = interval;
    }

    public boolean isRunning() {
        return run || running;
    }

    public String getPostRefreshCommand() {
        return postRefreshCommand;
    }

    public void setPostRefreshCommand(String postRefreshCommand) {
        this.postRefreshCommand = postRefreshCommand;
    }

    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format;
    }
}
