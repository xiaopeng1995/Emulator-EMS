package io.j1st.data.job;

import io.j1st.data.entity.Registry;
import io.j1st.storage.MongoStorage;
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
        MongoStorage mogo = (MongoStorage) Registry.INSTANCE.getValue().get("mogo");
        List<String> agentIds;
        Object data = Registry.INSTANCE.getValue().get("agentIdAll");
        if (data != null) {
            logger.info("已到凌晨开始清零当天数据");
            agentIds = (List<String>) data;
            System.out.println(agentIds.size());
            for (String agentID : agentIds) {
                boolean is;
                //当天电网放电清零
                is = mogo.updateEmulatorRegister(agentID, "DWhExp", 0.0);
                if (is)
                    logger.debug(agentID + "_DWhExp 电网放已清零..");
                //当天逆变器放电清零
                is = mogo.updateEmulatorRegister(agentID, "DCkWh", 0.0);
                if (is)
                    logger.debug(agentID + "_DCkWh 逆变器放已清零..");

                //当天电网充电清零
                is = mogo.updateEmulatorRegister(agentID, "DWhImp", 0.0);
                if (is)
                    logger.debug(agentID + "_DWhImp 电网充已清零..");
                //当天逆变器充电清零
                is = mogo.updateEmulatorRegister(agentID, "DDkWh", 0.0);
                if (is)
                    logger.debug(agentID + "_DDkWh 逆变器充已清零..");

                //当天PV电量清零
                is = mogo.updateEmulatorRegister(agentID, "DYield", 0.0);
                if (is)
                    logger.debug(agentID + "_DYield PV已清零..");
                //负载当天
                is = mogo.updateEmulatorRegister(agentID, "loadDWhImp", 0.0);
                if (is)
                    logger.debug(agentID + "_loadDWhImp load已清零..");

            }
        }
    }

}