package io.j1st.storage.entity;

import java.util.Map;

/**
 * Upstream or Downstream
 */
public class Stream {

    private String asn;
    private String dsn;
    private DeviceType type;
    private int direction;  //功率方向（0: none, 1: in , 2: out）储能侧和电网侧有此字段
   // private long dtime;
    private String model;
    private Map<String, Object> values;

    public String getAsn() {
        return asn;
    }

    public void setAsn(String asn) {
        this.asn = asn;
    }

    public String getDsn() {
        return dsn;
    }

    public void setDsn(String dsn) {
        this.dsn = dsn;
    }

    public DeviceType getType() {
        return type;
    }

    public void setType(DeviceType type) {
        this.type = type;
    }

//    public long getDtime() {
//        return dtime;
//    }
//
//    public void setDtime(long dtime) {
//        this.dtime = dtime;
//    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public Map<String, Object> getValues() {
        return values;
    }

    public void setValues(Map<String, Object> values) {
        this.values = values;
    }

    public int getDirection() {
        return direction;
    }

    public void setDirection(int direction) {
        this.direction = direction;
    }
}
