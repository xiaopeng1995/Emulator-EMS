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
        int [] cCloud={2, 3, 1, 0, 3, 9, 7, 2};
        Registry.INSTANCE.saveKey("aCloud",cCloud);
        DataMongoStorage dmogo = (DataMongoStorage) Registry.INSTANCE.getValue().get("dmogo");
        PVpredict p = new PVpredict(dmogo);
        SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmmss");
        String date = format.format(new Date());
        date = date.substring(0, 8) + "000000";
        logger.info("到凌晨添加实时数据");
        try {
            //添加实时数据
            p.PVInfo(date, "5848cacedafbaf35325b70e0", 1,pvcloud());
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }
    //太阳能云因子
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