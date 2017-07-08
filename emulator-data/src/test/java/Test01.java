import io.j1st.data.GetThreadAcount;
import io.j1st.data.job.TestJob;

import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by xiaopeng on 2017/5/3.
 */
public class Test01 {
    public static void main(String[] args) {
        ExecutorService es = Executors.newFixedThreadPool(20);
        System.out.println(GetThreadAcount.GetThreadAcountInfo());
        es.execute(new TestJob());
        es.execute(new Testmain(es));
    }

}
