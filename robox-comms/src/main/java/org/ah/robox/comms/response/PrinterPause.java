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
package org.ah.robox.comms.response;

/**
*
* @author Daniel Sendula
*/
public enum PrinterPause {

    UNKNOWN(-1, "Unknown"),
    WORKING(0, "Working"),
    PAUSING(1, "Pausing"),
    PAUSED(2, "Paused"),
    RESUMING(3, "Resuming");

    private int value;
    private String text;

    private PrinterPause(int value, String text) {
        this.value = value;
        this.text = text;
    }

    public int getValue() {
        return value;
    }

    public String getText() {
        return text;
    }
}
