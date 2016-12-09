package io.j1st.storage.entity;

import java.util.Date;

/**
 * GenData类型
 */
public class GenData {
    private int state;
    private String time;
    private Double pVPower;
    private Double eToday;
    private Double car1P;
    private Double car1SOC;
    private Double car2P;
    private Double car2SOC;
    private Double batP;
    private Double batSOC;
    private Double powerG;
    private Double meterG;
    private Double powerT;
    private Double meterT;

    public Double getPowerT() {
        return powerT;
    }

    public void setPowerT(Double powerT) {
        this.powerT = powerT;
    }

    public Double getMeterT() {
        return meterT;
    }

    public void setMeterT(Double meterT) {
        this.meterT = meterT;
    }

    public Double getMeterG() {
        return meterG;
    }

    public void setMeterG(Double meterG) {
        this.meterG = meterG;
    }


    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public Double getpVPower() {
        return pVPower;
    }

    public void setpVPower(Double pVPower) {
        this.pVPower = pVPower;
    }

    public Double geteToday() {
        return eToday;
    }

    public void seteToday(Double eToday) {
        this.eToday = eToday;
    }

    public Double getCar1P() {
        return car1P;
    }

    public void setCar1P(Double car1P) {
        this.car1P = car1P;
    }

    public Double getCar1SOC() {
        return car1SOC;
    }

    public void setCar1SOC(Double car1SOC) {
        this.car1SOC = car1SOC;
    }

    public Double getCar2P() {
        return car2P;
    }

    public void setCar2P(Double car2P) {
        this.car2P = car2P;
    }

    public Double getCar2SOC() {
        return car2SOC;
    }

    public void setCar2SOC(Double car2SOC) {
        this.car2SOC = car2SOC;
    }

    public Double getBatP() {
        return batP;
    }

    public void setBatP(Double batP) {
        this.batP = batP;
    }

    public Double getBatSOC() {
        return batSOC;
    }

    public void setBatSOC(Double batSOC) {
        this.batSOC = batSOC;
    }

    public Double getPowerG() {
        return powerG;
    }

    public void setPowerG(Double powerG) {
        this.powerG = powerG;
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }
}
