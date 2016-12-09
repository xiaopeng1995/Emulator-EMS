package io.j1st.storage.entity;

/**
 * Predefined Field Setting
 */
public class FieldSetting {

    private DeviceType deviceType;
    private String key;
    private String description;
    private DataFormat dataFormat;
    private String unit;
    private boolean inChart;
    private boolean inList;
    private boolean undefined;
    private int edit;

    public DeviceType getDeviceType() {
        return deviceType;
    }

    public void setDeviceType(DeviceType deviceType) {
        this.deviceType = deviceType;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public DataFormat getDataFormat() {
        return dataFormat;
    }

    public void setDataFormat(DataFormat dataFormat) {
        this.dataFormat = dataFormat;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public boolean isInChart() {
        return inChart;
    }

    public void setInChart(boolean inChart) {
        this.inChart = inChart;
    }

    public boolean isInList() {
        return inList;
    }

    public void setInList(boolean inList) {
        this.inList = inList;
    }

    public boolean isUndefined() {
        return undefined;
    }

    public void setUndefined(boolean undefined) {
        this.undefined = undefined;
    }

    public int getEdit() {
        return edit;
    }

    public void setEdit(int edit) {
        this.edit = edit;
    }

    @Override
    public String toString() {
        return "FieldSetting{" +
                "deviceType=" + deviceType +
                ", key='" + key + '\'' +
                ", description='" + description + '\'' +
                ", dataFormat=" + dataFormat +
                ", unit='" + unit + '\'' +
                ", inChart=" + inChart +
                ", inList=" + inList +
                ", undefined=" + undefined +
                ", edit=" + edit +
                '}';
    }
}
