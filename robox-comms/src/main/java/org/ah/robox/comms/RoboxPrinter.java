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
package org.ah.robox.comms;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;

import org.ah.robox.comms.request.RequestFactory;
import org.ah.robox.comms.response.GCodeResponse;
import org.ah.robox.comms.response.PrintJobsResponse;
import org.ah.robox.comms.response.PrinterDetailsResponse;
import org.ah.robox.comms.response.PrinterStatusResponse;
import org.ah.robox.comms.response.Response;
import org.ah.robox.comms.response.ResponseFactory;
import org.ah.robox.comms.response.StandardResponse;

/**
 *
 * @author Daniel Sendula
 */
public class RoboxPrinter implements Printer {

    private PrinterChannel printerChannel;
    private ResponseFactory printerResponseFactory;
    private RequestFactory printerRequestFactory;
    private PrinterDetailsResponse printerDetails;
    private String printerId;

    public RoboxPrinter(PrinterChannel printerChannel) throws IOException {
        this.printerChannel = printerChannel;

        printerResponseFactory = new ResponseFactory(printerChannel.getInputStream());
        printerRequestFactory = new RequestFactory(printerChannel.getOutputStream());
    }

    public void init() throws IOException {
        printerRequestFactory.sendGetPrinterDetails();
        Response response = printerResponseFactory.readResponse();

        if (response instanceof PrinterDetailsResponse) {
            printerDetails = (PrinterDetailsResponse)response;

            printerId = printerDetails.getSerialNumber();
            if (printerId == null || "".equals(printerId)) {
                printerId = printerChannel.getPrinterPath().replace('/', '_').replace('\\', '_');
            }
        } else {
            throw new UnexpectedPrinterResponse(response);
        }
    }

    @Override
    public void close() {
        printerChannel.close();
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
        printerRequestFactory.sendPrinterStatusRequest();
        Response response = printerResponseFactory.readResponse();
        if (response instanceof PrinterStatusResponse) {
            return (PrinterStatusResponse)response;
        }

        throw new UnexpectedPrinterResponse(response);
    }

    @Override
    public StandardResponse reportErrors() throws IOException {
        printerRequestFactory.sendReportErrors();
        Response response = printerResponseFactory.readResponse();
        if (response instanceof StandardResponse) {
            return (StandardResponse)response;
        }

        throw new UnexpectedPrinterResponse(response);
    }

    @Override
    public StandardResponse resetErrors() throws IOException {
        printerRequestFactory.sendResetErrors();
        Response response = printerResponseFactory.readResponse();
        if (response instanceof StandardResponse) {
            return (StandardResponse)response;
        }

        throw new UnexpectedPrinterResponse(response);
    }

    @Override
    public StandardResponse pausePrinter() throws IOException {
        printerRequestFactory.sendPrinterPause();
        Response response = printerResponseFactory.readResponse();
        if (response instanceof StandardResponse) {
            return (StandardResponse)response;
        }

        throw new UnexpectedPrinterResponse(response);
    }

    @Override
    public StandardResponse resumePrinter() throws IOException {
        printerRequestFactory.sendPrinterResume();
        Response response = printerResponseFactory.readResponse();
        if (response instanceof StandardResponse) {
            return (StandardResponse)response;
        }

        throw new UnexpectedPrinterResponse(response);
    }

    @Override
    public StandardResponse abortPrint() throws IOException {
        printerRequestFactory.sendPrinterAbortPrint();
        Response response = printerResponseFactory.readResponse();
        if (response instanceof StandardResponse) {
            return (StandardResponse)response;
        }

        throw new UnexpectedPrinterResponse(response);
    }

    @Override
    public GCodeResponse sendGCode(String gcode) throws IOException {
        printerRequestFactory.sendGCode(gcode);
        Response response = printerResponseFactory.readResponse();
        if (response instanceof GCodeResponse) {
            return (GCodeResponse)response;
        }

        throw new UnexpectedPrinterResponse(response);
    }

    @Override
    public StandardResponse startPrint(String printJobId) throws IOException {
        printerRequestFactory.sendStartPrintJob(printJobId);
        Response response = printerResponseFactory.readResponse();
        if (response instanceof StandardResponse) {
            return (StandardResponse)response;
        }

        throw new UnexpectedPrinterResponse(response);
    }

    @Override
    public PrintJobsResponse getPrintJobs() throws IOException {
        printerRequestFactory.sendGetPrintJobs();
        Response response = printerResponseFactory.readResponse();
        if (response instanceof PrintJobsResponse) {
            return (PrintJobsResponse)response;
        }

        throw new UnexpectedPrinterResponse(response);
    }

    @Override
    public StandardResponse transmitPrintJob(String printJobId, Reader gcodeReader, TransmitCallback callback) throws IOException {
        StandardResponse standardResponse = null;
        printerRequestFactory.sendStartPrintJobTransfer(printJobId);
        Response response = printerResponseFactory.readResponse();
        if (!(response instanceof StandardResponse)) {
            throw new UnexpectedPrinterResponse(response);
        }
        standardResponse = (StandardResponse)response;
        // TODO check response...

        int sequence = 0;
        int totalBytes = 0;
        BufferedReader in = new BufferedReader(gcodeReader);
        StringBuilder buffer = new StringBuilder();
        String line = in.readLine();
        while (line != null) {
            int i = line.indexOf(';');
            if (i >= 0) {
                line = line.substring(0, i);
            }
            line = line.trim();
            if (line.length() > 0) {
                buffer.append(line).append("\r\n");
                if (buffer.length() > 512) {
                    String packet = buffer.substring(0, 512);
                    buffer.delete(0, 512);

                    printerRequestFactory.sendPrintJobData(packet, sequence);
                    response = printerResponseFactory.readResponse();
                    if (!(response instanceof StandardResponse)) {
                        throw new UnexpectedPrinterResponse(response);
                    }
                    standardResponse = (StandardResponse)response;
                    // TODO check response...

                    totalBytes = totalBytes + 512;

                    if (callback != null) {
                        callback.transmitted(sequence, totalBytes);
                    }
                    sequence = sequence + 1;
                }
            }

            line = in.readLine();
        }

        printerRequestFactory.sendEndPrintJobData(buffer.toString(), sequence);
        response = printerResponseFactory.readResponse();
        if (!(response instanceof StandardResponse)) {
            throw new UnexpectedPrinterResponse(response);
        }
        standardResponse = (StandardResponse)response;

        return standardResponse;
    }

    @Override
    public String getPrinterName() {
        return printerDetails.getPrinterName();
    }

    @Override
    public String getModel() {
        return printerDetails.getModel();
    }

    @Override
    public String getSerialNumber() {
        return printerDetails.getSerialNumber();
    }
}
