package io.j1st.utils.http.resource;

import io.dropwizard.auth.Auth;
import io.j1st.storage.DataMongoStorage;
import io.j1st.storage.MongoStorage;
import io.j1st.utils.http.entity.ResultEntity;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.security.PermitAll;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.HashMap;
import java.util.Map;

/**
 * delete util
 */
@Path("/delete/infos")
@Produces(MediaType.APPLICATION_JSON)
public class DeleteUtil extends AbstractResource{

    // Logger
    private static final Logger logger = LoggerFactory.getLogger(DeleteUtil.class);

    public DeleteUtil(MongoStorage mongo, DataMongoStorage dataMongoStorage) {
        super(mongo,dataMongoStorage);

    }

    @Path("/basic/{userName}/{role}")
    @PermitAll
    @DELETE
    @Consumes(MediaType.APPLICATION_JSON)
    public ResultEntity deleteInfo(@PathParam("userName") String userName, @PathParam("role") int role) {
        logger.debug("删除用户: {} 相关的所有信息", userName);
        // basic info
        String result=this.mongo.deleteBasicInfo(userName, role);
        Map hashmap=new HashMap<>();
        return new ResultEntity<>(result);
    }

    @Path("/analysis/{agentId}")
    @PermitAll
    @DELETE
    @Consumes(MediaType.APPLICATION_JSON)
    public ResultEntity deleteInfo(@PathParam("agentId") String agentId) {
        logger.debug("删除Agent: {} 相关的所有信息", agentId);
        // analysis info
        String result=this.dataMongoStorage.deleteAnalysisInfo(new ObjectId(agentId));
        return new ResultEntity<>(result);
    }
}
