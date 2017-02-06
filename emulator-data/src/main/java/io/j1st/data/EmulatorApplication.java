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

import org.apache.commons.configuration.reloading.FileChangedReloadingStrategy;
import org.apache.commons.lang3.StringUtils;
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

        PropertiesConfiguration emulatorConfig;
        PropertiesConfiguration mongoConfig;
        PropertiesConfiguration mqttConfig;
        if (args.length >= 3) {
            emulatorConfig = new PropertiesConfiguration(args[0]);
            mongoConfig = new PropertiesConfiguration(args[1]);
            mqttConfig = new PropertiesConfiguration(args[2]);
            // quartzConfig = new PropertiesConfiguration(args[4]);


        } else {
            emulatorConfig = new PropertiesConfiguration("config/emulator.properties");
            mongoConfig = new PropertiesConfiguration("config/mongo.properties");
            mqttConfig = new PropertiesConfiguration("config/mqtt.properties");
            //quartzConfig = new PropertiesConfiguration("config/quartz.properties");
        }
        /**************管理配置文件***************/
        //自动重新加载
        emulatorConfig.setReloadingStrategy(new FileChangedReloadingStrategy());
        //自动保存
        emulatorConfig.setAutoSave(true);


        //***********************mqtt
        MemoryPersistence persistence = new MemoryPersistence();
        MqttClient mqtt;
        MqttConnectOptions options;
        //***********************mongodb
        MongoStorage mogo = new MongoStorage();
        mogo.init(mongoConfig);

        DataMongoStorage dmogo = new DataMongoStorage();
        dmogo.init(mongoConfig);
        if (dmogo.deleteDataByTime() > 0)
            logger.info("已删除旧数据");
        //mogo加入内存
        Registry.INSTANCE.saveKey("dmogo", dmogo);
        Registry.INSTANCE.saveKey("mogo", mogo);
        /***********************************/
        //mqtt
        String serverid = emulatorConfig.getString("sever_id");
        mqtt = new MqttClient(mqttConfig.getString("mqtt.url"),
                new ObjectId(serverid).toHexString(), persistence);
        options = new MqttConnectOptions();
        options.setUserName(new ObjectId(serverid).toHexString());
        String token = mogo.getAgentsById(new ObjectId(serverid)).getToken();
        options.setPassword(token.toCharArray());
        MqttConnThread serverThread = new MqttConnThread(mqtt, options, mogo, dmogo, emulatorConfig);
        //seve mqtt info
        Registry.INSTANCE.saveKey("mqtt_url", mqttConfig.getString("mqtt.url"));
        Registry.INSTANCE.saveSession(serverid, serverThread);
        //add a agent mqtt Send and receive sever
        Registry.INSTANCE.startThread(serverThread);
        /***********************************/

        //获取默认间隔时间
        int defaultTime = Integer.parseInt(emulatorConfig.getString("default_Time"));
        PVpredict pVpredict = new PVpredict(dmogo);
        SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmmss");
        String date = format.format(new Date());
        int agunt = 0;
        //获取所有需要运行ems的Agentid
        List<String> emsAgentall = mogo.findEmulatorAgentInfoBy(1, 1);
        for (String emsAgentid : emsAgentall) {
            List<Agent> agents = new ArrayList<>();
            agents.add(mogo.getAgentsById(new ObjectId(emsAgentid)));
            for (Agent agent : agents) {
                agunt++;
                String agentID = agent.getId().toString();
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
                MqttConnThread mqttConnThread = new MqttConnThread(mqtt, options, mogo, dmogo, emulatorConfig);
                //seve mqtt info
                Registry.INSTANCE.saveSession(agentID, mqttConnThread);
                //add a agent mqtt Send and receive sever
                Registry.INSTANCE.startThread(mqttConnThread);
                Thread.sleep(90);
                //设置间隔时间
                Registry.INSTANCE.saveKey(agentID + "_jgtime", defaultTime);
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
        int pvagunt = 0;

        //获取所有需要运行ems的Agentid
        List<String> pvAgentall = mogo.findEmulatorAgentInfoBy(1, 0);
        for (String pvAgentId : pvAgentall) {
            List<Agent> pvagents = new ArrayList<>();
            pvagents.add(mogo.getAgentsById(new ObjectId(pvAgentId)));
            for (Agent pvagent : pvagents) {
                pvagunt++;
                String agentID = pvagent.getId().toString();
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
                MqttConnThread mqttConnThread = new MqttConnThread(mqtt, options, mogo, dmogo, emulatorConfig);
                //seve mqtt info
                Registry.INSTANCE.saveSession(agentID, mqttConnThread);
                //add a agent mqtt Send and receive sever
                Registry.INSTANCE.startThread(mqttConnThread);
                Thread.sleep(90);
                //设置间隔时间
                Registry.INSTANCE.saveKey(agentID + "_jgtime", defaultTime);
                //防止MQTT先启动线程做判断
                if (Registry.INSTANCE.getValue().get(agentID + "_Job") == null) {
                    PVjob thread = new PVjob(agentID, "upstream", mogo, dmogo);
                    Registry.INSTANCE.startJob(thread);
                    Registry.INSTANCE.saveKey(agentID + "_Job", thread);
                    logger.debug(agentID + "PV所有设备准备成功开始上传数据..");
                }
            }
        }
        logger.info("启动完毕,本次启动共{}个Agent任务", agunt + pvagunt);
    }


}
