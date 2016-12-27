package io.j1st.utils.http.resource;

/**
 * Created by 旭冬 on 2016/11/1.
 */

import io.j1st.storage.DataMongoStorage;
import io.j1st.storage.MongoStorage;
import io.j1st.storage.entity.User;
import io.j1st.storage.entity.UserRole;
import io.j1st.utils.http.entity.ResultEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.security.PermitAll;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.HashMap;
import java.util.Map;

/**
 * insert util
 */
@Path("/insert/info")
@Produces(MediaType.APPLICATION_JSON)
public class InsertUtil extends  AbstractResource{
    // Logger
    private static final Logger logger = LoggerFactory.getLogger(DeleteUtil.class);

    public InsertUtil(MongoStorage mongo, DataMongoStorage dataMongoStorage) {
        super(mongo,dataMongoStorage);

    }

    /**
     * 新增用户
     *
     * @param name      用户名
     * @param password  密码
     * @param email     邮箱
     * @param mobile    电话
     * @param role      角色
     * @return  User
     */
    @Path("/user/{name}/{password}/{email}/{mobile}/{role}")
    @PermitAll
    @GET
    @Consumes(MediaType.APPLICATION_JSON)
    public ResultEntity insertInfo(@PathParam("name") String name, @PathParam("password") String password,
                                   @PathParam("email") String email,@PathParam("mobile") String mobile,
                                   @PathParam("role") String role) {
        logger.debug("添加: {} 用户及其相关的信息", name);

        User user = new User();
        user.setRole(UserRole.valueOf(1));
        user.setName(name);
        user.setPassword(password);
        user.setEmail(email);
        user.setMobile(mobile);

        // basic info
        user=this.mongo.insertUser(user);
        Map hashmap=new HashMap<>();
        return new ResultEntity<>(user);
    }
}
