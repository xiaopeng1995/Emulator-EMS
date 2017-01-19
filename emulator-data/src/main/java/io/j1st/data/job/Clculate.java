package io.j1st.data.job;


import io.j1st.storage.DataMongoStorage;
import io.j1st.storage.utils.DateUtils;
import org.bson.types.ObjectId;
import org.joda.time.DateTimeZone;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 数据算法
 */
public class Clculate {
    private Logger logger = LoggerFactory.getLogger(Clculate.class);

    private double sunI0 = 1367.0;    // Sun radiation constant
    private double pi_v = Math.PI;
    private double policy = 0;
    private double itv, N = 0.0;
    private int CYear, CMonth, CDay, CHour, CMinute;
    private double CWeekday;
    private double[] CirTemp = new double[]{6, 8, 10, 14, 18, 22, 26, 33, 32, 26, 20, 15, 10, 10};
    private double[] TTemp = new double[]{-2, -2, -3, -3, -4, -4, -4, -5, -3, -1, 1, 3,
            4, 5, 6, 4, 3, 2, 1, 1, 0, 0, -1, -1};
    private double effCar = 0.98;
    private double effBatC = 0.99, effBatD = 0.98;
    private Map<String, Double> EMPara = new ConcurrentHashMap();
    private Map<String, Double> LC = new ConcurrentHashMap();
    private double[] PVArray = new double[1441];
    private double[] aCloud = new double[1441];
    private double[] HeaterArray = new double[1440];
    private double[] ACArray = new double[1440];

    private double ADEff = 0.85;

