package io.j1st.data;

import io.j1st.data.entity.Registry;
import io.j1st.data.entity.config.BatConfig;
import io.j1st.data.job.BatJob;
import io.j1st.data.job.Job;
import io.j1st.data.mqtt.MqttConnThread;
import io.j1st.data.quartz.QuartzManager;
import io.j1st.storage.MongoStorage;
import io.j1st.storage.entity.Agent;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.bson.types.ObjectId;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.quartz.impl.StdSchedulerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
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
        PropertiesConfiguration quartzConfig;

        if (args.length >= 4) {
            productIdConfig = new PropertiesConfiguration(args[0]);
            mongoConfig = new PropertiesConfiguration(args[1]);
            mqttConfig = new PropertiesConfiguration(args[2]);
            //quartzConfig = new PropertiesConfiguration(args[4]);


        } else {
            productIdConfig = new PropertiesConfiguration("config/product.properties");
            mongoConfig = new PropertiesConfiguration("config/mongo.properties");
            mqttConfig = new PropertiesConfiguration("config/mqtt.properties");
            //quartzConfig = new PropertiesConfiguration("config/quartz.properties");
        }

        // Mqtt

        MemoryPersistence persistence = new MemoryPersistence();
        MqttClient mqtt;
        MqttConnectOptions options;
//        //定时任务开始
//        QuartzManager quartzManager = new QuartzManager(new StdSchedulerFactory(quartzConfig.getString("config.path")));
//        //mongodb
        MongoStorage mogo = new MongoStorage();
        mogo.init(mongoConfig);
        //  List<Agent> agents = mogo.getAgentsByProductId(new ObjectId(productIdConfig.getString("product_id")));
        List<Agent> agents = mogo.getAgentsByProductId(new ObjectId(productIdConfig.getString("product_id")));

        for (Agent agent : agents) {
            String agentID = agent.getId().toString();
            // quartzManager.addJob(agentID + "_Job", agentID + "_Job", agentID + "_Trigger", agentID + "_Trigger", BatJob.class, "0/10 * * * * ?");
            mqtt = new MqttClient(mqttConfig.getString("mqtt.url"), agentID, persistence);
            options = new MqttConnectOptions();
            options.setUserName(agent.getId().toHexString());
            options.setPassword(agent.getToken().toCharArray());
            Registry.INSTANCE.saveKey(agentID + "_STROAGE_002Config", new BatConfig());
            Job thread = new Job(agentID, 30, "jsonUp");
            thread.start();
            Registry.INSTANCE.saveKey(agentID + "_Job", thread);
            //mqtt
            MqttConnThread mqttConnThread = new MqttConnThread(mqtt, options, null);
            //保存mqtt连接信息
            Registry.INSTANCE.saveSession(agentID, mqttConnThread);
            //添加新线程到线程池
            Registry.INSTANCE.startThread(mqttConnThread);
            //保存启动时间
            Registry.INSTANCE.saveKey(agentID + "_date", new Date().getTime());
        }
        //起点时间
        Registry.INSTANCE.saveKey("startDate", new Date().getTime());
    }
}
