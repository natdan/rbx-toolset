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
package org.ah.robox.comms.response;

/**
 *
 * @author Daniel Sendula
 */
public class PrinterStatus implements Response {

    private String printJob;
    private int lineNumber;
    private PrinterPause pause = PrinterPause.UNKNOWN;
    private boolean busy;

    public PrinterStatus() {
    }

    public String toString() {
        return "Status[printJob='" + printJob + "' , lineNumber=" + lineNumber + ", pause=" + pause.getText() + ", isBusy=" + busy + "]";
    }

    public void setPrintJob(String printJob) {
        this.printJob = printJob;
    }

    public String getPrintJob() {
        return printJob;
    }

    public void setLineNumber(int lineNumber) {
        this.lineNumber = lineNumber;
    }

    public int getLineNumber() {
        return lineNumber;
    }

    public PrinterPause getPause() {
        return pause;
    }

    public void setPause(PrinterPause pause) {
        if (pause == null) { throw new NullPointerException("Pause cannot be null"); }
        this.pause = pause;
    }

    public boolean isBusy() {
        return busy;
    }

    public void setBusy(boolean busy) {
        this.busy = busy;
    }
}
