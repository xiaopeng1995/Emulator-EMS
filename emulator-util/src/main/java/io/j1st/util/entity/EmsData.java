package io.j1st.util.entity;

import io.j1st.util.entity.data.Values;

import java.util.Map;

/**
 * EmsEntity
 */
public class EmsData {
    private String dsn;
    private String asn;
    private String type;
    private String model;
    private Integer modId;
    private Integer sta;
    private Map values;

    public void setDsn(String dsn){
        this.dsn = dsn;
    }
    public String getDsn(){
        return this.dsn;
    }
    public void setType(String type){
        this.type = type;
    }
    public String getType(){
        return this.type;
    }
    public void setValues(Map values){
        this.values = values;
    }
    public Map getValues(){
        return this.values;
    }

    public String getAsn() {
        return asn;
    }

    public void setAsn(String asn) {
        this.asn = asn;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public Integer getSta() {
        return sta;
    }

    public void setSta(Integer sta) {
        this.sta = sta;
    }

    public Integer getModId() {
        return modId;
    }

    public void setModId(Integer modId) {
        this.modId = modId;
    }
}
