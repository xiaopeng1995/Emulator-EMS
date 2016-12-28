package io.j1st.storage;


import com.mongodb.MongoClient;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.UpdateOptions;
import io.j1st.storage.entity.Product;
import io.j1st.storage.entity.User;
import io.j1st.storage.utils.DateUtils;
import org.apache.commons.configuration.AbstractConfiguration;
import org.apache.commons.lang3.StringUtils;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.joda.time.DateTimeZone;

import java.util.*;
import java.util.function.Consumer;

import static com.mongodb.client.model.Filters.and;
import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Filters.in;


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
     *
     */
    public String deleteAnalysisInfo(ObjectId agent_id) {
        long attributeCount=0,agentLogs=0;

        //删除agent attr info
        attributeCount = this.database.getCollection("agent_data").deleteMany(eq("agent_id", agent_id)).getDeletedCount();

        //删除agent log info
        agentLogs = this.database.getCollection("data_agents_logs").deleteMany(eq("agent_id", agent_id)).getDeletedCount();

        return "成功删除: " + attributeCount + " 条agent attributes数据 "+agentLogs+" 条agent logs数据";
    }
    /**
     * 预测一天的pac数据
     *
     * @param agentId
     * @param deviceSn
     * @param type          硬件类型
     * @param date          时间
     * @param timeZone      时区
     * @param key           当前毫秒数
     * @param value         当前值
     * @param dyield        一天的总值
     */
    public void updateAnalysisInfo(ObjectId agentId, String deviceSn, Integer type,Date date,DateTimeZone timeZone,
                                   long key,double value,double dyield){
        if(timeZone == null){
            timeZone = DateTimeZone.getDefault();
        }
        Integer day = DateUtils.getIntDayForTimeZone(date, timeZone);
        Document soid = new Document();
        soid.append("device_sn", deviceSn);
        soid.append("agent_id", agentId);
        soid.append("type", type);

        Document sd = new Document();
        sd.append("updated_at",date);
        sd.append("DYield",dyield);
        sd.append("Pac."+key,value);

        this.database.getCollection("ems_forecast_data")
                .updateOne(and(eq("device_sn", deviceSn),
                        eq("agent_id", agentId),
                        eq("day", day)),
                        new Document()
                                .append("$set",sd)
                                .append("$setOnInsert", soid),
                        new UpdateOptions().upsert(true));

    }
}
