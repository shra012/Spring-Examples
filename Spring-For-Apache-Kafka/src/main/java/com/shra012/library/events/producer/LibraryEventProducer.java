package com.shra012.library.events.producer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shra012.library.events.common.LibraryHeaders;
import com.shra012.library.model.Book;
import com.shra012.library.model.LibraryEvent;
import com.shra012.library.util.LibraryUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaNull;
import org.springframework.kafka.support.SendResult;
import org.springframework.lang.NonNull;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;

import java.util.Objects;

@Log4j2
@RequiredArgsConstructor
public class LibraryEventProducer {
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public ListenableFuture<SendResult<String, String>> sendLibraryEvent(final LibraryEvent libraryEvent) {
        var key = libraryEvent.getEventId().toString();
        var value = libraryEvent.getBook();
        var payload = Objects.isNull(value) ?
                KafkaNull.INSTANCE :
                LibraryUtil.writeObjectAsJsonString(objectMapper, value);
        var libraryMessage = LibraryHeaders
                .messageWithStandardHeaders(payload, key, libraryEvent.getLibraryEventType());
        var future = kafkaTemplate.send(libraryMessage);
        future.addCallback(new ListenableFutureCallback<>() {
            @Override
            public void onFailure(@NonNull Throwable throwable) {
                handleFailure(key, value, throwable);
            }

            @Override
            public void onSuccess(SendResult<String, String> result) {
                handleSuccess(key, value, result);
            }
        });
        return future;
    }

    private void handleSuccess(String key, Book value, SendResult<String, String> result) {
        log.info("Message Sent Successfully for the key {} and the value {}, posted to partition {}", key, value, result.getRecordMetadata().partition());
    }

    private void handleFailure(String key, Book value, Throwable ex) {
        log.error("Error Sending Message for the key {} and the value {}, with exception message {}", key, value, ex.getMessage());
    }
}
