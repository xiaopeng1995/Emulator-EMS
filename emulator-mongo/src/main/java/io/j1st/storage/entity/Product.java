package io.j1st.storage.entity;

import org.bson.types.ObjectId;

import java.util.Date;
import java.util.List;

/**
 * Product
 */
public class Product {

    private ObjectId id;
    private ObjectId userId;
    private String name;
    private String token;
    private ProductScopes scopes;
    private ProductStatus status;
    private ProductSettings settings;
    private List<FnButton> fnx;
    private Date updatedAt;

    public ProductScopes getScopes() {
        return scopes;
    }

    public void setScopes(ProductScopes scopes) {
        this.scopes = scopes;
    }

    public ObjectId getId() {
        return id;
    }

    public void setId(ObjectId id) {
        this.id = id;
    }

    public ObjectId getUserId() {
        return userId;
    }

    public void setUserId(ObjectId userId) {
        this.userId = userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public ProductStatus getStatus() {
        return status;
    }

    public void setStatus(ProductStatus status) {
        this.status = status;
    }

    public ProductSettings getSettings() {
        return settings;
    }

    public void setSettings(ProductSettings settings) {
        this.settings = settings;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }

    public List<FnButton> getFnx() {
        return fnx;
    }

    public void setFnx(List<FnButton> fnx) {
        this.fnx = fnx;
    }

    public boolean isFieldInList(DeviceType type, String key) {
        return settings != null && settings.getFields() != null && settings.getFields().stream() !=null && settings.getFields().stream()
                .filter(field -> field.getDeviceType() == type && field.getKey() != null && field.getKey().equals(key) && field.isInList())
                .findAny()
                .isPresent();
    }

    public boolean isFieldInChart(DeviceType type, String key) {
        return settings != null && settings.getFields() != null && settings.getFields().stream() !=null && settings.getFields().stream()
                .filter(field -> field.getDeviceType() == type && field.getKey() != null && field.getKey().equals(key) && field.isInChart())
                .findAny()
                .isPresent();
    }

    //判断是否配置了某个key的LineChart
    public boolean isLineChart( String key) {
        if(fnx != null && fnx.size() > 0) {
            for(FnButton button : fnx){
                if(!button.getType().equals(FnTypeEnum.FN_LINE_CHART.value())){
                    continue;
                }
                for(FnKeyItem item : button.getKeys()){
                    if(item.getKey().equals(key)){
                        return true;
                    }
                }
            }
        }
        return false;
    }


    //判断是否配置了某个key的FnMessage
    public boolean isFnMessage( String key) {
        if(fnx != null && fnx.size() > 0) {
            for(FnButton button : fnx){
                if(!button.getType().equals(FnTypeEnum.FN_MESSAGE.value())){
                    continue;
                }
                for(FnKeyItem item : button.getKeys()){
                    if(item.getKey().equals(key)){
                        return true;
                    }
                }
            }
        }
        return false;
    }
}
