package io.j1st.util.util;

import java.util.Random;

/**
 * Created by xiaopeng on 2017/5/25.
 */
public class RandomNumberUtils {

    /**
     * 随机字符
     *
     * @return 随机字符
     */
    private static char[] getChar() {
        char[] passwordLit = new char[62];
        char fword = 'a';//大小写
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
    public static String getRandom() {
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
