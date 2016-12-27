package io.j1st.utils.http.resource;

/**
 * Created by wenxudong on 2016/11/1.
 */

import io.j1st.storage.DataMongoStorage;
import io.j1st.storage.MongoStorage;
import io.j1st.storage.entity.Product;
import io.j1st.utils.http.entity.ResultEntity;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.security.PermitAll;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Select Util
 */
@Path("/select/infos")
@Produces(MediaType.APPLICATION_JSON)
public class SelectUtile extends AbstractResource{

    private static final Logger logger = LoggerFactory.getLogger(SelectUtile.class);

    public SelectUtile(MongoStorage mongo, DataMongoStorage dataMongoStorage) {
        super(mongo,dataMongoStorage);
    }

    /**
     * get UserToken by email
     * @param email
     * @return Token
     */
    @Path("/token/{email}")
    @PermitAll
    @GET
    @Consumes(MediaType.APPLICATION_JSON)
    public ResultEntity getUserTokenByMail(@PathParam("email") String email) {
        logger.debug("查询邮箱为: {} 的Token", email);
        // basic info
        String result=this.mongo.getUserTokenByMail(email);
        Map hashmap=new HashMap<>();
        System.out.println(result);
        return new ResultEntity<>(result);
    }

    /**
     * 根据用户Id获取产品列表
     * @param userId 用户Id
     * @return
     */
    @Path("/products/{userId}")
    @PermitAll
    @GET
    @Consumes(MediaType.APPLICATION_JSON)
    public ResultEntity getProductsByUserId(@PathParam("userId") String userId) {
        logger.debug("查询用户Id为: {} 的产品列表", userId);
        // basic info
        List<Product> result=this.mongo.getProductsByUserId(new ObjectId(userId),2,2,true);
        Map hashmap=new HashMap<>();
        System.out.println(result);
        return new ResultEntity<>(result);
    }
}
