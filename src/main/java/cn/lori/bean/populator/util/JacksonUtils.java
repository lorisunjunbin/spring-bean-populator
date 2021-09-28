package cn.lori.bean.populator.util;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.StringWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

public class JacksonUtils {

    private final static Logger logger = LogManager.getLogger(JacksonUtils.class);

    public static final ObjectMapper mapper = new ObjectMapper();

    static {
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        mapper.setSerializationInclusion(Include.NON_NULL);

        DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        mapper.setDateFormat(df);

    }

    public static <K, V> Map<K, V> asMap(Object... params) {
        if (params == null || params.length % 2 != 0)
            throw new IllegalArgumentException(" input params.length must be even number.");

        Map<K, V> result = new LinkedHashMap<>();
        for (int i = 0; i < params.length; i += 2) {
            result.put((K) (params[i]), (V) (params[i + 1]));
        }
        return result;
    }

    public static String objectToJson(Object object) {
        StringWriter writer = new StringWriter();
        try {
            mapper.writeValue(writer, object);
        } catch (JsonGenerationException | JsonMappingException e) {
            logger.error(e.getLocalizedMessage());
        } catch (IOException e) {
            logger.error(e.getLocalizedMessage());
        }

        return writer.toString();
    }

    public static <V> Map<String, V> objectToMap(Object object, Class<V> valueClass) {
        return jsonToMap(objectToJson(object), String.class, valueClass);
    }

    @SuppressWarnings("unchecked")
    public static <T> T jsonToObject(String json, Class<?> objectClass) {
        try {
            return (T) mapper.readValue(json, objectClass);
        } catch (JsonParseException | JsonMappingException e) {
            logger.error(e.getLocalizedMessage());
        } catch (IOException e) {
            logger.error(e.getLocalizedMessage());
        }

        return null;
    }

    @SuppressWarnings("unchecked")
    public static <T> List<T> jsonToList(String json, Class<?> elementClass) {
        JavaType javaType = getCollectionType(ArrayList.class, elementClass);
        List<T> result = Collections.<T>emptyList();
        try {
            result = mapper.readValue(json, javaType);
        } catch (JsonParseException | JsonMappingException e) {
            logger.error(e.getLocalizedMessage());
        } catch (IOException e) {
            logger.error(e.getLocalizedMessage());
        }

        return result;
    }

    @SuppressWarnings("unchecked")
    public static <K, V> Map<K, V> jsonToMap(String json, Class<K> kClass, Class<V> vClass) {
        JavaType javaType = getCollectionType(LinkedHashMap.class, kClass, vClass);
        Map<K, V> result = new LinkedHashMap<>();
        try {
            result = mapper.readValue(json, javaType);
        } catch (JsonParseException | JsonMappingException e) {
            logger.error(e.getLocalizedMessage());
        } catch (IOException e) {
            logger.error(e.getLocalizedMessage());
        }
        return result;
    }

    public static <T> T convertOnCopy(Object object, Class<T> objectClass) {
        return jsonToObject(objectToJson(object), objectClass);
    }

    public static JavaType getCollectionType(Class<?> collectionClass, Class<?>... elementClasses) {
        return mapper.getTypeFactory().constructParametricType(collectionClass, elementClasses);
    }

}