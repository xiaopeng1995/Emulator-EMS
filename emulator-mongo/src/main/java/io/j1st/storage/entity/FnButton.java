package io.j1st.storage.entity;

import java.util.Date;
import java.util.List;

/**
 * Fn Button Entity
 */
public class FnButton {

    private String id;              //标示项
    private Object code;            //开发者定义的id
    private String alias;           //fn button 别名
    private Integer type;           //fn button web/phone page html
    private List<FnKeyItem> keys;   //fn button 项
    private String comment;         //fn button 描述
    private Date createDate;
    private Date updatedDate;

    public FnButton(){}

    public FnButton(String alias, Integer type, String html, List<FnKeyItem> keys, String comment) {
        this.alias = alias;
        this.type = type;
        this.keys = keys;
        this.comment = comment;
    }

    public String getId() {
        return id;
    }

    public Object getCode() {
        return code;
    }

    public void setCode(Object code) {
        this.code = code;
    }

    public void setId(String id) {
        this.id = id;

    }



    public Date getCreateDate() {
        return createDate;
    }

    public void setCreateDate(Date createDate) {
        this.createDate = createDate;
    }

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public List<FnKeyItem> getKeys() {
        return keys;
    }

    public void setKeys(List<FnKeyItem> keys) {
        this.keys = keys;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public Date getUpdatedDate() {
        return updatedDate;
    }

    public void setUpdatedDate(Date updatedDate) {
        this.updatedDate = updatedDate;
    }
}
