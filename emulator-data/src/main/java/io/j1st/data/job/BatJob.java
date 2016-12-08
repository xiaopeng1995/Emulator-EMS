package io.j1st.data.job;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.j1st.data.entity.Registry;
import io.j1st.data.mqtt.MqttConnThread;
import io.j1st.util.entity.EmsData;
import io.j1st.util.entity.bat.BatReceive;
import io.j1st.util.entity.data.Values;
import io.j1st.util.util.GttRetainValue;
import io.j1st.util.util.JsonUtils;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.SimpleDateFormat;
import java.util.*;

/**
 * 电池模拟数据工作
 */
public class BatJob implements Job {
    Logger logger = LoggerFactory.getLogger(BatJob.class);
    private PropertiesConfiguration STROAGE_002;

    private int Reg12551;
    //逆变器参数
    Map<String, String> storage01 = new HashMap<>();
    private EmsData emsData01 = new EmsData();
    //电池参数
    Map<String, String> storage02 = new HashMap<>();
    private EmsData emsData02 = new EmsData();//
    //GRID参数
    Map<String, String> grid = new HashMap<>();
    private EmsData gridData = new EmsData();//
    //PV参数
    Map<String, String> pv = new HashMap<>();
    private EmsData pvData = new EmsData();//
    //load参数
    Map<String, String> load = new HashMap<>();
    private EmsData loadData = new EmsData();//

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
        STROAGE_002 = Registry.INSTANCE.getConfig().get("STROAGE_002");
        BatReceive batReceive = (BatReceive) Registry.INSTANCE.getValue().get("AB123456");
        if (batReceive != null) {
            Reg12551 = batReceive.getSetMHReg().get(0).getReg12551();
            logger.info("Reg12551:" + Reg12551);
        }
        logger.info("Data:" + getDate());
        String msg = getDate();
        logger.debug("开始发数据" + msg);
        mqttConnThread = Registry.INSTANCE.getSession().get("874804605");
        topic = getTopic();
        if (mqttConnThread != null && mqttConnThread.getMqttClient().isConnected()) {
            mqttConnThread.sendMessage(topic, msg);
            logger.debug("发送的数据为：" + msg);
        } else {
            logger.info("MQTT链接信息错误,链接失败");
        }

    }

    /**
     * Get Topic
     */
    private static String getTopic() {
        return "jsonUp";
    }

    private String getDate() {
        emsData01.setType("120");
        emsData01.setDsn("AB123456");

        emsData02.setType("801");
        emsData02.setDsn("ST123456");

        gridData.setType("202");
        gridData.setDsn("GR123456");

        loadData.setType("201");
        loadData.setDsn("LO123456");

        pvData.setType("103");
        pvData.setDsn("PV123456");
//        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");//可以方便地修改日期格式
//        String date = dateFormat.format(new Date());
//        int hh = Integer.parseInt(date.substring(8, 10));
//        int mm = Integer.parseInt(date.substring(10, 12));
//        int ss = Integer.parseInt(date.substring(12, 14));
//        double i = hh * 60 + mm;
        Date now = new Date();
        long interval = (now.getTime() - (long) Registry.INSTANCE.getValue().get("date")) / 1000;
        List<EmsData> datas = new ArrayList<>();
        if (Reg12551 != 0)//计算
        {
            discharge(interval);
        } else  //默认值
        {
            getDefault();
        }
        getPvData();
        getLoadData();
        emsData01.setValues(storage01);
        emsData02.setValues(storage02);
        pvData.setValues(pv);
        loadData.setValues(load);
        gridData.setValues(grid);
        datas.add(emsData01);
        datas.add(emsData02);
        datas.add(gridData);
        datas.add(loadData);
        datas.add(pvData);
        String msg = null;
        try {
            msg = JsonUtils.Mapper.writeValueAsString(datas);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return msg;
    }

    private void discharge(long interval) {
        double WHRtg = STROAGE_002.getInt("WHRtg");//电池总能量
        double PDC;//充电放电功率
        double EFF = ((1.0 + Math.random() * (10.0 - 1.0 + 1.0)) / 100.0 + 0.75);
        double PAC;//Active power from inverter 来自逆变器的有功功率
        double W;
        double MaxRsvPct = STROAGE_002.getInt("MaxRsvPct");
        double MinRsvPct = STROAGE_002.getInt("MinRsvPct");
        double TotWh = 0.0;
        double TotWhImp = 0.0;
        double TotWhExp = 0.0;
        double VAR = 0.0;
        double PF = 0.0;
        double Hz = 0.0;
        double Evt = 0.0;
        double Soc = (double) STROAGE_002.getInt("SoC") / 100;
        double dqrl;//当前容量kw/h
        double BV;//电压
        double BI;// 电流
        double TCkWh;//总排放功率（千瓦时）
        double DCkWh;

        //储能放电
        if (Reg12551 > 0)//使用受到到放电功率计算
        {
            PDC = WHRtg * ((Reg12551 / 1000.0));
            TCkWh=PDC * (((double) interval) / 3600);
            PAC = PDC / EFF;
            W = PAC;
            dqrl = WHRtg * Soc - TCkWh;
            Soc = dqrl / WHRtg;
            BV = (dqrl * 4.2 / WHRtg);
            BI = dqrl / BV;
            DCkWh=TCkWh/EFF;

        } else //充电
        {
            PDC = WHRtg * (((Reg12551 * -1) / 1000.0));
            TCkWh=PDC * (((double) interval) / 3600);
            PAC = PDC / EFF;
            W = PAC;
            dqrl = WHRtg * Soc + TCkWh;
            Soc = dqrl / WHRtg;
            BV = (dqrl * 4.2 / WHRtg);
            BI = dqrl / BV;
            PDC = PDC * -1;
            DCkWh=TCkWh/EFF;
        }
        //逆变器

        storage01.put(Values.PDC, GttRetainValue.getRealVaule(PDC, 3) + "");
        storage01.put(Values.PDC, GttRetainValue.getRealVaule(PDC, 3) + "");
        storage01.put(Values.PAC, GttRetainValue.getRealVaule(PAC, 3) + "");
        storage01.put(Values.BI, GttRetainValue.getRealVaule(BI, 3) + "");
        storage01.put(Values.BV, GttRetainValue.getRealVaule(BV, 3) + "");
        storage01.put(Values.TCkWh, GttRetainValue.getRealVaule(TCkWh, 3)+"" );
        storage01.put(Values.DCkWh, GttRetainValue.getRealVaule(DCkWh, 3)+"" );
        //储能
        storage02.put(Values.WHRtg, "" + GttRetainValue.getRealVaule(WHRtg, 3));
        storage02.put(Values.SoCNpMaxPct, STROAGE_002.getString("SoCNpMaxPct"));
        storage02.put(Values.SoCNpMinPct, STROAGE_002.getString("SoCNpMinPct"));
        storage02.put(Values.SoC, GttRetainValue.getRealVaule(Soc, 3) + "");
        storage02.put(Values.MaxRsvPct, GttRetainValue.getRealVaule(MaxRsvPct, 3) + "");
        storage02.put(Values.MinRsvPct, GttRetainValue.getRealVaule(MinRsvPct, 3) + "");
        //电网电表
        grid.put(Values.TotWh, GttRetainValue.getRealVaule(TotWh, 3) + "");
        grid.put(Values.TotWhExp, GttRetainValue.getRealVaule(TotWhExp, 3) + "");
        grid.put(Values.TotWhImp, GttRetainValue.getRealVaule(TotWhImp, 3) + "");
        grid.put(Values.W, GttRetainValue.getRealVaule(W, 3) + "");
        grid.put(Values.VAR, GttRetainValue.getRealVaule(VAR, 3) + "");
        grid.put(Values.PF, GttRetainValue.getRealVaule(PF, 3) + "");
        grid.put(Values.Hz, GttRetainValue.getRealVaule(Hz, 3) + "");
        grid.put(Values.Evt, GttRetainValue.getRealVaule(Evt, 3) + "");

    }

    private void getDefault() {    // TODO:
        double WHRtg = STROAGE_002.getInt("WHRtg");
        double PDC = WHRtg * Reg12551 / 1000;
        double PAC = PDC / ((int) (1 + Math.random() * (10 - 1 + 1)) / 100 + 0.75);
        double W = PAC;
        double BV = 0;
        double BI = 0;
        double MaxRsvPct = STROAGE_002.getInt("MaxRsvPct");
        double MinRsvPct = STROAGE_002.getInt("MinRsvPct");
        double TotWh = 0.0;
        double TotWhImp = 0.0;
        double TotWhExp = 0.0;
        double VAR = 0.0;
        double PF = 0.0;
        double Hz = 0.0;
        double Evt = 0.0;
        double Soc = (double) STROAGE_002.getInt("SoC") / 100;
        //逆变器

        storage01.put(Values.PDC, GttRetainValue.getRealVaule(PDC, 3) + "");
        storage01.put(Values.PDC, GttRetainValue.getRealVaule(PDC, 3) + "");
        storage01.put(Values.PAC, GttRetainValue.getRealVaule(PAC, 3) + "");
        storage01.put(Values.BI, GttRetainValue.getRealVaule(BI, 3) + "");
        storage01.put(Values.BV, GttRetainValue.getRealVaule(BV, 3) + "");
        //储能
        storage02.put(Values.WHRtg, "" + GttRetainValue.getRealVaule(WHRtg, 3));
        storage02.put(Values.SoCNpMaxPct, STROAGE_002.getString("SoCNpMaxPct"));
        storage02.put(Values.SoCNpMinPct, STROAGE_002.getString("SoCNpMinPct"));
        storage02.put(Values.SoC, GttRetainValue.getRealVaule(Soc, 3) + "");
        storage02.put(Values.MaxRsvPct, GttRetainValue.getRealVaule(MaxRsvPct, 3) + "");
        storage02.put(Values.MinRsvPct, GttRetainValue.getRealVaule(MinRsvPct, 3) + "");
        //电网电表
        grid.put(Values.TotWh, GttRetainValue.getRealVaule(TotWh, 3) + "");
        grid.put(Values.TotWhExp, GttRetainValue.getRealVaule(TotWhExp, 3) + "");
        grid.put(Values.TotWhImp, GttRetainValue.getRealVaule(TotWhImp, 3) + "");
        grid.put(Values.W, GttRetainValue.getRealVaule(W, 3) + "");
        grid.put(Values.VAR, GttRetainValue.getRealVaule(VAR, 3) + "");
        grid.put(Values.PF, GttRetainValue.getRealVaule(PF, 3) + "");
        grid.put(Values.Hz, GttRetainValue.getRealVaule(Hz, 3) + "");
        grid.put(Values.Evt, GttRetainValue.getRealVaule(Evt, 3) + "");

    }

    private void getPvData() {
        Clculate clculate = new Clculate();
        pv.put(Values.Pac, clculate.TotalCalc().get("eToday").toString());
        pv.put(Values.TYield, clculate.TotalCalc().get("pVPower").toString());
    }

    private void getLoadData() {
        Clculate clculate = new Clculate();
        load.put(Values.W, clculate.TotalCalc().get("powerT").toString());
        load.put(Values.TotWhImp, clculate.TotalCalc().get("pVPower").toString());
        load.put(Values.TotWhExp, clculate.TotalCalc().get("meterT").toString());
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
