package com.shra012.springkafkaclients.configuration;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;

import java.util.List;

@Getter
@RequiredArgsConstructor
@ConstructorBinding
@ConfigurationProperties(prefix = "kafkaclients")
public class KafkaClientsConfiguration {
    /**
     * Comma Separated Brokers host or ip:port.
     */
    private final String bootstrapServers;
    /**
     * List of topics to produce records which looks up the key name to find the matching topic for this list.
     */
    private final List<String> topics;

    private final String groupId;
    /**
     * Invoice Topic.
     */
    private final String invoiceTopic;

    /**
     * Finds the matching topic which starts with the key name
     *
     * @param key - key name
     * @return - topic name.
     */
    public String getTopicWithKey(String key) {
        return topics.stream()
                .filter(key::startsWith).findFirst()
                .orElseThrow(() -> new IllegalArgumentException(String.format("no matching topic could be found for the key %s", key)));
    }
}
