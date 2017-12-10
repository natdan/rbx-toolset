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
package org.ah.robox.comms.utils;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Daniel Sendula
 */
public class PrintHex {

    public static void printHex(Logger logger, Level level, byte[] buffer) {
        int readSize = buffer.length;
        int ptr = 0;
        while (ptr < readSize) {
            StringBuilder builder = new StringBuilder();
            String adr = Integer.toString(ptr, 16);
            builder.append("        ".substring(0, 8 - adr.length()));
            builder.append(adr);
            builder.append(": ");
            int ptr2 = 0;
            while (ptr + ptr2 < readSize && ptr2 < 16) {
                int b = buffer[ptr + ptr2];
                if (b < 0) { b = 256 + b; }
                String a = Integer.toString(b, 16);
                if (a.length() == 1) { a = "0" + a; }
                builder.append(a);
                builder.append(' ');
                if (ptr2 == 7) {
                    builder.append(' ');
                }
                ptr2 = ptr2 + 1;
            }
            while (ptr2 < 16) {
                builder.append("   ");
                if (ptr2 == 7) {
                    builder.append(' ');
                }
                ptr2 = ptr2 + 1;
            }
            builder.append(" |");

            ptr2 = 0;
            while (ptr + ptr2 < readSize && ptr2 < 16) {
                int b = buffer[ptr + ptr2];
                if (b >= 32 && b < 128) {
                    builder.append((char)b);
                } else {
                    builder.append('.');
                }
                ptr2 = ptr2 + 1;
            }
            while (ptr2 < 16) {
                builder.append(' ');
                ptr2 = ptr2 + 1;
            }
            builder.append("|");
            logger.log(level, builder.toString());
            ptr = ptr + 16;
        }
    }

}
