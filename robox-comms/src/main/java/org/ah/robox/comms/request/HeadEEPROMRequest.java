package org.ah.robox.comms.request;

import org.ah.robox.comms.response.HeadEEPROMResponse;

public class HeadEEPROMRequest {

    private int eeprom;
    private String headType;
    private String headId;
    private String maxTemperature;
    private String beta;
    private String tcal;
    private String x0;
    private String y0;
    private String z0;
    private String b0;
    private String x1;
    private String y1;
    private String z1;
    private String b1;
    private String lastTemperature1;
    private String lastTemperature0;
    private String hours;
    private String filament0;
    private String filament1;

    public void copyFromResponse(HeadEEPROMResponse response) {
        this.headType = response.getHeadType();
        this.headId = response.getHeadId();
        this.maxTemperature = response.getMaxTemperature();
        this.beta = response.getBeta();
        this.tcal = response.getTcal();
        this.x0 = response.getX0();
        this.y0 = response.getY0();
        this.z0 = response.getZ0();
        this.b0 = response.getB0();
        this.x1 = response.getX1();
        this.y1 = response.getY1();
        this.z1 = response.getZ1();
        this.b1 = response.getB1();
        this.lastTemperature0 = response.getLastTemperature0();
        this.lastTemperature1 = response.getLastTemperature1();
        this.hours = response.getHours();
        this.filament0 = "";
        this.filament1 = "";
    }

    public int getEEPROM() {
        return eeprom;
    }

    public void setEEPROM(int eeprom) {
        this.eeprom = eeprom;
    }

    public int getEeprom() {
        return eeprom;
    }

    public void setEeprom(int eeprom) {
        this.eeprom = eeprom;
    }

    public String getHeadType() {
        return headType;
    }

    public void setHeadType(String headType) {
        this.headType = headType;
    }

    public String getHeadId() {
        return headId;
    }

    public void setHeadId(String headId) {
        this.headId = headId;
    }

    public String getMaxTemperature() {
        return maxTemperature;
    }

    public void setMaxTemperature(String maxTemperature) {
        this.maxTemperature = maxTemperature;
    }

    public String getBeta() {
        return beta;
    }

    public void setBeta(String beta) {
        this.beta = beta;
    }

    public String getTcal() {
        return tcal;
    }

    public void setTcal(String tcal) {
        this.tcal = tcal;
    }

    public String getX0() {
        return x0;
    }

    public void setX0(String x0) {
        this.x0 = x0;
    }

    public String getY0() {
        return y0;
    }

    public void setY0(String y0) {
        this.y0 = y0;
    }

    public String getZ0() {
        return z0;
    }

    public void setZ0(String z0) {
        this.z0 = z0;
    }

    public String getB0() {
        return b0;
    }

    public void setB0(String b0) {
        this.b0 = b0;
    }

    public String getX1() {
        return x1;
    }

    public void setX1(String x1) {
        this.x1 = x1;
    }

    public String getY1() {
        return y1;
    }

    public void setY1(String y1) {
        this.y1 = y1;
    }

    public String getZ1() {
        return z1;
    }

    public void setZ1(String z1) {
        this.z1 = z1;
    }

    public String getB1() {
        return b1;
    }

    public void setB1(String b1) {
        this.b1 = b1;
    }

    public String getLastTemperature1() {
        return lastTemperature1;
    }

    public void setLastTemperature1(String lastTemperature1) {
        this.lastTemperature1 = lastTemperature1;
    }

    public String getLastTemperature0() {
        return lastTemperature0;
    }

    public void setLastTemperature0(String lastTemperature0) {
        this.lastTemperature0 = lastTemperature0;
    }

    public String getHours() {
        return hours;
    }

    public void setHours(String hours) {
        this.hours = hours;
    }

    public String getFilament0() {
        return filament0;
    }

    public void setFilament0(String filament0) {
        this.filament0 = filament0;
    }

    public String getFilament1() {
        return filament1;
    }

    public void setFilament1(String filament1) {
        this.filament1 = filament1;
    }
}
