package org.ah.robox.comms.response;

public class ReelEEPROMResponse implements Response {

    private int eeprom;
    private String id;
    private String colour;
    private String firstLayerNozzleTemperature;
    private String nozzleTemperature;
    private String firstLayerBedTemperature;
    private String bedTemperature;
    private String ambientTemperature;
    private String filamentSize;
    private String multiplier;
    private String feedRate;
    private String name;
    private String type;

    public int getEEPROM() {
        return eeprom;
    }

    public void setEEPROM(int eeprom) {
        this.eeprom = eeprom;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getColour() {
        return colour;
    }

    public void setColour(String colour) {
        this.colour = colour;
    }

    public String getFirstLayerNozzleTemperature() {
        return firstLayerNozzleTemperature;
    }

    public void setFirstLayerNozzleTemperature(String firstLayerNozzleTemperature) {
        this.firstLayerNozzleTemperature = firstLayerNozzleTemperature;
    }

    public String getNozzleTemperature() {
        return nozzleTemperature;
    }

    public void setNozzleTemperature(String nozzleTemperature) {
        this.nozzleTemperature = nozzleTemperature;
    }

    public String getFirstLayerBedTemperature() {
        return firstLayerBedTemperature;
    }

    public void setFirstLayerBedTemperature(String firstLayerBedTemperature) {
        this.firstLayerBedTemperature = firstLayerBedTemperature;
    }

    public String getBedTemperature() {
        return bedTemperature;
    }

    public void setBedTemperature(String bedTemperature) {
        this.bedTemperature = bedTemperature;
    }

    public String getAmbientTemperature() {
        return ambientTemperature;
    }

    public void setAmbientTemperature(String ambientTemperature) {
        this.ambientTemperature = ambientTemperature;
    }

    public String getFilamentSize() {
        return filamentSize;
    }

    public void setFilamentSize(String filamentSize) {
        this.filamentSize = filamentSize;
    }

    public String getMultiplier() {
        return multiplier;
    }

    public void setMultiplier(String multiplier) {
        this.multiplier = multiplier;
    }

    public String getFeedRate() {
        return feedRate;
    }

    public void setFeedRate(String feedRate) {
        this.feedRate = feedRate;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
