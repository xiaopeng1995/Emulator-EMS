package io.j1st.data.entity;


import io.j1st.data.mqtt.MqttConnThread;

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

    // get session
    public Map<String, MqttConnThread> getSession() {
        return this.map;
    }

    // get Value
    public Map<String, Object> getValue() {
        return this.value;
    }

    // delete Session
    public void deleteSession(String agentId) {
        this.map.remove(agentId);
    }

    // Start Thread
    public void startThread(MqttConnThread client) {
        this.es.submit(client);
    }
}
