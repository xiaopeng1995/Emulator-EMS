import java.util.Stack;

/**
 * Created by xiaopeng on 2017/5/3.
 */
public class Stacktest {
    final static int i=1;
    int s;

    public Stacktest() {
        //i = 4;
        s = 4;
    }

    public Stacktest(int j) {
       // i = j;
        s = j;
    }

    public static void main(String args[]) {
        Stacktest t = new Stacktest(5); //声明对象引用，并实例化
        Stacktest tt = new Stacktest(); //同上
        System.out.println("i=" + t.i + ",s=" + t.s);
        System.out.println("i=" + tt.i + ",s=" + tt.s);
        System.out.println("i=" + t.i + ",s=" + t.s);
    }
}
