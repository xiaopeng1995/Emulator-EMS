package io.j1st.storage.entity;


import java.util.Date;
import java.util.Map;

/**
 * Created by Administrator on 2016/5/16.
 */
public class FnKeyItem implements Cloneable {

    private String key;  //param key

    private String alias; // 显示名称

    private Integer dataType; //数据类型

    private Integer method; //method 类型

    private Object defaultValue;  //默认值

    private Object value;  //属性值

    private String unit;  //单位

    private Integer valueType; //值类型（chart时选择的类型最大值 ，最小值 平均数等）

    private Map<String,Object> valid; //验证集合

    private String deviceSn;

    private Integer deviceType;

    private Date updateAt; //更新时间

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public Integer getDataType() {
        return dataType;
    }

    public void setDataType(Integer dataType) {
        this.dataType = dataType;
    }

    public Integer getMethod() {
        return method;
    }

    public void setMethod(Integer method) {
        this.method = method;
    }

    public Object getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(Object defaultValue) {
        this.defaultValue = defaultValue;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public Map<String, Object> getValid() {
        return valid;
    }

    public void setValid(Map<String, Object> valid) {
        this.valid = valid;
    }

    public Integer getValueType() {
        return valueType;
    }

    public void setValueType(Integer valueType) {
        this.valueType = valueType;
    }

    public String getDeviceSn() {
        return deviceSn;
    }

    public void setDeviceSn(String deviceSn) {
        this.deviceSn = deviceSn;
    }

    public Integer getDeviceType() {
        return deviceType;
    }

    public void setDeviceType(Integer deviceType) {
        this.deviceType = deviceType;
    }

    public Date getUpdateAt() {
        return updateAt;
    }

    public void setUpdateAt(Date updateAt) {
        this.updateAt = updateAt;
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }
}
