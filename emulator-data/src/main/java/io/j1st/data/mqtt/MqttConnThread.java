package io.j1st.data.mqtt;


import io.j1st.data.entity.Registry;
import io.j1st.data.job.GetDataAll;
import io.j1st.data.quartz.QuartzManager;
import io.j1st.util.entity.Payload;
import io.j1st.util.entity.bat.BatReceive;
import io.j1st.util.entity.bat.SetMHReg;
import io.j1st.util.util.GetJsonEmsData;
import io.j1st.util.util.JsonUtils;
import org.eclipse.paho.client.mqttv3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
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
                        Map<Object, Object> msgData = JsonUtils.Mapper.readValue(message.toString().getBytes(), Map.class);
                        if (msgData.keySet().toString().contains("Query")) {
                            List<Map> bbc=(List<Map>)msgData.get("Query");
                            int d = (Integer) bbc.get(0).get("D");
                            int i = (Integer) bbc.get(0).get("I");
                            quartzManager.modifyJobTime(null, null, "batTrigger", "batTrigger", "0/"+i+" * * * * ?");
                            logger.info("间隔已经恢复改为" + i + "秒");
                            Thread.sleep(d * 1000);
                            BatReceive batReceive = (BatReceive) Registry.INSTANCE.getValue().get("AB123456");
                            double Reg12551 = 0.0;
                            if (batReceive != null) {
                                Reg12551 = Integer.parseInt(batReceive.getSetMHReg().get(0).get("Reg12551").toString());
                            }
                            GetDataAll getDataAll = new GetDataAll(Reg12551, Registry.INSTANCE.getConfig().get("STROAGE_002"));
                            String msg = getDataAll.getDate();
                            mqttClient.publish("agents/" + mqttClient.getClientId() + "/systemQuery", new MqttMessage(msg.getBytes("utf-8")));
                            logger.debug("上传数据为：" + msg);

                        } else if (msgData.keySet().toString().contains("SetMHReg")) {
                            List<Map> bbc=(List<Map>)msgData.get("SetMHReg");
                            String d = bbc.get(0).get("dsn").toString();
                            double i = (double) bbc.get(0).get("Reg12551");
                            Object num1=Registry.INSTANCE.getValue().get("Soc");
                            if(num1!=null)//判断当前容量是否处于极限值.
                            {
                                double num=(double) num1;

                                i=num>0.95&i<0?0:num<0.05&i>0?0:i;
                                logger.debug("收到指令,当前Soc:"+num);
                                logger.debug("收到指令,功率百分比:"+i);
                            }
                            Registry.INSTANCE.saveKey(d, i);
                        } else if(msgData.keySet().toString().contains("upSTAEAM")) {

                        }else {
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
