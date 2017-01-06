package io.j1st.data.job;

import io.j1st.data.entity.Registry;
import io.j1st.data.entity.config.BatConfig;
import io.j1st.data.mqtt.MqttConnThread;
import io.j1st.data.predict.PVpredict;
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
public class Job extends Thread {
    Logger logger = LoggerFactory.getLogger(Job.class);
    public volatile boolean exit = false;
    private BatConfig STROAGE_002;
    private String agentId;
    private double Reg12551;
    private String topic;
    private MongoStorage mogo;
    private DataMongoStorage dmogo;

    public Job(String agentid, String topic, MongoStorage mogo, DataMongoStorage dmogo) {
        this.agentId = agentid;
        this.topic = topic;
        this.mogo = mogo;
        this.dmogo = dmogo;
    }

    public void run() {
        //本线程运行时间
        Date timeThread = new Date();
        int jgtime = (int) Registry.INSTANCE.getValue().get(agentId + "_jgtime");
        // mqtt topic
        String topicall;
        //更新间隔时间
        Registry.INSTANCE.saveKey(agentId + "_jgdate", timeThread.getTime());
        while (!exit) {
            MqttConnThread mqttConnThread;
            //添加预测数据
            SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmmss");
            String date = format.format(new Date());
            //零点开始
            if (date.substring(8, 12).equals("0000")) {
                date = date.substring(0, 8) + "000000";
                logger.info("已到凌晨..开始工作");
                try {
                    if (dmogo.findycdata(agentId, Integer.parseInt(date.substring(0, 8)))) {
                        /********************清理数据****************/
                        boolean is;
                        //当天电网放电清零
                        is = mogo.updateEmulatorRegister(agentId, "DWhExp", 0.0);
                        if (is)
                            logger.debug(agentId + "_DWhExp 电网放已清零..");
                        //当天逆变器放电清零
                        is = mogo.updateEmulatorRegister(agentId, "DCkWh", 0.0);
                        if (is)
                            logger.debug(agentId + "_DCkWh 逆变器放已清零..");

                        //当天电网充电清零
                        is = mogo.updateEmulatorRegister(agentId, "DWhImp", 0.0);
                        if (is)
                            logger.debug(agentId + "_DWhImp 电网充已清零..");
                        //当天逆变器充电清零
                        is = mogo.updateEmulatorRegister(agentId, "DDkWh", 0.0);
                        if (is)
                            logger.debug(agentId + "_DDkWh 逆变器充已清零..");

                        //当天PV电量清零
                        is = mogo.updateEmulatorRegister(agentId, "DYield", 0.0);
                        if (is)
                            logger.debug(agentId + "_DYield PV已清零..");
                        //负载当天
                        is = mogo.updateEmulatorRegister(agentId, "loadDWhImp", 0.0);
                        if (is)
                            logger.debug(agentId + "_loadDWhImp load已清零..");

                        Object num = mogo.findEmulatorRegister(agentId, "TYield");
                        double TYield = num != null ? (double) num : 0.0;
                        num = mogo.findEmulatorRegister(agentId, "DYield");
                        double DYield = num != null ? (double) num : 0.0;
                        is = mogo.updateEmulatorRegister(agentId, "TYield", TYield + DYield);
                        if (is)
                            logger.debug(agentId + "TYield累加前一天.");
                        //添加预测数据
                        PVpredict p = new PVpredict(dmogo);
                        p.PVInfo(date, agentId, 0, pvcloud());
                    }
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
            /*结束*/

            STROAGE_002 = (BatConfig) Registry.INSTANCE.getValue().get(agentId + "_STROAGE_002Config");
            Object batReceive = mogo.findEmulatorRegister(agentId, agentId + "120");
            if (batReceive != null) {
                Reg12551 = (Double) batReceive;
            } else {
                mogo.updateEmulatorRegister(agentId, agentId + "120", 0.0);
            }
            GetDataAll dataAll = new GetDataAll(Reg12551, STROAGE_002, mogo, jgtime);
            String msg = dataAll.getDate(agentId);
            mqttConnThread = Registry.INSTANCE.getSession().get(agentId);
            topicall = getTopic(agentId);
            if (mqttConnThread != null && mqttConnThread.getMqttClient().isConnected()) {
                mqttConnThread.sendMessage(topicall, msg);
                //间隔时间差
                long interval ;
                //总时间差
                long startDate;
                Date now = new Date();
                interval = (now.getTime() - (long) Registry.INSTANCE.getValue().get(agentId + "_jgdate")) / 1000;
                long startThtead = (now.getTime() - timeThread.getTime()) / 1000;
                startDate=(now.getTime() - (long) Registry.INSTANCE.getValue().get("startDate")) / 1000;
                logger.info("\n##########start###########\nThread[{}]Send Data Info:\nAgentID:\t{}\nTopic:\t{}\nTime Interval:\t{} \tSet the time:{}\tThread run time:{}\tserver run time:{}\nSend Data:\t{}\nOther Info:\t{}\n##########end###########"
                        , super.getId(), agentId, topic, interval, jgtime,startThtead, startDate, msg, "other");
            } else {
                logger.info(agentId + "MQTT链接信息错误,链接失败");
                logger.debug(agentId + "发送的数据为：" + msg);
            }
            //更新间隔时间
            Registry.INSTANCE.saveKey(agentId + "_jgdate", new Date().getTime());
            try {
                sleep(jgtime * 1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }
        logger.debug(agentId + "Old  worker thread:" + super.getId() + "End!");
        return;
    }

    /**
     * Get Topic
     */
    private String getTopic(String agentId) {
        return "agents/" + agentId + "/" + topic;
    }

    //太阳能云因子
    private static int[] pvcloud() {
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
