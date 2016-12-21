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
        query.put("I", 4);
        List<Map> d1 = new ArrayList<>();
        d1.add(query);
        Map<String, Object> payload = new HashMap<>();
        payload.put("Query",d1);

        List<Map> d = new ArrayList<>();
        Map<String, Object> setMHReg = new HashMap<>();
        setMHReg.put("dsn", "5848cacedafbaf35325b70e0120");
        setMHReg.put("Reg12551", -300.0);
        d.add(setMHReg);
        Map<String, Object> batReceive = new HashMap<>();
        batReceive.put("SetMHReg", d);
        String batReceivemsg = JsonUtils.Mapper.writeValueAsString(batReceive);
        String payloadmsg = JsonUtils.Mapper.writeValueAsString(payload);
        String agent ="5833e406dafbaf59a0d39672";
        System.out.println(agent);
        System.out.println("batReceivemsg\n"+batReceivemsg);
        System.out.println("payloadmsg\n"+payloadmsg);
         mqtt.publish("agents/"+agent+"/downstream", new MqttMessage(payloadmsg.getBytes("utf-8")));

    }
}
