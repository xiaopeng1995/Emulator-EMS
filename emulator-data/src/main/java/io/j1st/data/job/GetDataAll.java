package io.j1st.data.job;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.j1st.data.entity.Registry;
import io.j1st.data.entity.config.BatConfig;
import io.j1st.storage.entity.Value;
import io.j1st.util.entity.EmsData;
import io.j1st.util.entity.data.Device;
import io.j1st.util.entity.data.Values;
import io.j1st.util.util.GttRetainValue;
import io.j1st.util.util.JsonUtils;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.SimpleDateFormat;
import java.util.*;

/**
 * 获取数据
 */
public class GetDataAll {
    private double Reg12551;//以多少功率的百分比
    Logger logger = LoggerFactory.getLogger(GetDataAll.class);
    private BatConfig STROAGE_002;

    public GetDataAll(double Reg12551, BatConfig STROAGE_002) {
        this.Reg12551 = Reg12551;
        this.STROAGE_002 = STROAGE_002;
    }


    //逆变器参数
    Map<String, Object> data120 = new HashMap<>();
    private EmsData emsData01 = new EmsData();
    //电池参数
    Map<String, Object> data801 = new HashMap<>();
    private EmsData emsData02 = new EmsData();//
    //GRID参数
    Map<String, Object> data202 = new HashMap<>();
    private EmsData gridData = new EmsData();//
    //PV参数
    Map<String, Object> data103 = new HashMap<>();
    private EmsData pvData = new EmsData();//
    //load参数
    Map<String, Object> data201 = new HashMap<>();
    private EmsData loadData = new EmsData();//


    //根数据
    List<EmsData> datas = new ArrayList<>();

