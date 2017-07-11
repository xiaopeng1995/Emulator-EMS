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
import java.text.SimpleDateFormat;
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
    public ResultEntity fnxDownStream(@HeaderParam("agentId") String agentId,
                                      @Valid List<Stream> data) {
        logger.debug("请求添加AgentID:{}的数据", agentId);
        int count = 0;
        try {
            String status = "";
            String asn = "";
            String dsn = "";
            java.util.Date date = null;
            String dataId = get32UUID();
            for (Stream stream : data) {
                if (stream.getValues() != null)
                    for (String key : stream.getValues().keySet()) {
                        if (key.equals("FSta"))
                            status = stream.getValues().get(key).toString();
                        if (key.equals("sn"))
                            asn = stream.getValues().get(key).toString();
                    }
            }
            for (Stream stream : data) {
                logger.debug("add data :{}", stream);
                if (stream.getDsn() != null)
                    dsn = stream.getDsn();
                if (stream.getValues() != null)
                    for (String key : stream.getValues().keySet()) {
                        int xy = 0; //0跳过 1是需要的key储存 2特殊字段
                        for (String xkey : DataMap.getkey()) {
                            if (key.equals(xkey)) {
                                xy = 1;
                                if (key.equals("FirstBrk")) {
                                    try {
                                        xy = 2;
                                        int firstBrkCount = 0;
                                        String value = stream.getValues().get(key).toString();
                                        String[] valuess = value.split(";");
                                        for (String s : valuess) {
                                            System.out.println(s);
                                        }
                                        //第一个值 Overload fault@
                                        String[] fault = valuess[0].split("@:");
                                        String faultbw = fault[0];
                                        String faultbwTime = fault[1];
                                        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd HH:mm:ss");
                                        date = sdf.parse(faultbwTime);
                                        String faultbwTime1 = faultbwTime.replace(" ", "").replace(":", "");
                                        //添加头信息
                                        if (true) {
                                            DataField datatbw = new DataField();
                                            datatbw.setDataId(dataId);
                                            datatbw.setId(get32UUID());
                                            datatbw.setCate(faultbw);
                                            datatbw.setUnit("s");
                                            datatbw.setFieldName("脱扣信息在Cate");
                                            datatbw.setFieldValue(0d);
                                            datatbw.setCreateTime(new Date(new java.util.Date().getTime()));
                                            Boolean curr = this.dataMySqlStorage.insertRDdata(datatbw);
                                            if (curr)
                                                count++;
                                        }
                                        //添加时间信息
                                        if (true) {
                                            DataField datatbw = new DataField();
                                            datatbw.setDataId(dataId);
                                            datatbw.setId(get32UUID());
                                            datatbw.setCate("BCD\\BCD ");
                                            datatbw.setUnit("-");
                                            datatbw.setFieldName("脱扣时间(年\\月)");
                                            datatbw.setFieldValue(Double.valueOf(faultbwTime1.substring(0, 6)));
                                            datatbw.setCreateTime(new Date(new java.util.Date().getTime()));
                                            Boolean curr = this.dataMySqlStorage.insertRDdata(datatbw);
                                            if (curr)
                                                count++;
                                        }
                                        if (true) {
                                            DataField datatbw = new DataField();
                                            datatbw.setDataId(dataId);
                                            datatbw.setId(get32UUID());
                                            datatbw.setCate("BCD\\BCD ");
                                            datatbw.setUnit("-");
                                            datatbw.setFieldName("脱扣时间(日\\时)");
                                            datatbw.setFieldValue(Double.valueOf(faultbwTime1.substring(6, 10)));
                                            datatbw.setCreateTime(new Date(new java.util.Date().getTime()));
                                            Boolean curr = this.dataMySqlStorage.insertRDdata(datatbw);
                                            if (curr)
                                                count++;
                                        }
                                        if (true) {
                                            DataField datatbw = new DataField();
                                            datatbw.setDataId(dataId);
                                            datatbw.setId(get32UUID());
                                            datatbw.setCate("BCD\\BCD ");
                                            datatbw.setUnit("-");
                                            datatbw.setFieldName("脱扣时间(分\\秒)");
                                            datatbw.setFieldValue(Double.valueOf(faultbwTime1.substring(10, faultbwTime1.length())));
                                            datatbw.setCreateTime(new Date(new java.util.Date().getTime()));
                                            Boolean curr = this.dataMySqlStorage.insertRDdata(datatbw);
                                            if (curr)
                                                count++;
                                        }
                                        //第二个值Type
                                        String Type = valuess[1].replace("Type: ","");
                                        if(true)
                                        {
                                            String duration = valuess[valuess.length - 2].replace("Duration: ", "").replace("s", "");
                                            DataField datadur = new DataField();
                                            datadur.setDataId(dataId);
                                            datadur.setId(get32UUID());
                                            datadur.setCate("WORD");
                                            datadur.setUnit(" ");
                                            datadur.setFieldName("脱扣原因");
                                            datadur.setFieldValue(Double.valueOf(Type));
                                            datadur.setCreateTime(new Date(new java.util.Date().getTime()));
                                            Boolean curr = this.dataMySqlStorage.insertRDdata(datadur);
                                            if (curr)
                                                count++;
                                        }
                                        //第三个值Duration
                                        String duration = valuess[valuess.length - 2].replace("Duration: ", "").replace("s", "");
                                        DataField datadur = new DataField();
                                        datadur.setDataId(dataId);
                                        datadur.setId(get32UUID());
                                        datadur.setCate("UINT");
                                        datadur.setUnit("s");
                                        datadur.setFieldName("脱扣时间");
                                        datadur.setFieldValue(Double.valueOf(duration));
                                        datadur.setCreateTime(new Date(new java.util.Date().getTime()));
                                        Boolean curr = this.dataMySqlStorage.insertRDdata(datadur);
                                        if (curr)
                                            count++;
                                        //最后一个值Current
                                        String[] currents = valuess[valuess.length - 1].replace("Current: ", "").split(",");
                                        for (String cur : currents) {
                                            System.out.println(cur);
                                        }
                                        for (String cur : currents) {
                                            DataField datacur = new DataField();
                                            datacur.setDataId(dataId);
                                            datacur.setId(get32UUID());
                                            datacur.setCate("UINT");
                                            datacur.setUnit("A");
                                            datacur.setFieldName("脱扣" + cur.split(":")[0] + "电流");
                                            datacur.setFieldValue(Double.valueOf(cur.split(":")[1].replace("A", "")));
                                            datacur.setCreateTime(new Date(new java.util.Date().getTime()));
                                            Boolean cuc = this.dataMySqlStorage.insertRDdata(datacur);
                                            if (cuc)
                                                count++;
                                        }
                                        //添加特殊字段
                                        firstBrkCount = valuess.length + fault.length;
                                    } catch (Exception fir) {
                                        DataField dataf = new DataField();
                                        dataf.setDataId(dataId);
                                        dataf.setId(get32UUID());
                                        dataf.setCate(stream.getValues().get(key).toString().substring(0, 50));
                                        dataf.setUnit("UNKNOWN");
                                        dataf.setFieldName("不能解析的脱口信息保存在Cate中");
                                        dataf.setFieldValue(0d);
                                        dataf.setCreateTime(new Date(new java.util.Date().getTime()));
                                        Boolean cud = this.dataMySqlStorage.insertRDdata(dataf);
                                        if (cud)
                                            count++;
                                    }
                                    /**************************************************************/
                                }

                            }
                        }
                        if (xy == 1) {
                            DataField dataf = new DataField();
                            dataf.setDataId(dataId);
                            dataf.setId(get32UUID());
                            dataf.setCate(DataMap.getCate(key));
                            dataf.setUnit(DataMap.getUnit(key));
                            dataf.setFieldName(DataMap.getZHName(key));
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
            }
            this.dataMySqlStorage.insertRD(dataId, status, asn, dsn, agentId, date);
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
        Map<String, Integer> data = new HashMap();
        data.put("rd_real_time_data", this.dataMySqlStorage.getTimeCount());
        data.put("rd_data_field", this.dataMySqlStorage.getCount());
        return new ResultEntity<>(data);
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
