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
        return cate.get(key);
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
        return unit.get(key);
    }

    public static List<String> getkey() {
        List<String> key = new ArrayList<>();
        key.add("I1");
        key.add("I2");
        key.add("I3");
        key.add("IN");
        key.add("IG");
        key.add("V1");
        key.add("V2");
        key.add("V3");
        key.add("V12");
        key.add("V23");
        key.add("V31");
        key.add("PF");
        key.add("Freq");

        key.add("VAvg");
        key.add("IUnb");

        key.add("P1");
        key.add("P1R");
        key.add("P1A");
        key.add("P2");
        key.add("P2R");
        key.add("P2A");
        key.add("P3");
        key.add("P3R");
        key.add("P3A");

        key.add("PTot");
        key.add("PRTot");
        key.add("PATot");
        return key;
    }
}
