package io.j1st.data.predict;

import io.j1st.data.entity.Registry;
import io.j1st.data.job.Clculate;
import io.j1st.storage.DataMongoStorage;
import io.j1st.storage.entity.GenData;
import io.j1st.storage.utils.DateUtils;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.joda.time.DateTimeZone;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * PV预测数据
 */
public class PVpredict {

    private Logger logger = LoggerFactory.getLogger(PVpredict.class);
    private DataMongoStorage dataMongoStorage;
    Clculate clculate = new Clculate();

    public PVpredict(DataMongoStorage dataMongoStorage) {
        this.dataMongoStorage = dataMongoStorage;
    }

    private double pi_v = Math.PI;

    /**
     * @param CYear    年
     * @param CMonth   月
     * @param CDay     日
     * @param CHour    时
     * @param CMinute  分
     * @param EMPara   经纬度参数、太阳能电池板参数 Long LatiN pVTiltN pVAuziN PVPower
     * @param atoTrans 云量
     */
    public double CalcSolarPowerPredict(int CYear, int CMonth, int CDay, double CHour, double CMinute,
                                        Map<String, Double> EMPara, double atoTrans) {

        double PVOut = 0.0;
        double N0 = 79.6764 + 0.2422 * (CYear - 1985) - Math.floor((CYear - 1985) / 4);
        double C = 32.8;
        if (CMonth <= 2) {
            C = 30.6;
        } else if (CYear % 4 == 0 && CMonth > 2) {
            C = 31.8;
        }
        double H = CHour + CMinute / 60;
        double N = Math.floor(30.6 * CMonth - C + 0.5) + CDay + (H - 8 - EMPara.get("Long") / 15.0) / 24;
        double theta = 2 * pi_v / 365.2422 * (N - N0);
        double ed = 0.3723 + 23.2567 * Math.sin(theta) + 0.1149 * Math.sin(2 * theta) - 0.1712 * Math.sin(3 * theta) - 0.758 * Math.cos(theta) + 0.3656 * Math.cos(2 * theta) + 0.0201 * Math.cos(3 * theta);        // 太阳赤纬
        double edN = ed * pi_v / 180;
        double er = 1.000423 + 0.032359 * Math.sin(theta) + 0.000086 * Math.sin(2 * theta) - 0.008349 * Math.cos(theta) + 0.000115 * Math.cos(2 * theta);
        // 归一化后的日地距离平方
        double et = 0.0028 - 1.9857 * Math.sin(theta) + 9.9059 * Math.sin(2 * theta) - 7.0924 * Math.cos(theta) - 0.6882 * Math.cos(2 * theta); //时差
        H = H - (120 - EMPara.get("Long")) / 15.0 + et / 60;        // Local sun hour
        double tao = ((H - 12) * 15) * pi_v / 180;        // Sun time angle
        //0; // 时间角， 12点=0
        double sunHN = Math.asin(Math.sin(EMPara.get("LatiN")) * Math.sin(edN) + Math.cos(EMPara.get("LatiN")) * Math.cos(edN) * Math.cos(tao));

        if (sunHN <= 0) {
            return PVOut;
        }


        double sunAN;
        sunAN = Math.acos((Math.sin(sunHN) * Math.sin(EMPara.get("LatiN")) - Math.sin(edN)) / Math.cos(sunHN) / Math.cos(EMPara.get("LatiN")));

        double costb = -Math.tan(edN) * Math.tan(EMPara.get("LatiN") - EMPara.get("pVTiltN"));
        double sunInputN = Math.acos(Math.cos(EMPara.get("pVTiltN")) * Math.sin(sunHN) + Math.sin(EMPara.get("pVTiltN")) * Math.cos(sunHN) * Math.cos(sunAN - EMPara.get("pVAuziN")));

        double atoMass = CalcAtoMass(sunHN);

        double radID = 1367.0 / er * Math.pow(atoTrans, atoMass) * Math.pow(Math.cos(sunInputN), 2);
        double radId = 1367.0 / er / 2 * Math.sin(sunHN) * (1 - Math.pow(atoTrans, atoMass)) / (1 - 1.4 * Math.log(atoTrans)) * (1 + Math.cos(EMPara.get("pVTiltN"))) / 2;
        double rD = radID / 1000 * EMPara.get("PVPower") * Aeff(sunInputN);
        double rd = radId / 1000 * EMPara.get("PVPower") * 0.85;
        PVOut = rD + rd;
        return PVOut;
    }

