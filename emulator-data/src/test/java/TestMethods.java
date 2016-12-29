/**
 * Created by xiaopeng on 2016/12/13.
 */


import com.mongodb.connection.Cluster;
import io.j1st.data.job.Clculate;
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
        SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmmss");
        String date = format.format(new Date());
        System.out.println(date.substring(8));
    }
}