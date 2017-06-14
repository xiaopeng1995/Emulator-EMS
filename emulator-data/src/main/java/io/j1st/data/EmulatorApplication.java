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

import io.j1st.storage.entity.EmulatorRegister;
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
import java.util.*;


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
        /********************************初始话参数配置开始*********************************/
        //初始话时，需要删除历史数据的数量
        long deleteNum = 0l;
        //准备要启动的接受任务线程
        List<MqttConnThread> mqttConnThreads = new ArrayList<>();
        //准备要启动的发送任务线程
        Map<String, Thread> threadSend = new HashMap<>();
        /********************************初始话参数配置结束*********************************/
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
        String serverid;
        //查看是否本地测试
        boolean islocal = emulatorConfig.getString("islocal").equals("1") ? true : false;
        if (islocal) {
            serverid = emulatorConfig.getString("local_sever_id");
            logger.info("启动本地模拟测试!!");
        } else {
            serverid = emulatorConfig.getString("sever_id");
        }
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
        Thread.sleep(2 * 1000);
        logger.info("开始加载数据库配置..");
        /***********************************/
        //获取默认间隔时间
        int defaultTime = Integer.parseInt(emulatorConfig.getString("default_Time"));
        PVpredict pVpredict = new PVpredict(dmogo);
        SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmmss");
        String date = format.format(new Date());
        //获取所有需要运行的Agentid
        List<EmulatorRegister> EmulatorRegisterAll = islocal ? new ArrayList<>() : mogo.findEmulatorAgentInfoBy(1);
        //应配置总数
        int initagunt = EmulatorRegisterAll.size();
        int jobagunt = 0;
        if (islocal) {
            EmulatorRegister emulatorRegister = new EmulatorRegister();
            emulatorRegister.setAgent_id(emulatorConfig.getString("pvagent_id"));
            emulatorRegister.setSystemType(emulatorConfig.getInt("systemType"));
            EmulatorRegisterAll.add(emulatorRegister);
        }
        //选择是否清空历史数据
        Scanner s = new Scanner(System.in);
        System.out.println("是否清空已查询的" + EmulatorRegisterAll.size() + "个数据的历史数据?(1清空其他不清空)");
        String delet = s.nextLine();
        logger.info("正在加载配置到内存..");
        for (EmulatorRegister eR : EmulatorRegisterAll) {
            //获取agentId
            String agentId = eR.getAgent_id();
            Agent agent = null;
            try {
                agent = mogo.getAgentsById(new ObjectId(agentId));
            } catch (NullPointerException e) {
                logger.info("agentID不存在跳过:" + agentId);
            } catch (IllegalArgumentException es) {
                logger.info("没有启动的ID");
            }
            if (agent != null) {
                /****************初始化接受线程配置开始***************/
                mqtt = new MqttClient(mqttConfig.getString("mqtt.url"), agent.getId().toHexString(), persistence);
                options = new MqttConnectOptions();
                options.setUserName(agent.getId().toHexString());
                options.setPassword(agent.getToken().toCharArray());
                //save a job config
                Registry.INSTANCE.saveKey(agentId + "_STROAGE_002Config", new BatConfig());
                //mqtt
                MqttConnThread mqttConnThread = new MqttConnThread(mqtt, options, mogo, dmogo, emulatorConfig);
                //seve mqtt info
                Registry.INSTANCE.saveSession(agentId, mqttConnThread);
                mqttConnThreads.add(mqttConnThread);
                /****************初始化接受线程配置结束***************/
                /****************添加预测数据***************/
                //删除历史数据
                if (delet.equals("1")) {
                    deleteNum = dmogo.deleteGendDataByTime(agentId);
                    logger.info("{}:已删除历史数据{}条", agentId, deleteNum);
                    //add now data
                    if (dmogo.findGendDataByTime(agentId, "pVPower") == null)
                        pVpredict.PVInfo(date.substring(0, 8) + "000000", agentId, 1, EmsJob.pvcloud());
                    //add predict data
                    if (dmogo.findycdata(agentId, Integer.parseInt(date.substring(0, 8)))) {
                        pVpredict.PVInfo(date.substring(0, 8) + "000000", agentId, 0, EmsJob.pvcloud());
                    }
                }
                /****************添加预测数据结束***************/

                //设置间隔时间
                Registry.INSTANCE.saveKey(agentId + "_jgtime", defaultTime);
                //填装PV和EMS系统任务
                if (eR.getSystemType() == 0) {
                    PVjob thread = new PVjob(agentId, "upstream", mogo, dmogo);
                    threadSend.put(agentId, thread);
                    jobagunt++;
                } else if (eR.getSystemType() == 1) {
                    EmsJob thread = new EmsJob(agentId, "jsonUp", mogo, dmogo);
                    threadSend.put(agentId, thread);
                } else {
                    logger.info("其他系统类型暂不支持。。");
                }
            } else {
                initagunt--;
                long delete = mogo.deleteemulatorRegisterById(eR.getAgent_id());
                if (delete > 0)
                    logger.info("没有找到初始化的任务！已删除此任务");
            }
            logger.debug("Agent:" + agentId + ".加载完毕.");
        }
        //打印配置信息
        logger.info("数据库任务加载完毕！\n应加载任务 {} 个，实际加载任务 {} 个 .EMS数据{}个.PV数据{}个",
                EmulatorRegisterAll.size(), initagunt, initagunt - jobagunt, jobagunt);
        //是否继续开始任务..112.74.97.162
        Scanner ss = new Scanner(System.in);
        System.out.println("是否启动已加载的任务?(输入 1 继续,其他不启动)");
        String line = ss.nextLine();
        if (!line.equals("1")) {
            logger.info("模拟器准备就绪..");
            return;
        }
        logger.info("5秒后开始启动任务.");
        Thread.sleep(5 * 1000);
        //开始执行任务
        EmulatorApplication emu = new EmulatorApplication();
        //1.启动接收任务
        emu.startReceiveThreadAll(mqttConnThreads);
        Thread.sleep(5 * 1000);
        //2.启动发送任务
        emu.startSendThreadAll(threadSend);
    }

    /**
     * 1.启动所有初始话好的接受任务
     *
     * @param mqttConnThreads 接收线程任务。
     */
    private void startReceiveThreadAll(List<MqttConnThread> mqttConnThreads) throws InterruptedException {
        logger.info("开始启动所有接收任务..");
        for (MqttConnThread mqttConnThread : mqttConnThreads) {
            Registry.INSTANCE.startThread(mqttConnThread);
            Thread.sleep(50);
        }
        logger.info("所有接收任务启动完毕 共{}个 ", mqttConnThreads.size());
    }


    /**
     * 2.启动所有初始话好的发送任务
     *
     * @param threadSend
     */
    private void startSendThreadAll(Map<String, Thread> threadSend) throws InterruptedException {
        logger.info("开始启动所有发送任务..");
        Set<String> agentIds = threadSend.keySet();
        for (String agentid : agentIds) {
            //防止MQTT先启动线程做判断
            if (Registry.INSTANCE.getValue().get(agentid + "_Job") == null) {
                Registry.INSTANCE.startJob(threadSend.get(agentid));
                //任务配置添加到内存！
                Registry.INSTANCE.saveKey(agentid + "_Job", threadSend.get(agentid));
                Thread.sleep(50);
            }
        }
        logger.info("所有发送任务启动完毕 共{}个", agentIds.size());
    }


}
