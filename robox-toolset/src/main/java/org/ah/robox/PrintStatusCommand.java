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

import java.util.List;

import org.ah.robox.comms.Printer;
import org.ah.robox.comms.PrinterChannel;
import org.ah.robox.comms.RoboxPrinter;
import org.ah.robox.comms.response.PrinterStatus;

/**
 *
 *
 * @author Daniel Sendula
 */
public class PrintStatusCommand {

    public static void execute(PrinterChannel selectedChannel, List<String> args) throws Exception {

        Printer printer = new RoboxPrinter(selectedChannel);

        PrinterStatus printStatus = printer.getPrinterStatus();

        String printJob = printStatus.getPrintJob();

        if (printJob != null && printJob.length() > 0) {
            System.out.println("There a print in progress @ " + selectedChannel.getPrinterDeviceId() + "(" + selectedChannel.getPrinterPath() + ")");
            System.out.println("    Job id      : '" + printJob + "'");
            System.out.println("    Current line: " + printStatus.getLineNumber());
        } else {
            System.out.println("There is no current jobs on the printer @ " + selectedChannel.getPrinterDeviceId() + "(" + selectedChannel.getPrinterPath() + ")");
        }
        System.out.println("    Status      : " + printStatus.getPause().getText());
        System.out.println("    Busy        : " + printStatus.isBusy());
    }

}
