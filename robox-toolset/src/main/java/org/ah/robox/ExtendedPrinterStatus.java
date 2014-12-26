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
package org.ah.robox;

import org.ah.robox.PrintStatusCommand.Estimate;
import org.ah.robox.PrintStatusCommand.EstimateState;
import org.ah.robox.comms.Printer;
import org.ah.robox.comms.response.PrinterPause;
import org.ah.robox.comms.response.PrinterStatusResponse;

/**
 *
 *
 * @author Daniel Sendula
 */
public class ExtendedPrinterStatus {

    private PrinterStatusResponse printerStatus;
    private Estimate estimate;
    private Printer printer;

    public ExtendedPrinterStatus(Printer printer) {
        this.printer = printer;
    }

    public Printer getPrinter() {
        return printer;
    }

    public PrinterStatusResponse getPrinterStatus() {
        return printerStatus;
    }

    public void setPrinterStatus(PrinterStatusResponse printerStatus) {
        this.printerStatus = printerStatus;
        if (printerStatus == null) {
            estimate = null;
        }
    }

    public Estimate getEstimate() {
        return estimate;
    }

    public void setEstimate(Estimate estimate) {
        this.estimate = estimate;
    }

    public OverallPrinterStatus getOverallPrinterStatus() {
        if (printerStatus == null) {
            return OverallPrinterStatus.NOT_CONNECTED;
        }
        if (printerStatus.getPause() == PrinterPause.PAUSED) {
            return OverallPrinterStatus.PAUSED;
        } else if (printerStatus.getPause() == PrinterPause.PAUSING) {
            return OverallPrinterStatus.PAUSING;
        } else if (printerStatus.getPause() == PrinterPause.RESUMING) {
            return OverallPrinterStatus.RESUMING;
        } else if (printerStatus.getPause() == PrinterPause.UNKNOWN) {
            return OverallPrinterStatus.UNKNOWN;
        } else if (printerStatus.getPause() == PrinterPause.NOT_PAUSED) {
            if (estimate == null) {
                return OverallPrinterStatus.IDLE;
            } else if (estimate.getPrintStatus() == EstimateState.IDLE) {
                return OverallPrinterStatus.IDLE;
            } else if (estimate.getPrintStatus() == EstimateState.NO_LINES) {
                return OverallPrinterStatus.PRINTING_NO_ESTIMATE_FILE;
            } else if (estimate.getPrintStatus() == EstimateState.PREPARING) {
                return OverallPrinterStatus.PREPARING_TO_PRINT;
            } else if (estimate.getPrintStatus() == EstimateState.PRINTING) {
                return OverallPrinterStatus.PRINTING;
            }
        }

        return OverallPrinterStatus.UNKNOWN;
    }
}
