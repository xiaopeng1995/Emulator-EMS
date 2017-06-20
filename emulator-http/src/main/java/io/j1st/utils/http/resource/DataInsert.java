package io.j1st.utils.http.resource;


import io.j1st.utils.http.DataMap;
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
            String status = null;
            for (Stream stream : data) {
                if (stream.getValues() != null)
                    for (String key : stream.getValues().keySet()) {
                        if (key.equals("FSta"))
                            status = stream.getValues().get(key).toString();
                    }
            }
            for (Stream stream : data) {
                logger.debug("add data :{}", stream);
                String dataId = get32UUID();
                if (stream.getValues() != null)
                    for (String key : stream.getValues().keySet()) {
                        boolean xy = false;
                        for (String xkey : DataMap.getkey()) {
                            if (key.equals(xkey))
                                xy = true;
                        }
                        if (xy) {
                            DataField dataf = new DataField();
                            dataf.setDataId(dataId);
                            dataf.setId(get32UUID());
                            dataf.setCate(DataMap.getCate(key));
                            dataf.setUnit(DataMap.getUnit(key));
                            dataf.setFieldName(key);
                            Double value = 0d;
                            try {
                                value = Double.parseDouble(stream.getValues().get(key).toString());
                            } catch (Exception ex) {
                                logger.error("Type mismatch! key:{} , value:{}", key, stream.getValues().get(key));
                            }
                            dataf.setFieldValue(value);
                            dataf.setCreateTime(new Date(new java.util.Date().getTime()));
                            Boolean cu = this.dataMySqlStorage.insertRDdata(dataf);
                            if (cu)
                                count++;
                        }
                    }
                if (status == null) {
                    status = "4";
                }
                this.dataMySqlStorage.insertRD(dataId, status);
            }
            Map<String, Object> m = new HashMap<>();
            m.put("trueAmount", count);
            logger.debug("Successfully adding count:{}", count);
            return new ResultEntity<>(m);
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("Error format, add failure! ");
            return new ResultEntity<>(500, e.getMessage());
        }
    }

    @Path("/find")
    @GET
    @PermitAll
    @Consumes(MediaType.APPLICATION_JSON)
    public ResultEntity findGendDataBytime() {

        return new ResultEntity<>(this.dataMySqlStorage.getCount());
    }

    @Path("/test")
    @GET
    @PermitAll
    @Consumes(MediaType.APPLICATION_JSON)
    public ResultEntity test() {

        return new ResultEntity<>(true);
    }

    private static String get32UUID() {
        String uuid = UUID.randomUUID().toString().trim().replaceAll("-", "");
        return uuid;
    }
}
