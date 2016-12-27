package io.j1st.utils.http.resource;

import com.google.common.base.Optional;
import io.j1st.storage.DataMongoStorage;
import io.j1st.storage.MongoStorage;
import io.j1st.storage.entity.GenData;
import io.j1st.utils.http.entity.ResultEntity;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.security.PermitAll;
import javax.validation.Valid;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.*;

/**
 * Created by xiaopeng on 2016/11/21.
 */
@Path("/data")
@Produces(MediaType.APPLICATION_JSON)
public class GenDataAddUtil extends AbstractResource {
    // Logger
    private static final Logger logger = LoggerFactory.getLogger(GenDataAddUtil.class);

    public GenDataAddUtil(MongoStorage mongo, DataMongoStorage dataMongoStorage) {
        super(mongo, dataMongoStorage);
    }

//    @GET
//    public ResultEntity getticket(@QueryParam("username") Optional<String> username) {
//        logger.debug("进入GET: ticket");
//        Map<String, String> r = new HashMap<>();
//        if (username.isPresent())
//            r.put("username", username.get());
//        else
//            r.put("username", "不存在");
//        return new ResultEntity<>(r);
//    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public ResultEntity postticket(@Valid Map data) {
        logger.debug("Process signIn request: {}", data);
        // TODO: validate name password mobile email format
        Map<String, Object> r = new HashMap<>();
        r.put("username", "test");
        return new ResultEntity<>(r);
    }

    @Path("/add")
    @POST
    @PermitAll
    @Consumes(MediaType.APPLICATION_JSON)
    public ResultEntity addticket(@Valid GenData data) {
        mongo.addGenData(data);
        return new ResultEntity<>(null);
    }

    @Path("/findone")
    @GET
    @PermitAll
    @Consumes(MediaType.APPLICATION_JSON)
    public ResultEntity findGendDataBytime(@QueryParam("time") @DefaultValue("201611180750") String time,
                                           @QueryParam("state") @DefaultValue("0") int state) {

        return new ResultEntity<>(mongo.findGendDataByTime(time,state));
    }

    @Path("/delete")
    @GET
    @PermitAll
    @Consumes(MediaType.APPLICATION_JSON)
    public ResultEntity findUntreated(@QueryParam("time") String time) {

        return new ResultEntity<>("已经删除"+mongo.deleteDataByTime(time)+"行");
    }

}
