package io.j1st.data.job;

/**
 * Created by xiaopeng on 2016/12/14.
 */
public class Test extends Thread{
    public void run(){

        System.out.println(222);
        try {
            Thread.sleep(333);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println(333);
    }

}
