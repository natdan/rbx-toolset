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
import java.io.Reader;

import org.ah.robox.comms.response.GCodeResponse;
import org.ah.robox.comms.response.PrintJobsResponse;
import org.ah.robox.comms.response.PrinterStatusResponse;
import org.ah.robox.comms.response.StandardResponse;

/**
 *
 * @author Daniel Sendula
 */
public interface Printer {

    void close();

    String getPrinterId();

    String getPrinterName();

    String getModel();

    String getSerialNumber();

    PrinterChannel getPrinterChannel();

    PrinterStatusResponse getPrinterStatus() throws IOException;

    StandardResponse reportErrors() throws IOException;

    StandardResponse resetErrors() throws IOException;

    StandardResponse pausePrinter() throws IOException;

    StandardResponse resumePrinter() throws IOException;

    StandardResponse abortPrint() throws IOException;

    GCodeResponse sendGCode(String gcode) throws IOException;

    StandardResponse startPrint(String printJob) throws IOException;

    PrintJobsResponse getPrintJobs() throws IOException;

    StandardResponse transmitPrintJob(String printJobId, Reader gcodeReader, TransmitCallback callback) throws IOException;
}
