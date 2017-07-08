package io.j1st.data;

/**
 * Created by xiaopeng on 2017/7/7.
 */
public class GetThreadAcount {
    public static String GetThreadAcountInfo(){
        ThreadGroup threadGroup = Thread.currentThread().getThreadGroup();
        while(threadGroup.getParent() != null){
            threadGroup = threadGroup.getParent();
        }
        int totalThread = threadGroup.activeCount();
        return "当前进程线程数量为：★"+totalThread;
    }

    public static Integer GetThreadAcount(){
        ThreadGroup threadGroup = Thread.currentThread().getThreadGroup();
        while(threadGroup.getParent() != null){
            threadGroup = threadGroup.getParent();
        }
        int totalThread = threadGroup.activeCount();
        return totalThread;
    }
}
