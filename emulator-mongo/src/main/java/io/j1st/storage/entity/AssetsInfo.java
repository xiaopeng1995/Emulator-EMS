package io.j1st.storage.entity;

import org.bson.types.ObjectId;

import java.util.Date;
import java.util.List;

/**
 * 资产详细数据对象定义
 * AssetsInfo
 */
public class AssetsInfo {

    private ObjectId id;
    private String productName;
    private String moldName; //设备类型（Inverter等级）
    private String type;//设备型号
    private String alias; //别名
    private Integer assetsType; //类型（0：agent, 1: device）
    private  ObjectId agentId;
    private String sn;     //设备SN
    private List<String> tags;  //标签
    private ObjectId groupId;  //GroupId 关联AssetsGroup表
    private Integer status; //状态： 0：正常， 1：禁用
    private Date createdAt;
    private Date updatedAt;
    private ObjectId userId;
    private ObjectId systemId;
    private ObjectId plantId;
    private Date activatedAt;
    private boolean connected;


    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }

    public ObjectId getUserId() {
        return userId;
    }

    public void setUserId(ObjectId userId) {
        this.userId = userId;
    }

    public ObjectId getId() {
        return id;
    }

    public void setId(ObjectId id) {
        this.id = id;
    }

    public ObjectId getAgentId() {
        return agentId;
    }

    public void setAgentId(ObjectId agentId) {
        this.agentId = agentId;
    }

    public String getSn() {
        return sn;
    }

    public void setSn(String sn) {
        this.sn = sn;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    public ObjectId getGroupId() {
        return groupId;
    }

    public void setGroupId(ObjectId groupId) {
        this.groupId = groupId;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public ObjectId getSystemId() {
        return systemId;
    }

    public void setSystemId(ObjectId systemId) {
        this.systemId = systemId;
    }

    public ObjectId getPlantId() {
        return plantId;
    }

    public void setPlantId(ObjectId plantId) {
        this.plantId = plantId;
    }

    public Date getActivatedAt() {
        return activatedAt;
    }

    public void setActivatedAt(Date activatedAt) {
        this.activatedAt = activatedAt;
    }

    public boolean isConnected() {
        return connected;
    }

    public void setConnected(boolean connected) {
        this.connected = connected;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Integer getAssetsType() {
        return assetsType;
    }

    public void setAssetsType(Integer assetsType) {
        this.assetsType = assetsType;
    }

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public String getMoldName() {
        return moldName;
    }

    public void setMoldName(String moldName) {
        this.moldName = moldName;
    }
}
