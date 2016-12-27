package io.j1st.utils.http.entity;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;

/**
 * error util
 */
public class ErrorMessageUtils {

    public static String getMessage(String lang,String key) {
        if(lang != null && lang.length() > 2){
            lang = lang.substring(0,2);
        }
        PropertiesConfiguration msgConfig = null;
        try {
            String path = "config/message_"+lang+".properties";
            msgConfig = new PropertiesConfiguration(path);
            return msgConfig.getString(key);
        } catch (ConfigurationException e) {
            e.printStackTrace();
        }
        return null;
    }
}
