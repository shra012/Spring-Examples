package com.shra012.library.configuration;

import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaAdmin;

import java.util.HashMap;

@Configuration
public class LibraryKafkaAdminConfiguration {

    @Bean
    public KafkaAdmin kafkaAdmin(final KafkaProperties kafkaProperties) {
        var configs = new HashMap<String, Object>();
        configs.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaProperties.getProducer().getBootstrapServers());
        return new KafkaAdmin(configs);
    }

    @Bean
    public NewTopic libraryEvents(final KafkaProperties kafkaProperties) {
        return new NewTopic(kafkaProperties.getTemplate().getDefaultTopic(), 3, (short) 3);
    }
}
