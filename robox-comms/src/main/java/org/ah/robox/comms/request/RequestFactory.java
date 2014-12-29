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
package org.ah.robox.comms.request;

import java.io.IOException;
import java.io.OutputStream;

import org.ah.robox.comms.response.ResponseFactory;
import org.ah.robox.comms.utils.PrintHex;

/**
 *
 * @author Daniel Sendula
 */
public class RequestFactory {

    public static int START_PRINT_JOB_TRANSFER = 0x90;
    public static int PRINT_JOB_DATA = 0x91;
    public static int END_PRINT_JOB_DATA = 0x92;
    public static int START_PRINT_JOB = 0x94;
    public static int SEND_GCODE = 0x95;
    public static int GET_PRINT_JOBS = 0x96;
    public static int PRINTER_PAUSE_RESUME_COMMAND = 0x98;
    public static int PRINTER_STATUS_REQ_COMMAND = 0xb0;
    public static int GET_PRINTER_DETAILS = 0xb2;
    public static int REPORT_ERRORS = 0xb3;
    public static int RESET_ERRORS = 0xc0;
    public static int WRITE_PRINTER_DETAILS = 0xc1;
    public static int ABORT_PRINT_COMMAND = 0xff;


    private OutputStream out;

    public RequestFactory(OutputStream out) {
        this.out = out;
    }

    public void sendReportErrors() throws IOException {
        byte[] buffer = new byte[1];
        buffer[0] = (byte)REPORT_ERRORS;
        sendBuffer(buffer);
    }

    public void sendResetErrors() throws IOException {
        byte[] buffer = new byte[1];
        buffer[0] = (byte)RESET_ERRORS;
        sendBuffer(buffer);
    }

    public void sendPrinterStatusRequest() throws IOException {
        byte[] buffer = new byte[1];
        buffer[0] = (byte)PRINTER_STATUS_REQ_COMMAND;
        sendBuffer(buffer);
    }

    public void sendPrinterPause() throws IOException {
        byte[] buffer = new byte[2];
        buffer[0] = (byte)PRINTER_PAUSE_RESUME_COMMAND;
        buffer[1] = (byte)'1';
        sendBuffer(buffer);
    }

    public void sendPrinterResume() throws IOException {
        byte[] buffer = new byte[2];
        buffer[0] = (byte)PRINTER_PAUSE_RESUME_COMMAND;
        buffer[1] = (byte)'0';
        sendBuffer(buffer);
    }

    public void sendPrinterAbortPrint() throws IOException {
        byte[] buffer = new byte[1];
        buffer[0] = (byte)ABORT_PRINT_COMMAND;
        sendBuffer(buffer);
    }

    public void sendGetPrinterDetails() throws IOException {
        byte[] buffer = new byte[1];
        buffer[0] = (byte)GET_PRINTER_DETAILS;
        sendBuffer(buffer);
    }

    public void sendGetPrintJobs() throws IOException {
        byte[] buffer = new byte[1];
        buffer[0] = (byte)GET_PRINT_JOBS;
        sendBuffer(buffer);
    }

    public void sendStartPrintJob(String printJobId) throws IOException {
        if (printJobId.length() > 16) {
            throw new IOException("Print job id cannot be longer than 16 bytes.");
        }
        byte[] buffer = new byte[17];
        for (int i = 1; i < 17; i++) { buffer[i] = 0; }
        buffer[0] = (byte)START_PRINT_JOB;
        System.arraycopy(printJobId.getBytes("US-ASCII"), 0, buffer, 1, printJobId.length());

        sendBuffer(buffer);
    }

    public void sendStartPrintJobTransfer(String printJobId) throws IOException {
        if (printJobId.length() > 16) {
            throw new IOException("Print job id cannot be longer than 16 bytes.");
        }
        byte[] buffer = new byte[17];
        for (int i = 1; i < 17; i++) { buffer[i] = 0; }
        buffer[0] = (byte)START_PRINT_JOB_TRANSFER;
        System.arraycopy(printJobId.getBytes("US-ASCII"), 0, buffer, 1, printJobId.length());
        sendBuffer(buffer);
    }

    public void sendEndPrintJobData(String lastPacket, int sequence) throws IOException {
        if (lastPacket.length() > 512) {
            throw new IOException("Packets must not be larger than 512 bytes");
        }
        byte[] buffer = new byte[1 + 8 + 4 + lastPacket.length()];
        buffer[0] = (byte)END_PRINT_JOB_DATA;
        System.arraycopy(String.format("%08X", sequence).getBytes("US-ASCII"), 0, buffer, 1, 8);
        System.arraycopy((String.format("%04X", lastPacket.length()) + lastPacket).getBytes("US-ASCII"), 0, buffer, 9, 4);
        System.arraycopy(lastPacket.getBytes("US-ASCII"), 0, buffer, 13, lastPacket.length());

        sendBuffer(buffer);
    }

    public void sendPrintJobData(String packet, int sequence) throws IOException {
        if (packet.length() != 512) {
            throw new IOException("Packets must be exactly 512 bytes");
        }
        byte[] buffer = new byte[1 + 8 + 512];
        buffer[0] = (byte)PRINT_JOB_DATA;
        System.arraycopy(String.format("%08X", sequence).getBytes("US-ASCII"), 0, buffer, 1, 8);
        System.arraycopy(packet.getBytes("US-ASCII"), 0, buffer, 9, 512);

        sendBuffer(buffer);
    }

    public void sendGCode(String gcode) throws IOException {
        gcode = gcode.replaceFirst(";.*$", "").trim(); // Don't send comments
        if (!gcode.endsWith("\n")) { gcode = gcode + "\n"; }
        byte[] buffer = new byte[1 + gcode.length() + 4];
        buffer[0] = (byte)SEND_GCODE;
        System.arraycopy((String.format("%04X", gcode.length()) + gcode).getBytes("US-ASCII"), 0, buffer, 1, buffer.length - 1);
        sendBuffer(buffer);
    }

    private void sendBuffer(byte[] buffer) throws IOException {
        out.write(buffer);
        out.flush();
        if (ResponseFactory.DEBUG) {
            System.out.println("Transmitted packet:");
            PrintHex.printHex(buffer);
        }
    }
}
