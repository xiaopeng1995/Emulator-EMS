
package io.j1st.data.job;

import io.j1st.data.GetThreadAcount;
import io.j1st.data.entity.Registry;
import io.j1st.data.entity.config.BatConfig;
import io.j1st.data.mqtt.MqttConnThread;
import io.j1st.data.predict.PVpredict;
import io.j1st.data.rabbitmq.RabittMQSend;
import io.j1st.storage.DataMongoStorage;
import io.j1st.storage.MongoStorage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 单线程工作任务,模拟单个ems系统数据
 */
public class PVjob extends Thread {
    Logger logger = LoggerFactory.getLogger(PVjob.class);
    public volatile boolean exit = false;
    private String agentId;
    private String topic;
    private MongoStorage mogo;
    private DataMongoStorage dmogo;

    public PVjob(String agentid, String topic, MongoStorage mogo, DataMongoStorage dmogo) {
        this.agentId = agentid;
        this.topic = topic;
        this.mogo = mogo;
        this.dmogo = dmogo;
    }

    public void run() {
        //本线程运行时间
        Date timeThread = new Date();
        mogo.updateEmulatorRegister(agentId, "created_at", new Date());
        int jgtime = (int) Registry.INSTANCE.getValue().get(agentId + "_jgtime");
        // mqtt topic
        String topicall;
        //更新间隔时间
        Registry.INSTANCE.saveKey(agentId + "_jgdate", timeThread.getTime());
        mogo.updateEmulatorRegister(agentId, "systemTpye", 0);
        mogo.updateEmulatorRegister(agentId, "topic", topic);
        //更新数据格式
        Object datapacking = mogo.findEmulatorRegister(agentId, "packing");
        if (datapacking == null) {
            datapacking = "0,0,0,0,1";
        }
        mogo.updateEmulatorRegister(agentId, "packing", datapacking);
        mogo.updateEmulatorRegister(agentId, "onlinefail", 1);
        while (!exit) {
            MqttConnThread mqttConnThread;
            //添加预测数据
            SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmmss");
            String date = format.format(new Date());
            //零点开始
            if (date.substring(8, 10).equals("00")) {
                date = date.substring(0, 8) + "000000";
                logger.info("已在凌晨时间..开始工作");
                try {
                    if (dmogo.findycdata(agentId, Integer.parseInt(date.substring(0, 8)))) {
                        /********************清理数据****************/
                        boolean is;
                        Object num = mogo.findEmulatorRegister(agentId, "TYield");
                        double TYield = num != null ? (double) num : 0.0;
                        num = mogo.findEmulatorRegister(agentId, "DYield");
                        double DYield = num != null ? (double) num : 0.0;
                        is = mogo.updateEmulatorRegister(agentId, "TYield", TYield + DYield);
                        if (is)
                            logger.debug(agentId + "TYield累加前一天.");
                        //当天PV电量清零
                        is = mogo.updateEmulatorRegister(agentId, "DYield", 0.0);
                        if (is)
                            logger.debug(agentId + "_DYield PV已清零..");
                        PVpredict p = new PVpredict(dmogo);
                        //add now data
                        p.PVInfo(date.substring(0, 8) + "000000", agentId, 1, pvcloud());
                        //添加预测数据
                        p.PVInfo(date, agentId, 0, pvcloud());

                    } else {
                        logger.debug("过滤凌晨重复动作");
                    }
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
            /*结束*/
            //GetDataAll(0d, null, mogo, jgtime)
            //第一个参数接收指令充放电
            //第二个电池配置文件
            //第三个间隔时间
            //第四个数据系统类型 0ems  1pv 2..
            GetDataAll dataAll = new GetDataAll(0d, null, mogo, jgtime);

            String msg = dataAll.getDate(agentId);
            mqttConnThread = Registry.INSTANCE.getSession().get(agentId);
            topicall = getTopic(agentId);
            if (mqttConnThread != null && mqttConnThread.getMqttClient().isConnected()) {
                mqttConnThread.sendMessage(topicall, msg);
                //间隔时间差
                long interval;
                //总时间差
                long startDate;
                Date now = new Date();
                interval = (now.getTime() - (long) Registry.INSTANCE.getValue().get(agentId + "_jgdate")) / 1000;
                long startThtead = (now.getTime() - timeThread.getTime()) / 1000;
                startDate = (now.getTime() - (long) Registry.INSTANCE.getValue().get("startDate")) / 1000;
                RabittMQSend.sendRabbitMQ("线程ID["+super.getId()+"]发送数据详情:<br>AgentID:\t["+agentId+"]<br>Topic:\t"+topicall+"<br>上传时间间隔:\t"+interval+" \t设置时间间隔:"+jgtime+"\t此线程运行时间:"+startThtead+"\t模拟器运行时间:"+startDate+" 单位:秒<br>发送数据为:\t"+msg+"<br>");
                logger.info("\n##########start###########\nThread[{}]Send Data Info:\nAgentID:\t{}\nTopic:\t{}\nTime Interval:\t{} \tSet the time:{}\tThread run time:{}\tserver run time:{}\nSend Data:\t{}\nOther Info:\t{}\n##########end###########"
                        , super.getId(), agentId, topicall, interval, jgtime, startThtead, startDate, msg, GetThreadAcount.GetThreadAcountInfo());
            } else {
                //睡眠300秒
                try {
                    Thread.sleep(300 * 1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                logger.info(agentId + "MQTT链接信息错误,链接失败");
                Registry.INSTANCE.startThread(mqttConnThread);
            }
            //更新间隔时间
            Registry.INSTANCE.saveKey(agentId + "_jgdate", new Date().getTime());
            try {
                sleep(jgtime * 1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }
        mogo.deleteemulatorRegisterById(agentId);
        logger.debug(agentId + "Old  worker thread:" + super.getId() + "End!");
    }

    /**
     * Get Topic
     */
    private String getTopic(String agentId) {
        return "agents/" + agentId + "/" + topic;
    }

    //太阳能云因子
    public static int[] pvcloud() {
        int[] cCloud = new int[8];
        int ran = (int) (Math.random() * 10);
        cCloud[0] = ran > 5 ? 1 : ran > 3 ? 2 : ran > 2 ? 3 : 4;
        cCloud[1] = ran > 5 ? 3 : ran > 3 ? 2 : ran > 2 ? 1 : 5;
        cCloud[2] = ran > 5 ? 0 : ran > 3 ? 1 : ran > 2 ? 2 : 3;
        ran = (int) (Math.random() * 10);
        cCloud[3] = ran > 5 ? 0 : ran > 3 ? 1 : ran > 2 ? 3 : 2;
        cCloud[4] = ran > 5 ? 0 : ran > 3 ? 1 : ran > 2 ? 2 : 3;
        cCloud[5] = ran > 5 ? 6 : ran > 3 ? 5 : ran > 2 ? 4 : 7;
        ran = (int) (Math.random() * 10);
        cCloud[6] = ran > 5 ? 6 : ran > 3 ? 5 : ran > 2 ? 4 : 3;
        cCloud[7] = ran > 5 ? 3 : ran > 3 ? 4 : ran > 2 ? 1 : 2;
        return cCloud;
    }
}
