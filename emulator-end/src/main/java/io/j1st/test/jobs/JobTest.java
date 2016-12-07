package io.j1st.test.jobs;

import org.eclipse.paho.client.mqttv3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Callable;

/**
 * 测试接收,模拟器上传信息
 */
public class JobTest implements Callable {

    // Logger
    private static final Logger logger = LoggerFactory.getLogger(JobTest.class);
    private String a;
    // Mqtt Client
    private MqttClient mqttClient;

    // Mqtt Connect Options
    private MqttConnectOptions options;

    public JobTest(MqttClient mqttClient, MqttConnectOptions options) {
        this.mqttClient = mqttClient;
        this.options = options;
    }

    public String call() throws Exception {
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
                        new JobTest(mqttClient, options);
                    }

                    @Override
                    public void messageArrived(String topic, MqttMessage message) throws Exception {
                        logger.debug("收到的消息为：" + message.toString());
                    }

                    @Override
                    public void deliveryComplete(IMqttDeliveryToken token) {
                        logger.debug("数据已发送");
                    }
                });

                String topic = "systemQuery";
                String topic2 = "jsonUp";
                mqttClient.subscribe(topic);
                mqttClient.subscribe(topic2);
            }
            logger.debug("后台mqtt客户端:{}连接服务器 broker成功！", mqttClient.getClientId());
        } catch (Exception e) {
            logger.error("后台mqtt客户端:{}连接服务器 broker失败！重新连接开始...", mqttClient.getClientId());
            new JobTest(mqttClient, options);
            //每个5秒连接一次
            Thread.sleep(5000);
        }
        return "hello";
    }
}
