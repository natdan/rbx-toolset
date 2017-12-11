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
import org.ah.robox.comms.response.ReelEEPROMResponse;

/**
 *
 *
 * @author Daniel Sendula
 */
public class ReadReelCommand {

    private static final Logger logger = Logger.getLogger(ReadReelCommand.class.getName());

    public static void execute(Printer printer, List<String> args) throws Exception {

        String eepromFlag = "";
        for (String a : args) {
            if ("-h".equals(a) || "--help".equals(a) || "-?".equals(a)) {
                printHelp();
                System.exit(0);
            } else {
                if ("0".equals(a) || "1".equals(a)) {
                    eepromFlag = a;
                } else {
                    logger.severe("Unknown option: '" + a + "'");
                    printHelp();
                    System.exit(1);
                }
            }
        }

        if (eepromFlag.length() == 0) {
            printHelp();
            System.exit(1);
        }

        ReelEEPROMResponse reel = printer.getReadReel(Integer.parseInt(eepromFlag));

        logger.info("Id: " + reel.getId());
//        logger.info("Colour: " +  reel.getColour());
        logger.info("FirstLayerNozzleTemperature: " +  reel.getFirstLayerNozzleTemperature());
        logger.info("NozzleTemperature: " + reel.getNozzleTemperature());
        logger.info("FirstLayerBedTemperature: " + reel.getFirstLayerBedTemperature());
        logger.info("BedTemperature: " + reel.getBedTemperature());
        logger.info("AmbientTemperature: " + reel.getAmbientTemperature());
        logger.info("FilamentSize: " + reel.getFilamentSize());
        logger.info("Multiplier: " + reel.getMultiplier());
        logger.info("FeedRate: " + reel.getFeedRate());
        logger.info("Name: " +reel.getName());
        logger.info("Type: " + reel.getType());

    }

    public static void printHelp() {
        logger.info("Usage: rbx [<general-options>] readreel (0|1)");
        logger.info("");
        Main.printGeneralOptions();
        logger.info("");
        Main.printSpecificOptions();
        logger.info("");
        logger.info("  -h | --help | -?     - this page");

        logger.info("");
        logger.info("Reads reel EEPROM.");
        logger.info("");
        logger.info("Example: rbx readreel 1");
    }
}
