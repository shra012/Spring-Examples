package com.shra012.springkafkaclients;

import com.shra012.springkafkaclients.configuration.KafkaClientsConfiguration;
import com.shra012.springkafkaclients.model.PosInvoice;
import lombok.extern.log4j.Log4j2;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;

@Log4j2
@SpringBootApplication
@EnableConfigurationProperties(KafkaClientsConfiguration.class)
public class SpringKafkaClientsApplication {


    public static void main(String[] args) {
        SpringApplication.run(SpringKafkaClientsApplication.class, args);
    }

    @KafkaListener(topics = "invoice", groupId = "spring.consumer.1")
    public void invoiceConsumerWithHeaders(@Payload PosInvoice message,
                                           @Header(name = KafkaHeaders.RECEIVED_MESSAGE_KEY, required = false) String key) {
        log.info("Received Message: " + message + "with key: " + key);
    }
}
