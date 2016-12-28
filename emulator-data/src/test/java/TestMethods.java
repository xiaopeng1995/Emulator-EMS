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
        long a=Long.parseLong("1483027170000");
        System.out.println(new java.util.Date(a));
    }
}