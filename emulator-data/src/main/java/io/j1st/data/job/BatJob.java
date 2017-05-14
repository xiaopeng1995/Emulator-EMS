package io.j1st.data.job;

import io.j1st.data.mqtt.MqttConnThread;
import io.j1st.util.entity.data.Values;
import io.j1st.util.util.GetJsonEmsData;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 电池模拟数据工作
 */
public class BatJob implements Job {
    Logger logger = LoggerFactory.getLogger(BatJob.class);
    double  BattPC,
            BattCapa=30.0,//电池容量
            BattPChg = 6000.0,//最大充电功率
            BattPDis = 6000.0,//最大放电功率EMPara
            BattICapa = 15.0,//初始容量
            BattSOC,
            BattCSPara = 20.0;//停止补电容量EMPara
    double BattReal=1.02,BattCCE=0.8;
    private double effBatC = 0.99, effBatD = 0.98;

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        // mqtt topic
        String topic;

        MqttConnThread mqttConnThread;
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        String msg = getDate();
//        logger.debug("开始发数据" + Registry.INSTANCE.getValue().get("test"));
//        mqttConnThread = Registry.INSTANCE.getSession().get("874804605");
//        topic = getTopic();
//        if (mqttConnThread != null && mqttConnThread.getMqttClient().isConnected()) {
//            String msg = getDate();
//            mqttConnThread.sendMessage(topic, msg);
//            logger.debug("发送的数据为：" + msg);
//        } else {
//            logger.info("MQTT链接信息错误,链接失败");
//        }

    }

    /**
     * Get Topic
     */
    private static String getTopic() {
        return "jsonUp";
    }

    private  String getDate() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");//可以方便地修改日期格式
        String date =dateFormat.format(new Date());
        int hh = Integer.parseInt(date.substring(10, 12));
        int mm = Integer.parseInt(date.substring(12, 14));
        int ss=  Integer.parseInt(date.substring(14, 16));
        double itv=0.0,batSOC;
        double i = itv+ hh*60+mm;
        BattSOC =BattICapa;
        BattPC=0.0;
        //放电
             System.out.println( GenStorage(hh,i)); // Policy adopted HERE!查
            batSOC=getRealVaule(BattSOC, 2).doubleValue();
            System.out.println(batSOC);

        Values values = new Values();

        return GetJsonEmsData.getData(values, "801", "AB123456");
    }
    private double GenStorage(int hh,double itv) {    // TODO:
        double b= BattPC,BattE;
        //补电
        //当SOC小于停止补电容量并且系统时间小许晨谷结束时间或者当前时间大于等于晚谷开始时间
        if (BattSOC < BattCSPara&&
        hh < 6 || hh >= 22){
            // 电网补电
            if (BattSOC < BattCCE*BattCapa) { // CC
                BattPC = BattPChg;
                BattE=BattPC*itv/60000;
            } else {
                double resi = (BattReal -BattSOC / BattCapa) / (BattReal - BattCCE);
                BattPC=BattPChg * resi;
                if (b == 0) {
                   BattE= BattPC * itv / 60000;
                } else {
                    BattE=(BattPC * 0.6 + b * 0.4) * itv / 60000;
                }
            }
            BattSOC += BattE * effBatC;
        }
        return BattSOC;
    }
    private static Number getRealVaule(double value, int resLen) {
        if (resLen == 0)
            //原理:123.456*10=1234.56+5=1239.56/10=123
            //原理:123.556*10=1235.56+5=1240.56/10=124
            return Math.round(value * 10 + 5) / 10;
        double db = Math.pow(10, resLen);
        return Math.round(value * db) / db;
    }
}
