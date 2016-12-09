package io.j1st.storage.entity;

import org.bson.types.ObjectId;

import java.util.List;

/**
 * User
 */
public class FnMessage {

    private ObjectId id;
    private ObjectId agentId;
    List<FnMessageItem> items;

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

    public List<FnMessageItem> getItems() {
        return items;
    }

    public void setItems(List<FnMessageItem> items) {
        this.items = items;
    }
}
