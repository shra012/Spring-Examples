package com.shra012.springkafkaclients.util;

public class KafkaClientsUtil {

    private KafkaClientsUtil() throws IllegalAccessException {
        throw new IllegalAccessException("This is a utility class, all methods are static and should be accessed with class");
    }

    public static <T> String toJSONString(T object) {
        return JacksonJsonSerializer.getInstance().serialize(object);
    }
}
