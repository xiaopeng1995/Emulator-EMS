package io.j1st.test;

import io.j1st.test.jobs.JobTest;
import io.j1st.util.entity.Payload;
import io.j1st.util.entity.bat.BatReceive;
import io.j1st.util.entity.bat.SetMHReg;
import io.j1st.util.entity.payload.Query;
import io.j1st.util.util.JsonUtils;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * DownloadEmulator
 */
public class DownloadEmulator {

    public static void main(String[] args) throws Exception {
        MemoryPersistence persistence = new MemoryPersistence();
        MqttClient mqtt;
        MqttConnectOptions options;
        mqtt = new MqttClient("tcp://139.196.230.150:1883", "endDownload", persistence);
        options = new MqttConnectOptions();
        mqtt.connect(options);
        mqtt.setTimeToWait(2000);
        Map<String, Object> query = new HashMap<>();
        query.put("D", 0);
        query.put("I", 10);
        List<Map> d1 = new ArrayList<>();
        d1.add(query);
        Map<String, Object> payload = new HashMap<>();
        payload.put("Query",d1);

        List<Map> d = new ArrayList<>();
        Map<String, Object> setMHReg = new HashMap<>();
        setMHReg.put("dsn", "AB123456");
        setMHReg.put("Reg12551", 30.0);
        d.add(setMHReg);
        Map<String, Object> batReceive = new HashMap<>();
        batReceive.put("SetMHReg", d);
        String msg1 = JsonUtils.Mapper.writeValueAsString(batReceive);
        String msg2 = JsonUtils.Mapper.writeValueAsString(payload);
        System.out.println(msg1);
        System.out.println(msg2);
         mqtt.publish("agents/5833e406dafbaf59a0d39671/downstream", new MqttMessage(msg2.getBytes("utf-8")));
    }
}
