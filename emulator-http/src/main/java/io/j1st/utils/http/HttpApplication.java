package io.j1st.utils.http;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.SerializationFeature;
import io.dropwizard.Application;

import io.dropwizard.lifecycle.Managed;
import io.dropwizard.setup.Environment;
import io.j1st.storage.DataMongoStorage;
import io.j1st.storage.MongoStorage;
import io.j1st.utils.http.resource.*;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.lang3.ArrayUtils;
import org.eclipse.jetty.servlets.CrossOriginFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.DispatcherType;
import javax.servlet.FilterRegistration;
import java.util.EnumSet;

/**
 * service class
 */
public class HttpApplication extends Application<HttpConfiguration> {
    private static final Logger logger = LoggerFactory.getLogger(HttpApplication.class);

    private static PropertiesConfiguration mongoConfig;

    public static void main(String[] args) throws Exception {
        if (args.length >= 1) {
            mongoConfig = new PropertiesConfiguration(args[0]);
            args = ArrayUtils.subarray(args, 1, args.length);
        } else {
            mongoConfig = new PropertiesConfiguration("config/mongo.properties");
        }
        new HttpApplication().run(args);
    }

    @Override
    public void run(HttpConfiguration configuration, Environment environment) throws Exception {
        // storage & message queue
        final MongoStorage mongo = new MongoStorage();
        final DataMongoStorage dataMongo = new DataMongoStorage();

        environment.lifecycle().manage(new Managed() {
            @Override
            public void start() throws Exception {
                logger.debug("Initializing mongo storage ...");
                mongo.init(mongoConfig);
                dataMongo.init(mongoConfig);
            }

            @Override
            public void stop() throws Exception {
                logger.debug("Destroying mongo storage ...");
                mongo.destroy();
            }
        });

        // CORS
        FilterRegistration.Dynamic cors = environment.servlets().addFilter("CORS", CrossOriginFilter.class);
        cors.setInitParameter("allowedOrigins", "*");
        cors.setInitParameter("allowedHeaders", "Origin, Content-Type, Accept, Authorization,If-Match, If-Modified-Since, If-None-Match, If-Unmodified-Since, X-GitHub-OTP, X-Requested-With");
        cors.setInitParameter("allowedMethods", "GET,POST,OPTIONS,DELETE,PUT,HEAD,PATCH");
        cors.setInitParameter("allowCredentials", "true");
        cors.setInitParameter("exposedHeaders", "ETag, Link, X-GitHub-OTP, X-RateLimit-Limit, X-RateLimit-Remaining, X-RateLimit-Reset, X-OAuth-Scopes, X-Accepted-OAuth-Scopes, X-Poll-Interval");
        cors.addMappingForUrlPatterns(EnumSet.allOf(DispatcherType.class), true, "/*");


        // register resources
        environment.jersey().register(new DeleteUtil(mongo,dataMongo));
        environment.jersey().register(new SelectUtile(mongo,dataMongo));
        environment.jersey().register(new UpdateUtil(mongo,dataMongo));
        environment.jersey().register(new JudgeUtil(mongo,dataMongo));
        environment.jersey().register(new InsertUtil(mongo,dataMongo));
        environment.jersey().register(new GenDataAddUtil(mongo,dataMongo));

        // config jackson
        environment.getObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        environment.getObjectMapper().configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        environment.getObjectMapper().configure(SerializationFeature.WRITE_NULL_MAP_VALUES, false);
        environment.getObjectMapper().setSerializationInclusion(JsonInclude.Include.NON_NULL);
        environment.getObjectMapper().configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
    }
}
