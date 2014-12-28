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

import org.ah.robox.comms.Printer;
import org.ah.robox.comms.PrinterChannel;
import org.ah.robox.comms.response.GCodeResponse;
import org.ah.robox.comms.response.PrinterPause;
import org.ah.robox.comms.response.PrinterStatusResponse;
import org.ah.robox.comms.response.StandardResponse;

/**
 *
 *
 * @author Daniel Sendula
 */
public class DummyTestPrinter implements Printer {

    private PrinterStatusResponse printerStatusResponse = new PrinterStatusResponse();
    private StandardResponse standardResponse = new StandardResponse();

    private PrinterChannel printerChannel;
    private String printerId;
    private String serialNumber;
    private String model = "dummy-printer";

    public DummyTestPrinter(PrinterChannel printerChannel, String printerId, String serialNumber) {
        this.printerChannel = printerChannel;
        this.printerId = printerId;
        this.serialNumber = serialNumber;
    }

    public void initDefaults() {
        printerStatusResponse.setPrintJob("test-print-job");
        printerStatusResponse.setLineNumber(220);
        printerStatusResponse.setPause(PrinterPause.NOT_PAUSED);
        printerStatusResponse.setBusy(true);
        printerStatusResponse.setXPosition("10.002");
        printerStatusResponse.setYPosition("20.003");
        printerStatusResponse.setZPosition("5.004");
        printerStatusResponse.setFilamentMultiplier("0.85");
        printerStatusResponse.setFeedRateMultiplier("0.9");
    }

    public void close() {
    }

    public String getPrinterId() {
        return printerId;
    }

    public PrinterChannel getPrinterChannel() {
        return printerChannel;
    }

    public PrinterStatusResponse getPrinterStatus() throws IOException {
        return printerStatusResponse;
    }

    public StandardResponse pausePrinter() throws IOException {
        System.out.println("Issued PAUSE command");
        return standardResponse;
    }

    public StandardResponse resumePrinter() throws IOException {
        System.out.println("Issued RESUME command");
        return standardResponse;
    }

    public StandardResponse abortPrint() throws IOException {
        System.out.println("Issued ABORT PRINT command");
        return standardResponse;
    }

    public PrinterStatusResponse getPrinterStatusResponse() {
        return printerStatusResponse;
    }

    public void setPrinterStatusResponse(PrinterStatusResponse printerStatusResponse) {
        this.printerStatusResponse = printerStatusResponse;
    }

    public StandardResponse getStandardResponse() {
        return standardResponse;
    }

    public void setStandardResponse(StandardResponse standardResponse) {
        this.standardResponse = standardResponse;
    }

    public String getPrinterName() {
        return printerId;
    }

    public String getModel() {
        return model;
    }

    public String getSerialNumber() {
        return serialNumber;
    }

    public GCodeResponse sendGCode(String gcode) throws IOException {
        return null;
    }

    public StandardResponse reportErrors() throws IOException {
        return null;
    }

    public StandardResponse resetErrors() throws IOException {
        return null;
    }
}
