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
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.bson.types.ObjectId;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 生产数据配置表
 */
public class ConfigFun {
    private static final Logger logger = LoggerFactory.getLogger(ConfigFun.class);
    private DataMongoStorage dmogo;
    private MongoStorage mogo;
    private int agunt = 0;
    private PropertiesConfiguration emulatorConfig;

    public ConfigFun(DataMongoStorage dmogo, MongoStorage mogo, PropertiesConfiguration emulatorConfig) {
        this.dmogo = dmogo;
        this.mogo = mogo;
        this.emulatorConfig = emulatorConfig;
    }

    public void startOne(String emulatorId, int type, int system) {
        List<Agent> agents = new ArrayList<>();
        String ems_agent_id = emulatorConfig.getString("ems_agent_id")==null?"":emulatorConfig.getString("ems_agent_id");
        String ems_product_id = emulatorConfig.getString("ems_product_id")==null?"":emulatorConfig.getString("ems_product_id");
        String pv_agent_id = emulatorConfig.getString("pv_agent_id")==null?"":emulatorConfig.getString("pv_agent_id");
        String pv_product_id = emulatorConfig.getString("pv_product_id")==null?"":emulatorConfig.getString("pv_product_id");
        //emulatorId类型判断
        switch (type) {
            //AgentID
            case 0:
                if (system != 0)
                    ems_agent_id += emulatorId + "_";
                else
                    pv_agent_id += emulatorId + "_";
                agents.add(mogo.getAgentsById(new ObjectId(emulatorId)));
                break;
            //BAID
            case 1:
                if (system != 0)
                    ems_product_id += emulatorId + "_";
                else
                    pv_product_id += emulatorId + "_";
                agents = mogo.getAgentsByProductId(new ObjectId(emulatorId));
                break;
        }

        //启动接收任务包括预测数据 启动MQTT线程

        try {
            statbatch(agents, system);
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("添加批次出错");
        }

        emulatorConfig.setProperty("ems_agent_id", ems_agent_id);
        emulatorConfig.setProperty("ems_product_id", ems_product_id);
        emulatorConfig.setProperty("pv_agent_id", pv_agent_id);
        emulatorConfig.setProperty("pv_product_id", pv_product_id);
        logger.info("启动完毕,本次启动共{}个Agent任务:\nems_agent_id:{}" +
                "\nems_product_id{}\npv_agent_id{}\npv_product_id{}", agunt, ems_agent_id, ems_product_id, pv_agent_id, pv_product_id);
    }

    private void statbatch(List<Agent> agents, int system) throws Exception {
        //获取mqtt url
        String mqtturl = Registry.INSTANCE.getValue().get("mqtt_url").toString();
        PVpredict pVpredict = new PVpredict(dmogo);
        SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmmss");
        String date = format.format(new Date());
        //mqtt
        MemoryPersistence persistence = new MemoryPersistence();
        MqttConnectOptions options;
        MqttClient mqtt;
        //获取默认间隔时间
        int defaultTime=Integer.parseInt(emulatorConfig.getString("default_Time"));
        for (Agent agent : agents) {
            agunt++;
            String agentID = agent.getId().toString();
            mqtt = new MqttClient(mqtturl, agent.getId().toHexString(), persistence);
            options = new MqttConnectOptions();
            options.setUserName(agent.getId().toHexString());
            options.setPassword(agent.getToken().toCharArray());
            //添加实时数据和预测数据
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
            //开始执行发送任务
            //防止MQTT先启动线程做判断
            if (system == 1 && Registry.INSTANCE.getValue().get(agentID + "_Job") == null) {
                EmsJob thread = new EmsJob(agentID, "jsonUp", mogo, dmogo);
                Registry.INSTANCE.saveKey(agentID + "_Job", thread);
                Registry.INSTANCE.startJob(thread);
                logger.debug(agentID + "EMS设备准备成功开始上传数据..");
            }
            //防止MQTT先启动线程做判断
            if (system == 0 && Registry.INSTANCE.getValue().get(agentID + "_Job") == null) {
                PVjob thread = new PVjob(agentID, "upstream", mogo, dmogo);
                Registry.INSTANCE.saveKey(agentID + "_Job", thread);
                Registry.INSTANCE.startJob(thread);
                logger.debug(agentID + "PV设备准备成功开始上传数据..");
            }
        }
    }

}

