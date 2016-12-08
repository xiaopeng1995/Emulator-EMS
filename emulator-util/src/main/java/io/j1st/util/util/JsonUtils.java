package io.j1st.util.util;


import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

/**
 * JsonUtils
 */
public class JsonUtils {
    // Global JSON ObjectMapper
    public static final ObjectMapper Mapper = new ObjectMapper();

    static {
        Mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        Mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        Mapper.configure(SerializationFeature.WRITE_NULL_MAP_VALUES, false);
        Mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        Mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
    }

    private JsonUtils() {
    }
}
