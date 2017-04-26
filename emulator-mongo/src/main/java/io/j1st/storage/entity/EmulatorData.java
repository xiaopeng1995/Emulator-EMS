package io.j1st.storage.entity;

import org.bson.types.ObjectId;

/**
 * 数据库储存数据类型
 */
public class EmulatorData {
    private ObjectId id;
    private String agent_id;
    private int systemTpye;

    public ObjectId getId() {
        return id;
    }

    public void setId(ObjectId id) {
        this.id = id;
    }

    public String getAgent_id() {
        return agent_id;
    }

    public void setAgent_id(String agent_id) {
        this.agent_id = agent_id;
    }

    public int getSystemTpye() {
        return systemTpye;
    }

    public void setSystemTpye(int systemTpye) {
        this.systemTpye = systemTpye;
    }
}
