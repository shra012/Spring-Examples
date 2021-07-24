package com.shra012.library.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.shra012.library.entity.Book;
import com.shra012.library.events.common.LibraryHeaders;
import com.shra012.library.model.LibraryEventType;
import com.shra012.library.repository.BookRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.header.Header;
import org.jetbrains.annotations.Nullable;
import org.springframework.dao.RecoverableDataAccessException;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.messaging.Message;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.util.Iterator;
import java.util.Objects;
import java.util.Optional;

@Log4j2
@RequiredArgsConstructor
public class LibraryEventPersistenceService {

    private final ObjectMapper objectMapper;
    private final BookRepository bookRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;

    public void processLibraryEvent(ConsumerRecord<String, String> consumerRecord) {
        if ("$".equals(consumerRecord.key())) {
            throw new RecoverableDataAccessException("$ Key Is Not Permitted");
        }
        try {
            Book book = objectMapper.readValue(consumerRecord.value(), Book.class);
            LibraryEventType eventType = getLibraryEventType(consumerRecord);
            if (Objects.nonNull(eventType)) {
                saveOrDeleteBook(consumerRecord, book, eventType);
            } else {
                log.error("Event Type is Null, skipping the record {}", consumerRecord);
            }
        } catch (JsonProcessingException ex) {
            log.error("Value is cannot be cast to LibraryEvent class....", ex);
        }
    }

    public void handleRecovery(ConsumerRecord<String, String> consumerRecord) {
        log.info("Trying to recover consumer record {}", consumerRecord);
        LibraryEventType eventType = getLibraryEventType(consumerRecord);
        String epoch = String.valueOf(LocalTime.now().toEpochSecond(LocalDate.now(), ZoneOffset.UTC));
        String key = "$".equals(consumerRecord.key()) ? epoch : consumerRecord.key();
        Message<String> message = LibraryHeaders.messageWithStandardHeaders(consumerRecord.value(), key, eventType);
        kafkaTemplate.send(message);
    }

    private void saveOrDeleteBook(ConsumerRecord<String, String> consumerRecord, Book book, LibraryEventType eventType) {
        switch (eventType) {
            case NEW:
                bookRepository.save(book);
                log.info("{} book request with name \"{}\" has been processed", eventType.toString(), book.getName());
                break;
            case UPDATE:
                validate(book);
                bookRepository.save(book);
                log.info("{} book request with name \"{}\" has been processed", eventType.toString(), book.getName());
                break;
            case DELETE:
                validate(book);
                bookRepository.delete(book);
                log.info("Book with name \"{}\" has been DELETED", book.getName());
                break;
            default:
                log.info("Unknown Event type \"{}\" skipping message {}", eventType, consumerRecord.value());
                break;
        }
    }

    private void validate(Book book) {
        if (Objects.isNull(book.getId())) {
            throw new IllegalArgumentException("Book Id is null");
        }
        Optional<Book> bookOptional = bookRepository.findById(book.getId());
        if (bookOptional.isEmpty()) {
            throw new IllegalArgumentException("Not a valid book id skipping update update");
        }
        log.info("Validation is successful for the book : {}", bookOptional.get());
    }

    @Nullable
    private LibraryEventType getLibraryEventType(ConsumerRecord<String, String> consumerRecord) {
        Iterator<Header> headers = consumerRecord.headers().headers(LibraryHeaders.LIBRARY_EVENT_TYPE).iterator();
        LibraryEventType eventType = null;
        if (headers.hasNext()) {
            Header eventTypeHeader = headers.next();
            eventType = LibraryEventType.valueOf(new String(eventTypeHeader.value(), StandardCharsets.UTF_8));
        }
        return eventType;
    }

}
