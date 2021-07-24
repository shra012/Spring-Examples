package com.shra012.library.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shra012.library.events.consumer.LibraryEventConsumer;
import com.shra012.library.exception.LibraryRuntimeException;
import com.shra012.library.repository.BookRepository;
import com.shra012.library.service.LibraryEventPersistenceService;
import lombok.extern.log4j.Log4j2;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.kafka.ConcurrentKafkaListenerContainerFactoryConfigurer;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.dao.RecoverableDataAccessException;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.retry.backoff.FixedBackOffPolicy;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.retry.support.RetryTemplate;

import java.util.HashMap;
import java.util.Objects;

@Configuration
@EnableKafka
@Log4j2
public class LibraryEventConsumerConfiguration {

    private static final String PROCESSING_EXCEPTION_MESSAGE = "Exception while processing the book with cause {}, for book {}";

    @Bean
    ConcurrentKafkaListenerContainerFactory<?, ?> kafkaListenerContainerFactory(
            ConcurrentKafkaListenerContainerFactoryConfigurer configurer,
            ObjectProvider<ConsumerFactory<Object, Object>> kafkaConsumerFactory,
            KafkaProperties kafkaProperties,
            LibraryEventPersistenceService libraryEventPersistenceService) {
        ConcurrentKafkaListenerContainerFactory<Object, Object> factory = new ConcurrentKafkaListenerContainerFactory<>();
        configurer.configure(factory, kafkaConsumerFactory
                .getIfAvailable(() -> new DefaultKafkaConsumerFactory<>(kafkaProperties.buildConsumerProperties())));
        factory.setConcurrency(3);
        factory.setErrorHandler(this::handleException);
        factory.setRetryTemplate(handleRetry());
        factory.setRecoveryCallback(retryContext -> {
            if (retryContext.getLastThrowable().getCause() instanceof RecoverableDataAccessException) {
                log.info("Recoverable exception");
                ConsumerRecord<String, String> consumerRecord = (ConsumerRecord<String, String>) retryContext.getAttribute("record");
                libraryEventPersistenceService.handleRecovery(consumerRecord);
            } else {
                log.info("Non recoverable exception");
                throw new LibraryRuntimeException(retryContext.getLastThrowable().getMessage());
            }
            return null;
        });
        //factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL);
        return factory;
    }

    private RetryTemplate handleRetry() {
        var exceptions = new HashMap<Class<? extends Throwable>, Boolean>();
        exceptions.put(IllegalArgumentException.class, false);
        exceptions.put(RecoverableDataAccessException.class, true);
        SimpleRetryPolicy simpleRetryPolicy = new SimpleRetryPolicy(3, exceptions, true);
        FixedBackOffPolicy fixedBackOffPolicy = new FixedBackOffPolicy();
        fixedBackOffPolicy.setBackOffPeriod(1000);
        RetryTemplate retryTemplate = new RetryTemplate();
        retryTemplate.setRetryPolicy(simpleRetryPolicy);
        retryTemplate.setBackOffPolicy(fixedBackOffPolicy);
        return retryTemplate;
    }

    private void handleException(Exception ex, ConsumerRecord<?, ?> data) {
        Objects.requireNonNull(data);
        if (Objects.nonNull(ex.getCause())) {
            log.info(PROCESSING_EXCEPTION_MESSAGE, ex.getCause().getMessage(), data.value(), ex);
        } else {
            log.info(PROCESSING_EXCEPTION_MESSAGE, ex.getMessage(), data.value(), ex);
        }
    }

    @Bean
    LibraryEventPersistenceService libraryEventPersistenceService(final ObjectMapper objectMapper,
                                                                  final BookRepository bookRepository,
                                                                  final KafkaTemplate<String, String> kafkaTemplate) {
        return new LibraryEventPersistenceService(objectMapper, bookRepository, kafkaTemplate);
    }

    @Bean
    LibraryEventConsumer libraryEventConsumer(final LibraryEventPersistenceService libraryEventPersistenceService) {
        return new LibraryEventConsumer(libraryEventPersistenceService);
    }
}
