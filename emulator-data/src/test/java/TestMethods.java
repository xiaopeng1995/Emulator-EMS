/**
 * Created by xiaopeng on 2016/12/13.
 */



import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



enum Shrubbery {GROUND, CRAWING, HANGING}

public class TestMethods {
    private static Logger log = LoggerFactory.getLogger(TestMethods.class);

    @Test
    public void testLogBack() {

        for (int a:pvcloud()){
            System.out.print(a);
        }

    }
    private static int[] pvcloud()
    {
        int [] cCloud=new int[8];
        int ran=(int)(Math.random()*10);
        cCloud[0]=ran>5?1:ran>3?2:ran>2?3:4;
        cCloud[1]=ran>5?3:ran>3?2:ran>2?1:5;
        cCloud[2]=ran>5?0:ran>3?1:ran>2?2:3;
        ran=(int)(Math.random()*10);
        cCloud[3]=ran>5?0:ran>3?1:ran>2?3:2;
        cCloud[4]=ran>5?0:ran>3?1:ran>2?2:3;
        cCloud[5]=ran>5?6:ran>3?5:ran>2?4:7;
        ran=(int)(Math.random()*10);
        cCloud[6]=ran>5?6:ran>3?5:ran>2?4:3;
        cCloud[7]=ran>5?3:ran>3?4:ran>2?1:2;
        return  cCloud;
    }
}