package com.shra012.library.events.common;

import com.shra012.library.model.LibraryEventType;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;

import java.util.Map;

public class LibraryHeaders {

    private LibraryHeaders() throws IllegalAccessException {
        throw new IllegalAccessException("Helper class to be accessed with statically");
    }

    public static final String LIBRARY_EVENT_TYPE = "library-event-type";

    public static Message<?> messageWithStandardHeaders(Message<?> message, String key, LibraryEventType libraryEventType) {
        return messageWithStandardHeaders(message.getPayload(), key, libraryEventType, message.getHeaders());
    }

    public static <T> Message<T> messageWithStandardHeaders(T payload, String key, LibraryEventType libraryEventType) {
        return messageWithStandardHeaders(payload, key, libraryEventType, null);
    }

    public static <T> Message<T> messageWithStandardHeaders(T payload, String key, LibraryEventType libraryEventType, Map<String, ?> headers) {
        return MessageBuilder
                .withPayload(payload)
                .copyHeadersIfAbsent(headers)
                .setHeader(KafkaHeaders.MESSAGE_KEY, key)
                .setHeader(LIBRARY_EVENT_TYPE, libraryEventType.toString())
                .build();
    }
}
