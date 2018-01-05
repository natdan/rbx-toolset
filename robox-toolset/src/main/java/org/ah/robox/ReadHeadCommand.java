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
import org.ah.robox.comms.response.HeadEEPROMResponse;

/**
 *
 *
 * @author Daniel Sendula
 */
public class ReadHeadCommand {

    private static final Logger logger = Logger.getLogger(ReadHeadCommand.class.getName());

    public static void execute(Printer printer, List<String> args) throws Exception {

        for (String a : args) {
            if ("-h".equals(a) || "--help".equals(a) || "-?".equals(a)) {
                printHelp();
                System.exit(0);
            } else {
                logger.severe("Unknown option: '" + a + "'");
                printHelp();
                System.exit(1);
            }
        }

        HeadEEPROMResponse head = printer.getReadHead();

        logger.info("HeadType: " + head.getHeadType());
        logger.info("HeadId: " + head.getHeadId());
        logger.info("MaxTemperature: " + head.getMaxTemperature());
        logger.info("Beta: " + head.getBeta());
        logger.info("TCal: " + head.getTcal());
        logger.info("X0: " + head.getX0());
        logger.info("Y0: " + head.getY0());
        logger.info("Z0: " + head.getZ0());
        logger.info("B0: " + head.getB0());
        logger.info("X1: " + head.getX1());
        logger.info("Y1: " + head.getY1());
        logger.info("Z1: " + head.getZ1());
        logger.info("B1: " + head.getB0());

        logger.info("LastTemperature0: " + head.getLastTemperature0());
        logger.info("LastTemperature1: " + head.getLastTemperature1());
        logger.info("Hours: " + head.getB0());

    }

    public static void printHelp() {
        logger.info("Usage: rbx [<general-options>] readhead");
        logger.info("");
        Main.printGeneralOptions();
        logger.info("");
        Main.printSpecificOptions();
        logger.info("");
        logger.info("  -h | --help | -?     - this page");

        logger.info("");
        logger.info("Reads head EEPROM.");
        logger.info("");
        logger.info("Example: rbx readhead");
    }
}
