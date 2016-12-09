package io.j1st.storage.entity;

import org.bson.types.ObjectId;

import java.util.Date;

/**
 * Value
 */
public class ValidateCode {

    private ObjectId id;
    private int gtServerStatus;
    private String userId;
    private Date datetime;

    public ObjectId getId() {  return id;   }

    public void setId(ObjectId id) {  this.id = id;  }

    public int getGtServerStatus() {
        return gtServerStatus;
    }

    public void setGtServerStatus(int gtServerStatus) {
        this.gtServerStatus = gtServerStatus;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public Date getDatetime() {
        return datetime;
    }

    public void setDatetime(Date datetime) {
        this.datetime = datetime;
    }
}
