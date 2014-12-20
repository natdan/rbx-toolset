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

/**
 *
 *
 * @author Daniel Sendula
 */
public enum OverallPrinterStatus {

    UNKNOWN("Unknown"),
    ERROR("Error"),
    NOT_CONNECTED("Not connected"),
    IDLE("Idle"),
    PREPARING_TO_PRINT("Preparing to print"),
    PRINTING_NO_ESTIMATE("Printing, but don't have estimate yes"),
    PRINTING_NO_ESTIMATE_FILE("Printing, but don't have estimate file"),
    PRINTING("Printing"),
    PAUSING("Pausing"),
    PAUSED("Paused"),
    RESUMING("Resuming");

    private String text;

    private OverallPrinterStatus(String text) {
        this.text = text;
    }

    public String getText() {
        return text;
    }
}
