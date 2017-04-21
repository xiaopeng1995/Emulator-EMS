package io.j1st.data.job;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.j1st.data.entity.Registry;
import io.j1st.data.entity.config.BatConfig;
import io.j1st.storage.DataMongoStorage;
import io.j1st.storage.MongoStorage;
import io.j1st.util.entity.EmsData;
import io.j1st.util.entity.data.Values;
import io.j1st.util.util.GttRetainValue;
import io.j1st.util.util.JsonUtils;
import org.bson.Document;
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
    private MongoStorage mogo;
    private DataMongoStorage dmogo = (DataMongoStorage) Registry.INSTANCE.getValue().get("dmogo");
    private int jgtime;

    /**
     * @param Reg12551    充放电指令
     * @param STROAGE_002 配置
     * @param mogo        数据库操作
     * @param jgtime      间隔时间
     */
    public GetDataAll(double Reg12551, BatConfig STROAGE_002, MongoStorage mogo, int jgtime) {
        this.Reg12551 = Reg12551;
        this.STROAGE_002 = STROAGE_002;
        this.mogo = mogo;
        this.jgtime = jgtime;

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
        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("HHmm");//可以方便地修改日期格式
            String date = dateFormat.format(now);
            if (date.equals("00:00")) {
                logger.info("凌晨了,当天功率需要重新计算");
            }
        } catch (NullPointerException e) {
            logger.debug("过滤初始0");
        }

        Date now1 = new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmm");//可以方便地修改日期格式
        String date = dateFormat.format(now1);
        if (date.contains("0000"))
            date = date.replace("0000", "0001");
        getPvData(agentID, date);

        //packing 远程指令
        Object datapacking;
        //对应类型120 810 202 201 130
        //默认只有PV系统
        int[] packing = {0, 0, 0, 0, 0};

        //查找上传数据类型
        datapacking = mogo.findEmulatorRegister(agentID, "packing");
        //EMS 系统时才会计算一下值
        if (!datapacking.toString().equals("0,0,0,0,1")) {
            battery01(date, agentID);
        }

        emsData01.setType("SUNS120");
        pvData.setSta(0);
        emsData01.setDsn(agentID + "SUNS120");
        emsData01.setModel("SC36KTL-DO");
        emsData01.setValues(data120);

        emsData02.setType("SUNS801");
        pvData.setSta(0);
        emsData02.setDsn(agentID + "SUNS801");
        emsData02.setModel("ZE60BATTERY");
        emsData02.setValues(data801);

        gridData.setType("SUNS202");
        pvData.setSta(0);
        gridData.setDsn(agentID + "SUNS202");
        gridData.setModel("ZEMETERG");
        gridData.setValues(data202);

        loadData.setType("SUNS201");
        pvData.setSta(0);
        loadData.setDsn(agentID + "SUNS201");
        loadData.setModel("ZEMETERL");
        loadData.setValues(data201);

        pvData.setType("SUNS103");
        pvData.setSta(0);
        pvData.setDsn(agentID + "SUNS103");
        pvData.setModel("SC30KTL-DO");
        pvData.setValues(data103);


        if (datapacking != null) {
            String datapackings = datapacking.toString();
            String[] a = datapackings.split(",");
            for (int i = 0; i < a.length; i++) {
                try {
                    packing[i] = Integer.parseInt(a[i]);
                } catch (Exception e) {
                    packing[i] = 1;
                }

            }
        } else {
            logger.error("数据出错!!!!!!!!!!!!!!!!!!!!!!");
        }
        if (packing[0] == 101) {
            Map<String, Object> noDevice = new HashMap<>();
            EmsData device = new EmsData();
            noDevice.put(Values.RunTime, 1);
            device.setValues(noDevice);
            device.setAsn(agentID);
            device.setType("AGENT");
            datas.add(device);
        } else {
            if (packing[0] < 100) {
                for (int i = 0; i < packing[0]; i++) {

                    datas.add(emsData01);
                }
            } else { //告警数据
                getAlarm(packing[0], agentID, "SUNS120", "SC36KTL-DO", data120);
            }

            if (packing[1] < 100) {
                for (int i = 0; i < packing[1]; i++) {
                    datas.add(emsData02);
                }
            } else {//告警数据
                getAlarm(packing[1], agentID, "SUNS801", "ZE60BATTERY", data801);
            }

            if (packing[2] < 100) {
                for (int i = 0; i < packing[2]; i++) {
                    datas.add(gridData);
                }
            } else {//告警数据
                getAlarm(packing[2], agentID, "SUNS202", "ZEMETERG", data202);
            }
            if (packing[3] < 100) {
                for (int i = 0; i < packing[3]; i++) {
                    datas.add(loadData);
                }
            } else {//告警数据
                getAlarm(packing[3], agentID, "SUNS201", "ZEMETERL", data201);
            }
            if (packing[4] < 100) {
                for (int i = 0; i < packing[4]; i++) {
                    datas.add(pvData);
                }
            } else {//告警数据
                getAlarm(packing[4], agentID, "SUNS103", "SC30KTL-DO", data103);
            }
        }

        String msg = null;
        try {
            msg = JsonUtils.Mapper.writeValueAsString(datas);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return msg;
    }

    //峰谷电价策略
    private void battery01(String startDate, String agentID) {
        //负载
        Document powerT = dmogo.findGendDataByTime(agentID, "powerT");
        Document DWhlmp = dmogo.findGendDataByTime(agentID, "DWhImp");
        double loadW = 0.0;
        double loadDWhImp = 0.0;
        if (DWhlmp != null)
            loadDWhImp = DWhlmp.getDouble(startDate);
        if (powerT != null)
            loadW = powerT.getDouble(startDate);
        Object num = mogo.findEmulatorRegister(agentID, "loadTotWhImp");
        double loadTotWhImp = (num == null ? loadDWhImp : (double) num);
        mogo.updateEmulatorRegister(agentID, "loadDWhImp", loadDWhImp);
        getLoadData(loadW, loadTotWhImp, loadDWhImp);
        //电网参数
        double TotWh;//组合总和TotWhImp+TotWhExp
        num = mogo.findEmulatorRegister(agentID, "TotWhImp");
        double TotWhImp = (num == null ? 0.0 : (double) num);//电网负向有功总电能  (充电总功率)

        num = mogo.findEmulatorRegister(agentID, "DWhImp");
        double DWhImp = (num == null ? 0.0 : (double) num);

        num = mogo.findEmulatorRegister(agentID, "TotWhExp");
        double TotWhExp = (num == null ? 0.0 : (double) num);//电网正向有功总电能  (放电总功率)
        num = mogo.findEmulatorRegister(agentID, "DWhExp");
        double DWhExp = (num == null ? 0.0 : (double) num);

        double VAR = 0.0;//Reactive Power 瞬时总无功功率 kw
        double PF = Math.random();//Power Factor 总功率因数
        double Hz = 50.0;//电网频率
        double Evt = 0.0;//标志事件?
        //逆变器 电池参数
        double WHRtg = STROAGE_002.WHRtg;//电池总能量
        double PDC;//充电放电功率
        double EFF = 0.985;
        double PAC;//Active power from inverter 来自逆变器的有功功率
        double W;//Total Real Power 瞬时总有功功率 kw
        double MaxRsvPct = STROAGE_002.MaxRsvPct;
        double MinRsvPct = STROAGE_002.MinRsvPct;
        //总充电电量
        num = mogo.findEmulatorRegister(agentID, "TCkWh");
        double TCkWh = (num == null ? 0.0 : (double) num);
        //去数据库获取当天逆变器充电情况
        num = mogo.findEmulatorRegister(agentID, "DCkWh");
        double DCkWh = (num == null ? 0.0 : (double) num);

        //总放电电量
        num = mogo.findEmulatorRegister(agentID, "TDkWh");
        double TDkWh = (num == null ? 0.0 : (double) num);
        //去数据库获取当天放电情况
        num = mogo.findEmulatorRegister(agentID, "DDkWh");
        double DDkWh = (num == null ? 0.0 : (double) num);


        //电池参数
        num = mogo.findEmulatorRegister(agentID, "Soc");
        double Soc = (num == null ? STROAGE_002.SoC : (double) num);//当前电量百分比
        double dqrl;//当前容量kw/h
        double BV;//电压
        double BI;// 电流
        PAC = STROAGE_002.kWp * ((Reg12551 / 1000.0));//总功率*功率百分比   当前放电充电瞬时功率
        W = PAC;
        //页面显示w
        double showW = -PAC + loadW;
        //储能放电
        if (PAC > 0 && Soc > STROAGE_002.SoCNpMinPct)//使用受到到放电功率计算
        {
            PDC = PAC / EFF;
            //当前间隔电网放电消耗功率
            double J_TotWhExp = W * (((double) jgtime) / 3600);
            //当前间隔逆变器放电消耗功率
            double J_TDkWh = PAC * (((double) jgtime) / 3600);
            dqrl = WHRtg * Soc - (PDC * (((double) jgtime) / 3600));
            Soc = dqrl / WHRtg;
            /*Soc>20 BV=1.312SOC+293.8  Soc<=20   BV=1SOC+260 */
            if (Soc > 20) {
                BV = 1.312 * Soc * 100 + 293.8;
            } else {
                BV = 1 * Soc * 100 + 260;
            }
            //更新逆变器总放电 电量
            TDkWh += J_TDkWh;
            mogo.updateEmulatorRegister(agentID, "TDkWh", TDkWh);
            //更新电网累计值
            TotWhExp += Math.abs(J_TotWhExp);
            mogo.updateEmulatorRegister(agentID, "TotWhExp", TotWhExp);

            //去数据库获取当天放电情况
            DDkWh += J_TDkWh;
            mogo.updateEmulatorRegister(agentID, "DDkWh", DDkWh);
            //去数据库获取当天电网
            DWhExp += Math.abs(J_TotWhExp);//电网当天放电
            mogo.updateEmulatorRegister(agentID, "DWhExp", DWhExp);
        } else if (PAC < 0 && Soc < STROAGE_002.SoCNpMaxPct)//充电
        {
            PDC = PAC * EFF;
            //PDC=(102PDC-PDC*soc)/27
            if (Soc > 0.75)
                PDC = PDC * (102 - Soc * (100)) / 27;
            /*  Soc>80 BV=425 Soc<=80  BV=16.5Soc+316.44  Soc<10  BV=2Soc+260  */
            if (Soc > 0.8) {
                BV = 425.0;
            } else if (Soc <= 0.80 & Soc > 0.10) {
                BV = 1.65 * (Soc * 100) + 316.44;
            } else {
                BV = 2 * (Soc * 100) + 260;
            }

            double J_TCkWh = PAC * (((double) jgtime) / 3600);//当前逆变器侧间隔充电消耗功率

            dqrl = WHRtg * Soc - (PDC * (((double) jgtime) / 3600));
            Soc = dqrl / WHRtg;
            if (Soc > STROAGE_002.SoCNpMaxPct)
                Soc = STROAGE_002.SoCNpMaxPct;
            //更新逆变器总充电电量
            //*****  绝对值
            TCkWh += Math.abs(J_TCkWh);
            mogo.updateEmulatorRegister(agentID, "TCkWh", TCkWh);

            //去内存获取当天逆变器充电情况
            //*****  绝对值
            DCkWh += Math.abs(J_TCkWh);//当天
            mogo.updateEmulatorRegister(agentID, "DCkWh", DCkWh);
            //电网消耗
            //更新电网累计值
            //*****  绝对值
            double J_TotWhImp = W * (((double) jgtime) / 3600);//当前电网侧间隔充电消耗功率
            TotWhImp += Math.abs(J_TotWhImp);
            mogo.updateEmulatorRegister(agentID, "TotWhImp", TotWhImp);
            //当天电网
            //*****  绝对值
            DWhImp += Math.abs(J_TotWhImp);
            mogo.updateEmulatorRegister(agentID, "DWhImp", DWhImp);

        }//待机不符合要求
        else {
            BV = 16.5 * Soc + 316.44;
            PDC = 0;
        }
        BI = (PDC * 1000) / BV + ((Math.random() * 3) / 10);
        TotWh = TotWhImp + loadTotWhImp - TotWhExp;//Total Real Energy (当前)组合有功总电能
        mogo.updateEmulatorRegister(agentID, "Soc", Soc);
        //逆变器
        data120.put(Values.PDC, GttRetainValue.getRealVaule(PDC, 2));
        data120.put(Values.PAC, GttRetainValue.getRealVaule(PAC, 2));
        data120.put(Values.BI, GttRetainValue.getRealVaule(BI, 2));
        data120.put(Values.BV, GttRetainValue.getRealVaule(BV, 2));
        data120.put(Values.TCkWh, GttRetainValue.getRealVaule(TCkWh, 2));
        data120.put(Values.DCkWh, GttRetainValue.getRealVaule(DCkWh, 2));
        data120.put(Values.TDkWh, GttRetainValue.getRealVaule(TDkWh, 2));
        data120.put(Values.DDkWh, GttRetainValue.getRealVaule(DDkWh, 2));
        //储能
        data801.put(Values.BatSt, 1);
        data801.put(Values.Vol, GttRetainValue.getRealVaule(BV, 2));
        data801.put(Values.MaxBatACha, STROAGE_002.MaxBatACha);
        data801.put(Values.SoC, GttRetainValue.getRealVaule(Soc, 2));
        data801.put(Values.MaxBatADischa, STROAGE_002.MaxBatADischa);
        data801.put(Values.StrCur, GttRetainValue.getRealVaule(BI, 2));
        data801.put(Values.SoH, STROAGE_002.SoH);
        //电网电表
        data202.put(Values.DWhImp, GttRetainValue.getRealVaule(DWhImp + loadDWhImp, 2));
        data202.put(Values.DWhExp, GttRetainValue.getRealVaule(DWhExp, 2));
        data202.put(Values.TotWh, GttRetainValue.getRealVaule(TotWh, 2));
        data202.put(Values.TotWhExp, GttRetainValue.getRealVaule(TotWhExp, 2));
        data202.put(Values.TotWhImp, GttRetainValue.getRealVaule(TotWhImp + loadTotWhImp, 2));
        data202.put(Values.W, GttRetainValue.getRealVaule(showW, 3));
        data202.put(Values.VAR, GttRetainValue.getRealVaule(VAR, 3));
        data202.put(Values.PF, GttRetainValue.getRealVaule(PF, 3));
        data202.put(Values.Hz, GttRetainValue.getRealVaule(Hz, 2));
        data202.put(Values.Evt, GttRetainValue.getRealVaule(Evt, 2));

    }

    private void getPvData(String agentID, String date) {

        Document pVPower = dmogo.findGendDataByTime(agentID, "pVPower");
        Document eTodayy = dmogo.findGendDataByTime(agentID, "eToday");
        double Pac = 0.0;
        double eToday = 0.0;
        if (pVPower != null && eTodayy != null) {
            if ("587741c7dafbaf42f1617753".equals(agentID))
                System.out.println(pVPower);
            System.out.println(date);
            Pac = (pVPower.getDouble(date));
            eToday = eTodayy.getDouble(date);
        }
        data103.put(Values.Pac, GttRetainValue.getRealVaule(Pac, 1));
        Object num = mogo.findEmulatorRegister(agentID, "TYield");
        double TYield = num == null ? eToday : (double) num + eToday;
        double DYield = eToday;
        SimpleDateFormat dateFormat = new SimpleDateFormat("HH");//可以方便地修改日期格式
        String datepp = dateFormat.format(new Date());
        int hhpp = Integer.parseInt(datepp);
        //除去凌晨
        if (hhpp < 23 && hhpp > 2)
            mogo.updateEmulatorRegister(agentID, "DYield", DYield);
        data103.put(Values.DYield, GttRetainValue.getRealVaule(DYield, 1));
        data103.put(Values.TYield, GttRetainValue.getRealVaule(TYield, 0));
    }

    private void getLoadData(double loadW, double loadTotWhImp, double loadDWhImp) {

        data201.put(Values.W, GttRetainValue.getRealVaule(loadW, 2));
        data201.put(Values.TotWhImp, GttRetainValue.getRealVaule(loadTotWhImp, 2));
        data201.put(Values.DWhImp, GttRetainValue.getRealVaule(loadDWhImp, 2));
    }

    private static Number getRealVaule(double value, int resLen) {
        if (resLen == 0)
            //原理:123.456*10=1234.56+5=1239.56/10=123
            //原理:123.556*10=1235.56+5=1240.56/10=124
            return Math.round(value * 10 + 5) / 10;
        double db = Math.pow(10, resLen);
        return Math.round(value * db) / db;
    }

    private void getAlarm(int number, String agentID, String type, String model, Map<String, Object> data) {
        //告警参数
        Map<String, Object> noDevice = new HashMap<>();
        EmsData device = new EmsData();
        if (number > 5000) {//Fault
            switch (number) {
                case 5101:
                    data.put(Values.evt, "F0001");
                    data.put(Values.evtD, "TempOver");
                    break;
                case 5102:
                    data.put(Values.evt, "F0002");
                    data.put(Values.evtD, "GridV.OutLow");
                    break;
                case 5103:
                    data.put(Values.evt, "F0003");
                    data.put(Values.evtD, "EmergencyStp");
                    break;
                case 5201:
                    data.put(Values.evt, "F0001");
                    data.put(Values.evtD, "TempRiseDown");
                    break;
                case 5202:
                    data.put(Values.evt, "F0002");
                    data.put(Values.evtD, "CAOverDown");
                    break;
                case 5203:
                    data.put(Values.evt, "F0003");
                    data.put(Values.evtD, "DAOverDown");
                    break;
                case 5204:
                    data.put(Values.evt, "F0004");
                    data.put(Values.evtD, "RelayStp");
                    break;
                case 5301:
                    data.put(Values.evt, "F0001");
                    data.put(Values.evtD, "TempOver");
                    break;
                case 5302:
                    data.put(Values.evt, "F0002");
                    data.put(Values.evtD, "GridV.OutLow");
                    break;
                case 5303:
                    data.put(Values.evt, "F0003");
                    data.put(Values.evtD, "EmergencyStp");
                    break;
            }
            device.setSta(4);
            device.setValues(data);
            device.setDsn(agentID + type);
            device.setType(type);
            device.setModel(model);
        } else if (number < 5000 && number > 1000) {//Warning

            switch (number) {
                case 1101:
                    data.put(Values.evt, "W0020");
                    data.put(Values.evtD, "ACFanWarn");
                    break;
                case 1102:
                    data.put(Values.evt, "W0010");
                    data.put(Values.evtD, "DCFanWarn");
                    break;
                case 1201:
                    data.put(Values.evt, "W0001");
                    data.put(Values.evtD, "ModTempLowWarn");
                    break;
                case 1202:
                    data.put(Values.evt, "W0002");
                    data.put(Values.evtD, "ModTempHighWarn");
                    break;
                case 1301:
                    data.put(Values.evt, "W0020");
                    data.put(Values.evtD, "ACFanWarn");
                    break;
            }
            //data.put(Values.WarnT, "待定");
            device.setSta(2);
            device.setValues(data);
            device.setDsn(agentID + type);
            device.setType(type);
            device.setModel(model);

        } else if (number == 102) {//Device disconnect（Node通讯异常）
            noDevice.put(Values.evt, "102");
            noDevice.put(Values.evtD, "Device communication is lost");
            device.setSta(2);
            device.setValues(noDevice);
            device.setDsn(agentID + type);
            device.setType(type);
            device.setModel(model);

        } else if (number == 103) {//Device fault（Node运行出现错误）
            data.put(Values.evt, "103");
            data.put(Values.evtD, "Device fault");
            device.setSta(4);
            device.setValues(data);
            device.setDsn(agentID + type);
            device.setType(type);
            device.setModel(model);

        } else if (number == 104) {//Device warning （Node运行出现警告）
            data.put(Values.evt, "la104");
            data.put(Values.evtD, "Device communication is lost");
            device.setSta(2);
            device.setValues(data);
            device.setDsn(agentID + type);
            device.setType(type);
            device.setModel(model);

        }
        datas.add(device);
    }
}
