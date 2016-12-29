package io.j1st.data.job;

import io.j1st.data.entity.Registry;
import io.j1st.data.predict.PVpredict;
import io.j1st.storage.DataMongoStorage;
import io.j1st.storage.MongoStorage;
import io.j1st.storage.entity.Agent;
import org.quartz.Job;
import org.quartz.JobExecutionContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * 电池模拟数据工作
 */
public class DayJob implements Job {
    Logger logger = LoggerFactory.getLogger(DayJob.class);

    @Override
    public void execute(JobExecutionContext context) {
        DataMongoStorage dmogo = (DataMongoStorage) Registry.INSTANCE.getValue().get("dmogo");
        PVpredict p = new PVpredict(dmogo);
        SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmmss");
        String date = format.format(new Date());
        date = date.substring(0, 8) + "000000";
        logger.info("到凌晨添加实时数据");
        try {
            //添加实时数据
            p.PVInfo(date, "5848cacedafbaf35325b70e0", 1);
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

}