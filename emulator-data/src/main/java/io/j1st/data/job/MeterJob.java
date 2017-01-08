package io.j1st.data.job;


import io.j1st.data.entity.Registry;
import io.j1st.data.mqtt.MqttConnThread;
import io.j1st.util.util.GetJsonEmsData;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Upstream EmsJob
 */
public class MeterJob implements Job {
    Logger logger = LoggerFactory.getLogger(MeterJob.class);

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        // mqtt topic
        String topic;

        MqttConnThread mqttConnThread;
        try {
            Thread.sleep(300);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
//        logger.debug("开始发数据" + Registry.INSTANCE.getValue().get("test"));
        mqttConnThread = Registry.INSTANCE.getSession().get("874804605");
        topic = getTopic();
        if (mqttConnThread != null && mqttConnThread.getMqttClient().isConnected()) {
            mqttConnThread.sendMessage(topic, GetJsonEmsData.getData(null,null,null));
            logger.debug("发送的数据为：" + GetJsonEmsData.getData(null,null,null));
        } else {
            logger.info("MQTT链接信息错误,链接失败");
        }

    }

    /**
     * Get Topic
     */
    private static String getTopic() {
        return "jsonUp";
    }

}
