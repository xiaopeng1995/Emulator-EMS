package io.j1st.utils.http;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by xiaopeng on 2017/6/20.
 */
public class DataMap {
    public static String getCate(String key) {
        Map<String, String> cate = new HashMap<>();
        cate.put("I1", "UINT");
        cate.put("I2", "UINT");
        cate.put("I3", "UINT");
        cate.put("IN", "UINT");
        cate.put("IG", "UINT");
        cate.put("V1", "UINT");
        cate.put("V2", "UINT");
        cate.put("V3", "UINT");
        cate.put("V12", "UINT");
        cate.put("V23", "UINT");
        cate.put("V31", "UINT");
        cate.put("PF", "SINT");
        cate.put("Freq", "UINT");

        cate.put("VAvg", "UINT");
        cate.put("IUnb", "UINT");

        cate.put("P1", "SINT");
        cate.put("P1R", "SINT");
        cate.put("P1A", "UINT");
        cate.put("P2", "SINT");
        cate.put("P2R", "SINT");
        cate.put("P2A", "UINT");
        cate.put("P3", "UINT");
        cate.put("P3R", "UINT");
        cate.put("P3A", "UINT");

        cate.put("PTot", "SINT");
        cate.put("PRTot", "SINT");
        cate.put("PATot", "UINT");

        cate.put("LTC", "UINT");
        cate.put("GPC", "UINT");
        cate.put("Ue", "UINT");
        return cate.get(key);
    }
    public static String getZHName(String key) {
        Map<String, String> unit = new HashMap<>();
        unit.put("I1","L1 电流");//0x0001
        unit.put("I2","L2 电流");//0x0002
        unit.put("I3","L3 电流");//0x0003
        unit.put("IN","LN 电流");//0x0004
        unit.put("IG","LG 电流");//0x0005
        unit.put("V1","L1 电压");//0x0006
        unit.put("V2","L2 电压");//0x0007
        unit.put("V3","L3 电压");//0x0008
        unit.put("V12","L1-2 电压");//0x0009
        unit.put("V23","L2-3 电压");//0x000A
        unit.put("V31","L3-1 电压");//0x000B
        unit.put("PF","功率因数");//0x000C
        unit.put("Freq","频率");//0x000D

        unit.put("VAvg","平均线电压");//0x0010
        unit.put("IUnb","电流不平衡率");//0x0011

        unit.put("P1","L1 相有功功率");//0x0021
        unit.put("P1R","L1 相无功功率");//0x0022
        unit.put("P1A","L1 相视在功率");//0x0023
        unit.put("P2","L2 相有功功率");//0x0024
        unit.put("P2R","L2 相无功功率");//0x0025
        unit.put("P2A","L2 相视在功率");//0x0026
        unit.put("P3","L3 相有功功率");//0x0027
        unit.put("P3R","L3 相无功功率");//0x0028
        unit.put("P3A","L3 相视在功率");//0x0029
        unit.put("PTot","总有功功率");//0x002A
        unit.put("PRTot","总无功功率");//0x002B
        unit.put("PATot","总视在功率");//0x002C

        unit.put("LTC","长延时电流整定值");//0x2007   IR
        unit.put("GPC","接地保护电流整定值");//0x200c   Ig
        unit.put("Ue","额定电压");//0x0182
        return unit.get(key);
    }
    public static String getUnit(String key) {
        Map<String, String> unit = new HashMap<>();
        unit.put("I1", "A");
        unit.put("I2", "A");
        unit.put("I3", "A");
        unit.put("IN", "A");
        unit.put("IG", "A");
        unit.put("V1", "V");
        unit.put("V2", "V");
        unit.put("V3", "V");
        unit.put("V12", "V");
        unit.put("V23", "V");
        unit.put("V31", "V");
        unit.put("PF", "%");
        unit.put("Freq", "Hz");

        unit.put("VAvg", "V");
        unit.put("IUnb", "%");

        unit.put("P1", "kW");
        unit.put("P1R", "kVar");
        unit.put("P1A", "kVA");
        unit.put("P2", "kW");
        unit.put("P2R", "kVar");
        unit.put("P2A", "kVA");
        unit.put("P3", "kW");
        unit.put("P3R", "kVar");
        unit.put("P3A", "kVA");

        unit.put("PTot", "kW");
        unit.put("PRTot", "kVar");
        unit.put("PATot", "KVA");

        unit.put("LTC", "A");
        unit.put("GPC", "A");
        unit.put("Ue", "V");
        return unit.get(key);
    }

    public static List<String> getkey() {
        List<String> key = new ArrayList<>();
        key.add("I1");//0x0001
        key.add("I2");//0x0002
        key.add("I3");//0x0003
        key.add("IN");//0x0004
        key.add("IG");//0x0005
        key.add("V1");//0x0006
        key.add("V2");//0x0007
        key.add("V3");//0x0008
        key.add("V12");//0x0009
        key.add("V23");//0x000A
        key.add("V31");//0x000B
        key.add("PF");//0x000C
        key.add("Freq");//0x000D

        key.add("VAvg");//0x0010
        key.add("IUnb");//0x0011

        key.add("P1");//0x0021
        key.add("P1R");//0x0022
        key.add("P1A");//0x0023
        key.add("P2");//0x0024
        key.add("P2R");//0x0025
        key.add("P2A");//0x0026
        key.add("P3");//0x0027
        key.add("P3R");//0x0028
        key.add("P3A");//0x0029
        key.add("PTot");//0x002A
        key.add("PRTot");//0x002B
        key.add("PATot");//0x002C

        key.add("LTC");//0x2007   IR
        key.add("GPC");//0x200c   Ig
        key.add("Ue");//0x0182
        return key;
    }
}
