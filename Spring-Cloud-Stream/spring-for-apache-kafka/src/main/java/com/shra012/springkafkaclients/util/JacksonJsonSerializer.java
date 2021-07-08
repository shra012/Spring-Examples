package com.shra012.springkafkaclients.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.shra012.springkafkaclients.exception.SerializationException;
import lombok.RequiredArgsConstructor;


import java.util.Objects;

@RequiredArgsConstructor
public final class JacksonJsonSerializer implements Serializer {
    private final ObjectMapper objectMapper;
    private static final JacksonJsonSerializer instance = new JacksonJsonSerializer(new ObjectMapper());

    public static JacksonJsonSerializer getInstance() {
        return instance;
    }

    @Override
    public <T> String serialize(T object) {
        if (Objects.isNull(object)) return null;
        try {
            return objectMapper.writeValueAsString(object);
        } catch (JsonProcessingException jsonProcessingException) {
            throw new SerializationException(String.format("Unable to serialize %s to JSON with Jackson ObjectMapper", object.getClass().getSimpleName()));
        }
    }

    @Override
    public <T> T deserialize(String string, TypeReference<T> typeReference) {
        try {
            return objectMapper.readValue(string, typeReference);
        } catch (JsonProcessingException jsonProcessingException) {
            throw new SerializationException(String.format("Unable to de-serialize JSON %s to %s with Jackson ObjectMapper", string, typeReference.getType().getTypeName()));
        }
    }

    @Override
    @SuppressWarnings("java:S1182")
    protected Object clone() throws CloneNotSupportedException {
        throw new CloneNotSupportedException("This is a singleton class, so clone should not be used");
    }
}
