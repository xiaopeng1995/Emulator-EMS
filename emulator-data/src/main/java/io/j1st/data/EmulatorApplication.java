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

import org.bson.Document;
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

        if (args.length >= 3) {
            productIdConfig = new PropertiesConfiguration(args[0]);
            mongoConfig = new PropertiesConfiguration(args[1]);
            mqttConfig = new PropertiesConfiguration(args[2]);
            // quartzConfig = new PropertiesConfiguration(args[4]);


        } else {
            productIdConfig = new PropertiesConfiguration("config/product_agent.properties");
            mongoConfig = new PropertiesConfiguration("config/mongo.properties");
            mqttConfig = new PropertiesConfiguration("config/mqtt.properties");
            //quartzConfig = new PropertiesConfiguration("config/quartz.properties");
        }
        //mqtt
        MemoryPersistence persistence = new MemoryPersistence();
        MqttClient mqtt;
        MqttConnectOptions options;
        //mongodb
        MongoStorage mogo = new MongoStorage();
        mogo.init(mongoConfig);
        DataMongoStorage dmogo = new DataMongoStorage();
        dmogo.init(mongoConfig);
        //
        Registry.INSTANCE.saveKey("dmogo", dmogo);
        Registry.INSTANCE.saveKey("mogo", mogo);
//        //timing thread (Trigger twelve o 'clock every day)
//        QuartzManager quartzManager = new QuartzManager(new StdSchedulerFactory(quartzConfig.getString("config.path")));
//        quartzManager.addJob("day_Job", "day_Job", "day_Trigger", "dat_Trigger", DayJob.class, "0 0 0 * * ?");
        String[] productIds ;
        String[] agentIds ;
        try {
            agentIds = productIdConfig.getString("agent_id").split("_");
        } catch (NullPointerException e) {
            agentIds = new String[0];
        }
        try {
            productIds = productIdConfig.getString("product_id").split("_");
        } catch (NullPointerException e) {
            productIds = new String[0];

        }
        PVpredict pVpredict = new PVpredict(dmogo);
        SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmmss");
        String date = format.format(new Date());

        List<String> agentIdAll = new ArrayList<>();
        int n = productIds.length > agentIds.length ? productIds.length : agentIds.length;
        int agunt = 0;
        //start a job thread
        Registry.INSTANCE.saveKey("startDate", new Date().getTime());
        for (int i = 0; i < n; i++) {
            List<Agent> agents = new ArrayList<>();
            if (i < productIds.length)
                agents = mogo.getAgentsByProductId(new ObjectId(productIds[i]));
            if (i < agentIds.length)
                agents.add(mogo.getAgentsById(new ObjectId(agentIds[i])));
            for (Agent agent : agents) {
                agunt++;
                String agentID = agent.getId().toString();
                agentIdAll.add(agentID);
                mqtt = new MqttClient(mqttConfig.getString("mqtt.url"), agent.getId().toHexString(), persistence);
                options = new MqttConnectOptions();
                options.setUserName(agent.getId().toHexString());
                options.setPassword(agent.getToken().toCharArray());
                //add now data
                if (dmogo.findGendDataByTime(agentID, "pVPower") == null)
                    pVpredict.PVInfo(date.substring(0, 8) + "000000", agentID, 1, pvcloud());
                //add predict data
                if (dmogo.findycdata(agentID, Integer.parseInt(date.substring(0, 8)))) {
                    pVpredict.PVInfo(date.substring(0, 8) + "000000", agentID, 0, pvcloud());

                }

                //save a job config
                Registry.INSTANCE.saveKey(agentID + "_STROAGE_002Config", new BatConfig());
                //mqtt
                MqttConnThread mqttConnThread = new MqttConnThread(mqtt, options, null, mogo, dmogo);
                //seve mqtt info
                Registry.INSTANCE.saveSession(agentID, mqttConnThread);
                //add a agent mqtt Send and receive sever
                Registry.INSTANCE.startThread(mqttConnThread);
                Thread.sleep(90);
                //设置间隔时间
                Registry.INSTANCE.saveKey(agentID + "_jgtime", 300);
                //防止MQTT先启动线程做判断
                if (Registry.INSTANCE.getValue().get(agentID + "_Job") == null) {
                    Job thread = new Job(agentID, "jsonUp", mogo, dmogo);
                    Registry.INSTANCE.startJob(thread);
                    Registry.INSTANCE.saveKey(agentID + "_Job", thread);
                    logger.debug(agentID + "准备成功开始上传数据..");
                }
            }
        }
        Registry.INSTANCE.saveKey("agentIdAll", agentIdAll);
        logger.info("启动完毕,本次启动共{}个Agent任务", agunt);

    }

    //太阳能云因子
    private static int[] pvcloud() {
        int[] cCloud = new int[8];
        int ran = (int) (Math.random() * 10);
        cCloud[0] = ran > 5 ? 1 : ran > 3 ? 2 : ran > 2 ? 3 : 4;
        cCloud[1] = ran > 5 ? 3 : ran > 3 ? 2 : ran > 2 ? 1 : 5;
        cCloud[2] = ran > 5 ? 0 : ran > 3 ? 1 : ran > 2 ? 2 : 3;
        ran = (int) (Math.random() * 10);
        cCloud[3] = ran > 5 ? 0 : ran > 3 ? 1 : ran > 2 ? 3 : 2;
        cCloud[4] = ran > 5 ? 0 : ran > 3 ? 1 : ran > 2 ? 2 : 3;
        cCloud[5] = ran > 5 ? 6 : ran > 3 ? 5 : ran > 2 ? 4 : 7;
        ran = (int) (Math.random() * 10);
        cCloud[6] = ran > 5 ? 6 : ran > 3 ? 5 : ran > 2 ? 4 : 3;
        cCloud[7] = ran > 5 ? 3 : ran > 3 ? 4 : ran > 2 ? 1 : 2;
        return cCloud;
    }
}
