package io.j1st.storage.entity;

import java.util.Date;

/**
 * Created by Administrator on 2016/6/4.
 */
public class FnMessageItem {

    String id; //标示列

    String key; // attribute名

    String message; //消息内容

    Date createAt;//创建时间

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Date getCreateAt() {
        return createAt;
    }

    public void setCreateAt(Date createAt) {
        this.createAt = createAt;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }
}
