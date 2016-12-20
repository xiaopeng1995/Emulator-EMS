package io.j1st.data.job;

import io.j1st.data.entity.Registry;
import io.j1st.storage.entity.Agent;
import org.quartz.Job;
import org.quartz.JobExecutionContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * 电池模拟数据工作
 */
public class DayJob implements Job {
    Logger logger = LoggerFactory.getLogger(DayJob.class);

    @Override
    public void execute(JobExecutionContext context) {
        List<Agent> agents;
        if (Registry.INSTANCE.getValue().get("agents") != null) {
            logger.info("已到凌晨统计开始当天功率情况...");
            agents = (List<Agent>) Registry.INSTANCE.getValue().get("agents");
            for (Agent agent : agents) {
                String agentID=agent.getId().toString();
                Object num = Registry.INSTANCE.getValue().get(agentID + "_TotWhImp");
                double TotWhImp = (num == null ? 0.0 : (double) num);//电网正向有功总电能  (放电总功率)


                num = Registry.INSTANCE.getValue().get(agentID + "_TotWhExp");
                double TotWhExp = (num == null ? 0.0 : (double) num);//电网负向有功总电能  (充电总功率)


                num = Registry.INSTANCE.getValue().get(agentID + "_DayTotWhImp");
                double DCkWh=num != null?-(TotWhImp-(double) num):-TotWhImp; //当天放电

                num = Registry.INSTANCE.getValue().get(agentID + "_DayTotWhExp");
                double DDkWh=num != null?TotWhExp-(double) num:TotWhExp;//当天放电

                Registry.INSTANCE.saveKey(agentID + "_DCkWh",DCkWh);
                Registry.INSTANCE.saveKey(agentID + "_DDkWh",DDkWh);

                Registry.INSTANCE.saveKey(agentID + "_DayTotWhImp",TotWhImp);
                Registry.INSTANCE.saveKey(agentID + "_DayTotWhExp",TotWhExp);
            }
        }
    }

}