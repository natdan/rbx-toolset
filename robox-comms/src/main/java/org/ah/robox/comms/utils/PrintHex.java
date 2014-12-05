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

/**
 *
 * @author Daniel Sendula
 */
public class PrintHex {

    public static void printHex(byte[] buffer) {
        int readSize = buffer.length;
        int ptr = 0;
        while (ptr < readSize) {
            String adr = Integer.toString(ptr, 16);
            System.out.print("        ".substring(0, 8 - adr.length()));
            System.out.print(adr);
            System.out.print(": ");
            int ptr2 = 0;
            while (ptr + ptr2 < readSize && ptr2 < 16) {
                int b = buffer[ptr + ptr2];
                if (b < 0) { b = 256 + b; }
                String a = Integer.toString(b, 16);
                if (a.length() == 1) { a = "0" + a; }
                System.out.print(a);
                System.out.print(' ');
                if (ptr2 == 7) {
                    System.out.print(' ');
                }
                ptr2 = ptr2 + 1;
            }
            while (ptr2 < 16) {
                System.out.print("   ");
                if (ptr2 == 7) {
                    System.out.print(' ');
                }
                ptr2 = ptr2 + 1;
            }
            System.out.print(" |");

            ptr2 = 0;
            while (ptr + ptr2 < readSize && ptr2 < 16) {
                int b = buffer[ptr + ptr2];
                if (b >= 32 && b < 128) {
                    System.out.print((char)b);
                } else {
                    System.out.print('.');
                }
                ptr2 = ptr2 + 1;
            }
            while (ptr2 < 16) {
                System.out.print(' ');
                ptr2 = ptr2 + 1;
            }
            System.out.println("|");
            ptr = ptr + 16;
        }
    }

}