    public double Aeff(double AngleIn) {//查12
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

    /**
     * 每小时的云量计算
     *
     * @return
     */

    public double[] CalTrans(Integer p3, Integer p4, Integer p5, Integer p6, Integer p7, Integer p8, Integer p9, double fClouds) {
        double[] aCloud = new double[1440];
        double fCloud = fClouds;//云量快速变化因子
        double[] p = new double[14];
        double b0, b1, b2, b3;
        //气象参数(分时云量)
        p[3] = p3;
        p[4] = p4;
        p[5] = p5;
        p[6] = p6;
        p[7] = p7;
        p[8] = p8;
        p[9] = p9;
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
                    aCloud[i * 120 + j] = clculate.valTrans(p[i]);
                }
            } else if (i == 0) {
                for (int j = 0; j < 120; j++) {
                    aCloud[i * 120 + j] = clculate.valTrans(p[i + 1]);
                }
            } else {
                b0 = (-p[i - 1] + 3 * p[i] - 3 * p[i + 1] + p[i + 2]) / 6;
                b1 = (3 * p[i - 1] - 6 * p[i] + 3 * p[i + 1]) / 6;
                b2 = (-3 * p[i - 1] + 3 * p[i + 1]) / 6;
                b3 = (p[i - 1] + 4 * p[i] + p[i + 1]) / 6;
                for (int j = 0; j < 120; j++) {
                    double x = j / 120;
                    aCloud[i * 120 + j] = clculate.valTrans(b3 + (b2 + (b1 + b0 * x) * x) * x + clculate.GenRandom(0, fCloud, 1));
                }
            }

        }
        return aCloud;
    }

    public void PVInfo(String tdate, String agentid, int is, int[] cCloud) throws ParseException {
        Map<String, Double> EMPara = new HashMap<>();

        int CYear = Integer.parseInt(tdate.substring(0, 4));
        int CMonth = Integer.parseInt(tdate.substring(4, 6));
        int CDay = Integer.parseInt(tdate.substring(6, 8));

        double pi_v = Math.PI;
        EMPara.put("Long", 121.5);
        EMPara.put("LatiN", 31.2 * pi_v / 180);
        EMPara.put("pVTiltN", 25d / 180 * pi_v);
        EMPara.put("pVAuziN", 0d / 180 * pi_v);
        EMPara.put("PVPower", 30000.0);
        double epv = 0;
        double eToday = 0;
        double pVOut = 0;

        double[] aCloud = CalTrans(cCloud[0], cCloud[1], cCloud[2], cCloud[3], cCloud[4], cCloud[5], cCloud[6], cCloud[7] * 0.1);
        //声明时间
        SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmmss");
        Date date = format.parse(tdate);
        long now = Long.parseLong(DateUtils.getLongTimeForTimeZone(date, DateTimeZone.getDefault()));
        //load数据获取
        List<Map<String, String>> mapList = clculate.TotalCalc();
        double DWhlmp = 0;

        String msg = is == 0 ? "预测000" : "实时111";
        logger.debug("开始计算并添加一天的" + msg);

        Document allpac = new Document();
        Document allW = new Document();
        Document alleToday = new Document();
        //循环一天的每分钟
        for (int i = 0; i < mapList.size(); i++) {
            double b = pVOut;
            double k = clculate.GenRandom(0.5, 0.4, 0);
            double CHour = Math.floor(i / 60);
            double CMinute = i % 60;
            double atoTrans = aCloud[i];

            pVOut = CalcSolarPowerPredict(CYear, CMonth, CDay, CHour, CMinute, EMPara, atoTrans);
            epv = (b * k + pVOut * (1 - k)) * 1 / 60 / 1000;
            eToday += epv;
            //load
            double W = Integer.parseInt(mapList.get(i).get("powerT")) * 0.8;
            double lepv = W / 60d / 1000d;
            DWhlmp += lepv;
            //添加至预测数据库
            if (is == 0) {

                allpac.append(now + "", pVOut / 1000);
                allpac.append(now + 30000 + "", pVOut / 1000);
                allW.append(now + "", W / 1000);
                allW.append(now + 30000 + "", W / 1000);
            } else { //添加实时数据
                int chh = i / 60;
                String shhh = chh + "";
                if (chh < 10)
                    shhh = "0" + chh;
                int cmm = i - chh * 60;
                String smmm = cmm + "";
                if (cmm < 10)
                    smmm = "0" + cmm;
                String stime = tdate.substring(0, 8) + shhh + smmm;
                allpac.append(stime, pVOut / 1000);
                allW.append(stime, W / 1000);
                alleToday.append(stime, DWhlmp);

            }
            now += 60000;
        }
        //数据持久化
        //PV
        ObjectId agentId = new ObjectId(agentid);
        if (is == 0) {
            Boolean pv = dataMongoStorage.updateAnalysisInfo(agentId, agentid + "103", 103, date, DateTimeZone.getDefault(), allpac, eToday);
            Boolean loode = dataMongoStorage.updatePowerT(agentId, agentid + "201", "201", date, DateTimeZone.getDefault(), allW, DWhlmp);
            if (pv && loode)
                logger.debug("已成功添加了一天的预测数据至数据库！");

        } else {
            Boolean dataaa = dataMongoStorage.addGenData(agentId, tdate, allpac, alleToday, allW);
            if (dataaa)
                logger.debug("已成功添加了一天的实时数据至数据库！");
        }

    }
//
//    public void PowerTData(String tdate, String agentid, String dsn, int is) throws ParseException {
//
//
//        SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmmss");
//        String s = tdate;
//        Date date = format.parse(s);
//        long now = Long.parseLong(DateUtils.getLongTimeForTimeZone(date, DateTimeZone.getDefault()));
//        ObjectId agentId = new ObjectId(agentid);
//        String deviceSn = dsn;
//        double k = clculate.GenRandom(0.5, 0.4, 0);
//        double DWhlmp = 0;
//        for (int i = 0; i < data.size(); i++) {
//            System.out.println(data.get(i).get("powerT"));
//            double W = Integer.parseInt(data.get(i).get("powerT"))*0.8;
//            double epv = W / 60d / 1000d;
//            DWhlmp += epv;
//            if (is == 0)//预测
//                dataMongoStorage.updatePowerT(agentId, deviceSn, "201", date, DateTimeZone.getDefault(), now, W, DWhlmp);
//            else //实时
//            {
//
//            }
//            now += 60000;
//
//        }
//    }
}
