package com.shra012.library.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shra012.library.events.producer.LibraryEventProducer;
import com.shra012.library.validation.LibraryEventValidator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaTemplate;

import javax.validation.Validator;

@Configuration
public class LibraryConfiguration {

    @Bean
    public LibraryEventProducer libraryEventProducer(final KafkaTemplate<String, String> kafkaTemplate, final ObjectMapper objectMapper) {
        return new LibraryEventProducer(kafkaTemplate, objectMapper);
    }

    @Bean
    public LibraryEventValidator libraryEventValidator(final Validator validator) {
        return new LibraryEventValidator(validator);
    }

}
