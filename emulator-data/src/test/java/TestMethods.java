/**
 * Created by xiaopeng on 2016/12/13.
 */


import io.j1st.util.util.GttRetainValue;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.SimpleDateFormat;
import java.util.Date;


enum Shrubbery {GROUND, CRAWING, HANGING}

public class TestMethods {
    private static Logger log = LoggerFactory.getLogger(TestMethods.class);

    @Test
    public void testLogBack() {
        double a=-0.65;
        SimpleDateFormat dateFormat = new SimpleDateFormat("HH");//可以方便地修改日期格式
        String date = dateFormat.format(new Date());
        System.out.println(date);
    }
}