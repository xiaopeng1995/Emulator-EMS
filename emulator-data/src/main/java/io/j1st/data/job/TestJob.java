package io.j1st.data.job;

import io.j1st.data.GetThreadAcount;
import io.j1st.data.entity.Registry;

/**
 * Created by xiaopeng on 2017/7/7.
 */
public class TestJob extends Thread {
    public void run() {
        System.out.println("监控线程启动。。");
        while (true) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println(new GetThreadAcount().GetThreadAcountInfo()
                    +"\n en:"+ Registry.INSTANCE.getValue().get("en")
                    +"\n sn:"+ Registry.INSTANCE.getValue().get("sn"));
        }
    }
}
