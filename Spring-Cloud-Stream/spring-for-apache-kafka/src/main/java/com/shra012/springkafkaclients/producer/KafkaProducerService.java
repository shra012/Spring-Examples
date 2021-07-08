package com.shra012.springkafkaclients.producer;

import com.shra012.springkafkaclients.configuration.KafkaClientsConfiguration;
import com.shra012.springkafkaclients.model.SimpleResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;

import java.util.Objects;

@Log4j2
@RequiredArgsConstructor
public class KafkaProducerService {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final KafkaClientsConfiguration kafkaClientsConfiguration;


    public SimpleResponse sendMessage(String key, String value) {
        String topic = kafkaClientsConfiguration.getTopicWithKey(key);
        kafkaTemplate.send(topic, key, value);
        return SimpleResponse.builder().withMessage("Success Message Has Been Loaded").build();
    }

    public SimpleResponse sendMessage(Message<?> incomingMessage) {
        String topic = kafkaClientsConfiguration.getTopicWithKey((String) Objects.requireNonNull(incomingMessage.getHeaders().get(KafkaHeaders.MESSAGE_KEY)));
        Message<String> message = MessageBuilder
                .withPayload(incomingMessage.getPayload().toString())
                .copyHeaders(incomingMessage.getHeaders())
                .setHeader(KafkaHeaders.TOPIC, topic).build();
        kafkaTemplate.send(message);
        return SimpleResponse.builder().withMessage("Success Message Has Been Loaded").build();
    }

}
