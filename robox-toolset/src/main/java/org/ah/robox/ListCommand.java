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
import java.util.logging.Logger;

import org.ah.robox.comms.Printer;

/**
 *
 *
 * @author Daniel Sendula
 */
public class ListCommand {
    private static final Logger logger = Logger.getLogger(ListCommand.class.getName());

    public static void execute(List<Printer> printers) {
        if (printers.size() == 0) {
            logger.info("There are not detected printers.");
        } else {
            logger.info("Detected printers:");
            int i = 1;
            for (Printer printer : printers) {
                logger.info("    " + i + ":" + printer.getPrinterName() + " @ " + printer.getPrinterChannel().getPrinterPath());
                printer.close();
                i++;
            }
        }
    }
}