    public String getDate(String agentID) {

        Date now = new Date();
        //间隔时间差
        long interval = 0;
        //总时间差
        long startDate = 0;
        try {
            interval = (now.getTime() - (long) Registry.INSTANCE.getValue().get(agentID + "_date")) / 1000;
            startDate = (now.getTime() - (long) Registry.INSTANCE.getValue().get("startDate")) / 1000;
            SimpleDateFormat dateFormat = new SimpleDateFormat("HHmm");//可以方便地修改日期格式
            String date = dateFormat.format(now);
            if (date.equals("00:00")) {
                logger.info("凌晨了,当天功率需要重新计算");
            }
        } catch (NullPointerException e) {
            logger.debug("过滤初始0");
        }
        /*信息打印*/
        logger.info(agentID + "本次间隔:" + interval + "秒");
        /* 结束 */

        discharge(interval, startDate, agentID);
        getPvData(agentID);
        getLoadData();
        //填装数据

        emsData01.setType("120");
        emsData01.setDsn(agentID + "120");
        emsData01.setValues(data120);

        emsData02.setType("801");
        emsData02.setDsn(agentID + "801");
        emsData02.setValues(data801);

        gridData.setType("202");
        gridData.setDsn(agentID + "202");
        gridData.setValues(data202);

        loadData.setType("201");
        loadData.setDsn(agentID + "201");
        loadData.setValues(data201);

        pvData.setType("103");
        pvData.setDsn(agentID + "103");
        pvData.setValues(data103);

        //packing
        Object datapacking = Registry.INSTANCE.getValue().get(agentID + "_packing");
        //对应类型120 810 202 201 130
        int[] packing = {1, 1, 1, 1, 1,};
        if (datapacking != null)
            packing = (int[]) datapacking;
        if (packing[0] < 100) {
            for (int i = 0; i < packing[0]; i++) {

                datas.add(emsData01);
            }
        } else { //告警数据
            getAlarm(packing[0], agentID, "120", data120);
        }

        if (packing[1] < 100) {
            for (int i = 0; i < packing[1]; i++) {
                datas.add(emsData02);
            }
        } else {//告警数据
            getAlarm(packing[1], agentID, "801", data801);
        }

        if (packing[2] < 100) {
            for (int i = 0; i < packing[2]; i++) {
                datas.add(gridData);
            }
        } else {//告警数据
            getAlarm(packing[2], agentID, "202", data202);
        }
        if (packing[3] < 100) {
            for (int i = 0; i < packing[3]; i++) {
                datas.add(loadData);
            }
        } else {//告警数据
            getAlarm(packing[3], agentID, "201", data201);
        }
        if (packing[4] < 100) {
            for (int i = 0; i < packing[4]; i++) {
                datas.add(pvData);
            }
        } else {//告警数据
            getAlarm(packing[4], agentID, "103", data103);
        }

        String msg = null;
        try {
            msg = JsonUtils.Mapper.writeValueAsString(datas);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return msg;
    }

    private void discharge(long interval, long startDate, String agentID) {
        //电网参数
        double TotWh;//组合总和TotWhImp+TotWhExp
        Object num = Registry.INSTANCE.getValue().get(agentID + "_TotWhImp");
        double TotWhImp = (num == null ? 0.0 : (double) num);//电网正向有功总电能  (放电总功率)
        double DWhImp = 0.0;

        num = Registry.INSTANCE.getValue().get(agentID + "_TotWhExp");
        double TotWhExp = (num == null ? 0.0 : (double) num);//电网负向有功总电能  (充电总功率)
        double DWhExp = 0.0;

        double VAR = 0.0;//Reactive Power 瞬时总无功功率 kw
        double PF = Math.random();//Power Factor 总功率因数
        double Hz = 50.0;//电网频率
        double Evt = 0.0;//标志事件?
        //逆变器 电池参数
        double WHRtg = STROAGE_002.WHRtg;//电池总能量
        double PDC;//充电放电功率
        double EFF = ((1.0 + Math.random() * (10.0 - 1.0 + 1.0)) / 100.0 + 0.75);
        double PAC;//Active power from inverter 来自逆变器的有功功率
        double W;//Total Real Power 瞬时总有功功率 kw
        double MaxRsvPct = STROAGE_002.MaxRsvPct;
        double MinRsvPct = STROAGE_002.MinRsvPct;
        double TCkWh;//总充电电量
        double DCkWh=0.0;//当天的总充电电量
        double TDkWh;//总放电电量
        double DDkWh=0.0;//当天总放电电量


        //电池参数
        num = Registry.INSTANCE.getValue().get(agentID + "_Soc");
        double Soc = (num == null ? STROAGE_002.SoC : (double) num);//当前电量百分比
        double dqrl;//当前容量kw/h
        double BV;//电压
        double BI;// 电流

        PDC = WHRtg * ((Reg12551 / 1000.0));//总功率*功率百分比   当前放电充电瞬时功率
        PAC = PDC / EFF;
        W = PAC;
        //储能放电
        if (Reg12551 > 0)//使用受到到放电功率计算
        {
            double J_TotWhExp = PDC * (((double) interval) / 3600);//当前间隔放电消耗功率
            dqrl = WHRtg * Soc - J_TotWhExp;
            Soc = dqrl / WHRtg;
            /*Soc>20 BV=1.312SOC+293.8  Soc<=20   BV=1SOC+260 */
            if (Soc > 20) {
                BV = 1.312 * Soc * 100 + 293.8;
            } else {
                BV = 1 * Soc * 100 + 260;
            }

            BI = (PDC * 1000) / BV;
            //更新累计值
            TotWhExp += J_TotWhExp;
            Registry.INSTANCE.saveKey(agentID + "_TotWhExp", TotWhExp);

            //去内存获取当天情况
            num = Registry.INSTANCE.getValue().get(agentID + "_DCkWh");
            DCkWh = num != null ? (double) num+J_TotWhExp : TotWhExp;
            Registry.INSTANCE.saveKey(agentID + "_DCkWh",DCkWh);

        } else //充电
        {
            double J_TotWhImp = PDC * (((double) interval) / 3600);//当前间隔充电消耗功率
            dqrl = WHRtg * Soc - J_TotWhImp;
            Soc = dqrl / WHRtg;
            /*  Soc>80 BV=425 Soc<=80  BV=16.5Soc+316.44  Soc<10  BV=2Soc+260  */
            if (Soc > 0.8) {
                BV = 425.0;
            } else if (Soc <= 0.80 & Soc > 0.10) {
                BV = 1.65 * (Soc * 100) + 316.44;
            } else {
                BV = 2 * (Soc * 100) + 260;
            }
            BI = (PDC * 1000) / BV + ((Math.random() * 3) / 10);
            //更新累计值
            TotWhImp += J_TotWhImp;
            Registry.INSTANCE.saveKey(agentID + "_TotWhImp", TotWhImp);
            //去内存获取当天情况
            num = Registry.INSTANCE.getValue().get(agentID + "_DDkWh");
            DDkWh = num != null ? (double) num+J_TotWhImp : -TotWhImp;//当天
            Registry.INSTANCE.saveKey(agentID + "_DDkWh",DDkWh);
        }
        TotWh = TotWhExp + TotWhImp;
        TCkWh = -TotWhImp;//总充电电量
        TDkWh = TotWhExp;//总放电 电量


        DWhImp = -DCkWh;
        DWhExp = DDkWh;

        Registry.INSTANCE.saveKey(agentID + "_Soc", Soc);//本次间隔Soc
        logger.debug(agentID + "存Soc值为:" + Registry.INSTANCE.getValue().get(agentID + "_Soc"));
        //逆变器

        data120.put(Values.PDC, GttRetainValue.getRealVaule(PDC, 2));
        data120.put(Values.PAC, GttRetainValue.getRealVaule(PAC, 2));
        data120.put(Values.BI, GttRetainValue.getRealVaule(BI, 3));
        data120.put(Values.BV, GttRetainValue.getRealVaule(BV, 3));
        data120.put(Values.TCkWh, GttRetainValue.getRealVaule(TCkWh, 2));
        data120.put(Values.DCkWh, GttRetainValue.getRealVaule(DCkWh, 2));
        data120.put(Values.TDkWh, GttRetainValue.getRealVaule(TDkWh, 2));
        data120.put(Values.DDkWh, GttRetainValue.getRealVaule(DDkWh, 2));
        //储能
        data801.put(Values.WHRtg, GttRetainValue.getRealVaule(WHRtg, 2));
        data801.put(Values.SoCNpMaxPct, STROAGE_002.SoCNpMaxPct);
        data801.put(Values.SoCNpMinPct, STROAGE_002.SoCNpMinPct);
        data801.put(Values.SoC, GttRetainValue.getRealVaule(Soc*100, 2));
        data801.put(Values.MaxRsvPct, GttRetainValue.getRealVaule(MaxRsvPct, 3));
        data801.put(Values.MinRsvPct, GttRetainValue.getRealVaule(MinRsvPct, 3));
        data801.put(Values.WMaxChaRte, GttRetainValue.getRealVaule(STROAGE_002.WMaxChaRte, 2));
        data801.put(Values.WMaxDisChaRte, GttRetainValue.getRealVaule(STROAGE_002.WMaxDisChaRte, 2));
        //电网电表
        data202.put(Values.DWhImp, GttRetainValue.getRealVaule(DWhImp, 2));
        data202.put(Values.DWhExp, GttRetainValue.getRealVaule(DWhExp, 2));
        data202.put(Values.TotWh, GttRetainValue.getRealVaule(TotWh, 2));
        data202.put(Values.TotWhExp, GttRetainValue.getRealVaule(TotWhExp, 2));
        data202.put(Values.TotWhImp, GttRetainValue.getRealVaule(TotWhImp, 2));
        data202.put(Values.W, GttRetainValue.getRealVaule(W, 2));
        data202.put(Values.VAR, GttRetainValue.getRealVaule(VAR, 2));
        data202.put(Values.PF, GttRetainValue.getRealVaule(PF, 2));
        data202.put(Values.Hz, GttRetainValue.getRealVaule(Hz, 2));
        data202.put(Values.Evt, GttRetainValue.getRealVaule(Evt, 2));

    }

    private void getPvData(String agentID) {

        Clculate clculate = new Clculate();
        double Pac = ((double) clculate.TotalCalc().get("pVPower") / 1000);
        data103.put(Values.Pac, GttRetainValue.getRealVaule(Pac, 2));

        //去内存获取累计情况
        Object num = Registry.INSTANCE.getValue().get(agentID + "_TYield");
        double eToday = (double) clculate.TotalCalc().get("eToday");
        double TYield = eToday;
        if (num != null)
            TYield += (double) num;
        Registry.INSTANCE.saveKey(agentID + "_TYield", TYield);

        //去内存获取当天情况
        double DYield = eToday;
        num = Registry.INSTANCE.getValue().get(agentID + "_DYield");
        if (num != null)
            DYield += (double) num;
        Registry.INSTANCE.saveKey(agentID + "_DYield", DYield);

        data103.put(Values.DYield, GttRetainValue.getRealVaule(DYield, 2));
        data103.put(Values.TYield, GttRetainValue.getRealVaule(TYield, 2));
    }

    private void getLoadData() {
        Clculate clculate = new Clculate();
        data201.put(Values.W, clculate.TotalCalc().get("powerT"));
        data201.put(Values.TotWhImp, clculate.TotalCalc().get("pVPower"));
        data201.put(Values.TotWhExp, clculate.TotalCalc().get("meterT"));
    }

    private static Number getRealVaule(double value, int resLen) {
        if (resLen == 0)
            //原理:123.456*10=1234.56+5=1239.56/10=123
            //原理:123.556*10=1235.56+5=1240.56/10=124
            return Math.round(value * 10 + 5) / 10;
        double db = Math.pow(10, resLen);
        return Math.round(value * db) / db;
    }

    private void getAlarm(int number, String agentID, String type, Map<String, Object> data) {
        //告警参数
        Map<String, Object> noDevice = new HashMap<>();
        EmsData device = new EmsData();
        if (number == 101)//No Device（Node未激活或无法初始化通讯）
        {
            noDevice.put(Values.RunTime, 1);
            device.setValues(noDevice);
            device.setAsn(agentID.substring(10, 20) + type);
            device.setType("AGENT");


        } else if (number == 102) {//Device disconnect（Node通讯异常）
            noDevice.put(Values.WarnV, "101");
            noDevice.put(Values.WarnD, "Device communication is lost");
            noDevice.put(Values.WarnT, "待定");
            device.setValues(noDevice);
            device.setDsn(agentID + type);
            device.setType(type);

        } else if (number == 103) {//Device fault（Node运行出现错误）
            data.put(Values.FaultV, "103");
            data.put(Values.FaultD, "Device fault");
            data.put(Values.FaultT, "aaa");
            device.setValues(data);
            device.setDsn(agentID + type);
            device.setType(type);


        } else if (number == 104) {//Device warning （Node运行出现警告）
            data.put(Values.WarnV, "101");
            data.put(Values.WarnD, "Device communication is lost");
            data.put(Values.WarnT, "待定");
            device.setValues(data);
            device.setDsn(agentID + type);
            device.setType(type);

        }
        datas.add(device);
    }
}
