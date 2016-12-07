package io.j1st.util.util;

/**
 * 保留小数点计算
 */
public class GttRetainValue {
    /**
     *
     * @param value 初始值
     * @param resLen 保留多少位
     * @return 初始值保留多少位后值
     */
    public static Number getRealVaule(double value, int resLen) {
        if (resLen == 0)
            //原理:123.456*10=1234.56+5=1239.56/10=123
            //原理:123.556*10=1235.56+5=1240.56/10=124
            return Math.round(value * 10 + 5) / 10;
        double db = Math.pow(10, resLen);
        return Math.round(value * db) / db;
    }
}
