/**
 * Created by xiaopeng on 2016/12/13.
 */


import io.j1st.data.job.Clculate;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;

public class TestMethods {
    private static Logger log = LoggerFactory.getLogger(TestMethods.class);

    @Test
    public void testLogBack() {
        double x = 1.1d;
        double z = 1.123d;
        double y = x;
        for (int j = 0; j < 90000000; j++) {
            y *= x;
            y /= z;
            y += 0.01d;
            y -= 0.01d;
        }
        System.out.println(x);
        System.out.println(y);
        System.out.println(z);
    }

}