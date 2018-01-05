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
import org.ah.robox.comms.request.HeadEEPROMRequest;
import org.ah.robox.comms.response.HeadEEPROMResponse;
import org.ah.robox.comms.response.StandardResponse;

/**
 *
 *
 * @author Daniel Sendula
 */
public class CalibrateHeadCommand {

    private static final Logger logger = Logger.getLogger(CalibrateHeadCommand.class.getName());

    public static void execute(Printer printer, List<String> args) throws Exception {

        for (String a : args) {
            if ("-h".equals(a) || "--help".equals(a) || "-?".equals(a)) {
                printHelp();
                System.exit(0);
            }
        }

        HeadEEPROMResponse response = printer.getReadHead();

        logger.info("Old head values:");
        logger.info("HeadType: " + response.getHeadType());
        logger.info("HeadId: " + response.getHeadId());
        logger.info("MaxTemperature: " + response.getMaxTemperature());
        logger.info("Beta: " + response.getBeta());
        logger.info("TCal: " + response.getTcal());
        logger.info("X0: " + response.getX0());
        logger.info("Y0: " + response.getY0());
        logger.info("Z0: " + response.getZ0());
        logger.info("B0: " + response.getB0());
        logger.info("X1: " + response.getX1());
        logger.info("Y1: " + response.getY1());
        logger.info("Z1: " + response.getZ1());
        logger.info("B1: " + response.getB0());

        logger.info("LastTemperature0: " + response.getLastTemperature0());
        logger.info("LastTemperature1: " + response.getLastTemperature1());
        logger.info("Hours: " + response.getB0());

        HeadEEPROMRequest request = new HeadEEPROMRequest();
        request.copyFromResponse(response);

        String swtch = null;
        for (String a : args) {
            if ("-h".equals(a) || "--help".equals(a) || "-?".equals(a)) {
            } else {
                if (swtch != null) {
                    if ("-x0".equals(swtch)) {
                        request.setX0(a);
                        swtch = null;
                    } else if ("-x1".equals(swtch)) {
                        request.setX1(a);
                        swtch = null;
                    } else if ("-y0".equals(swtch)) {
                        request.setY0(a);
                        swtch = null;
                    } else if ("-y1".equals(swtch)) {
                        request.setY1(a);
                        swtch = null;
                    } else if ("-z0".equals(swtch)) {
                        request.setZ0(a);
                        swtch = null;
                    } else if ("-z1".equals(swtch)) {
                        request.setZ1(a);
                        swtch = null;
                    } else if ("-b0".equals(swtch)) {
                        request.setB0(a);
                        swtch = null;
                    } else if ("-b1".equals(swtch)) {
                        request.setB1(a);
                        swtch = null;
                    }
                } else {
                    if ("-x0".equals(a) || "-y0".equals(a) || "-z0".equals(a) || "-b0".equals(a)
                            || "-x1".equals(a) || "-y1".equals(a) || "-z1".equals(a) || "-b1".equals(a)) {
                        swtch = a;
                    } else {
                        logger.severe("Unknown option: '" + a + "'");
                        printHelp();
                        System.exit(1);
                    }
                }
            }
        }
        if (swtch != null) {
            logger.severe("Switch must be followed by a value: '" + swtch + "'");
            printHelp();
            System.exit(1);
        }

        StandardResponse standardResponse = printer.sendWriteHead(request);
        if (Main.processStandardResponse(printer, standardResponse)) {

            logger.info("New head values:");
            logger.info("HeadType: " + request.getHeadType());
            logger.info("HeadId: " + request.getHeadId());
            logger.info("MaxTemperature: " + request.getMaxTemperature());
            logger.info("Beta: " + request.getBeta());
            logger.info("TCal: " + request.getTcal());
            logger.info("X0: " + request.getX0());
            logger.info("Y0: " + request.getY0());
            logger.info("Z0: " + request.getZ0());
            logger.info("B0: " + request.getB0());
            logger.info("X1: " + request.getX1());
            logger.info("Y1: " + request.getY1());
            logger.info("Z1: " + request.getZ1());
            logger.info("B1: " + request.getB0());

            logger.info("LastTemperature0: " + request.getLastTemperature0());
            logger.info("LastTemperature1: " + request.getLastTemperature1());
            logger.info("Hours: " + request.getB0());
        }
    }

    public static void printHelp() {
        logger.info("Usage: rbx [<general-options>] calibrate {-x0 <val> | -x1 <va> | -y0 <val> | -y1 <va> | -z0 <val> | -z1 <va> | -b0 <val> | -b1 <va>}");
        logger.info("");
        Main.printGeneralOptions();
        logger.info("");
        Main.printSpecificOptions();
        logger.info("");
        logger.info("  -h | --help | -?  - this page");
        logger.info("  -x0               - nozzle 0 X value");
        logger.info("  -x1               - nozzle 1 X value");
        logger.info("  -y0               - nozzle 0 Y value");
        logger.info("  -y1               - nozzle 1 Y value");
        logger.info("  -z0               - nozzle 0 Z value");
        logger.info("  -z1               - nozzle 1 Z value");
        logger.info("  -b0               - nozzle 0 B value");
        logger.info("  -b1               - nozzle 1 B value");

        logger.info("");
        logger.info("Writes head EEPROM calibration values.");
        logger.info("");
        logger.info("Example: rbx calibrate -x0 1.00 -z0 -0.35 ");
    }
}
