package io.j1st.utils.http.resource;

import com.google.common.base.Optional;
import io.j1st.storage.DataMongoStorage;
import io.j1st.storage.MongoStorage;
import io.j1st.storage.entity.EmulatorRegister;
import io.j1st.storage.entity.GenData;
import io.j1st.utils.http.entity.PageResponse;
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


    @Path("/findall")
    @GET
    @PermitAll
    @Consumes(MediaType.APPLICATION_JSON)
    public ResultEntity findGendDataBytime(@QueryParam("page") @DefaultValue("1") int page,
                                           @QueryParam("limit") @DefaultValue("10") int limit,
                                           @QueryParam("isAsc") @DefaultValue("false") Boolean isAsc,
                                           @QueryParam("isRead") @DefaultValue("false") Boolean isRead) {

        List<EmulatorRegister> sy = mongo.getEmulatorRegisterByno(page, limit, isAsc);
        Long count = mongo.getEmulatorRegister();
        PageResponse pageResponse = new PageResponse();
        pageResponse.setCount(count);
        Long totalPage;
        if (count % limit == 0)
            totalPage = count / limit;
        else
            totalPage = count / limit + 1;
        pageResponse.setTotalPage(totalPage);
        Map<String, Object> info = new HashMap<>();
        info.put("pageInfo", pageResponse);
        info.put("registerInfo", sy);
        return new ResultEntity<>(info);
    }

    @Path("/findone")
    @GET
    @PermitAll
    @Consumes(MediaType.APPLICATION_JSON)
    public ResultEntity findGendDataBytime(@QueryParam("emulatorid") String emulatorid) {
        EmulatorRegister sy=new EmulatorRegister();
        List<EmulatorRegister> syall = mongo.getEmulatorRegisterByID(emulatorid);
        if (syall.size() > 0)
             sy = mongo.getEmulatorRegisterByID(emulatorid).get(0);
        return new ResultEntity<>(sy);
    }

    @Path("/delete")
    @GET
    @PermitAll
    @Consumes(MediaType.APPLICATION_JSON)
    public ResultEntity findUntreated(@QueryParam("time") String time,
                                      @QueryParam("is") @DefaultValue("0") int is) {
        if (is > 0)
            logger.info("开始删除" + time + "之后数据111");
        else
            logger.info("开始删除" + time + "之前数据000");
        return new ResultEntity<>("已经删除" + mongo.deleteDataByTime(time, is) + "行");
    }


}
