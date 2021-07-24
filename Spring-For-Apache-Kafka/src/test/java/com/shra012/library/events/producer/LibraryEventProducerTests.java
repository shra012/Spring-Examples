package com.shra012.library.events.producer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.shra012.library.model.Book;
import com.shra012.library.model.LibraryEvent;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.TopicPartition;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.messaging.Message;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.SettableListenableFuture;

import java.util.UUID;
import java.util.concurrent.ExecutionException;

@ExtendWith(MockitoExtension.class)
class LibraryEventProducerTests {

    private static final String LIBRARY_EVENTS_TOPIC = "library.events";
    private static final LibraryEvent LIBRARY_EVENT = LibraryEvent.builder()
            .withEventId(UUID.randomUUID())
            .withBook(Book.builder().withId("123").withAuthor("Dilip")
                    .withName("SomeBook").build())
            .build();

    @Mock
    KafkaTemplate<String, String> kafkaTemplate;

    @Spy
    ObjectMapper objectMapper = new ObjectMapper();

    @InjectMocks
    LibraryEventProducer libraryEventProducer;

    @Test
    void sendLibraryEventFailure() {
        //given
        SettableListenableFuture<SendResult<String, String>> future = new SettableListenableFuture<>();
        future.setException(new RuntimeException("Mock Exception"));
        Mockito.when(kafkaTemplate.send(ArgumentMatchers.any(Message.class))).thenReturn(future);
        //when and then
        Assertions.assertThrows(ExecutionException.class, () -> libraryEventProducer.sendLibraryEvent(LIBRARY_EVENT).get());
    }

    @Test
    void sendLibraryEventSuccess() throws JsonProcessingException, ExecutionException, InterruptedException {
        //given
        final String response = objectMapper.writeValueAsString(LIBRARY_EVENT);
        ProducerRecord<String, String> producerRecord = new ProducerRecord<>(LIBRARY_EVENTS_TOPIC, LIBRARY_EVENT.getEventId().toString(), objectMapper.writeValueAsString(LIBRARY_EVENT));
        RecordMetadata recordMetadata = new RecordMetadata(new TopicPartition(LIBRARY_EVENTS_TOPIC, 1), 1, 1, 342, System.currentTimeMillis(), 1, 2);
        SendResult<String, String> sendResult = new SendResult<>(producerRecord, recordMetadata);
        SettableListenableFuture<SendResult<String, String>> future = new SettableListenableFuture<>();
        future.set(sendResult);
        Mockito.when(kafkaTemplate.send(ArgumentMatchers.any(Message.class))).thenReturn(future);
        //when
        ListenableFuture<SendResult<String, String>> sendResultListenableFuture = libraryEventProducer.sendLibraryEvent(LIBRARY_EVENT);
        //then
        SendResult<String, String> sendResultResponse = sendResultListenableFuture.get();
        Assertions.assertEquals(1, sendResultResponse.getRecordMetadata().partition());
        Assertions.assertEquals(LIBRARY_EVENT.getEventId().toString(), sendResultResponse.getProducerRecord().key());
        Assertions.assertEquals(response, sendResultResponse.getProducerRecord().value());
    }
}
