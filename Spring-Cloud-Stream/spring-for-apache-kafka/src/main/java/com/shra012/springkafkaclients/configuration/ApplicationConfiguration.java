package com.shra012.springkafkaclients.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shra012.springkafkaclients.generator.InvoiceGenerator;
import com.shra012.springkafkaclients.generator.InvoiceGeneratorImpl;
import com.shra012.springkafkaclients.producer.KafkaProducerService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaTemplate;

@Configuration
public class ApplicationConfiguration {

    @Bean
    public KafkaProducerService kafkaProducer(final KafkaTemplate<String, String> kafkaTemplate,
                                              final KafkaClientsConfiguration kafkaClientsConfiguration) {
        return new KafkaProducerService(kafkaTemplate, kafkaClientsConfiguration);
    }

    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }

    @Bean
    public InvoiceGenerator invoiceGenerator(final ObjectMapper objectMapper) {
        return new InvoiceGeneratorImpl(objectMapper);
    }
}
