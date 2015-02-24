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

import java.io.IOException;

import org.ah.robox.comms.Printer;
import org.ah.robox.comms.response.StandardResponse;

/**
 *
 *
 * @author Daniel Sendula
 */
public class StandardResponseHelper {
    public static String processStandardResponse(Printer printer, StandardResponse response) throws IOException {
        String hrs = null; // human readable string
        if (response.isBufferOverFlow()) {
            hrs = "ERR: Buffer overflow.";
        } else if (response.isTooLongLine()) {
            hrs = "ERR: Line too long.";
        } else if (response.isUnknownCommand()) {
            hrs = "ERR: Unknown command.";
        } else if (response.isSequenceError()) {
            hrs = "ERR: Sequence error.";
        } else if (response.isFileTooLargeError()) {
            hrs = "ERR: File too large error.";
        } else if (response.isError1()) {
            hrs = "ERR: error code 1.";
        } else if (response.isError5()) {
            hrs = "ERR: error code 5.";
        } else if (response.isError6()) {
            hrs = "ERR: error code 6.";
        } else if (response.isError8()) {
            hrs = "ERR: error code 8.";
        } else if (response.isError9()) {
            hrs = "ERR: error code 9.";
        } else if (response.isError10()) {
            hrs = "ERR: error code 10.";
        } else if (response.isError12()) {
            hrs = "ERR: error code 12.";
        } else if (response.isError13()) {
            hrs = "ERR: error code 13.";
        } else if (response.isError14()) {
            hrs = "ERR: error code 14.";
        } else if (response.isError15()) {
            hrs = "ERR: error code 15.";
        } else if (response.isError16()) {
            hrs = "ERR: error code 16.";
        } else if (response.isError17()) {
            hrs = "ERR: error code 17.";
        } else if (response.isError18()) {
            hrs = "ERR: error code 18.";
        } else if (response.isError19()) {
            hrs = "ERR: error code 19.";
        } else if (response.isError20()) {
            hrs = "ERR: error code 20.";
        }
        if (response.isError()) {
            StandardResponse standardResponse = printer.resetErrors();
            if (standardResponse.isError()) {
                hrs = hrs + "\n*** Cannot clear error!";
            }
        }

        return hrs;
    }

}
