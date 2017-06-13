package io.j1st.utils.http.resource;


import io.j1st.utils.http.entity.DataField;
import io.j1st.utils.http.entity.ResultEntity;
import io.j1st.utils.http.entity.Stream;
import io.j1st.utils.http.mysql.DataMySqlStorage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.security.PermitAll;
import javax.validation.Valid;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

import java.sql.Date;
import java.util.*;

/**
 * Created by xiaopeng on 2017/6/13.
 */
@Path("/data")
@Produces(MediaType.APPLICATION_JSON)
public class DataInsert {
    private DataMySqlStorage dataMySqlStorage;

    public DataInsert(DataMySqlStorage dataMySqlStorage) {
        this.dataMySqlStorage = dataMySqlStorage;
    }

    private static final Logger logger = LoggerFactory.getLogger(DataInsert.class);

    @Path("/add")
    @POST
    @PermitAll
    @Consumes(MediaType.APPLICATION_JSON)
    public ResultEntity fnxDownStream(@HeaderParam("Accept-Language") @DefaultValue("zh") String lang,
                                      @Valid List<Stream> data) {
        int count = 0;
        try {
            for (Stream stream : data) {
                String dataId = get32UUID();
                Boolean zu = this.dataMySqlStorage.insertRD(dataId, stream.getAsn(),stream.getSta().toString());

                if (zu)
                    for (String key : stream.getValues().keySet()) {
                        DataField dataf = new DataField();
                        dataf.setDataId(dataId);
                        dataf.setId(get32UUID());
                        dataf.setCate("po");
                        dataf.setFieldName(key);
                        dataf.setFieldValue((Double) stream.getValues().get(key));
                        dataf.setCreateTime(new Date(new java.util.Date().getTime()));
                        Boolean cu = this.dataMySqlStorage.insertRDdata(dataf);
                        if (cu)
                            count++;
                    }
            }
            Map<String, Object> m = new HashMap<>();
            m.put("trueAmount", count);
            return new ResultEntity<>(m);
        } catch (Exception e) {
            return new ResultEntity<>(500, e.getMessage());
        }
    }

    @Path("/find")
    @GET
    @PermitAll
    @Consumes(MediaType.APPLICATION_JSON)
    public ResultEntity findGendDataBytime() {

        return new ResultEntity<>(this.dataMySqlStorage.getUserIds());
    }

    private static String get32UUID() {
        String uuid = UUID.randomUUID().toString().trim().replaceAll("-", "");
        return uuid;
    }
}
