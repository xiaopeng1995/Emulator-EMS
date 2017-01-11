package io.j1st.data;

import io.j1st.storage.DataMongoStorage;
import io.j1st.storage.MongoStorage;
import io.j1st.storage.entity.Agent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.bson.types.ObjectId;

/**
 * 生产数据配置表
 */
public class ConfigFun {
    private static final Logger logger = LoggerFactory.getLogger(ConfigFun.class);
    private DataMongoStorage dmogo;
    private MongoStorage mogo;

    public ConfigFun(DataMongoStorage dmogo, MongoStorage mogo) {
        this.dmogo = dmogo;
        this.mogo = mogo;
    }

    public void startOne(String emulatorId, int type, int system) {
        Agent agent = null;
        Object job;
        //emulatorId类型判断
        switch (type) {
            case 0:
                agent = mogo.getAgentsById(new ObjectId(emulatorId));
                break;
            case 1:
                statbatch(emulatorId);
                break;
        }
        //添加实时数据和预测数据

        //启动接收任务

        switch (system) {
            case 0:
                job = "PV";
                break;
            case 1:
                job = "EMS";
                break;
        }
        //启动发送任务

    }

    private void statbatch(String emulatorId) {

    }
}
