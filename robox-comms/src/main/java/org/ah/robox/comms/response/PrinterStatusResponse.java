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
public class PrinterStatusResponse implements Response {

    private String printJob;
    private int lineNumber;
    private PrinterPause pause = PrinterPause.UNKNOWN;
    private boolean busy;

    private boolean xLimit;
    private boolean yLimit;
    private boolean zLimit;

    private boolean filament1;
    private boolean filament2;

    private boolean nozzleSwitch;
    private boolean doorOpen;
    private boolean reelButton;

    private String nozzleTemperature;
    private String nozzleSetTemperature;

    private String bedTemperature;
    private String bedSetTemperature;

    private String ambientTemperature;
    private String ambientSetTemperature;

    private boolean fan;
    private boolean headFan;

    private String xPosition;
    private String yPosition;
    private String zPosition;

    private String filamentMultiplier;
    private String feedRateMultiplier;

    private TemperatureState temperatureState = TemperatureState.WORKING;

    public PrinterStatusResponse() {
    }

    public String toString() {
        return "Status[printJob='" + printJob + "' , lineNumber=" + lineNumber + ", pause=" + pause.getText() + ", isBusy=" + busy + "]";
    }

    public void setPrintJob(String printJob) {
        this.printJob = printJob;
    }

    public String getPrintJob() {
        return printJob;
    }

    public void setLineNumber(int lineNumber) {
        this.lineNumber = lineNumber;
    }

    public int getLineNumber() {
        return lineNumber;
    }

    public PrinterPause getPause() {
        return pause;
    }

    public void setPause(PrinterPause pause) {
        if (pause == null) { throw new NullPointerException("Pause cannot be null"); }
        this.pause = pause;
    }

    public boolean isBusy() {
        return busy;
    }

    public void setBusy(boolean busy) {
        this.busy = busy;
    }

    public boolean isXLimit() {
        return xLimit;
    }

    public void setXLimit(boolean xLimit) {
        this.xLimit = xLimit;
    }

    public boolean isYLimit() {
        return yLimit;
    }

    public void setYLimit(boolean yLimit) {
        this.yLimit = yLimit;
    }

    public boolean isZLimit() {
        return zLimit;
    }

    public void setZLimit(boolean zLimit) {
        this.zLimit = zLimit;
    }

    public boolean isFilament1() {
        return filament1;
    }

    public void setFilament1(boolean filament1) {
        this.filament1 = filament1;
    }

    public boolean isFilament2() {
        return filament2;
    }

    public void setFilament2(boolean filament2) {
        this.filament2 = filament2;
    }

    public boolean isNozzleSwitch() {
        return nozzleSwitch;
    }

    public void setNozzleSwitch(boolean nozzleSwitch) {
        this.nozzleSwitch = nozzleSwitch;
    }

    public boolean isDoorOpen() {
        return doorOpen;
    }

    public void setDoorOpen(boolean doorOpen) {
        this.doorOpen = doorOpen;
    }

    public boolean isReelButton() {
        return reelButton;
    }

    public void setReelButton(boolean reelButton) {
        this.reelButton = reelButton;
    }

    public String getNozzleTemperature() {
        return nozzleTemperature;
    }

    public void setNozzleTemperature(String nozzleTemperature) {
        this.nozzleTemperature = nozzleTemperature;
    }

    public String getNozzleSetTemperature() {
        return nozzleSetTemperature;
    }

    public void setNozzleSetTemperature(String nozzleSetTemperature) {
        this.nozzleSetTemperature = nozzleSetTemperature;
    }

    public String getBedTemperature() {
        return bedTemperature;
    }

    public void setBedTemperature(String bedTemperature) {
        this.bedTemperature = bedTemperature;
    }

    public String getBedSetTemperature() {
        return bedSetTemperature;
    }

    public void setBedSetTemperature(String bedSetTemperature) {
        this.bedSetTemperature = bedSetTemperature;
    }

    public boolean isFan() {
        return fan;
    }

    public void setFan(boolean fan) {
        this.fan = fan;
    }

    public String getAmbientTemperature() {
        return ambientTemperature;
    }

    public void setAmbientTemperature(String ambientTemperature) {
        this.ambientTemperature = ambientTemperature;
    }

    public String getAmbientSetTemperature() {
        return ambientSetTemperature;
    }

    public void setAmbientSetTemperature(String ambientSetTemperature) {
        this.ambientSetTemperature = ambientSetTemperature;
    }

    public boolean isHeadFan() {
        return headFan;
    }

    public void setHeadFan(boolean headFan) {
        this.headFan = headFan;
    }

    public String getXPosition() {
        return xPosition;
    }

    public void setXPosition(String xPosition) {
        this.xPosition = xPosition;
    }

    public String getYPosition() {
        return yPosition;
    }

    public void setYPosition(String yPosition) {
        this.yPosition = yPosition;
    }

    public String getZPosition() {
        return zPosition;
    }

    public void setZPosition(String zPosition) {
        this.zPosition = zPosition;
    }

    public String getFilamentMultiplier() {
        return filamentMultiplier;
    }

    public void setFilamentMultiplier(String filamentMultiplier) {
        this.filamentMultiplier = filamentMultiplier;
    }

    public String getFeedRateMultiplier() {
        return feedRateMultiplier;
    }

    public void setFeedRateMultiplier(String feedRateMultiplier) {
        this.feedRateMultiplier = feedRateMultiplier;
    }

    public TemperatureState getTemperatureState() {
        return temperatureState;
    }

    public void setTemperatureState(TemperatureState temperatureState) {
        this.temperatureState = temperatureState;
    }
}
