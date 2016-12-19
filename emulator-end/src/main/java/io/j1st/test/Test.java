package io.j1st.test;

import io.j1st.util.entity.Payload;
import io.j1st.util.util.JsonUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Created by xiaopeng on 2016/12/8.
 */
public class Test {
    public static void main(String[] args)throws  Exception {
        String c="16,1,1,1,1";
        String[] a=c.split(",");
        for(int i=0;i<a.length;i++)
        {
            System.out.println(a[i]);
        }

    }
}
