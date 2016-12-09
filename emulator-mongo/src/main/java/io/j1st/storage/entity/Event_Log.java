package io.j1st.storage.entity;

import org.bson.types.ObjectId;

import java.util.Date;

/**
 * Event Log Data Structure
 */
public class Event_Log {

    private ObjectId id;
    private ObjectId user_id;
    private ObjectId product_id;
    private ObjectId agent_id;
    private String handlers;
    private Event_Type action_type;
    private Date action_date;
    private String action_description;
    private String action_data;
    private String reply_data;

    public String getHandlers() {
        return handlers;
    }

    public void setHandlers(String handlers) {
        this.handlers = handlers;
    }

    public ObjectId getId() {
        return id;
    }

    public void setId(ObjectId id) {
        this.id = id;
    }

    public ObjectId getUser_id() {
        return user_id;
    }

    public void setUser_id(ObjectId user_id) {
        this.user_id = user_id;
    }

    public ObjectId getProduct_id() {
        return product_id;
    }

    public void setProduct_id(ObjectId product_id) {
        this.product_id = product_id;
    }

    public ObjectId getAgent_id() {
        return agent_id;
    }

    public void setAgent_id(ObjectId agent_id) {
        this.agent_id = agent_id;
    }

    public Event_Type getAction_type() {
        return action_type;
    }

    public void setAction_type(Event_Type action_type) {
        this.action_type = action_type;
    }

    public Date getAction_date() {
        return action_date;
    }

    public void setAction_date(Date action_date) {
        this.action_date = action_date;
    }

    public String getAction_description() {
        return action_description;
    }

    public void setAction_description(String action_description) {
        this.action_description = action_description;
    }

    public String getAction_data() {
        return action_data;
    }

    public void setAction_data(String action_data) {
        this.action_data = action_data;
    }

    public String getReply_data() {
        return reply_data;
    }

    public void setReply_data(String reply_data) {
        this.reply_data = reply_data;
    }
}
