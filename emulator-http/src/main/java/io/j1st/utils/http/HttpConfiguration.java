package io.j1st.utils.http;

import io.dropwizard.Configuration;

import javax.validation.constraints.NotNull;

/**
 * Config
 */
public class HttpConfiguration extends Configuration{
    @NotNull
    private String serverId;

    public String getServerId() {
        return serverId;
    }

    public void setServerId(String serverId) {
        this.serverId = serverId;
    }
}
