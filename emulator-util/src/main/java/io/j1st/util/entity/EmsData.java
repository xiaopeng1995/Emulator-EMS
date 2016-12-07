package io.j1st.util.entity;

import io.j1st.util.entity.data.Values;

/**
 * EmsEntity
 */
public class EmsData {
    private String dsn;
    private String asn;
    private String type;

    private Values values;

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
    public void setValues(Values values){
        this.values = values;
    }
    public Values getValues(){
        return this.values;
    }

    public String getAsn() {
        return asn;
    }

    public void setAsn(String asn) {
        this.asn = asn;
    }
}
