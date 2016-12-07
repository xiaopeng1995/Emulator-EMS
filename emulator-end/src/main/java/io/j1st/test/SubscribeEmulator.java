package io.j1st.test;


import io.j1st.test.jobs.JobTest;
import io.j1st.util.entity.Payload;
import io.j1st.util.entity.payload.Query;
import io.j1st.util.util.JsonUtils;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


/**
 * Subscribe订阅
 */
public class SubscribeEmulator {
    private static final Logger logger = LoggerFactory.getLogger(SubscribeEmulator.class);

    public static void main(String[] args) throws Exception {



        MemoryPersistence persistence = new MemoryPersistence();
        MqttClient mqtt;
        MqttConnectOptions options;
        mqtt = new MqttClient("tcp://139.196.230.150:1883", "endSubscribe", persistence);
        options = new MqttConnectOptions();
        ExecutorService es= Executors.newFixedThreadPool(5000);
        JobTest jobTest=new JobTest(mqtt,options);
        es.submit(jobTest);
  }
}
