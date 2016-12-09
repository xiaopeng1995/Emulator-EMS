package io.j1st.storage.utils;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;

/**
 * Date Utils
 */
public class DateUtils {

    // Logger
    private static final Logger logger = LoggerFactory.getLogger(EncryptionUtils.class);


    public static String getDayForTimeZone(Date date, DateTimeZone timeZone) {
        DateTimeFormatter fmt = DateTimeFormat.forPattern("yyyyMMdd");
        DateTime dt = new DateTime(date, timeZone);
        return fmt.print(dt);
    }

    public static Integer getIntDayForTimeZone(Date date, DateTimeZone timeZone) {
        DateTimeFormatter fmt = DateTimeFormat.forPattern("yyyyMMdd");
        DateTime dt = new DateTime(date, timeZone);
        String day = fmt.print(dt);
        Integer intDay = 0;
        try {
            intDay = Integer.parseInt(day);
        }catch (NumberFormatException e){
            logger.error("日期{}转为数据时出错！",day);
        }
        return intDay;
    }

    public static String getHourForTimeZone(Date date, DateTimeZone timeZone) {
        DateTimeFormatter fmt = DateTimeFormat.forPattern("HH");
        DateTime dt = new DateTime(date, timeZone);
        return fmt.print(dt);
    }

    public static String getMinuteForTimeZone(Date date, DateTimeZone timeZone) {
        DateTimeFormatter fmt = DateTimeFormat.forPattern("mm");
        DateTime dt = new DateTime(date, timeZone);
        return fmt.print(dt);
    }
}
