package io.j1st.data.mqtt;


import io.j1st.data.entity.Registry;
import io.j1st.data.job.Job;
import io.j1st.storage.MongoStorage;
import io.j1st.util.util.JsonUtils;
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
    //定时任务修改
    private Job quartzManager;
    //数据持久操作
    private MongoStorage mogo;

    // Construction
    public MqttConnThread(MqttClient mqttClient, MqttConnectOptions options, Job quartzManager, MongoStorage mogo) {
        this.mqttClient = mqttClient;
        this.options = options;
        this.quartzManager = quartzManager;
        this.mogo = mogo;
    }

    @Override
    public Object call() throws Exception {
        try {
            // connect mqtt broker
            mqttClient.connect(options);
            mqttClient.setTimeToWait(200);

            //判断客户端是否连接上
            if (mqttClient.isConnected()) {
                mqttClient.setCallback(new MqttCallback() {

                    @Override
                    public void connectionLost(Throwable cause) {
                        logger.debug("线程:{}断开连接，开始重连", mqttClient.getClientId());
                        new MqttConnThread(mqttClient, options, quartzManager, mogo);
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
                            Job thread = (Job) Registry.INSTANCE.getValue().get(AgentID + "_Job");
                            //开启新的线程
                            Thread.sleep(d * 1000);
                            Job threadnew = new Job(AgentID, i, "systemQuery", mogo);
                            Registry.INSTANCE.startJob(threadnew);
                            //把新线程储存起来替换掉旧线程
                            Registry.INSTANCE.saveKey(AgentID + "_Job", threadnew);
                            logger.debug("开启新线程:{}", threadnew.getId());
                            mogo.updateEmulatorRegister(AgentID,"jgtime",i*1.0);
                            logger.info(AgentID + "间隔已经恢复改为" + i + "秒");
                            //停掉旧的线程
                            thread.exit = true;  // 终止线程thread
                            thread.join();

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
                        } else if (msgData.keySet().toString().contains("upSTAEAM")) {

                        } else if (msgData.keySet().toString().contains("packs")) {
                            List<Map> bbc = (List<Map>) msgData.get("packs");
                            String dataqc = bbc.get(0).get("packs").toString();
                            String[] a = dataqc.split(",");
                            int[] packs = new int[5];
                            for (int i = 0; i < a.length; i++) {
                                packs[i] = Integer.parseInt(a[i]);
                            }
                            Registry.INSTANCE.saveKey(AgentID + "_packing", packs);
                        } else {
                            logger.error("错误格式");
                        }

                    }

                    @Override
                    public void deliveryComplete(IMqttDeliveryToken token) {
                        logger.debug("数据已发送");
                    }
                });

                String topic = "agents/" + mqttClient.getClientId() + "/downstream";
                mqttClient.subscribe(topic);
            }
            logger.debug("后台mqtt客户端:{}连接服务器 broker成功！", mqttClient.getClientId());
        } catch (Exception e) {
            logger.error("后台mqtt客户端:{}连接服务器 broker失败！重新连接开始...", mqttClient.getClientId());
            new MqttConnThread(mqttClient, options, quartzManager, mogo);
            //睡眠5秒
            Thread.sleep(5000);
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

}
