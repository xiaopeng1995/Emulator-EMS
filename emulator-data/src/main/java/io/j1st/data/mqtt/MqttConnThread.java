package io.j1st.data.mqtt;


import io.j1st.data.ConfigFun;
import io.j1st.data.entity.Registry;
import io.j1st.data.entity.config.BatConfig;
import io.j1st.data.job.EmsJob;
import io.j1st.data.job.GetDataAll;
import io.j1st.storage.DataMongoStorage;
import io.j1st.storage.MongoStorage;
import io.j1st.util.util.JsonUtils;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.eclipse.paho.client.mqttv3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;


/**
 * Mqtt Connect Controller
 */
public class MqttConnThread implements Callable {

    // Logger
    private static final Logger logger = LoggerFactory.getLogger(MqttConnThread.class);

    // Mqtt Client
    private MqttClient mqttClient;

    // Mqtt Connect Options
    private MqttConnectOptions options;

    //cofig
    private PropertiesConfiguration emulatorConfig;

    //Data persistence operations
    private MongoStorage mogo;
    private DataMongoStorage dmogo;

    // Construction
    public MqttConnThread(MqttClient mqttClient, MqttConnectOptions options,
                          MongoStorage mogo, DataMongoStorage dmogo,
                          PropertiesConfiguration emulatorConfig) {
        this.mqttClient = mqttClient;
        this.options = options;
        this.emulatorConfig=emulatorConfig;
        this.mogo = mogo;
        this.dmogo = dmogo;
        logger.debug("connection agein");
    }

