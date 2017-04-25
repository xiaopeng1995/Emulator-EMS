/**
 * Created by xiaopeng on 2016/12/13.
 */


import io.j1st.util.util.GttRetainValue;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class TestMethods {
    private static Logger log = LoggerFactory.getLogger(TestMethods.class);

    @Test
    public void testLogBack() {

        System.out.println(getRanNum(8.99, 1));
    }

    /**
     * 获取指定数值区间的随机数,自动保留小数数位为区间给定值最大长度
     *
     * @param numMax 最大值
     * @param numMin 最小值
     * @return 所需的数值
     */
    private double getRanNum(double numMax, double numMin) {
        if (numMax < numMin)
            return 0d;
        if (numMax == numMin)
            return numMin;

        String numMaxlength = numMax + "";
        String numMinlength = numMin + "";
        //获取最大长度
        int maxleng = numMaxlength.split("\\.")[1].length() > numMinlength.split("\\.")[1].length() ? numMaxlength.split("\\.")[1].length() : numMinlength.split("\\.")[1].length();
        int num1 = (int) ((numMax + numMin) / 2);
        String a = num1 + "";
        Double bl = Math.pow(10, a.length());
        //随机配置值
        Double Ras;
        do {
            Ras = Math.random() * bl;
        } while (Ras > numMax || Ras < numMin);
        Double Ra = Ras;
        Double Ran = GttRetainValue.getRealVaule(Ra, maxleng).doubleValue();
        return Ran;
    }
}