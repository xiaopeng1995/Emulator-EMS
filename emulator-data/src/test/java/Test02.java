/**
 * Created by xiaopeng on 2017/7/8.
 */
public class Test02 extends Thread {

    public void run() {
        while (true) {
            try {
                System.out.println("新的线程");
                Thread.sleep(60 * 60 * 1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
