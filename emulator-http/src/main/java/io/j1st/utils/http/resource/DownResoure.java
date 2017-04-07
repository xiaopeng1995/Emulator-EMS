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
        String mapjson;
        try {
            mapjson = JsonUtils.Mapper.writeValueAsString(map);
        } catch (JsonProcessingException e) {
            return new ResultEntity(503, "格式错误,无法解析!");
        }
        logger.debug("解析客户端发送的json数据:{}", mapjson);
        // validate objectId
        if (!ObjectId.isValid(agentId)) {
            logger.warn("agentId {} 不是有效的 ObjectId", agentId);
            return new ResultEntity(503, "不是有效的 ObjectId");
        }
        if (!map.get("values").toString().contains("kill") && !map.get("code").toString().equals("emulatorJob")) {
            if (!mongo.findEmulatorRegister(agentId, "systemTpye").toString().equals("1")) {
                if (!mapjson.contains("0,0,0,0"))
                    return new ResultEntity(502, "不是EMS系统无法下发此类型指令!");

            }
            if (!mongo.findEmulatorRegister(agentId, "onlinefail").toString().equals("1")) {
                return new ResultEntity(502, "不是正在运行数据无法下发指令!");
            }
        }

        String result = null;
        String body = null;
        try {
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
            PropertiesConfiguration mqttHttp = new PropertiesConfiguration("config/mqttHttp.properties");
            //
            final String url = "http://" + mqttHttp.getString("mqtt.http") + "/mqtt/clients/" + agentId + "/publish?qos=" + qos + "&topicName=" + topic;
            logger.debug(agentId + "收到指令");
            final String token = this.mongo.getUserById(new ObjectId(agentId)).getToken();
            result = HttpClientUtils.sendDownStreamPost(url, token, null, body);
        } catch (ConfigurationException e) {
            e.printStackTrace();
            logger.info("读取配置文件发生错误:{}", e.getMessage());
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            logger.info("客户端发来的json格式解析发生错误:{},下发操作放弃", e.getMessage());
        }

        logger.debug("http请求发送成功，请求结果为：{} ", result);
        if (result.contains("true"))
            return new ResultEntity<>(result);
        else
            return new ResultEntity<>(500, "指令下发失败请检查信息");
    }

    public static String getTopic(String agetnId) {
        return "agents/" + agetnId + "/downstream";
    }
}
