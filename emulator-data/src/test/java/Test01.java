import org.junit.Test;

import java.util.Random;
import java.util.Stack;

/**
 * Created by xiaopeng on 2017/5/3.
 *
 *
 *
 *
 *
 */
public class Test01 {
     static Integer a=0;
    @Test
    public void testLogBack() {

        System.out.println(Test01.getRandom());
        System.out.println(Test01.a);
    }


    /**
     * 随机字符
     *
     * @return 随机字符
     */
    private static char[] getChar() {
        a=2;
        char[] passwordLit = new char[62];
        char fword = 'a';
        char mword = 'a';
        char bword = '0';
        for (int i = 0; i < 62; i++) {
            if (i < 26) {
                passwordLit[i] = fword;
                fword++;
            } else if (i < 52) {
                passwordLit[i] = mword;
                mword++;
            } else {
                passwordLit[i] = bword;
                bword++;
            }
        }
        return passwordLit;
    }

    /**
     * @return 激活码
     */
    private static  String getRandom() {
        char[] r = getChar();
        Random rr = new Random();
        String pw = "";
        for (int i = 0; i < 8; i++) {
            int num = rr.nextInt(62);
            pw += r[num];
        }
        return pw;
    }


}
