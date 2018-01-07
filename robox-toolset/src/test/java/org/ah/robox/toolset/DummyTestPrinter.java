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
import java.io.Reader;

import org.ah.robox.comms.Printer;
import org.ah.robox.comms.PrinterChannel;
import org.ah.robox.comms.TransmitCallback;
import org.ah.robox.comms.request.HeadEEPROMRequest;
import org.ah.robox.comms.response.GCodeResponse;
import org.ah.robox.comms.response.HeadEEPROMResponse;
import org.ah.robox.comms.response.PrintJobsResponse;
import org.ah.robox.comms.response.PrinterPause;
import org.ah.robox.comms.response.PrinterStatusResponse;
import org.ah.robox.comms.response.ReelEEPROMResponse;
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
        printerStatusResponse.setFeedRateMultiplier1("0.9");
    }

    @Override
    public void close() {
    }

    @Override
    public String getPrinterId() {
        return printerId;
    }

    @Override
    public PrinterChannel getPrinterChannel() {
        return printerChannel;
    }

    @Override
    public PrinterStatusResponse getPrinterStatus() throws IOException {
        return printerStatusResponse;
    }

    @Override
    public StandardResponse pausePrinter() throws IOException {
        System.out.println("Issued PAUSE command");
        return standardResponse;
    }

    @Override
    public StandardResponse resumePrinter() throws IOException {
        System.out.println("Issued RESUME command");
        return standardResponse;
    }

    @Override
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

    @Override
    public String getPrinterName() {
        return printerId;
    }

    @Override
    public String getModel() {
        return model;
    }

    @Override
    public String getSerialNumber() {
        return serialNumber;
    }

    @Override
    public GCodeResponse sendGCode(String gcode) throws IOException {
        return null;
    }

    @Override
    public StandardResponse reportErrors() throws IOException {
        return null;
    }

    @Override
    public StandardResponse resetErrors() throws IOException {
        return null;
    }

    @Override
    public StandardResponse startPrint(String printJob) throws IOException {
        return null;
    }

    @Override
    public PrintJobsResponse getPrintJobs() throws IOException {
        return null;
    }

    @Override
    public StandardResponse transmitPrintJob(String printJobId, Reader gcode, TransmitCallback callback) throws IOException {
        return null;
    }

    @Override
    public ReelEEPROMResponse getReadReel(int parseInt) throws IOException {
        return null;
    }

    @Override
    public HeadEEPROMResponse getReadHead() throws IOException {
        return null;
    }

    @Override
    public StandardResponse sendWriteHead(HeadEEPROMRequest request) throws IOException {
        // TODO Auto-generated method stub
        return null;
    }
}
