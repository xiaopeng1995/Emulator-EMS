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
import java.util.concurrent.Callable;
import java.util.jar.Pack200;

/**
 * Created by xiaopeng on 2016/12/14.
 */
public class Job extends Thread {
    Logger logger = LoggerFactory.getLogger(Job.class);
    public volatile boolean exit = false;
    private BatConfig STROAGE_002;
    private String agentId;
    private int time;
    private double Reg12551;
    private String topic;
    private MongoStorage mogo;
    private DataMongoStorage dmogo;

    public Job(String agentid, int time, String topic, MongoStorage mogo, DataMongoStorage dmogo) {
        this.agentId = agentid;
        this.time = time;
        this.topic = topic;
        this.mogo = mogo;
        this.dmogo = dmogo;
    }

    public void run() {
        // mqtt topic
        String topic;
        while (!exit) {
            //定时任务
            SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmmss");
            String date = format.format(new Date());
            //零点开始
            if (date.substring(8).equals("000000")) {
                logger.info("已到凌晨..开始工作");
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
                try {
                    //添加预测数据
                    PVpredict p = new PVpredict(dmogo);
                    p.PVInfo(date, agentId, 0);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
            /*结束*/
            logger.debug("执行线程:" + super.getId());
            MqttConnThread mqttConnThread;
            STROAGE_002 = (BatConfig) Registry.INSTANCE.getValue().get(agentId + "_STROAGE_002Config");
            Object batReceive = mogo.findEmulatorRegister(agentId, agentId + "120");
            if (batReceive != null) {
                Reg12551 = (Double) batReceive;
            } else {
                mogo.updateEmulatorRegister(agentId, agentId + "120", 0.0);
            }
            GetDataAll dataAll = new GetDataAll(Reg12551, STROAGE_002, mogo);
            String msg = dataAll.getDate(agentId);
            mqttConnThread = Registry.INSTANCE.getSession().get(agentId);
            topic = getTopic(agentId);
            if (mqttConnThread != null && mqttConnThread.getMqttClient().isConnected()) {
                mqttConnThread.sendMessage(topic, msg);
                logger.debug(agentId + "发送的数据为：" + msg);
                //更新间隔时间
                Registry.INSTANCE.saveKey(agentId + "_date", new Date().getTime());
            } else {
                logger.info("MQTT链接信息错误,链接失败");
                logger.debug(agentId + "发送的数据为：" + msg);
            }
            try {
                Thread.sleep(time * 1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }
        logger.debug("旧线程:" + super.getId() + "已退出!");
    }

    /**
     * Get Topic
     */
    private String getTopic(String agentId) {
        logger.debug("Topic:{}", topic);
        return "agents/" + agentId + "/" + topic;
    }
}
