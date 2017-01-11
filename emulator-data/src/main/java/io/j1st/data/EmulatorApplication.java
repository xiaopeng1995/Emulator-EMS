package io.j1st.data;

import io.j1st.data.entity.Registry;
import io.j1st.data.entity.config.BatConfig;
import io.j1st.data.job.EmsJob;
import io.j1st.data.job.PVjob;
import io.j1st.data.mqtt.MqttConnThread;

import io.j1st.data.predict.PVpredict;
import io.j1st.storage.DataMongoStorage;
import io.j1st.storage.MongoStorage;
import io.j1st.storage.entity.Agent;

import org.apache.commons.configuration.PropertiesConfiguration;

import org.bson.types.ObjectId;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
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
        //start a job thread
        Registry.INSTANCE.saveKey("startDate", new Date().getTime());

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

        /***********************************/
        //mqtt
//        String serverid = productIdConfig.getString("sever_id");
//        mqtt = new MqttClient(mqttConfig.getString("mqtt.url"),
//                new ObjectId(serverid).toHexString(), persistence);
//        options = new MqttConnectOptions();
//        options.setUserName(new ObjectId(serverid).toHexString());
//        options.setPassword("qOSWXgJcZFNCuVjhkHtAHfyqbujuayHy".toCharArray());
//        MqttConnThread mqttConnThread = new MqttConnThread(mqtt, options, null, mogo, dmogo);
//        //seve mqtt info
//        Registry.INSTANCE.saveSession(serverid, mqttConnThread);
//        //add a agent mqtt Send and receive sever
//        Registry.INSTANCE.startThread(mqttConnThread);
        /***********************************/

        //mogo加入内存
        Registry.INSTANCE.saveKey("dmogo", dmogo);
        Registry.INSTANCE.saveKey("mogo", mogo);
        String[] productIds;
        String[] agentIds;
        //启动ems模拟数据
        try {
            agentIds = productIdConfig.getString("ems_agent_id").split("_");
        } catch (NullPointerException e) {
            agentIds = new String[0];
        }
        try {
            productIds = productIdConfig.getString("ems_product_id").split("_");
        } catch (NullPointerException e) {
            productIds = new String[0];

        }
        PVpredict pVpredict = new PVpredict(dmogo);
        SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmmss");
        String date = format.format(new Date());

        List<String> agentIdAll = new ArrayList<>();
        int n = productIds.length > agentIds.length ? productIds.length : agentIds.length;
        int agunt = 0;

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
                    pVpredict.PVInfo(date.substring(0, 8) + "000000", agentID, 1, EmsJob.pvcloud());
                //add predict data
                if (dmogo.findycdata(agentID, Integer.parseInt(date.substring(0, 8)))) {
                    pVpredict.PVInfo(date.substring(0, 8) + "000000", agentID, 0, EmsJob.pvcloud());
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
                Registry.INSTANCE.saveKey(agentID + "_jgtime", 30);
                //防止MQTT先启动线程做判断
                if (Registry.INSTANCE.getValue().get(agentID + "_Job") == null) {
                    EmsJob thread = new EmsJob(agentID, "jsonUp", mogo, dmogo);
                    Registry.INSTANCE.startJob(thread);
                    Registry.INSTANCE.saveKey(agentID + "_Job", thread);
                    logger.debug(agentID + "EMS所有设备准备成功开始上传数据..");
                }
            }
        }
        //启动PV系统数据任务
        String[] pvagentIds;
        String[] pvproductIds;
        try {
            pvagentIds = productIdConfig.getString("pv_agent_id").split("_");
        } catch (NullPointerException e) {
            pvagentIds = new String[0];
        }
        try {
            pvproductIds = productIdConfig.getString("pv_product_id").split("_");
        } catch (NullPointerException e) {
            pvproductIds = new String[0];

        }
        int pvn = pvproductIds.length > pvagentIds.length ? pvproductIds.length : pvagentIds.length;
        int pvagunt = 0;

        for (int i = 0; i < pvn; i++) {
            List<Agent> pvagents = new ArrayList<>();
            if (i < pvproductIds.length)
                pvagents = mogo.getAgentsByProductId(new ObjectId(pvproductIds[i]));
            if (i < pvagentIds.length)
                pvagents.add(mogo.getAgentsById(new ObjectId(pvagentIds[i])));
            for (Agent pvagent : pvagents) {
                pvagunt++;
                String agentID = pvagent.getId().toString();
                agentIdAll.add(agentID);
                mqtt = new MqttClient(mqttConfig.getString("mqtt.url"), pvagent.getId().toHexString(), persistence);
                options = new MqttConnectOptions();
                options.setUserName(pvagent.getId().toHexString());
                options.setPassword(pvagent.getToken().toCharArray());
                //add now data
                if (dmogo.findGendDataByTime(agentID, "pVPower") == null)
                    pVpredict.PVInfo(date.substring(0, 8) + "000000", agentID, 1, EmsJob.pvcloud());
                //add predict data
                if (dmogo.findycdata(agentID, Integer.parseInt(date.substring(0, 8)))) {
                    pVpredict.PVInfo(date.substring(0, 8) + "000000", agentID, 0, EmsJob.pvcloud());

                }
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
                    PVjob thread = new PVjob(agentID, "upstream", mogo, dmogo);
                    Registry.INSTANCE.startJob(thread);
                    Registry.INSTANCE.saveKey(agentID + "_Job", thread);
                    logger.debug(agentID + "PV所有设备准备成功开始上传数据..");
                }
            }
        }


        Registry.INSTANCE.saveKey("agentIdAll", agentIdAll);
        logger.info("启动完毕,本次启动共{}个Agent任务", agunt + pvagunt);

    }


}
