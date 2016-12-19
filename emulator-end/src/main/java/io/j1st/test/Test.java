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
       List<Integer> num=new ArrayList<>();
        for(int a=0;a<6;a++)
        {
            num.add(a);
        }

        for (int cc:num)
        {
            System.out.println(cc);
        }
    }
}
