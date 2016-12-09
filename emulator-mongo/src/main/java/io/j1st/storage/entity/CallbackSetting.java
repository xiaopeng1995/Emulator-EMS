package io.j1st.storage.entity;

import java.util.List;

/**
 * Callback Setting
 */
public class CallbackSetting {

    private String id;

    //回调的地址
    private String url;

    private String contentType;

    //调用时的token,由第三方提供，请求时由第三方验证
    private String secret;

    //状态
    private Boolean active;

    //消息类型
    private List<TriggerType> trigger;

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public List<TriggerType> getTrigger() {
        return trigger;
    }

    public void setTrigger(List<TriggerType> trigger) {
        this.trigger = trigger;
    }
}
