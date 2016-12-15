package io.j1st.data.job;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.j1st.data.entity.Registry;
import io.j1st.data.entity.config.BatConfig;
import io.j1st.util.entity.EmsData;
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
    Map<String, Object> storage01 = new HashMap<>();
    private EmsData emsData01 = new EmsData();
    //电池参数
    Map<String, Object> storage02 = new HashMap<>();
    private EmsData emsData02 = new EmsData();//
    //GRID参数
    Map<String, Object> grid = new HashMap<>();
    private EmsData gridData = new EmsData();//
    //PV参数
    Map<String, Object> pv = new HashMap<>();
    private EmsData pvData = new EmsData();//
    //load参数
    Map<String, Object> load = new HashMap<>();
    private EmsData loadData = new EmsData();//

    public String getDate(String agentID) {

        Date now = new Date();
        //间隔时间差
        long interval = 0;
        //总时间差
        long startDate = 0;
        try {
            interval = (now.getTime() - (long) Registry.INSTANCE.getValue().get(agentID+"_date")) / 1000 ;
            startDate = (now.getTime() - (long) Registry.INSTANCE.getValue().get("startDate")) / 1000 ;
            SimpleDateFormat dateFormat = new SimpleDateFormat("HHmm");//可以方便地修改日期格式
            String date = dateFormat.format(now);
            if (date.equals("00:00")) {
                logger.info("凌晨了,当天功率需要重新计算");
            }
        } catch (NullPointerException e) {
            logger.debug("过滤初始0");
        }
        /*信息打印*/
        logger.debug(agentID+"本次间隔:" + interval + "秒");
        /* 结束 */
        List<EmsData> datas = new ArrayList<>();
        discharge(interval, startDate,agentID);
        getPvData();
        getLoadData();
        //填装数据

        emsData01.setType("120");
        emsData01.setDsn(agentID+"storage01");
        emsData01.setValues(storage01);

        emsData02.setType("801");
        emsData02.setDsn(agentID+"storage02");
        emsData02.setValues(storage02);

        gridData.setType("202");
        gridData.setDsn(agentID+"grid");
        gridData.setValues(grid);

        loadData.setType("201");
        loadData.setDsn(agentID+"load");
        loadData.setValues(load);

        pvData.setType("103");
        pvData.setDsn(agentID+"pv");
        pvData.setValues(pv);



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

    private void discharge(long interval, long startDate,String agentID) {
        //电网参数
        double TotWh;//组合总和TotWhImp+TotWhExp
        Object num = Registry.INSTANCE.getValue().get(agentID+"_TotWhImp");
        double TotWhImp = (num == null ? 0.0 : (double) num);//电网正向有功总电能  (放电总功率)
        num = Registry.INSTANCE.getValue().get(agentID+"_TotWhExp");
        double TotWhExp = (num == null ? 0.0 : (double) num);//电网负向有功总电能  (充电总功率)
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
        //电池参数
        num = Registry.INSTANCE.getValue().get(agentID+"_Soc");
        double Soc = (num == null ? STROAGE_002.SoC : (double) num);//当前电量百分比
        double dqrl;//当前容量kw/h
        double BV;//电压
        double BI;// 电流
        double TCkWh;//总排放功率（千瓦时）
        double DCkWh;//当天的总功率
        PDC = WHRtg * ((Reg12551 / 1000.0));//总功率*功率百分比   当前放电充电瞬时功率
        PAC = PDC / EFF;
        W = PAC;
        //储能放电
        if (Reg12551 > 0)//使用受到到放电功率计算
        {
            double J_TotWhImp = PDC * (((double) interval) / 3600);//当前间隔放电消耗功率
            dqrl = WHRtg * Soc - J_TotWhImp;
            Soc = dqrl / WHRtg;
            /*Soc>20 BV=1.312SOC+293.8  Soc<=20   BV=1SOC+260 */
            if (Soc > 20) {
                BV = 1.312 * Soc * 100 + 293.8;
            } else {
                BV = 1 * Soc * 100 + 260;
            }

            BI = (PDC * 1000) / BV;

            if (Registry.INSTANCE.getValue().get(agentID+"_TotWhImp") != null) {
                TotWhImp += J_TotWhImp;
            }
            Registry.INSTANCE.saveKey(agentID+"_TotWhImp", TotWhImp);

        } else //充电
        {
            double J_TotWhExp = PDC * (((double) interval) / 3600);//当前间隔充电消耗功率
            dqrl = WHRtg * Soc - J_TotWhExp;
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
            if (Registry.INSTANCE.getValue().get(agentID+"_TotWhExp") != null) {
                TotWhExp += J_TotWhExp;
            }
            Registry.INSTANCE.saveKey(agentID+"_TotWhExp", TotWhExp);
        }
        TotWh = TotWhExp + TotWhImp;
        TCkWh = TotWhImp - TotWhExp;
        DCkWh = TCkWh;
        Registry.INSTANCE.saveKey(agentID+"_Soc", Soc);//本次间隔Soc
        logger.debug(agentID+"存Soc值为:" + Registry.INSTANCE.getValue().get(agentID+"_Soc"));
        //逆变器

        storage01.put(Values.PDC, GttRetainValue.getRealVaule(PDC, 3));
        storage01.put(Values.PAC, GttRetainValue.getRealVaule(PAC, 3));
        storage01.put(Values.BI, GttRetainValue.getRealVaule(BI, 3));
        storage01.put(Values.BV, GttRetainValue.getRealVaule(BV, 3));
        storage01.put(Values.TCkWh, GttRetainValue.getRealVaule(TCkWh, 3));
        storage01.put(Values.DCkWh, GttRetainValue.getRealVaule(DCkWh, 3));
        //储能
        storage02.put(Values.WHRtg, GttRetainValue.getRealVaule(WHRtg, 3));
        storage02.put(Values.SoCNpMaxPct, STROAGE_002.SoCNpMaxPct);
        storage02.put(Values.SoCNpMinPct, STROAGE_002.SoCNpMinPct);
        storage02.put(Values.SoC, GttRetainValue.getRealVaule(Soc, 3));
        storage02.put(Values.MaxRsvPct, GttRetainValue.getRealVaule(MaxRsvPct, 3));
        storage02.put(Values.MinRsvPct, GttRetainValue.getRealVaule(MinRsvPct, 3));
        //电网电表
        grid.put(Values.TotWh, GttRetainValue.getRealVaule(TotWh, 3));
        grid.put(Values.TotWhExp, GttRetainValue.getRealVaule(TotWhExp, 3));
        grid.put(Values.TotWhImp, GttRetainValue.getRealVaule(TotWhImp, 3));
        grid.put(Values.W, GttRetainValue.getRealVaule(W, 3));
        grid.put(Values.VAR, GttRetainValue.getRealVaule(VAR, 3));
        grid.put(Values.PF, GttRetainValue.getRealVaule(PF, 3));
        grid.put(Values.Hz, GttRetainValue.getRealVaule(Hz, 3));
        grid.put(Values.Evt, GttRetainValue.getRealVaule(Evt, 3));

    }

    private void getPvData() {
        Clculate clculate = new Clculate();
        pv.put(Values.Pac, clculate.TotalCalc().get("eToday"));
        pv.put(Values.TYield, clculate.TotalCalc().get("pVPower"));
    }

    private void getLoadData() {
        Clculate clculate = new Clculate();
        load.put(Values.W, clculate.TotalCalc().get("powerT"));
        load.put(Values.TotWhImp, clculate.TotalCalc().get("pVPower"));
        load.put(Values.TotWhExp, clculate.TotalCalc().get("meterT"));
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
