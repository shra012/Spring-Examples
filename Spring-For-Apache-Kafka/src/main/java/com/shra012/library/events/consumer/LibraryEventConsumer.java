package com.shra012.library.events.consumer;

import com.shra012.library.service.LibraryEventPersistenceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;

@Log4j2
@RequiredArgsConstructor
public class LibraryEventConsumer /*implements AcknowledgingMessageListener<String, String>*/ {
    private final LibraryEventPersistenceService libraryEventPersistenceService;

//    @Override
//    @KafkaListener(topics = "library.events")
//    public void onMessage(@NonNull ConsumerRecord<String, String> consumerRecord, Acknowledgment acknowledgment) {
//        log.info("Consumer Record : {}", consumerRecord);
//        acknowledgment.acknowledge();
//    }

    @KafkaListener(topics = "library.events")
    public void onMessage(ConsumerRecord<String, String> consumerRecord) {
        log.info("Consumer Record : {}", consumerRecord);
        libraryEventPersistenceService.processLibraryEvent(consumerRecord);
    }
}
