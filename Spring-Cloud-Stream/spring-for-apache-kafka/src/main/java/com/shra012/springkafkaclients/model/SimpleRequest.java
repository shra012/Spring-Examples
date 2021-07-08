package com.shra012.springkafkaclients.model;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.shra012.springkafkaclients.util.KafkaClientsUtil;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder(setterPrefix = "with")
@JsonDeserialize(builder = SimpleRequest.SimpleRequestBuilder.class)
public class SimpleRequest {
    private final String key;
    private final String value;

    @Override
    public String toString() {
        return KafkaClientsUtil.toJSONString(this);
    }
}
