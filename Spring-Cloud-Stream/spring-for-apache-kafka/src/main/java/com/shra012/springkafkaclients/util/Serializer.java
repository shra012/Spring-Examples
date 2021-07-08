package com.shra012.springkafkaclients.util;

import com.fasterxml.jackson.core.type.TypeReference;

public interface Serializer {
    <T> String serialize(T object);

    <T> T deserialize(String string, TypeReference<T> typeReference);
}
