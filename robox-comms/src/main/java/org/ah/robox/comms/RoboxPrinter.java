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

import java.io.IOException;

import org.ah.robox.comms.request.RequestFactory;
import org.ah.robox.comms.response.GCodeResponse;
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

    public RoboxPrinter(PrinterChannel printerChannel) {
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

    public void close() {
        printerChannel.close();
    }

    public String getPrinterId() {
        return printerId;
    }

    public PrinterChannel getPrinterChannel() {
        return printerChannel;
    }

    public PrinterStatusResponse getPrinterStatus() throws IOException {
        printerRequestFactory.sendPrinterStatusRequest();
        Response response = printerResponseFactory.readResponse();
        if (response instanceof PrinterStatusResponse) {
            return (PrinterStatusResponse)response;
        }

        throw new UnexpectedPrinterResponse(response);
    }

    public StandardResponse reportErrors() throws IOException {
        printerRequestFactory.sendReportErrors();
        Response response = printerResponseFactory.readResponse();
        if (response instanceof StandardResponse) {
            return (StandardResponse)response;
        }

        throw new UnexpectedPrinterResponse(response);
    }

    public StandardResponse resetErrors() throws IOException {
        printerRequestFactory.sendResetErrors();
        Response response = printerResponseFactory.readResponse();
        if (response instanceof StandardResponse) {
            return (StandardResponse)response;
        }

        throw new UnexpectedPrinterResponse(response);
    }

    public StandardResponse pausePrinter() throws IOException {
        printerRequestFactory.sendPrinterPause();
        Response response = printerResponseFactory.readResponse();
        if (response instanceof StandardResponse) {
            return (StandardResponse)response;
        }

        throw new UnexpectedPrinterResponse(response);
    }

    public StandardResponse resumePrinter() throws IOException {
        printerRequestFactory.sendPrinterResume();
        Response response = printerResponseFactory.readResponse();
        if (response instanceof StandardResponse) {
            return (StandardResponse)response;
        }

        throw new UnexpectedPrinterResponse(response);
    }

    public StandardResponse abortPrint() throws IOException {
        printerRequestFactory.sendPrinterAbortPrint();
        Response response = printerResponseFactory.readResponse();
        if (response instanceof StandardResponse) {
            return (StandardResponse)response;
        }

        throw new UnexpectedPrinterResponse(response);
    }

    public GCodeResponse sendGCode(String gcode) throws IOException {
        printerRequestFactory.sendGCode(gcode);
        Response response = printerResponseFactory.readResponse();
        if (response instanceof GCodeResponse) {
            return (GCodeResponse)response;
        }

        throw new UnexpectedPrinterResponse(response);
    }

    public String getPrinterName() {
        return printerDetails.getPrinterName();
    }

    public String getModel() {
        return printerDetails.getModel();
    }

    public String getSerialNumber() {
        return printerDetails.getSerialNumber();
    }

}
