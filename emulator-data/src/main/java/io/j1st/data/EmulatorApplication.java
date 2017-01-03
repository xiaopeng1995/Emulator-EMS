package io.j1st.data;

import io.j1st.data.entity.Registry;
import io.j1st.data.entity.config.BatConfig;
import io.j1st.data.job.DayJob;
import io.j1st.data.job.Job;
import io.j1st.data.mqtt.MqttConnThread;

import io.j1st.data.predict.PVpredict;
import io.j1st.data.quartz.QuartzManager;
import io.j1st.storage.DataMongoStorage;
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
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


/**
 * MeterEmulator启动
 */
public class EmulatorApplication {
    private static final Logger logger = LoggerFactory.getLogger(EmulatorApplication.class);

    public static void main(String[] args) throws Exception {
        PropertiesConfiguration productIdConfig;
        PropertiesConfiguration mongoConfig;
        PropertiesConfiguration mqttConfig;
        PropertiesConfiguration quartzConfig;

        if (args.length >= 4) {
            productIdConfig = new PropertiesConfiguration(args[0]);
            mongoConfig = new PropertiesConfiguration(args[1]);
            mqttConfig = new PropertiesConfiguration(args[2]);
            quartzConfig = new PropertiesConfiguration(args[4]);


        } else {
            productIdConfig = new PropertiesConfiguration("config/product.properties");
            mongoConfig = new PropertiesConfiguration("config/mongo.properties");
            mqttConfig = new PropertiesConfiguration("config/mqtt.properties");
            quartzConfig = new PropertiesConfiguration("config/quartz.properties");
        }
        // Mqtt

        MemoryPersistence persistence = new MemoryPersistence();
        MqttClient mqtt;
        MqttConnectOptions options;
        //mongodb
        MongoStorage mogo = new MongoStorage();
        mogo.init(mongoConfig);
        DataMongoStorage dmogo = new DataMongoStorage();
        dmogo.init(mongoConfig);
        Registry.INSTANCE.saveKey("dmogo", dmogo);
        Registry.INSTANCE.saveKey("mogo", mogo);
        //定时任务开始
        QuartzManager quartzManager = new QuartzManager(new StdSchedulerFactory(quartzConfig.getString("config.path")));
        //定时每天算当天功率0 0 0 * * ?
        quartzManager.addJob("day_Job", "day_Job", "day_Trigger", "dat_Trigger", DayJob.class, "0 0 0 * * ?");
        String[] productIds = null;
        String[] agentIds = null;
        try {
            productIds = productIdConfig.getString("product_id").split("_");
            agentIds = productIdConfig.getString("agent_id").split("_");
        } catch (NullPointerException e) {
            productIds = productIds == null ? new String[0] : productIds;
            agentIds = agentIds == null ? new String[0] : agentIds;
        }
        SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmmss");
        String date = format.format(new Date());
        List<String> agentIdAll = new ArrayList<>();
        int n = productIds.length > agentIds.length ? productIds.length : agentIds.length;
        for (int i = 0; i < n; i++) {
            List<Agent> agents = new ArrayList<>();
            if (i < productIds.length)
                agents = mogo.getAgentsByProductId(new ObjectId(productIds[i]));
            if (i < agentIds.length)
                agents.add(mogo.getAgentsById(new ObjectId(agentIds[i])));
            for (Agent agent : agents) {
                String agentID = agent.getId().toString();
                agentIdAll.add(agentID);
                mqtt = new MqttClient(mqttConfig.getString("mqtt.url"), agent.getId().toHexString(), persistence);
                options = new MqttConnectOptions();
                options.setUserName(agent.getId().toHexString());
                options.setPassword(agent.getToken().toCharArray());
                //添加预测数据
                if (dmogo.findycdata(agentID, Integer.parseInt(date.substring(0, 8)))) {
                    PVpredict p = new PVpredict(dmogo);
                    p.PVInfo(date.substring(0, 8) + "000000", agentID, 0);
                }
                Registry.INSTANCE.saveKey(agentID + "_STROAGE_002Config", new BatConfig());
                Job thread = new Job(agentID, 30, "jsonUp", mogo, dmogo);
                Registry.INSTANCE.startJob(thread);
                Registry.INSTANCE.saveKey(agentID + "_Job", thread);
                //mqtt
                MqttConnThread mqttConnThread = new MqttConnThread(mqtt, options, null, mogo, dmogo);
                //保存mqtt连接信息
                Registry.INSTANCE.saveSession(agentID, mqttConnThread);
                //添加新线程到线程池
                Registry.INSTANCE.startThread(mqttConnThread);
                //保存启动时间
                Registry.INSTANCE.saveKey(agentID + "_date", new Date().getTime());
                Thread.sleep(80);
            }
        }
        Registry.INSTANCE.saveKey("agentIdAll", agentIdAll);
        //起点时间
        Registry.INSTANCE.saveKey("startDate", new Date().getTime());
        logger.info("Service has been ready!");
    }
}
