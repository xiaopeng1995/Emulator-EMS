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
            logger.info("已到凌晨开始清零当天数据");
            agents = (List<Agent>) Registry.INSTANCE.getValue().get("agents");
            for (Agent agent : agents) {
                String agentID=agent.getId().toString();
                //当天放电清零
                Registry.INSTANCE.saveKey(agentID + "_DCkWh",0.0);
                logger.debug(agentID + "_DCkWh 已清零..");
                //当天充电清零
                Registry.INSTANCE.saveKey(agentID + "_DDkWh",0.0);
                logger.debug(agentID + "_DDkWh 已清零..");
                //当天PV电量清零
                Registry.INSTANCE.saveKey(agentID + "_DYield",0.0);
                logger.debug(agentID + "_DYield 已清零..");
            }
        }
    }

}