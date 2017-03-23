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
        for(int a:pvcloud())
        {
            System.out.print(a);
        }
    }
    //太阳能云因子
    public static int[] pvcloud() {
        int[] cCloud = new int[8];
        int ran = (int) (Math.random() * 10);
        cCloud[0] = ran > 8 ? 3 :ran > 5 ? 3 : ran > 3 ? 4 : ran > 2 ? 5 : 1;//6
        cCloud[1] = ran > 8 ? 3 :ran > 5 ? 2 : ran > 3 ? 3 : ran > 2 ? 4 : 1;//8
        cCloud[2] = ran > 8 ? 1 :ran > 5 ? 1 : ran > 3 ? 2 : ran > 2 ? 3 : 0;//10
        cCloud[3] = ran > 8 ? 1 :ran > 5 ? 0 : ran > 3 ? 1 : ran > 2 ? 2 : 0;//12
        cCloud[4] = ran > 8 ? 1 :ran > 5 ? 1 : ran > 3 ? 2 : ran > 2 ? 3 : 0;//14
        cCloud[5] = ran > 8 ? 1 :ran > 5 ? 2 : ran > 3 ? 3 : ran > 2 ? 4 : 1;//16
        cCloud[6] = ran > 8 ? 2 :ran > 5 ? 3 : ran > 3 ? 4 : ran > 2 ? 5 : 1;//18
        ran = (int) (Math.random() * 10);
        cCloud[7] = ran > 5 ? 3 : ran > 3 ? 4 : ran > 2 ? 1 : 2;//变化因子
        return cCloud;
    }
}