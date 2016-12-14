package io.j1st.data.job;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.j1st.data.entity.Registry;
import io.j1st.data.entity.config.BatConfig;
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
    private BatConfig STROAGE_002;

    private double Reg12551;

    @Override
    public void execute(JobExecutionContext context) {
        // mqtt topic
        String topic;
        String agentId = context.getTrigger().getKey().toString().substring(0, 24);
        logger.debug("执行" + agentId);
        logger.info("内存中除配置文件外所有值 MAP:" + Registry.INSTANCE.getValue());
        MqttConnThread mqttConnThread;
        STROAGE_002 = (BatConfig)Registry.INSTANCE.getValue().get(agentId+"_STROAGE_002Config");
        Object batReceive = Registry.INSTANCE.getValue().get(agentId+"storage01");
        if (batReceive != null) {
            Reg12551 = (Double) Registry.INSTANCE.getValue().get(agentId+"storage01");
        }
        GetDataAll dataAll = new GetDataAll(Reg12551, STROAGE_002);
        String msg = dataAll.getDate(agentId);
        mqttConnThread = Registry.INSTANCE.getSession().get(agentId);
        topic = getTopic(agentId);
        if (mqttConnThread != null && mqttConnThread.getMqttClient().isConnected()) {
            // mqttConnThread.sendMessage(topic, msg);
            logger.debug(agentId + "发送的数据为：" + msg);
            //更新间隔时间
            Registry.INSTANCE.saveKey(agentId + "_date", new Date().getTime());
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