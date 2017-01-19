package io.j1st.storage;


import com.mongodb.MongoClient;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.UpdateOptions;
import com.mongodb.client.model.IndexOptions;
import com.mongodb.client.model.UpdateOptions;
import io.j1st.storage.entity.GenData;
import io.j1st.storage.utils.DateUtils;
import org.apache.commons.configuration.AbstractConfiguration;
import org.apache.commons.lang3.StringUtils;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.joda.time.DateTimeZone;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import static com.mongodb.client.model.Filters.and;
import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Filters.in;
import static com.mongodb.client.model.Indexes.ascending;
import static com.mongodb.client.model.Indexes.descending;


/**
 * 记录数据
 * Data MongoDB Storage
 */
public class DataMongoStorage {

    protected MongoClient client;
    protected MongoDatabase database;

    public void init(AbstractConfiguration config) {
        // MongoClient
        List<ServerAddress> addresses = parseAddresses(config.getString("data_mongo.address"));
        List<MongoCredential> credentials = parseCredentials(
                config.getString("data_mongo.userName"),
                "admin",
                config.getString("data_mongo.password"));
        if (addresses.size() == 1) {
            this.client = new MongoClient(addresses.get(0), credentials);
        } else {
            this.client = new MongoClient(addresses, credentials);
        }
        this.database = this.client.getDatabase(config.getString("data_mongo.database"));
        // indexes
        this.database.getCollection("emulator_datas").createIndex(descending("updated_at"), new IndexOptions().expireAfter(3L, TimeUnit.DAYS));
        this.database.getCollection("ems_forecast_data").createIndex(descending("updated_at"), new IndexOptions().expireAfter(3L, TimeUnit.DAYS));
        //drop index(这里暂时没用，预留，当我们需要改变与时间相关的检索字段时，需要先删除再新建，删除的前提是检索字段已经存在，不存在会报错，慎改)
        //this.database.getCollection("emulator_datas").dropIndex(ascending("_id"));
    }

    public void destroy() {
        if (this.client != null) this.client.close();
    }

    private ServerAddress parseAddress(String address) {
        int idx = address.indexOf(':');
        return (idx == -1) ?
                new ServerAddress(address) :
                new ServerAddress(address.substring(0, idx), Integer.parseInt(address.substring(idx + 1)));
    }

    private List<ServerAddress> parseAddresses(String addresses) {
        List<ServerAddress> result = new ArrayList<>();
        String[] addrs = addresses.split(" *, *");
        for (String addr : addrs) {
            result.add(parseAddress(addr));
        }
        return result;
    }

    private List<MongoCredential> parseCredentials(String userName, String database, String password) {
        List<MongoCredential> result = new ArrayList<>();
        result.add(MongoCredential.createCredential(userName, database, password.toCharArray()));
        return result;
    }

    /**
     * 删除用户相关的所有信息
     */
    public String deleteAnalysisInfo(ObjectId agent_id) {
        long attributeCount = 0, agentLogs = 0;

        //删除agent attr info
        attributeCount = this.database.getCollection("agent_data").deleteMany(eq("agent_id", agent_id)).getDeletedCount();

        //删除agent log info
        agentLogs = this.database.getCollection("data_agents_logs").deleteMany(eq("agent_id", agent_id)).getDeletedCount();

        return "成功删除: " + attributeCount + " 条agent attributes数据 " + agentLogs + " 条agent logs数据";
    }

    //是否添加预测数据
    public Boolean findycdata(String agentId, Integer day) {
        return this.database.getCollection("ems_forecast_data")
                .find(and(eq("agent_id", new ObjectId(agentId)), eq("day", day))).first() == null;
    }

    /**
     * 预测一天的pac数据
     *
     * @param agentId
     * @param deviceSn
     * @param type     硬件类型
     * @param date     时间
     * @param timeZone 时区
     * @param dyield   一天的总值
     */
    public boolean updateAnalysisInfo(ObjectId agentId, String deviceSn, Integer type, Date date, DateTimeZone timeZone,
                                      Document pac, double dyield) {
        if (timeZone == null) {
            timeZone = DateTimeZone.getDefault();
        }
        Integer day = DateUtils.getIntDayForTimeZone(date, timeZone);
        Document soid = new Document();
        soid.append("device_sn", deviceSn);
        soid.append("agent_id", agentId);
        soid.append("type", type + "");
        soid.append("DYield", dyield);
        soid.append("day", day);
        soid.append("Pac", pac);
        return this.database.getCollection("ems_forecast_data")
                .updateOne(and(eq("agent_id", agentId), eq("device_sn", deviceSn)),
                        new Document()
                                .append("$set", soid)
                                .append("$currentDate", new Document("updated_at", true)),
                        new UpdateOptions().upsert(true)).getModifiedCount() > 0;

    }

    /**
     * 预测一天的负载数据
     */
    public boolean updatePowerT(ObjectId agentId, String deviceSn, String type, Date date, DateTimeZone timeZone,
                                Document w, double DWhlmp) {
        if (timeZone == null) {
            timeZone = DateTimeZone.getDefault();
        }
        Integer day = DateUtils.getIntDayForTimeZone(date, timeZone);
        Document soid = new Document();
        soid.append("device_sn", deviceSn);
        soid.append("agent_id", agentId);
        soid.append("type", type);
        soid.append("day", day);
        soid.append("DWhImp", DWhlmp);
        soid.append("W", w);
        return this.database.getCollection("ems_forecast_data")
                .updateOne(and(eq("agent_id", agentId), eq("device_sn", deviceSn)),
                        new Document()
                                .append("$set", soid)
                                .append("$currentDate", new Document("updated_at", true)),
                        new UpdateOptions().upsert(true)).getModifiedCount() > 0;

    }

    /**
     * 添加GenData数据
     */
    public boolean addGenData(ObjectId agentId, String day,
                              Document pac, Document etoday, Document load , Document allDWhlmp) {

        Document soid = new Document();
        soid.append("agent_id", agentId);
        soid.append("day", day.substring(0, 8));
        soid.append("pVPower", pac);
        soid.append("powerT", load);
        soid.append("eToday", etoday);
        soid.append("DWhImp", allDWhlmp);
        return this.database.getCollection("emulator_datas")
                .updateOne(eq("agent_id", agentId),
                        new Document()
                                .append("$set", soid)
                                .append("$currentDate", new Document("updated_at", true)),
                        new UpdateOptions().upsert(true)).getModifiedCount() > 0;

    }

    /**
     * 通过时间查找对应数据
     *
     * @return genData
     */
    public Document findGendDataByTime(String agentid, String key) {
        Document d = this.database.getCollection("emulator_datas")
                .find(eq("agent_id", new ObjectId(agentid))).first();
        if (d != null) {
            Object genData = d.get(key);
            return (Document) genData;
        } else
            return null;
    }
    public long deleteGendDataByTime(String agentid) {
        long count = 0;
        count += this.database.getCollection("emulator_datas")
                .deleteMany(eq("agent_id", new ObjectId(agentid)))
                .getDeletedCount();
        count += this.database.getCollection("ems_forecast_data")
                .deleteMany(eq("agent_id", new ObjectId(agentid)))
                .getDeletedCount();

        return count;
    }

    /**
     * 删除time时间之前数据
     *
     *
     * @return 受影响行数
     */
    public long deleteDataByTime() {
        long count = 0;

         count += this.database.getCollection("emulator_datas")
                        .deleteMany(eq("state", 0))
                        .getDeletedCount();


        return count;
    }
}
