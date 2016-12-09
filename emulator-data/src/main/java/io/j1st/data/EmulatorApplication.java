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

        if (args.length >= 3) {
            productIdConfig = new PropertiesConfiguration(args[0]);
            mongoConfig = new PropertiesConfiguration(args[1]);
            mqttConfig = new PropertiesConfiguration(args[2]);
            STROAGE_002 = new PropertiesConfiguration(args[3]);


        } else {
            STROAGE_002 = new PropertiesConfiguration("config/STROAGE_002.properties");
            productIdConfig = new PropertiesConfiguration("config/product.properties");
            mongoConfig = new PropertiesConfiguration("config/mongo.properties");
            mqttConfig = new PropertiesConfiguration("config/mqtt.properties");
        }

        // Mqtt
        Registry.INSTANCE.saveConfig("STROAGE_002",STROAGE_002);
        MemoryPersistence persistence = new MemoryPersistence();
        MqttClient mqtt;
        MqttConnectOptions options;
        //定时任务开始
        Properties pros = new Properties();
        pros.setProperty("org.quartz.threadPool.threadCount", "500");
        QuartzManager quartzManager = new QuartzManager(new StdSchedulerFactory(pros));
       // quartzManager.addJob("meter_job", "meter_job", "meter_trigger", "meter_trigger", MeterJob.class, "1/59 * * * * ?");
        quartzManager.addJob("bat_job", "bat_job", "bat_trigger", "bat_trigger", BatJob.class, "0/30 * * * * ?");
        //mqttConfig.getString("mqtt.url")
        mqtt = new MqttClient("tcp://139.196.230.150:1883", "5848cacedafbaf35325b70e0", persistence);
        options = new MqttConnectOptions();
        options.setUserName("5848cacedafbaf35325b70e0");
        options.setPassword("hTCxJJkWtGkbVBzLLryEvTvRGzcBKFTm".toCharArray());
        //mqtt
        MqttConnThread mqttConnThread = new MqttConnThread(mqtt, options, quartzManager);
        //保存mqtt连接信息
        Registry.INSTANCE.saveSession("874804605", mqttConnThread);
        //添加新线程到线程池
        Registry.INSTANCE.startThread(mqttConnThread);
        //保存启动时间
        Registry.INSTANCE.saveKey("date", new Date().getTime());
        //起点时间
        Registry.INSTANCE.saveKey("startDate", new Date().getTime());
    }
}
