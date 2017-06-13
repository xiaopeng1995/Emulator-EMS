package io.j1st.utils.http.resource;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.j1st.storage.entity.EmulatorRegister;
import io.j1st.util.util.HttpClientUtils;
import io.j1st.util.util.JsonUtils;
import io.j1st.utils.http.entity.PageResponse;
import io.j1st.utils.http.entity.ResultEntity;
import io.j1st.utils.http.mysql.DataMySqlStorage;
import io.j1st.utils.http.mysql.manager.MySQLPool;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.security.PermitAll;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by xiaopeng on 2017/6/13.
 */
@Path("/rddata")
@Produces(MediaType.APPLICATION_JSON)
public class DataInsert {
    private DataMySqlStorage dataMySqlStorage;

    public DataInsert(DataMySqlStorage dataMySqlStorage) {
        this.dataMySqlStorage = dataMySqlStorage;
    }
    private static final Logger logger = LoggerFactory.getLogger(DataInsert.class);

    @Path("/add")
    @PermitAll
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public ResultEntity fnxDownStream(@HeaderParam("Accept-Language") @DefaultValue("zh") String lang,
                                       Map<String, Object> map) {
            return new ResultEntity<>(true);
    }
    @Path("/find")
    @GET
    @PermitAll
    @Consumes(MediaType.APPLICATION_JSON)
    public ResultEntity findGendDataBytime() {

        return new ResultEntity<>(this.dataMySqlStorage.getUserIds());
    }


}
