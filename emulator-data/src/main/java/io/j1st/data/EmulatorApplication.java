package io.j1st.data;

import io.j1st.data.entity.Registry;
import io.j1st.data.job.BatJob;
import io.j1st.data.mqtt.MqttConnThread;
import io.j1st.data.quartz.QuartzManager;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.quartz.impl.StdSchedulerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;


/**
 * MeterEmulator启动
 */
public class EmulatorApplication {
    private static final Logger logger = LoggerFactory.getLogger(EmulatorApplication.class);

    public static void main(String[] args) throws Exception {
        logger.debug("Starting data emulator module ...");

        // load config
        logger.debug("Loading product id by config files ...");

        PropertiesConfiguration productIdConfig;
        PropertiesConfiguration mongoConfig;
        PropertiesConfiguration mqttConfig;
        PropertiesConfiguration STROAGE_002;
        PropertiesConfiguration quartzConfig;

        if (args.length >= 4) {
            productIdConfig = new PropertiesConfiguration(args[0]);
            mongoConfig = new PropertiesConfiguration(args[1]);
            mqttConfig = new PropertiesConfiguration(args[2]);
            STROAGE_002 = new PropertiesConfiguration(args[3]);
            quartzConfig = new PropertiesConfiguration(args[4]);


        } else {
            STROAGE_002 = new PropertiesConfiguration("config/STROAGE_002.properties");
            productIdConfig = new PropertiesConfiguration("config/product.properties");
            mongoConfig = new PropertiesConfiguration("config/mongo.properties");
            mqttConfig = new PropertiesConfiguration("config/mqtt.properties");
            quartzConfig = new PropertiesConfiguration("config/quartz.properties");
        }

        // Mqtt
        Registry.INSTANCE.saveConfig("STROAGE_002", STROAGE_002);
        MemoryPersistence persistence = new MemoryPersistence();
        MqttClient mqtt;
        MqttConnectOptions options;
        //定时任务开始
        QuartzManager quartzManager = new QuartzManager(new StdSchedulerFactory(quartzConfig.getString("config.path")));
        for (int i = 0; i < 2; i++) {
            String agentID = productIdConfig.getString("agent_ID" + i);
            quartzManager.addJob(agentID + "_Job", agentID + "_Job", agentID + "_Trigger", agentID + "_Trigger", BatJob.class, "0/3 * * * * ?");
            mqtt = new MqttClient(mqttConfig.getString("mqtt.url"), agentID, persistence);
            options = new MqttConnectOptions();
            options.setUserName(agentID);
            if (i == 1)
                options.setPassword("sWrIOuytduErHAjVbLBQyUjhEDtRNmiJ".toCharArray());
            else
                options.setPassword("eheYOJNklYBkubBhDfXSaBXNJyvywDvX".toCharArray());
            //mqtt
            MqttConnThread mqttConnThread = new MqttConnThread(mqtt, options, quartzManager);
            //保存mqtt连接信息
            Registry.INSTANCE.saveSession(agentID, mqttConnThread);
            //添加新线程到线程池
            Registry.INSTANCE.startThread(mqttConnThread);
            //保存启动时间
            Registry.INSTANCE.saveKey(agentID+"_date", new Date().getTime());
        }
        //起点时间
        Registry.INSTANCE.saveKey("startDate", new Date().getTime());
    }
}
