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

/**
 *
 *
 * @author Daniel Sendula
 */
public class ListCommand {

    public static void execute(List<Printer> printers) {
        if (printers.size() == 0) {
            System.out.println("There are not detected printers.");
        } else {
            System.out.println("Detected printers:");
            int i = 1;
            for (Printer printer : printers) {
                System.out.println("    " + i + ":" + printer.getPrinterName() + " @ " + printer.getPrinterChannel().getPrinterPath());
                printer.close();
                i++;
            }
        }
    }
}
