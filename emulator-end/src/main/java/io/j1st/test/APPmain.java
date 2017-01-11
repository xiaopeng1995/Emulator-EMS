package io.j1st.test;

import io.j1st.storage.MongoStorage;
import org.apache.commons.configuration.PropertiesConfiguration;

import javax.swing.*;

/**
 * Created by xiaopeng on 2016/12/26.
 */
public class APPmain {
    public static void main(String args[]) throws Exception{

        PropertiesConfiguration productIdConfig;
        PropertiesConfiguration mongoConfig;
        PropertiesConfiguration mqttConfig;
        PropertiesConfiguration quartzConfig;

        if (args.length >= 4) {
            productIdConfig = new PropertiesConfiguration(args[0]);
            mongoConfig = new PropertiesConfiguration(args[1]);
            mqttConfig = new PropertiesConfiguration(args[2]);
            quartzConfig = new PropertiesConfiguration(args[4]);

        } else {
            productIdConfig = new PropertiesConfiguration("config/product_agent.properties");
            mongoConfig = new PropertiesConfiguration("config/mongo.properties");
            mqttConfig = new PropertiesConfiguration("config/mqtt.properties");
            quartzConfig = new PropertiesConfiguration("config/quartz.properties");
        }
        //mongodb
        MongoStorage mogo = new MongoStorage();
        mogo.init(mongoConfig);

        Interface666 calculator1 = new Interface666(mogo);
        calculator1.setVisible(true);
        calculator1.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }
}
