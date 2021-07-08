package com.shra012.springkafkaclients.configuration;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Getter
@RequiredArgsConstructor
@Configuration
@ConfigurationProperties(prefix = "kafkaclients")
public class KafkaClientsConfiguration {
    private final List<String> topics;

    public String getTopicWithKey(String key){
        return topics.stream()
                .filter(key::startsWith).findFirst()
                .orElseThrow(() -> new IllegalArgumentException(String.format("no matching topic could be found for the key %s", key)));
    }
}
