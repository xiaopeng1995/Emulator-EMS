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
import java.util.List;


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
        JobTest jobTest=new JobTest(mqtt,options);
//        Query query=new Query();
//        query.setD(0);
//        query.setI(20);
        List<SetMHReg> d=new ArrayList<>();
//        d.add(query);
//        Payload payload=new Payload();
//        payload.setQuery(d);
        SetMHReg setMHReg=new SetMHReg();
        setMHReg.setDsn("AB123456");
        setMHReg.setReg12551(800);
        d.add(setMHReg);
        BatReceive batReceive=new BatReceive();
        batReceive.setSetMHReg(d);
        String msg= JsonUtils.Mapper.writeValueAsString(batReceive);
        System.out.println(msg);
        mqtt.publish("agents/5833e406dafbaf59a0d39671/downstream", new MqttMessage(msg.getBytes("utf-8")));
    }
}
