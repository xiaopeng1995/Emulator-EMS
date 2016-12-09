package io.j1st.test;

import io.j1st.util.entity.Payload;
import io.j1st.util.util.JsonUtils;

import java.util.List;
import java.util.Map;

/**
 * Created by xiaopeng on 2016/12/8.
 */
public class Test {
    public static void main(String[] args)throws  Exception {
        String a="{\"SetMHReg\":[{\"dsn\":\"AB123456\",\"Reg12551\":800}]}";
        Map<Object, Object> msgData = JsonUtils.Mapper.readValue(a.toString().getBytes(), Map.class);
        System.out.println(msgData);
        System.out.println(msgData.keySet().toString());
        if (msgData.keySet().toString().contains("Query")) {
            List<Map> bbc=(List<Map>)msgData.get("Query");
            int d = (Integer) bbc.get(0).get("D");
            int i = (Integer) bbc.get(0).get("I");
            System.out.println(d);
            System.out.println(i);
        }
        else if (msgData.keySet().toString().contains("SetMHReg"))
        {
            List<Map> bbc=(List<Map>)msgData.get("SetMHReg");
            String d = bbc.get(0).get("dsn").toString();
            int i = (Integer) bbc.get(0).get("Reg12551");
            System.out.println(d);
            System.out.println(i);
        }

    }
}
