package io.j1st.util.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.j1st.util.entity.EmsData;
import io.j1st.util.entity.data.*;

import java.util.ArrayList;
import java.util.List;

/**
 * 获取Push Json体
 */
public class GetJsonEmsData {
    /**
     * 获取Push Json体
     * @param Edata Values
     * @param type type
     * @param dsn dsn
     * @return Json体
     */
    public static String getData(Values Edata,String type,String dsn) {
        EmsData data = new EmsData();
        Values values = new Values();
//        if (Edata != null) {
//            data.setType(type);
//            data.setDsn(dsn);
//            data.setValues(Edata);
//        } else {
//            int num = (int) (1 + Math.random() * (10 - 1 + 1));
//
//            if (num < 5)//正常
//            {
//                values.setFAC("1");
//                values.setPAC("2");
//                values.setPDC("3");
//                data.setType("202");
//                data.setDsn("Meter");
//                data.setValues(values);
//            } else if (num < 7)//No Device（Node未激活或无法初始化通讯）
//            {
//                data.setType("AGENT");
//                data.setAsn("xx");
//
//            } else if (num < 9)//Device disconnect（Node通讯异常）
//            {
//                values.setWarnD("Device communication is lost");
//                values.setWarnT("待定");
//                values.setWarnV("101");
//                data.setType("801");
//                data.setDsn("Device disconnect");
//            } else if (num == 9)//Device fault（Node运行出现错误）
//            {
//                values.setFAC("xx");
//                values.setPDC("xx");
//                values.setFAC("xx");
//                values.setFaultD("xx");
//                values.setFaultT("xx");
//                values.setFaultV("xx");
//                data.setType("801");
//                data.setDsn("Device fault");
//            } else {//Device warning （Node运行出现警告）
//                values.setFAC("warning");
//                values.setPDC("warning");
//                values.setPAC("warning");
//                values.setWarnD("warning");
//                values.setWarnT("warning");
//                values.setWarnV("warning");
//                data.setType("801");
//                data.setDsn("Device warning ");
//
//            }
//            data.setValues(values);
//        }
        List<EmsData> a = new ArrayList<>();
        a.add(data);
        String msg = null;
        try {
            msg = JsonUtils.Mapper.writeValueAsString(a);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return msg;
    }

}
