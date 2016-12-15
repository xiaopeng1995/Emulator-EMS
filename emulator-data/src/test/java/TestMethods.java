/**
 * Created by xiaopeng on 2016/12/13.
 */
import io.j1st.data.entity.config.BatConfig;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TestMethods {
    private static Logger log = LoggerFactory.getLogger(TestMethods.class);

    @Test
    public void testLogBack() {
        io.j1st.data.job.Test test=new io.j1st.data.job.Test();
        ExecutorService es = Executors.newFixedThreadPool(5000);
                es.submit(test);
    }
}