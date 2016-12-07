package io.j1st.data.mqtt;


import io.j1st.data.entity.Registry;
import io.j1st.data.quartz.QuartzManager;
import io.j1st.util.entity.Payload;
import io.j1st.util.entity.bat.BatReceive;
import io.j1st.util.entity.bat.SetMHReg;
import io.j1st.util.util.GetJsonEmsData;
import io.j1st.util.util.JsonUtils;
import org.eclipse.paho.client.mqttv3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.Callable;
import java.util.function.ObjDoubleConsumer;


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
    private QuartzManager quartzManager;

    // Construction
    public MqttConnThread(MqttClient mqttClient, MqttConnectOptions options, QuartzManager quartzManager) {
        this.mqttClient = mqttClient;
        this.options = options;
        this.quartzManager = quartzManager;
    }

    @Override
    public Object call() throws Exception {
        try {
            // connect mqtt broker
            mqttClient.connect(options);
            mqttClient.setTimeToWait(2000);

            //判断客户端是否连接上
            if (mqttClient.isConnected()) {
                mqttClient.setCallback(new MqttCallback() {

                    @Override
                    public void connectionLost(Throwable cause) {
                        logger.debug("线程:{}断开连接，开始重连", mqttClient.getClientId());
                        new MqttConnThread(mqttClient, options, quartzManager);
                    }

                    @Override
                    public void messageArrived(String topic, MqttMessage message) throws Exception {
                        logger.debug("收到的消息为：" + message.toString());
                        Map<Object, Object> msgData = JsonUtils.OBJECT_MAPPER.readValue(message.toString().getBytes(), Map.class);
                        System.out.println(msgData);
                        if (msgData.keySet().toString().contains("uery")) {
                            Payload payload = JsonUtils.OBJECT_MAPPER.readValue(message.toString().getBytes(), Payload.class);
                            int d = payload.getQuery().get(0).getD();
                            int i = payload.getQuery().get(0).getI();
                            Thread.sleep(d * 1000);
                            String msg = GetJsonEmsData.getData(null, null, null);
                            mqttClient.publish("systemQuery", new MqttMessage(msg.getBytes("utf-8")));
                            quartzManager.modifyJobTime(null, null, "meter_trigger", "meter_trigger", "0/" + i + " * * * * ?");
                            logger.debug("上传数据为：" + msg);
                            logger.info("间隔已经恢复改为" + i + "秒");
                        } else if (msgData.keySet().toString().contains("etMHReg")) {
                            BatReceive batReceive=JsonUtils.OBJECT_MAPPER.readValue(message.toString().getBytes(), BatReceive.class);
                            String dsn=batReceive.getSetMHReg().get(0).getDsn();
                            Registry.INSTANCE.saveKey(dsn,batReceive);
                        }else
                        {
                            logger.error("错误格式");
                        }

                    }

                    @Override
                    public void deliveryComplete(IMqttDeliveryToken token) {
                        logger.debug("数据已发送");
                    }
                });

                String topic = "agents/5833e406dafbaf59a0d39671/downstream";
                mqttClient.subscribe(topic);
            }
            logger.debug("后台mqtt客户端:{}连接服务器 broker成功！", mqttClient.getClientId());
        } catch (Exception e) {
            logger.error("后台mqtt客户端:{}连接服务器 broker失败！重新连接开始...", mqttClient.getClientId());
            new MqttConnThread(mqttClient, options, quartzManager);
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
