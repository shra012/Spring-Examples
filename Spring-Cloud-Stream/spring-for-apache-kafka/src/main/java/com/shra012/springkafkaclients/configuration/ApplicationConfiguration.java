package com.shra012.springkafkaclients.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shra012.springkafkaclients.generator.InvoiceGenerator;
import com.shra012.springkafkaclients.generator.InvoiceGeneratorImpl;
import com.shra012.springkafkaclients.producer.JsonValueKafkaProducerService;
import com.shra012.springkafkaclients.producer.StringValueKafkaProducerService;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.SeekToCurrentErrorHandler;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;
import org.springframework.util.backoff.FixedBackOff;

import java.util.Map;

@Configuration
public class ApplicationConfiguration {

    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }

    @Bean
    public InvoiceGenerator invoiceGenerator(final ObjectMapper objectMapper) {
        return new InvoiceGeneratorImpl(objectMapper);
    }

    @Bean
    public KafkaTemplate<String, String> stringValueKafkaTemplate(final KafkaClientsConfiguration kafkaClientsConfiguration) {
        DefaultKafkaProducerFactory<String, String> producerFactory =
                new DefaultKafkaProducerFactory<>(Map.of(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaClientsConfiguration.getBootstrapServers(),
                        ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class,
                        ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class));
        return new KafkaTemplate<>(producerFactory);
    }

    @Bean
    public KafkaTemplate<String, Object> jsonValueKafkaTemplate(final KafkaClientsConfiguration kafkaClientsConfiguration) {
        DefaultKafkaProducerFactory<String, Object> producerFactory = new DefaultKafkaProducerFactory<>(
                Map.of(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaClientsConfiguration.getBootstrapServers(),
                        ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class,
                        ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class));
        return new KafkaTemplate<>(producerFactory);
    }

    @Bean
    public StringValueKafkaProducerService stringValueKafkaProducer(final KafkaTemplate<String, String> stringValueKafkaTemplate,
                                                                    final KafkaClientsConfiguration kafkaClientsConfiguration) {
        return new StringValueKafkaProducerService(stringValueKafkaTemplate, kafkaClientsConfiguration);
    }

    @Bean
    public JsonValueKafkaProducerService jsonValueKafkaProducer(final KafkaTemplate<String, Object> jsonValueKafkaTemplate,
                                                                final KafkaClientsConfiguration kafkaClientsConfiguration) {
        return new JsonValueKafkaProducerService(jsonValueKafkaTemplate, kafkaClientsConfiguration);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, Object> kafkaListenerContainerFactory(final KafkaClientsConfiguration kafkaClientsConfiguration) {
        DefaultKafkaConsumerFactory<String, Object> consumerFactory = new DefaultKafkaConsumerFactory<>(
                Map.of(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaClientsConfiguration.getBootstrapServers(),
                        ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class,
                        ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class,
                        ConsumerConfig.GROUP_ID_CONFIG, kafkaClientsConfiguration.getGroupId(),
                JsonDeserializer.TRUSTED_PACKAGES, "com.shra012.springkafkaclients.model"));

        ConcurrentKafkaListenerContainerFactory<String, Object> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory);
        factory.setErrorHandler(new SeekToCurrentErrorHandler(new FixedBackOff(0L, 2L)));
        return factory;
    }
}
