package io.j1st.utils.http;

import io.j1st.util.util.JsonUtils;
import org.apache.commons.configuration.PropertiesConfiguration;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by xiaopeng on 2017/6/20.
 */
public class DataMap {
    private static PropertiesConfiguration dataConfig;

    public DataMap(PropertiesConfiguration dataConfig) {
        this.dataConfig = dataConfig;
    }


    public static String getCate(String key) {
        Map<String, String> cate = null;
        String json = dataConfig.getString("key_cate").replace("#", ",");
        try {
            cate = JsonUtils.Mapper.readValue(json, Map.class);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return cate.get(key);
    }


    public static String getZHName(String key) {
        Map<String, String> unit = new HashMap<>();
        String json = dataConfig.getString("key_name").replace("#", ",");
        try {
            unit = JsonUtils.Mapper.readValue(json, Map.class);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return unit.get(key);
    }

    public static String getUnit(String key) {
        Map<String, String> unit = new HashMap<>();
        String json = dataConfig.getString("key_unit").replace("#", ",");
        try {
            unit = JsonUtils.Mapper.readValue(json, Map.class);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return unit.get(key);
    }

    public static List<String> getkey() {
        List<String> key = new ArrayList<>();
        Map<String, String> unit = new HashMap<>();
        String json = dataConfig.getString("key_unit").replace("#", ",");
        try {
            unit = JsonUtils.Mapper.readValue(json, Map.class);
        } catch (IOException e) {
            e.printStackTrace();
        }
        for (String ke : unit.keySet()) {
            key.add(ke);//0x0001
        }
        return key;
    }
}