    @Override
    public Object call() throws Exception {
        try {
            // connect mqtt broker
            mqttClient.connect(options);
            mqttClient.setTimeToWait(200);

            //Whether the client connection
            if (mqttClient.isConnected()) {
                mqttClient.setCallback(new MqttCallback() {

                    @Override
                    public void connectionLost(Throwable cause) {
                        logger.debug("线程:{}断开连接!!!!", mqttClient.getClientId());
                    }

                    @Override
                    public void messageArrived(String topic, MqttMessage message) throws Exception {
                        String AgentID = mqttClient.getClientId();
                        logger.debug(AgentID + "收到的消息为：" + message.toString());
                        Map<Object, Object> msgData = JsonUtils.Mapper.readValue(message.toString().getBytes(), Map.class);


                        if (msgData.keySet().toString().contains("Query")) {
                            List<Map> bbc = (List<Map>) msgData.get("Query");
                            int d = (Integer) bbc.get(0).get("D");
                            int i = (Integer) bbc.get(0).get("I");
                            Object oldjob = Registry.INSTANCE.getValue().get(AgentID + "_Job");
                            // 如果有停掉旧的线程
                            if (oldjob != null) {
                                EmsJob thread = (EmsJob) Registry.INSTANCE.getValue().get(AgentID + "_Job");
                                thread.exit = true;  // 终止线程thread
                                thread.join();
                            }
                            //开启新的线程
                            Thread.sleep(d * 1000);
                            //设置间隔时间
                            Registry.INSTANCE.saveKey(AgentID + "_jgtime", i);
                            EmsJob threadnew = new EmsJob(AgentID, "systemQuery", mogo, dmogo);
                            Registry.INSTANCE.startJob(threadnew);
                            //更新内存线程池中线程
                            Registry.INSTANCE.saveKey(AgentID + "_Job", threadnew);
                            logger.debug(AgentID + "开启新上传工作线程:{}", threadnew.getId());
                            mogo.updateEmulatorRegister(AgentID, "jgtime", i * 1.0);
                            logger.info(AgentID + "间隔已经恢复改为" + i + "秒");

                        } else if (msgData.keySet().toString().contains("SetMHReg")) {
                            List<Map> bbc = (List<Map>) msgData.get("SetMHReg");
                            String d = bbc.get(0).get("dsn").toString();
                            double i = (double) bbc.get(0).get("Reg12551");
                            Object num1 = mogo.findEmulatorRegister(AgentID, "Soc");
                            if (num1 != null)//判断当前容量是否处于极限值.
                            {
                                double num = (double) num1;

                                i = num > 0.95 & i < 0 ? 0 : num < 0.05 & i > 0 ? 0 : i;
                                logger.debug("dsn:" + d);
                                logger.debug("收到指令,当前Soc:" + num);
                                logger.debug("收到指令,功率百分比:" + i);
                            }
                            Registry.INSTANCE.saveKey(d, i);
                            mogo.updateEmulatorRegister(AgentID, d, i);
                        } else if (msgData.keySet().toString().contains("emulatorJob")) {

                            List<Map> bbc = (List<Map>) msgData.get("emulatorJob");
                            //
                            String emulatorAgent = bbc.get(0).get("emulatorId").toString();
                            // 0    AgentId  1批次号
                            int type = (int) bbc.get(0).get("type");
                            // 0   PV   1 EMS
                            int system = (int) bbc.get(0).get("system");
                            //开始添加新任务
                            logger.info("\n开始收到新任务--ID:{}\n类型:{}\n系统:{}", emulatorAgent, type == 0 ? "AgentID" : "Batch ID", system == 0 ? "PV" : "EMS");
                            new ConfigFun(dmogo, mogo,emulatorConfig).startOne(emulatorAgent, type, system);
                        } else if (msgData.keySet().toString().contains("packs")) {
                            List<Map> bbc = (List<Map>) msgData.get("packs");
                            String dataqc = bbc.get(0).get("packs").toString();
                            mogo.updateEmulatorRegister(AgentID, "packing", dataqc);
                            BatConfig STROAGE_002 = (BatConfig) Registry.INSTANCE.getValue().get(AgentID + "_STROAGE_002Config");
                            Object batReceive = mogo.findEmulatorRegister(AgentID, AgentID + "120");
                            double Reg12551 = 0d;
                            if (batReceive != null) {
                                Reg12551 = (Double) batReceive;
                            } else {
                                mogo.updateEmulatorRegister(AgentID, AgentID + "120", 0.0);
                            }
                            int jgtime = (int) Registry.INSTANCE.getValue().get(AgentID + "_jgtime");
                            GetDataAll dataAll = new GetDataAll(Reg12551, STROAGE_002, mogo, jgtime, 0);
                            String msg = dataAll.getDate(AgentID);
                            logger.info("实时packs  类型:" + msg);
                            sendMessage(getTopic(AgentID), msg);
                        } else {
                            logger.error("错误格式");
                        }


                    }

                    @Override
                    public void deliveryComplete(IMqttDeliveryToken token) {
                        logger.debug(mqttClient.getClientId() + ":send successful!");
                    }
                });

                String topic = "agents/" + mqttClient.getClientId() + "/downstream";
                mqttClient.subscribe(topic);
            }
            logger.debug("后台mqtt客户端:{}连接服务器 broker成功！", mqttClient.getClientId());
        } catch (Exception e) {
            //睡眠10分钟
            Thread.sleep(10 * 60 * 1000);
            logger.error("后台mqtt客户端:{}连接服务器 broker失败！重新连接开始...", mqttClient.getClientId());
            Registry.INSTANCE.startThread(new MqttConnThread(mqttClient, options, mogo, dmogo,emulatorConfig));

        }
        return null;
    }

    public MqttClient getMqttClient() {
        return mqttClient;
    }

    public void setMqttClient(MqttClient mqttClient) {
        this.mqttClient = mqttClient;
    }

    public MqttConnectOptions getOptions() {
        return options;
    }

    public void setOptions(MqttConnectOptions options) {
        this.options = options;
    }

    //发布消息
    public void sendMessage(String topic, String message) {
        try {
            this.mqttClient.publish(topic, new MqttMessage(message.getBytes("utf-8")));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String getTopic(String agentId) {
        logger.debug(agentId + " Topic:systemQuery");
        return "agents/" + agentId + "/systemQuery";
    }

}
