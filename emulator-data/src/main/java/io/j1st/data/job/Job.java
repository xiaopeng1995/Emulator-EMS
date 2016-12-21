package io.j1st.data.job;

import io.j1st.data.entity.Registry;
import io.j1st.data.entity.config.BatConfig;
import io.j1st.data.mqtt.MqttConnThread;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.concurrent.Callable;
import java.util.jar.Pack200;

/**
 * Created by xiaopeng on 2016/12/14.
 */
public class Job extends Thread {
    Logger logger = LoggerFactory.getLogger(Job.class);
    public volatile boolean exit = false;
    private BatConfig STROAGE_002;
    private String agentId;
    private int time;
    private double Reg12551;
    private String topic;
    public Job(String agentid,int time,String topic){
        this.agentId=agentid;
        this.time=time;
        this.topic=topic;
    }

    public void run() {
        // mqtt topic
        String topic;
        while (!exit) {
            logger.debug("执行线程:" + super.getId());
            logger.debug("内存中除配置文件外所有值 MAP:" + Registry.INSTANCE.getValue());
            MqttConnThread mqttConnThread;
            STROAGE_002 = (BatConfig) Registry.INSTANCE.getValue().get(agentId + "_STROAGE_002Config");
            Object batReceive = Registry.INSTANCE.getValue().get(agentId + "120");
            if (batReceive != null) {
                Reg12551 = (Double) Registry.INSTANCE.getValue().get(agentId + "120");
            }
            GetDataAll dataAll = new GetDataAll(Reg12551, STROAGE_002);
            String msg = dataAll.getDate(agentId);
            mqttConnThread = Registry.INSTANCE.getSession().get(agentId);
            topic = getTopic(agentId);
            if (mqttConnThread != null && mqttConnThread.getMqttClient().isConnected()) {
                mqttConnThread.sendMessage(topic, msg);
                logger.debug(agentId + "发送的数据为：" + msg);
                //更新间隔时间
                Registry.INSTANCE.saveKey(agentId + "_date", new Date().getTime());
            } else {
                logger.info("MQTT链接信息错误,链接失败");
                logger.debug(agentId + "发送的数据为：" + msg);
            }
            try {
                Thread.sleep(time * 1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }
        logger.debug("旧线程:"+super.getId()+"已退出!");
    }

    /**
     * Get Topic
     */
    private  String getTopic(String agentId) {
        logger.debug("Topic:{}",topic);
        return "agents/" + agentId + "/"+topic;
    }
}
