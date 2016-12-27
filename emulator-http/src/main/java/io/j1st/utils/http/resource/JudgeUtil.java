package io.j1st.utils.http.resource;

import io.j1st.storage.DataMongoStorage;
import io.j1st.storage.MongoStorage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

/**
 * Created by 旭冬 on 2016/11/1.
 */
@Path("/judge/infos")
@Produces(MediaType.APPLICATION_JSON)
public class JudgeUtil extends  AbstractResource{
    // Logger
    private static final Logger logger = LoggerFactory.getLogger(DeleteUtil.class);

    public JudgeUtil(MongoStorage mongo, DataMongoStorage dataMongoStorage) {
        super(mongo,dataMongoStorage);

    }
}
