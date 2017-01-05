package io.j1st.test;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.j1st.test.jobs.JobTest;
import io.j1st.util.entity.Payload;
import io.j1st.util.entity.bat.BatReceive;
import io.j1st.util.entity.bat.SetMHReg;
import io.j1st.util.entity.payload.Query;
import io.j1st.util.util.JsonUtils;
import org.bson.types.ObjectId;
import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * DownloadEmulator
 */
public class DownloadEmulator {
    // Logger
    private static final Logger logger = LoggerFactory.getLogger(DownloadEmulator.class);

    public static void main(String[] args) throws Exception {
        MemoryPersistence persistence = new MemoryPersistence();
        MqttClient mqtt;
        MqttConnectOptions options;
        /*需要的传参*/
        String agentid = "586b617fdafbaf65c5ef2dd6";
        int type = 1;//1功率,2时间,3告警
        String pack="1,1,1,1,1301";
        /**/
        mqtt = new MqttClient("tcp://139.196.230.150:1883", "dd", persistence);
        options = new MqttConnectOptions();
        mqtt.connect(options);
        Map<String, Object> query = new HashMap<>();
        query.put("D", 0);
        query.put("I", 30);
        List<Map> d1 = new ArrayList<>();
        d1.add(query);
        Map<String, Object> payload = new HashMap<>();
        payload.put("Query", d1);

        List<Map> d = new ArrayList<>();
        Map<String, Object> setMHReg = new HashMap<>();
        setMHReg.put("dsn", agentid + "120");
        setMHReg.put("Reg12551", 66.0);
        d.add(setMHReg);
        Map<String, Object> batReceive = new HashMap<>();
        batReceive.put("SetMHReg", d);

        String batReceivemsg = JsonUtils.Mapper.writeValueAsString(batReceive);
        String payloadmsg = JsonUtils.Mapper.writeValueAsString(payload);
        String packs ="{\"packs\":[{\"packs\":\""+pack+"\"}]}";
        if (type == 1) {
            payloadmsg = batReceivemsg;
            logger.info("batReceivemsg\n" + batReceivemsg);
        } else if(type == 2){
            logger.info("payloadmsg\n" + payloadmsg);
        }
        else
        {
            payloadmsg=packs;
            logger.info("packs\n" + payloadmsg);
        }
        mqtt.publish("agents/" + agentid + "/downstream",payloadmsg.getBytes("utf-8"),0,false);
        mqtt.close();
    }
}
