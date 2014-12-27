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
 *
 * @author Daniel Sendula
 */
public enum TemperatureState {

    WORKING(0, "Working"),
    COOLING(1, "Cooling"),
    HEATING_BED(2, "Heating bed"),
    HEATING_NOZZLES(3, "Heating nozzles");

    private int state;
    private String text;

    private TemperatureState(int state, String text) {
        this.state = state;
        this.text = text;
    }

    public int getState() {
        return state;
    }

    public String getText() {
        return text;
    }

}
