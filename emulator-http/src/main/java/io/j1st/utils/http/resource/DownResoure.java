package io.j1st.utils.http.resource;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.dropwizard.auth.Auth;
import io.j1st.storage.DataMongoStorage;
import io.j1st.storage.MongoStorage;
import io.j1st.storage.entity.Agent;
import io.j1st.storage.entity.Product;
import io.j1st.util.util.HttpClientUtils;
import io.j1st.util.util.JsonUtils;
import io.j1st.utils.http.entity.ErrorCode;
import io.j1st.utils.http.entity.ResultEntity;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.security.PermitAll;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.*;

/**
 * 硬件下发指令接口
 */
@Path("/downstream")
@Produces(MediaType.APPLICATION_JSON)
public class DownResoure extends AbstractResource {

    public DownResoure(MongoStorage mongo, DataMongoStorage dataMongoStorage) {
        super(mongo, dataMongoStorage);
    }

    // Logger
    private static final Logger logger = LoggerFactory.getLogger(DownResoure.class);

    /**
     * 执行Fn Button，下发stream
     *
     * @param lang 中英文
     * @return 是否下发成功
     */
    @Path("/{agentId}")
    @PermitAll
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public ResultEntity fnxDownStream(@HeaderParam("Accept-Language") @DefaultValue("zh") String lang,
                                      @PathParam("agentId") String agentId, Map<String, Object> map) {

        // validate objectId
        if (!ObjectId.isValid(agentId)) {
            logger.warn("agentId {} 不是有效的 ObjectId", agentId);
            return new ResultEntity("不是有效的 ObjectId");
        }


        //验证是否在线
//        Agent agent = this.mongo.getAgentById(new ObjectId(agentId));
//        if (agent == null) {
//            logger.debug("Agent 不存在.");
//            return new ResultEntity("Agent 不存在.");
//        }
//        if (!agent.isConnected()) {
//            logger.debug("downstream 下发 Agent 已离线.");
//            return new ResultEntity("downstream 下发 Agent 已离线.");
//        }
//
        String result = null;
        String body = null;
        try {
            logger.debug("解析客户端发送的json数据:{}", JsonUtils.Mapper.writeValueAsString(map));
            //组装下发的数据格式
            Map<String, Object> payload = new HashMap();
            String key = map.get("code").toString();
            Integer qos = 0;
            if (map.get("qos") != null && map.get("qos") instanceof Integer) {
                qos = Integer.valueOf(map.get("qos").toString());
            }
            List<Map<String, Object>> fnItemList = new ArrayList<>();
            //判断是否有snList
            if (map.get("sns") != null && map.get("sns") instanceof List && ((List) map.get("sns")).size() != 0) {
                List<String> sns = (List<String>) map.get("sns");
                for (String sn : sns) {
                    if (map.get("values") != null && map.get("values") instanceof Map) {
                        Map values = new HashMap<>();
                        values.putAll((Map) map.get("values"));
                        values.put("dsn", sn);
                        fnItemList.add(values);
                    }
                }
            } else {
                if (map.get("values") != null && map.get("values") instanceof Map) {
                    Map values = (Map) map.get("values");
                    fnItemList.add(values);
                }
            }
            payload.put(key, fnItemList);
            final String topic = getTopic(agentId);

            body = JsonUtils.Mapper.writeValueAsString(payload);

//            final String body = JsonUtils.Mapper.writeValueAsString(map.get("payload"));
           // PropertiesConfiguration mqttHttp= new PropertiesConfiguration("config/mqttHttp.properties");
            //+ mqttHttp.getString("mqtt.http") +
            final String url = "http://139.196.230.150:8081/mqtt/clients/" + agentId + "/publish?qos=" + qos + "&topicName=" + topic;
            final String token = this.mongo.getUserById(new ObjectId(agentId)).getToken();
            result = HttpClientUtils.sendDownStreamPost(url, token, null, body);
        } /*catch (ConfigurationException e) {
            e.printStackTrace();
            logger.info("读取配置文件发生错误:{}", e.getMessage());
        } */catch (JsonProcessingException e) {
            e.printStackTrace();
            logger.info("客户端发来的json格式解析发生错误:{},下发操作放弃", e.getMessage());
        }

        logger.debug("http请求发送成功，请求结果为：{} ", result);

        return new ResultEntity<>(result);
    }

    public static String getTopic(String agetnId) {
        return "agents/" + agetnId + "/downstream";
    }
}
