package io.j1st.data;

import io.j1st.data.entity.Registry;
import io.j1st.data.entity.config.BatConfig;
import io.j1st.data.job.EmsJob;
import io.j1st.data.job.PVjob;
import io.j1st.data.mqtt.MqttConnThread;
import io.j1st.data.predict.PVpredict;
import io.j1st.data.rabbitmq.RabittMQSend;
import io.j1st.storage.DataMongoStorage;
import io.j1st.storage.MongoStorage;
import io.j1st.storage.entity.Agent;
import io.j1st.storage.entity.AssetsInfo;
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
    private int agunt = 0;//计算总数
    private PropertiesConfiguration emulatorConfig;

    public ConfigFun(DataMongoStorage dmogo, MongoStorage mogo, PropertiesConfiguration emulatorConfig) {
        this.dmogo = dmogo;
        this.mogo = mogo;
        this.emulatorConfig = emulatorConfig;
    }

    public void startOne(String emulatorId, int type, int system,int num) {
        List<Agent> agents = new ArrayList<>();
        //如果都没有就启动
        if (mogo.isEmulatorAgentInfoBy(emulatorId, 1, system, type)) {
            //emulatorId类型判断

            try {
                switch (type) {
                    //AgentID
                    case 0:
                        agents.add(mogo.getAgentsById(new ObjectId(emulatorId)));
                        break;
                    //ProductId
                    case 1:
                        agents = mogo.getAgentsByProductId(new ObjectId(emulatorId));
                        break;
                    //plantid
                    case 2:
                        List<AssetsInfo> assetsInfos = mogo.getAssetsListByPlantId(new ObjectId(emulatorId), null, 1);
                        for (AssetsInfo assetsInfo : assetsInfos) {
                            agents.add(mogo.getAgentsById(assetsInfo.getAgentId()));
                        }
                        break;
                }
            } catch (NullPointerException e) {
                logger.info("agentID不存在跳过:" + emulatorId);
                RabittMQSend.sendRabbitMQ("agentID不存在跳过:" + emulatorId);
            }
            //启动接收任务包括预测数据 启动MQTT线程

            try {
                statbatch(agents, system, type, emulatorId,num);
            } catch (Exception e) {
                e.printStackTrace();
                logger.error("添加批次出错");
                RabittMQSend.sendRabbitMQ("添加批次出错");
            }

        } else {
            logger.error("添加的重复任务..过滤");
            RabittMQSend.sendRabbitMQ("添加的重复任务..过滤");
        }
    }


    private void statbatch(List<Agent> agents, int system, int type, String Id,int num) throws Exception {
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
        int defaultTime = Integer.parseInt(emulatorConfig.getString("default_Time"));
        for (Agent agent : agents) {
            agunt++;
            String agentID = agent.getId().toString();
            switch (type) {
                //ProductId
                case 1:
                    mogo.updateEmulatorRegister(agentID, "product_id", Id);
                    break;
                //PlantId
                case 2:
                    mogo.updateEmulatorRegister(agentID, "Plant_id", Id);
                    break;
            }
            mqtt = new MqttClient(mqtturl, agent.getId().toHexString(), persistence);
            options = new MqttConnectOptions();
            options.setUserName(agent.getId().toHexString());
            options.setPassword(agent.getToken().toCharArray());
            //添加实时数据和预测数据
            //初始话数据..
            //初始话时，需要删除历史数据的数量
            long deleteNum;
            deleteNum = dmogo.deleteGendDataByTime(agentID);
            logger.info("{}:已删除历史数据{}条", agentID, deleteNum);
            mogo.updateEmulatorRegister(agentID, "created_at", new Date());
            //add now data
            if (dmogo.findGendDataByTime(agentID, "pVPower") == null)
                pVpredict.PVInfo(date.substring(0, 8) + "000000", agentID, 1, EmsJob.pvcloud());
            //add predict data
            if (dmogo.findycdata(agentID, Integer.parseInt(date.substring(0, 8)))) {
                pVpredict.PVInfo(date.substring(0, 8) + "000000", agentID, 0, EmsJob.pvcloud());
            }
            Thread.sleep(3000);
            //save a job config
            Registry.INSTANCE.saveKey(agentID + "_STROAGE_002Config", new BatConfig());
            //mqtt
            MqttConnThread mqttConnThread = new MqttConnThread(mqtt, options, mogo, dmogo, emulatorConfig);
            //seve mqtt info
            Registry.INSTANCE.saveSession(agentID, mqttConnThread);
            //add a agent mqtt Send and receive sever
            Registry.INSTANCE.startThread(mqttConnThread);
            //设置间隔时间
            Registry.INSTANCE.saveKey(agentID + "_jgtime", defaultTime);
            //开始执行发送任务
            //防止MQTT先启动线程做判断
            Object onlinefail = mogo.findEmulatorRegister(agentID, "onlinefail");
            int online = onlinefail == null ? 0 : Integer.parseInt(onlinefail.toString());
            if (system == 1 && online == 0) {
                EmsJob thread = new EmsJob(agentID, "jsonUp", mogo, dmogo);
                Registry.INSTANCE.saveKey(agentID + "_Job", thread);
                Registry.INSTANCE.startJob(thread);
                logger.debug(agentID + "EMS设备准备成功开始上传数据..");
                RabittMQSend.sendRabbitMQ(agentID + "EMS设备准备成功开始上传数据..");
            }
            //防止MQTT先启动线程做判断
            if (system == 0 && online == 0) {
                PVjob thread = new PVjob(agentID, "upstream", mogo, dmogo);
                mogo.updateEmulatorRegister(agentID, "packing", "0,0,0,0,"+num);
                Registry.INSTANCE.saveKey(agentID + "_Job", thread);
                Registry.INSTANCE.startJob(thread);
                logger.debug(agentID + "PV设备准备成功开始上传数据..");
                RabittMQSend.sendRabbitMQ(agentID + "PV设备准备成功开始上传数据..");
            }
            Thread.sleep(90);
        }
    }

}

//chunmianbujuexiaochuchuwentiniaoyelaifengyushenhualuozhiduoshao
//eeequxiangxiangtiangebaimaofulvshuihongzhangboqinbo
//heiyunyachengchengyucuijiaguangxiangrijinlinkai
//huruyiyechunfenglaiqianshuwangshulihuakai
//chuheridangwuhandihexiatusheizhipanzhongcanlilijiexinku
//jianjiacangcnagbailuweishuangsuoweiyiranzaishuiyifang
//shanbuzaigaoyouxianzemingshuibuzaishenyoulongzeling
//sishiloushiweiwudixintaihenshangjielvcaoserulianqin
//taohuatanshuisanqianchibujiwanglunsongwoqin
//feiliuzhixiasanqianchiyishiyinheloujiutian


