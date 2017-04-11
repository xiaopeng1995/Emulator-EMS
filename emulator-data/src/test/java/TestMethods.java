/**
 * Created by xiaopeng on 2016/12/13.
 */


import io.j1st.data.job.Clculate;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

public class TestMethods {
    private static Logger log = LoggerFactory.getLogger(TestMethods.class);

    @Test
    public void testLogBack() {
        List<String> a = new ArrayList<String>();
        a.add("1");
        a.add("1");
        a.add("1");
        a.add("2");
        for (int i=0;i<a.size();i++) {
            if("1".equals(a.get(i))){
                a.remove(a.get(i));
            }
            System.out.println("集合长度:"+a.size());
        }


//        Iterator<String> it = a.iterator();
//        while(it.hasNext()){
//            String temp = it.next();
//            if("2".equals(temp)){
//                it.remove();
//            }
//        }
        for(String temp1 : a)
            System.out.println(temp1);
    }

}