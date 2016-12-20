package io.j1st.data.entity;


import io.j1st.data.job.Job;
import io.j1st.data.mqtt.MqttConnThread;
import org.apache.commons.configuration.PropertiesConfiguration;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Mqtt 客户端连接注册表
 */
public enum Registry {

    // instance
    INSTANCE;

    public Map<String, MqttConnThread> map = new ConcurrentHashMap<>();
    public Map<String, PropertiesConfiguration> config = new ConcurrentHashMap<>();
    public Map<String, Object> value = new ConcurrentHashMap<>();

    // connect pool
    private final ExecutorService es = Executors.newFixedThreadPool(5000);

    // save session
    public void saveSession(String agentId, MqttConnThread client) {
        this.map.put(agentId, client);
    }

    // save 配置值
    public void saveKey(String key, Object value) {
        this.value.put(key, value);
    }

    // save 配置文件值
    public void saveConfig(String key, PropertiesConfiguration value) {
        this.config.put(key, value);
    }

    // get session
    public Map<String, MqttConnThread> getSession() {
        return this.map;
    }

    /**
     *
     * @return
     * AgentID_TotWhExp,AgentID_Job,AgentID_TotWhImp,AgentID_Soc,AgentIDstorage01
     * AgentID_STROAGE_002Config,AgentID_date,startDate,AgentID_packing
     * agents
     * */
    public Map<String, Object> getValue() {
        return this.value;
    }

    //get config
    public Map<String, PropertiesConfiguration> getConfig() {
        return this.config;
    }

    // delete Session
    public void deleteSession(String agentId) {
        this.map.remove(agentId);
    }

    // Start Thread
    public void startThread(MqttConnThread client) {
        this.es.submit(client);
    }
    public void startJob(Job job) {
        this.es.submit(job);

    }
    public void shutdown() {
        this.es.shutdown();
    }
}
