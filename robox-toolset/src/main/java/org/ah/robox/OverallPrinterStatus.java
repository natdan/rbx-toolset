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

    UNKNOWN("Unknown", true, false),
    ERROR("Error", true, false),
    NOT_CONNECTED("Not connected", true, false),
    IDLE("Idle", false, false),
    PREPARING_TO_PRINT("Preparing to print", false, true),
    PRINTING_NO_ESTIMATE("Printing, but don't have estimate yes", false, true),
    PRINTING_NO_ESTIMATE_FILE("Printing, but don't have estimate file", false, true),
    PRINTING("Printing", false, true),
    PAUSING("Pausing", false, true),
    PAUSED("Paused", false, true),
    RESUMING("Resuming", false, true);

    private String text;
    private boolean error;
    private boolean printing;

    private OverallPrinterStatus(String text, boolean error, boolean printing) {
        this.text = text;
        this.error = error;
        this.printing = printing;
    }

    public String getText() {
        return text;
    }

    public boolean isError() {
        return error;
    }

    public boolean isPrinting() {
        return printing;
    }
}
