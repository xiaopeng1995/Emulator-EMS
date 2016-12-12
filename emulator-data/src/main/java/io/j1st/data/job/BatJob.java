package io.j1st.data.job;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.j1st.data.entity.Registry;
import io.j1st.data.mqtt.MqttConnThread;
import io.j1st.util.entity.EmsData;
import io.j1st.util.entity.bat.BatReceive;
import io.j1st.util.entity.data.Values;
import io.j1st.util.util.GttRetainValue;
import io.j1st.util.util.JsonUtils;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.SimpleDateFormat;
import java.util.*;

/**
 * 电池模拟数据工作
 */
public class BatJob implements Job {
    Logger logger = LoggerFactory.getLogger(BatJob.class);
    private PropertiesConfiguration STROAGE_002;

    private double Reg12551;

    @Override
    public void execute(JobExecutionContext context){
        // mqtt topic
        String topic;

        MqttConnThread mqttConnThread;


        STROAGE_002 = Registry.INSTANCE.getConfig().get("STROAGE_002");
        Object batReceive = Registry.INSTANCE.getValue().get("ST123456");
        if (batReceive != null) {
            Reg12551 = (Double) Registry.INSTANCE.getValue().get("ST123456");
        }

        GetDataAll dataAll = new GetDataAll(Reg12551, STROAGE_002);
        String msg = dataAll.getDate();
        mqttConnThread = Registry.INSTANCE.getSession().get("874804605");
        topic = getTopic("5848cacedafbaf35325b70e0");
        if (mqttConnThread != null && mqttConnThread.getMqttClient().isConnected()) {
            mqttConnThread.sendMessage(topic, msg);
            logger.debug("发送的数据为：" + msg);
            //更新间隔时间
            Registry.INSTANCE.saveKey("date", new Date().getTime());
        } else {
            logger.info("MQTT链接信息错误,链接失败");
        }

    }

    /**
     * Get Topic
     */
    private static String getTopic(String agentId) {
        return "agents/" + agentId + "/systemQuery";
    }

}