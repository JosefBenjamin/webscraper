package app.utils;


import app.exceptions.ApiException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.javalin.http.Context;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class Utils {

    public static String getPropertyValue(String propName, String resourceName)  {
        try (InputStream is = Utils.class.getClassLoader().getResourceAsStream(resourceName)) {
            Properties prop = new Properties();
            prop.load(is);

            String value = prop.getProperty(propName);
            if (value != null) {
                return value.trim();  // Trim whitespace
            } else {
                throw new ApiException(500, String.format("Property %s not found in %s", propName, resourceName));
            }
        } catch (IOException ex) {
            throw new ApiException(500, String.format("Could not read property %s.", propName));
        }
    }

    public ObjectMapper getObjectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false); // Ignore unknown properties in JSON
        objectMapper.registerModule(new JavaTimeModule()); // Serialize and deserialize java.time objects
        objectMapper.writer(new DefaultPrettyPrinter());
        return objectMapper;
    }

    public static String convertToJsonMessage(Context ctx, String property, String message) {
        Map<String, String> msgMap = new HashMap<>();
        msgMap.put(property, message);  // Put the message in the map
        msgMap.put("status", String.valueOf(ctx.status()));  // Put the status in the map
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            return objectMapper.writeValueAsString(msgMap);  // Convert the map to JSON
        } catch (Exception e) {
            return "{\"error\": \"Could not convert  message to JSON\"}";
        }
    }

    //TODO:  JSON Helpers
    /**
     * Converts a Java Map to a JSON string.
     * What: Serializes a Map<String, Object> into a JSON string format.
     * Why: So you can store structured data (like selectors) in the database as plain text.
     * How: Uses Jackson’s ObjectMapper to turn key-value pairs into JSON.
     */
    public static String writeJson(Map<String, Object> map) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            return objectMapper.writeValueAsString(map);
        } catch (Exception e) {
            throw new RuntimeException("Failed to convert map to JSON", e);
        }
    }


    /**
     * Converts a JSON string back into a Java Map.
     * What: Deserializes a JSON text from the DB into a usable Map<String, Object>.
     * Why: So you can work with selectors as normal Java objects in your API.
     * How: Uses Jackson’s ObjectMapper to parse the JSON into a Map.
     */
    public static Map<String, Object> readMap(String json) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            if (json == null || json.isBlank()) {
                return Map.of();
            }
            return objectMapper.readValue(json, new TypeReference<Map<String, Object>>() {});
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse JSON string", e);
        }
    }

}