    public List<Map<String, String>> TotalCalc() {
        List<Map<String, String>> listMap = new ArrayList<>();
        SetPara();//查1
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmm");//可以方便地修改日期格式
        String date = dateFormat.format(new Date());
        CYear = Integer.parseInt(date.substring(0, 4));
        CMonth = Integer.parseInt(date.substring(4, 6));
        CDay = Integer.parseInt(date.substring(6, 8));
        String strDate;
        if (CMonth < 10)
            strDate = CYear + "0" + CMonth + "" + CDay;
        if (CDay < 10)
            strDate = CYear + "" + CMonth + "0" + CDay;
        else
            strDate = CYear + "" + CMonth + "" + CDay;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
        Calendar calendar = new GregorianCalendar();
        Date date1 = null;
        try {
            date1 = sdf.parse(strDate);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        calendar.setTime(date1); //放入你的日期
        CWeekday = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
        CalTrans();//查1
        InitData();//查1
        int hh = Integer.parseInt(date.substring(8, 10));
        int mm = Integer.parseInt(date.substring(10, 12));
        //int i = itv;
        for (int i = 1; i <= 1440; i++) {
            Map<String, String> data = new HashMap<String, String>();
            CHour = (int) Math.floor(i / 60);
            CMinute = (int) i % 60;
            LC.put("PC", 0.0);

            GenPV(i);    // Must be first查
            GenHA();//查
            GenECar();//查
            GenStorage(); // Policy adopted HERE!查
            GenMeter();//查
            // data.put("state", "policy");
            data.put("time", DisplayTime());
            data.put("pVPower", getRealVaule(LC.get("PVOut"), 1));
            data.put("eToday", getRealVaule(LC.get("EToday"), 3));
            data.put("car1P", getRealVaule(LC.get("CarPC"), 0));
            data.put("car1SOC", getRealVaule(LC.get("CarSOC"), 2));
            data.put("car2P", getRealVaule(LC.get("Car2PC"), 0));
            data.put("car2SOC", getRealVaule(LC.get("Car2SOC"), 2));
            data.put("batP", getRealVaule(LC.get("BattPC"), 0));
            data.put("batSOC", getRealVaule(LC.get("BattSOC"), 2));
            data.put("powerG", getRealVaule(LC.get("PowerG"), 0));
            data.put("meterG", getRealVaule(LC.get("MeterG"), 2));
            data.put("powerT", getRealVaule(LC.get("PowerU"), 0));
            data.put("meterT", getRealVaule(LC.get("MeterU"), 2));
            //System.out.println("meterT "+data.get("meterT"));
            listMap.add(data);
        }
        return listMap;
    }


    private String getRealVaule(double value, int resLen) {
        Number number = 0;
        if (resLen == 0) {
            //原理:123.456*10=1234.56+5=1239.56/10=123
            //原理:123.556*10=1235.56+5=1240.56/10=124
            number = Math.round(value * 10 + 5) / 10;
            return number.toString();
        }
        double db = Math.pow(10, resLen);
        number = Math.round(value * db) / db;
        return number.toString();
    }

    private String DisplayTime() {
        String out = "";
        out += CYear + "";
        if (CMonth < 10) out += "0";
        out += CMonth + "";
        if (CDay < 10) out += "0";
        out += CDay + "";
        if (CHour < 10) out += "0";
        out += CHour + "";
        if (CMinute < 10) out += "0";
        out += CMinute;
        return out;
    }

    private void GenMeter() {//查
        LC.put("PowerU", LC.get("PwrHA") + LC.get("PwrAC") + LC.get("CarPC") + LC.get("Car2PC"));
        LC.put("MeterU", LC.get("MeterU") + (LC.get("CARtE") + LC.get("BattE") + LC.get("EHA") + LC.get("EAC")));

        LC.put("PowerG",
                LC.get("PowerU") - LC.get("PVOut") + LC.get("BattPC"));
        LC.put("MeterG", LC.get("MeterG") + (LC.get("CARtE") + LC.get("BattE") + LC.get("EHA") + LC.get("EAC") - LC.get("EPV")));
    }

    private void GenStorage() {    // TODO:
        double b = LC.get("BattPC");
        LC.put("BattE", 0.0);
        LC.put("BattPC", 0.0);

        if (policy == 0) {
            return;
        }
        if (policy == 1) {
            return;
        }
        if (policy == 2) {    // Charge in valley
            if (LC.get("BattSOC") < EMPara.get("BattCSPara") &&
                    (CHour < EMPara.get("GridV0EH") || CHour >= EMPara.get("GridV3SM"))) {
                // 电网补电
                if (LC.get("BattSOC") < EMPara.get("BattCCE") * EMPara.get("BattCapa")) { // CC
                    LC.put("BattPC", EMPara.get("BattPChg"));
                    LC.put("BattE", LC.get("BattPC") * itv / 60000);
                } else {
                    double resi = (EMPara.get("BattReal") - LC.get("BattSOC") / EMPara.get("BattCapa")) / (EMPara.get("BattReal") - EMPara.get("BattCCE"));
                    LC.put("BattPC", EMPara.get("BattPChg") * resi);
                    if (b == 0) {
                        LC.put("BattE", LC.get("BattPC") * itv / 60000);
                    } else {
                        LC.put("BattE", (LC.get("BattPC") * 0.6 + b * 0.4) * itv / 60000);
                    }
                }
                LC.put("BattSOC", LC.get("BattSOC") + LC.get("BattE") * effBatC);
            }
        } else LC.put("BattPC", 0.0);
    }

    private void GenECar() {//查
        double cp;
        double b = LC.get("CarPC");
        double b2 = LC.get("Car2PC");
        LC.put("CARtE", 0.0);
        if (CHour >= EMPara.get("CarTBH")) {
            if (LC.get("CarBack") == null) {
                LC.put("CarBack", 1.0);
                LC.put("CarSOC", EMPara.get("CarINCapa") + 0);
            }
        }
        if (CHour >= EMPara.get("Car2TBH")) {
            if (LC.get("Car2Back") == null) {
                LC.put("Car2Back", 1.0);
                LC.put("Car2SOC", EMPara.get("Car2INCapa"));
            }
        }
        LC.put("CarPC", 0.0);
        LC.put("Car2PC", 0.0);
        if (CHour == 8) {    // The cars are OUT
            LC.put("CarSOC", 0.0);
            LC.put("Car2SOC", 0.0);
            LC.put("CarBack", 0.0);
            LC.put("Car2Back", 0.0);
            return;
        }

        if (CHour < EMPara.get("GridV0EH") || CHour >= EMPara.get("CarTSH")) {
            cp = 0;
            if (LC.get("CarSOC") < EMPara.get("CarCCE") * EMPara.get("CarCapa")) { // CC Charging
                LC.put("CarPC", EMPara.get("CarPChg"));
                cp = LC.get("CarPC") * itv / 60000;
            } else if (LC.get("CarSOC") < EMPara.get("CarCapa")) {    // CV
                double resi = (EMPara.get("CarReal") - LC.get("CarSOC") / EMPara.get("CarCapa")) / (EMPara.get("CarReal") - EMPara.get("CarCCE"));
                LC.put("CarPC", EMPara.get("CarPChg") * resi);
                if (b == 0) {
                    cp = LC.get("CarPC") * itv / 60000;
                } else {
                    cp = (LC.get("CarPC") * 0.6 + b * 0.4) * itv / 60000;
                }
            }
            LC.put("CarSOC", LC.get("CarSOC") + cp * effCar);
            LC.put("CARtE", LC.get("CARtE") + cp);
        } else {
            LC.put("CarPC", 00.);
        }

        if (CHour < EMPara.get("GridV0EH") || CHour >= EMPara.get("Car2TSH")) {
            cp = 0;
            if (LC.get("Car2SOC") < EMPara.get("Car2CCE") * EMPara.get("Car2Capa")) { // CC Charging
                LC.put("Car2PC", EMPara.get("Car2PChg"));
                cp = LC.get("Car2PC") * itv / 60000;
            } else if (LC.get("Car2SOC") < EMPara.get("Car2Capa")) {    // CV
                double resi = (EMPara.get("Car2Real") - LC.get("Car2SOC") / EMPara.get("Car2Capa")) / (EMPara.get("Car2Real") - EMPara.get("Car2CCE"));
                LC.put("Car2PC", EMPara.get("Car2PChg") * resi);
                if (b2 == 0) {
                    cp = LC.get("Car2PC") * itv / 60000;
                } else {
                    cp = (LC.get("Car2PC") * 0.6 + b * 0.4) * itv / 60000;
                }
            }
            LC.put("Car2SOC", LC.get("Car2SOC") + cp * effCar);
            LC.put("CARtE", LC.get("CARtE") + cp);
        } else {
            LC.put("Car2PC", 0.0);
        }
    }

    private void GenHA() {//查
        double b = LC.get("PwrHA");
        double p;
        LC.put("PwrHA", 0.0);

        // illumination
        if (CHour < 6 || LC.get("PVOut") > 0) {
            p = EMPara.get("HALight") / 10;
        } else {
            do {
                p = GenRandom(EMPara.get("HALight") / 3, EMPara.get("HALight") / 4, 1);
            } while (p < EMPara.get("HALight") / 10 || p > EMPara.get("HALight"));
        }
        LC.put("PwrHA", LC.get("PwrHA") + p);
        // Computer & TV
        p = 0;
        if (CWeekday == 0 || CWeekday == 6) {
            if (CHour >= 9) {
                p = GenRandom(EMPara.get("HATV") * 1 / 2, EMPara.get("HATV") / 6, 1);
            }
        } else {
            if (CHour >= 18 && CHour <= 22) {
                p = GenRandom(EMPara.get("HATV") * 1 / 2, EMPara.get("HATV") / 6, 1);
            }
        }
        if (p > EMPara.get("HATV")) p = EMPara.get("HATV");
        LC.put("PwrHA", LC.get("PwrHA") + p);
        // Kitchen & frig
        p = 0;
        if (CHour == 7) {
            if (Math.random() <= 0.05) {
                p += EMPara.get("HAKit") / 3;
            }
        }
        if (CHour == 11 && (CWeekday == 0 || CWeekday == 6)) {
            if (Math.random() <= 0.1) {
                p += EMPara.get("HAKit") / 3;
            }
        }
        if (CHour == 17 && (CWeekday == 0 || CWeekday == 6)) {
            if (Math.random() <= 0.08) {
                p += EMPara.get("HAKit") / 3;
            }
        }
        if (CHour == 18 && (CWeekday > 0 && CWeekday < 6)) {
            if (Math.random() <= 0.08) {
                p += EMPara.get("HAKit") / 3;
            }
        }
        if (CHour == 17 && CMinute >= LC.get("CookerStartM") && CMinute - 30 < LC.get("CookerStartM")) {
            p += EMPara.get("HAKit") / 4;
        }
        if (CHour == 17) {
            if (Math.random() <= 0.03) {
                p += EMPara.get("HAKit") * 2 / 3;
            }
        }
        if (Math.random() <= 0.05) {
            p += EMPara.get("HARef");
        }
        LC.put("PwrHA", LC.get("PwrHA") + p);

        double k = GenRandom(0.5, 0.4, 0);
        LC.put("EHA", (b * k + LC.get("PwrHA") * (1 - k)) * itv / 60 / 1000);
        p = 0;
        int index = (int) (CHour * 60 + CMinute - itv);

        if ((N > 80 && N < 160) || (N > 270 && N < 330)) {
            LC.put("PwrAC", HeaterArray[index]);
        } else {
            LC.put("PwrAC", (HeaterArray[index] + ACArray[index]));
        }

        for (int i = 0; i < itv; i++) {
            p += HeaterArray[i + index];
            if ((N > 80 && N < 160) || (N > 270 && N < 330)) p += ACArray[i + index];
        }
        LC.put("EAC", p / 60 / 1000);
    }

    private void GenPV(double t) {//查1
        double b = LC.get("PVOut");
        CalcSolarPower();
        LC.put("PVOut", PVArray[(int) t]);
        double k = GenRandom(0.5, 0.4, 0);
        LC.put("EPV", (b * k + LC.get("PVOut") * (1 - k)) * itv / 60 / 1000);
        LC.put("EToday", LC.get("EToday") + LC.get("EPV"));
    }

    private void SetPara() {//查1
        //地点参数
        EMPara.put("Long", 121.5);//经度Number(id('Longitude').value);
        EMPara.put("LatiN", 31.2 * pi_v / 180);//纬度EMPara.LatiN = id('Latitude').value * pi_v / 180;
        itv = 1.0;//间隔 Number(id('doubleerval').value);
        policy = 2;// 策略1 禁止向电网输出">2 高峰输出 低谷回购">
        // 晨谷结束时间dy = id('GridV0E').value.split(":");
        EMPara.put("GridV0EH", 6.0);//EMPara.GridV0EH = Number(dy[0]);
        EMPara.put("GridV0EM", 00.0);//EMPara.GridV0EM = Number(dy[1]);
        //  晚谷开始时间：dy = id('GridV3S').value.split(":");
        EMPara.put("GridV3SH", 22.0);//EMPara.GridV3SH = Number(dy[0]);
        EMPara.put("GridV3SM", 00.0);//EMPara.GridV3SM = Number(dy[1]);
        //峰1开始时间dy = id('GridP1S').value.split(":");
        EMPara.put("GridP1SH", 10.0);//EMPara.GridP1SH = Number(dy[0]);
        EMPara.put("GridP1SM", 00.0); //        EMPara.GridP1SM = Number(dy[1]);
        //峰1结束时间dy = id('GridP1E').value.split(":");
        EMPara.put("GridP1EH", 14.0); //        EMPara.GridP1EH = Number(dy[0]);
        EMPara.put("GridP1EM", 00.0); //        EMPara.GridP1EM = Number(dy[1]);
        //太阳能电池板参数
        EMPara.put("pVTiltN", 25 / 180 * pi_v);//倾角EMPara.pVTiltN = Number(id("PVTilt").value) / 180 * pi_v;
        EMPara.put("pVAuziN", 0 / 180 * pi_v); //方位角 EMPara.pVAuziN = Number(id("PVAuzi").value) / 180 * pi_v;
        EMPara.put("PVPower", 3000.0);//最大输出功率EMPara.PVPower = Number(id("PVPower").value);
        //储能逆变器/电池参数
        EMPara.put("BattCapa", 30.0);//电池容量：EMPara.BattCapa = Number(id("BattCapa").value);
        EMPara.put("BattPChg", 6000.0);//最大充电功率EMPara.BattPChg = Number(id("BattPChg").value);
        EMPara.put("BattPDis", 6000.0);//最大放电功率EMPara.BattPDis = Number(id("BattPDis").value);
        EMPara.put("BattICapa", 15.0);//初始容量EMPara.BattICapa = Number(id("BattICapa").value);
        EMPara.put("BattCSPara", 20.0);// 停止补电容量EMPara.BattCSPara = Number(id("BattCSPara").value);
        //电动车1
        EMPara.put("CarCapa", 15.0);//电池容量EMPara.CarCapa = Number(id("CarCapa").value);
        EMPara.put("CarPChg", 3000.0);//最大充电功率EMPara.CarPChg = Number(id("CarPChg").value);
        EMPara.put("CarIMcapa", 14.0);//子夜容量EMPara.CarIMCapa = Number(id("CarIMcapa").value);
        EMPara.put("CarINCapa", 8.0);//回家容量EMPara.CarINCapa = Number(id("CarINcapa").value);
        //电动车2纯电动
        EMPara.put("Car2Capa", 85.0);//EMPara.Car2Capa = Number(id("Car2Capa").value);
        EMPara.put("Car2PChg", 7000.0);//EMPara.Car2PChg = Number(id("Car2PChg").value);
        EMPara.put("Car2IMCapa", 74.0);//EMPara.Car2IMCapa = Number(id("Car2IMcapa").value);
        EMPara.put("Car2INCapa", 62.0);//EMPara.Car2INCapa = Number(id("Car2INcapa").value);

        EMPara.put("BattReal", 1.02);//EMPara.BattReal = 1.02;
        EMPara.put("BattCCE", 0.8);//EMPara.BattCCE = 0.8;
        EMPara.put("CarReal", 1.02);//EMPara.CarReal = 1.02;
        EMPara.put("Car2Real", 1.03);//EMPara.Car2Real = 1.03;
        EMPara.put("CarCCE", 0.9);//EMPara.CarCCE = 0.9;
        EMPara.put("Car2CCE", 0.85);//EMPara.Car2CCE = 0.85;

        //车一电网充电时间：dy = id('CarTStart').value.split(":");
        EMPara.put("CarTSH", 22.0);
        EMPara.put("CarTSM", 00.0);
        //车二电网充电时间dy = id('Car2TStart').value.split(":");
        EMPara.put("Car2TSH", 22.0);
        EMPara.put("Car2TSM", 00.0);
        //车一回家时间dy = id('CarTBack').value.split(":");
        EMPara.put("CarTBH", 18.0);
        EMPara.put("CarTBM", 00.0);
        //车二回家时间dy = id('Car2TBack').value.split(":");
        EMPara.put("Car2TBH", 19.0);
        EMPara.put("Car2TBM", 00.0);
        //家电负载
        EMPara.put("HAHeater", 1000.0);//热水器功率
        EMPara.put("HAAC", 4000.0);//HVAC功率
        EMPara.put("HALight", 400.0);//照明功率
        EMPara.put("HATV", 500.0);//电脑功率
        EMPara.put("HARef", 100.0);//冰箱功率
        EMPara.put("HAKit", 3000.0);//厨房功率
        //电表读数
        EMPara.put("MeterIG", 1363.384);//电网初始EMPara.MeterIG = Number(id("MeterIG").value);
        EMPara.put("MeterIU", 0.045);//负载初始EMPara.MeterIU = Number(id("MeterIU").value);
    }

    public double valTrans(double num) {
        double ret = (9 - num) / 10;
        if (ret < 0.1) {
            ret = Math.pow(10, 4 * (ret - 0.1) - 1);
        } else if (ret > 0.85) {
            ret = 0.85;
        }
        return ret;
    }

    private void CalTrans() {//查1
        LC.put("atoTrans", 0.65);
        double fCloud = 0.2;//云量快速变化因子
        double[] p = new double[14];
        double b0, b1, b2, b3;
        //气象参数(分时云量)
        p[3] = 2;
        p[4] = 3;
        p[5] = 1;
        p[6] = 0;
        p[7] = 3;
        p[8] = 9;
        p[9] = 7;
        p[0] = p[1] = p[2] = 2 * p[3] - p[4];
        p[10] = p[11] = p[12] = p[13] = 2 * p[9] - p[8];

        for (int i = 0; i < 12; i++) {
            if (p[i] < 0) {
                p[i] = 0;
            }
            if (p[i] > 10) {
                p[i] = 10;
            }
        }

        for (int i = 0; i < 12; i++) {
            if (i == 11) {
                for (int j = 0; j < 120; j++) {
                    aCloud[i * 120 + j] = valTrans(p[i]);
                }
            } else if (i == 0) {
                for (int j = 0; j < 120; j++) {
                    aCloud[i * 120 + j] = valTrans(p[i + 1]);
                }
            } else {
                b0 = (-p[i - 1] + 3 * p[i] - 3 * p[i + 1] + p[i + 2]) / 6;
                b1 = (3 * p[i - 1] - 6 * p[i] + 3 * p[i + 1]) / 6;
                b2 = (-3 * p[i - 1] + 3 * p[i + 1]) / 6;
                b3 = (p[i - 1] + 4 * p[i] + p[i + 1]) / 6;
                for (int j = 0; j < 120; j++) {
                    double x = j / 120;
                    aCloud[i * 120 + j] = valTrans(b3 + (b2 + (b1 + b0 * x) * x) * x + GenRandom(0, fCloud, 1));
                }
            }
        }

    }

    public double GenRandom(double std, double vari, double type) {//查
        double ret = 0;
        if (type == 0) {
            return std - vari / 2 + Math.random() * vari;
        }    // 均匀分布
        else if (type == 1)    // Normal distribution
        {
            double u, v;
            do {
                u = Math.random();
                v = Math.random();
            } while (u <= 0.0000001);
            double z = Math.sqrt(-2 * Math.log(u)) * Math.cos(2 * pi_v * v);
            ret = std + z * vari;
        }
        return ret;
    }

    private void InitData() {//查1
        LC.put("EToday", 0.0);
        LC.put("PVOut", 0.0);

//	LC.put("",0);atoTrans = EMPara.atoTrans;
        LC.put("BattSOC", EMPara.get("BattICapa"));
        LC.put("CarSOC", EMPara.get("CarIMcapa"));
        LC.put("Car2SOC", EMPara.get("Car2IMCapa"));

        LC.put("MeterG", EMPara.get("MeterIG"));    //
        LC.put("MeterU", EMPara.get("MeterIU"));    //

        LC.put("PowerG", 0.0);
        LC.put("PowerU", 0.0);

        LC.put("CarPC", 0.0);
        LC.put("Car2PC", 0.0);
        LC.put("BattPC", 0.0);

        LC.put("PwrHA", 0.0);    // Power HA - AC
        LC.put("PwrAC", 0.0);    // AC + Heater

        LC.put("EHA", 0.0);        // Energy HA
        LC.put("EAC", 0.0);        // Energy AC
        LC.put("EPV", 0.0);
        LC.put("CARtE", 0.0);
        LC.put("BattE", 0.0);

        LC.put("CookerStartM", 30 * Math.random());

        LC.put("CTemp", GenRandom(CirTemp[CMonth], 4, 1));

        InitPVData();
        InitHeaterData();
        InitACData();
    }

    private void InitACData() {//查12
        double[] HTemp = new double[24];
        double BTemp = GenRandom(CirTemp[CMonth], 5, 0);
        double ACOn = 0;
        String content = "";

        for (int j = 0; j < 24; j++) {
            HTemp[j] = BTemp + GenRandom(TTemp[j], 1, 1);
        }

        double RTemp = HTemp[0];
        if (RTemp < 20) {
            RTemp = 20;
        }
        if (RTemp > 24) {
            RTemp = 24;
        }

        for (int i = 0; i < 1440; i++) {
            int Hour = (int) Math.floor(i / 60);
            RTemp += (HTemp[Hour] - RTemp) / 180;

            if (CWeekday > 0 && CWeekday < 6 && i >= 8 * 60 && i <= 17 * 60) { // Nobody in
                ACArray[i] = 0;
                ACOn = 0;
                content += "0";
            } else {
                RTemp += 0.02;
                if ((ACOn == 1 && RTemp < 23 && RTemp > 21) ||
                        (ACOn == 0 && RTemp < 23.5 && RTemp > 20.5)) {    // AC Off
                    ACArray[i] = 0;
                    ACOn = 0;
                    content += "0";
                } else {
                    ACOn = 1;
                    double dt = 22 - RTemp;
                    if (dt > 10) {
                        dt = 10;
                    }
                    if (dt < -10) {
                        dt = -10;
                    }
                    RTemp += dt / 20;
                    //if (dt < 0) { dt = -dt; }
                    dt *= (22 - HTemp[Hour]);
                    if (dt < 0) dt = -dt;
                    if (dt > 50) {
                        dt = 50;
                    }
                    dt /= 5;
                    if (dt < 1) dt = 1;

                    ACArray[i] = EMPara.get("HAAC") * dt / 10;
                    dt = Math.floor(dt);
                    if (dt > 9) {
                        dt = 9;
                    }
                    content += dt;
                }
            }
        }

    }

    private void InitPVData() {//查12
        for (int i = 0; i <= 1440; i++) {
            CHour = (int) Math.floor(i / 60);
            CMinute = i % 60;

            LC.put("atoTrans", aCloud[i]);
            CalcSolarPower();
            PVArray[i] = LC.get("PVOut");
        }
    }

    private void CalcSolarPower() {//查12
        double N0 = 79.6764 + 0.2422 * (CYear - 1985) - Math.floor((CYear - 1985) / 4);
        double C = 32.8;
        if (CMonth <= 2) {
            C = 30.6;
        } else if (CYear % 4 == 0 && CMonth > 2) {
            C = 31.8;
        }
        double H = CHour + CMinute / 60;
        N = Math.floor(30.6 * CMonth - C + 0.5) + CDay + (H - 8 - EMPara.get("Long") / 15.0) / 24;

        double theta = 2 * pi_v / 365.2422 * (N - N0);
        double ed = 0.3723 + 23.2567 * Math.sin(theta) + 0.1149 * Math.sin(2 * theta) - 0.1712 * Math.sin(3 * theta) - 0.758 * Math.cos(theta) + 0.3656 * Math.cos(2 * theta) + 0.0201 * Math.cos(3 * theta);        // 太阳赤纬
        double edN = ed * pi_v / 180;
        double er = 1.000423 + 0.032359 * Math.sin(theta) + 0.000086 * Math.sin(2 * theta) - 0.008349 * Math.cos(theta) + 0.000115 * Math.cos(2 * theta);
        // 归一化后的日地距离平方
        double et = 0.0028 - 1.9857 * Math.sin(theta) + 9.9059 * Math.sin(2 * theta) - 7.0924 * Math.cos(theta) - 0.6882 * Math.cos(2 * theta); //时差
        // T Sun = T + et

        H = H - (120 - EMPara.get("Long")) / 15.0 + et / 60;        // Local sun hour
        double tao = ((H - 12) * 15) * pi_v / 180;        // Sun time angle
        //0; // 时间角， 12点=0
        double sunHN = Math.asin(Math.sin(EMPara.get("LatiN")) * Math.sin(edN) + Math.cos(EMPara.get("LatiN")) * Math.cos(edN) * Math.cos(tao));
        if (sunHN <= 0) {
            LC.put("PVOut", 0.0);
            return;
        }

        // double sSunA = Math.cos(edN) * Math.sin(tao) / Math.cos(sunHN);
        double sunAN;
        sunAN = Math.acos((Math.sin(sunHN) * Math.sin(EMPara.get("LatiN")) - Math.sin(edN)) / Math.cos(sunHN) / Math.cos(EMPara.get("LatiN")));
//        double sunH = sunHN * 180 / pi_v;
//        double sunA = sunAN * 180 / pi_v;
        //	+ "  " + sSunA;
        double costb = -Math.tan(edN) * Math.tan(EMPara.get("LatiN") - EMPara.get("pVTiltN"));
//	var sunInputA = Math.acos(Math.sin(edN)*Math.sin(EMPara.LatiN-EMPara.pVTiltN)+Math.cos(edN)*Math.cos(EMPara.LatiN-EMPara.pVTiltN)*costb);
        double sunInputN = Math.acos(Math.cos(EMPara.get("pVTiltN")) * Math.sin(sunHN) + Math.sin(EMPara.get("pVTiltN")) * Math.cos(sunHN) * Math.cos(sunAN - EMPara.get("pVAuziN")));
//	var sunInputB = Math.acos(Math.cos(EMPara.LatiN-EMPara.pVTiltN)*Math.cos(edN)*Math.cos(tao) + Math.sin(EMPara.LatiN-EMPara.pVTiltN)*Math.sin(edN));

        double atoMass = CalcAtoMass(sunHN);

        double radID = sunI0 / er * Math.pow(LC.get("atoTrans"), atoMass) * Math.pow(Math.cos(sunInputN), 2);
        double radId = sunI0 / er / 2 * Math.sin(sunHN) * (1 - Math.pow(LC.get("atoTrans"), atoMass)) / (1 - 1.4 * Math.log(LC.get("atoTrans"))) * (1 + Math.cos(EMPara.get("pVTiltN"))) / 2;
        // Math.pow(Math.cos(EMPara.pVTiltN/2), 2);  Same

        LC.put("radID", radID / 1000 * EMPara.get("PVPower") * Aeff(sunInputN));
        LC.put("radId", radId / 1000 * EMPara.get("PVPower") * ADEff);
        LC.put("PVOut", LC.get("radID") + LC.get("radId"));
//        if (sSunA > 1) {
//            sunAN = Math.acos(Math.sin(sunHN) * Math.sin(EMPara.get("LatiN")) - Math.sin(edN)) / Math.cos(sunHN) / Math.cos(EMPara.get("LatiN"));
//        } else {
//            sunAN = Math.asin(sSunA);
//        }
//
////	var sunAN = (Math.sin(sunHN)*Math.sin(tao)-Math.sin(edN))/Math.cos(sunHN)/Math.cos(EMPara.LatiN);
//
//        double sunH = sunHN * 180 / pi_v;
//        double sunA = sunAN * 180 / pi_v;
//
////	id("SunData").innerHTML = "Er:" + er + ";  Ed:" + ed + ";  Et:" + et;
////	id("SunResult").innerHTML = "高度角:" + sunH + "度" + "  方位角:" + sunA + "  " + sSunA;
//
//        double sunInputN = Math.acos(Math.cos(EMPara.get("pVTiltN")) * Math.sin(sunHN) + Math.sin(EMPara.get("pVTiltN")) * Math.cos(sunHN) * Math.cos(sunAN - EMPara.get("pVAuziN")));
//        double sunInputB = Math.acos(Math.cos(EMPara.get("LatiN") - EMPara.get("pVTiltN")) * Math.cos(edN) * Math.cos(tao) + Math.sin(EMPara.get("LatiN") - EMPara.get("pVTiltN")) * Math.sin(edN));
//        double atoMass = CalcAtoMass(sunHN);
//
//        if (CHour == 7) {
//            double a = 1;
//        }
//        double radID = sunI0 / er * Math.pow(LC.get("atoTrans"), atoMass) * Math.cos(EMPara.get("pVTiltN"));
//        double radId = sunI0 / er / 2 * Math.sin(sunHN) * (1 - Math.pow(LC.get("atoTrans"), atoMass)) / (1 - 1.4 * Math.log(LC.get("atoTrans"))) * Math.pow(Math.cos(EMPara.get("pVTiltN") / 2), 2);
//        LC.put("PVOut", (radID + radId) / 1000 * EMPara.get("PVPower"));
////	id("SunV").innerHTML = "入射角:" + sunInputN*180/pi_v + "<br />  辐照:" + radID + " " + radId;
    }

    private double Aeff(double AngleIn) {//查12
        if (AngleIn < 0) return 0;
        if (AngleIn > 2) {
            AngleIn = 2.0;
        }
        return 1 - AngleIn / 10;
    }

    public double CalcAtoMass(double heightN) {
        double res;
        if (heightN > pi_v / 6) {
            res = 1.0 / Math.sin(heightN);
        } else {
            double sh = 614 * Math.sin(heightN);
            res = Math.sqrt(1229 + sh * sh) - sh;
        }
        return res;
    }

    private void InitHeaterData() {//查12
        double HTemp = GenRandom(45, 5, 1);
        double[] UseS = new double[]{7 * 60 + GenRandom(20, 20, 0), 18 * 60 + GenRandom(30, 30, 0),
                20 * 60 + GenRandom(20, 10, 0), 21 * 60 + GenRandom(20, 10, 0), 1440};
        double[] UseT = new double[]{UseS[0] + GenRandom(10, 5, 0), UseS[1] + GenRandom(10, 5, 0),
                UseS[2] + GenRandom(30, 5, 1), UseS[3] + GenRandom(30, 5, 1), 1440};
        int j = 0;
        boolean HOn = false;

//	var content = "";

        for (int i = 0; i < 1440; i++) {
            if (i > UseT[j]) {
                j += 1;
            }

            if (i > UseS[j]) {
                HTemp -= (HTemp - LC.get("CTemp") - 4) / 15;
            } else {
                HTemp -= (HTemp - LC.get("CTemp") - 4) / 300;
            }

            if (HOn) {
                HTemp += 1.6;
                if (HTemp > 60) {
                    HOn = false;
                }
            } else {
                if (HTemp < 40) {
                    HOn = true;
                }
            }
            if (HOn)
                HeaterArray[i] = 1 * EMPara.get("HAHeater");
            else
                HeaterArray[i] = 0 * EMPara.get("HAHeater");
//		content += HOn;
        }
//	id("SunData").innerHTML = content;
    }
}
