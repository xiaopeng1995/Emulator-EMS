import io.j1st.data.GetThreadAcount;

import java.util.concurrent.ExecutorService;

/**
 * Created by xiaopeng on 2017/7/8.
 */
public class Testmain extends Thread{
    private ExecutorService es;
    public Testmain(ExecutorService es){
        this.es=es;
    }

    public void run() {
        System.out.println("主线程启动。");
        while (true) {
            try {
                Thread.sleep(2000);
                es.execute(new Test02());
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
