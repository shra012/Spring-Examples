package com.shra012.library.events.consumer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.shra012.library.events.common.LibraryHeaders;
import com.shra012.library.model.Book;
import com.shra012.library.model.LibraryEventType;
import com.shra012.library.repository.BookRepository;
import com.shra012.library.service.LibraryEventPersistenceService;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.dao.RecoverableDataAccessException;
import org.springframework.kafka.config.KafkaListenerEndpointRegistry;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.MessageListenerContainer;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.ContainerTestUtils;
import org.springframework.messaging.Message;
import org.springframework.test.context.TestPropertySource;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

@SpringBootTest
@EmbeddedKafka(topics = "library.events", partitions = 3)
@TestPropertySource(properties = {
        "spring.kafka.producer.bootstrap-servers=${spring.embedded.kafka.brokers}",
        "spring.kafka.consumer.bootstrap-servers=${spring.embedded.kafka.brokers}",
        "spring.datasource.url=jdbc:h2:mem:testdb"
})
class LibraryEventConsumerIT {

    @Autowired
    EmbeddedKafkaBroker embeddedKafkaBroker;

    @Autowired
    KafkaTemplate<String, String> kafkaTemplate;

    @Autowired
    KafkaListenerEndpointRegistry endpointRegistry;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    BookRepository bookRepository;

    @SpyBean
    LibraryEventConsumer libraryEventConsumerSpy;

    @SpyBean
    LibraryEventPersistenceService serviceSpy;

    @BeforeEach
    void setUp() {
        for (MessageListenerContainer messageListenerContainer : endpointRegistry.getListenerContainers()) {
            ContainerTestUtils.waitForAssignment(messageListenerContainer, embeddedKafkaBroker.getPartitionsPerTopic());
        }
    }

    @Test
    void shouldConsumeLibraryEvent() throws ExecutionException, InterruptedException, JsonProcessingException {
        //given
        Book book = Book.builder().withId("1").withName("test book").withAuthor("test author").build();
        String payload = objectMapper.writeValueAsString(book);
        Message<String> message = LibraryHeaders.messageWithStandardHeaders(payload, "1", LibraryEventType.NEW);
        kafkaTemplate.send(message).get();
        //when
        CountDownLatch latch = new CountDownLatch(1);
        latch.await(3, TimeUnit.SECONDS);
        //then
        Mockito.verify(libraryEventConsumerSpy, Mockito.times(1)).onMessage(ArgumentMatchers.isA(ConsumerRecord.class));
        Mockito.verify(serviceSpy, Mockito.times(1)).processLibraryEvent(ArgumentMatchers.isA(ConsumerRecord.class));
        com.shra012.library.entity.Book bookFromDB = bookRepository.findById("1").get();
        Assertions.assertEquals(book.getId(), bookFromDB.getId());
        Assertions.assertEquals(book.getName(), bookFromDB.getName());
        Assertions.assertEquals(book.getAuthor(), bookFromDB.getAuthor());
    }

    @Test
    void shouldConsumeUpdateLibraryEvent() throws ExecutionException, InterruptedException, JsonProcessingException {
        //given
        Book book = Book.builder().withId("1").withName("test book").withAuthor("test author").build();
        String payload = objectMapper.writeValueAsString(book);
        Message<String> message = LibraryHeaders.messageWithStandardHeaders(payload, "1", LibraryEventType.NEW);
        kafkaTemplate.send(message).get();
        Book updatedBook = book.toBuilder().withName("test book updated").build();
        String updatedPayload = objectMapper.writeValueAsString(updatedBook);
        Message<String> updatedMessage = LibraryHeaders.messageWithStandardHeaders(updatedPayload, "1", LibraryEventType.UPDATE);
        kafkaTemplate.send(updatedMessage).get();
        //when
        CountDownLatch latch = new CountDownLatch(1);
        latch.await(3, TimeUnit.SECONDS);
        //then
        Mockito.verify(libraryEventConsumerSpy, Mockito.times(2)).onMessage(ArgumentMatchers.isA(ConsumerRecord.class));
        Mockito.verify(serviceSpy, Mockito.times(2)).processLibraryEvent(ArgumentMatchers.isA(ConsumerRecord.class));
        com.shra012.library.entity.Book bookFromDB = bookRepository.findById("1").get();
        Assertions.assertEquals(updatedBook.getId(), bookFromDB.getId());
        Assertions.assertEquals(updatedBook.getName(), bookFromDB.getName());
        Assertions.assertEquals(updatedBook.getAuthor(), bookFromDB.getAuthor());
    }

    @Test
    void shouldThrowExceptionDuringUpdateLibraryEventWhenBookDoesNotExist() throws ExecutionException, InterruptedException, JsonProcessingException {
        //given
        Book book = Book.builder().withId("1").withName("test book updated").withAuthor("test author").build();
        String payload = objectMapper.writeValueAsString(book);
        Message<String> message = LibraryHeaders.messageWithStandardHeaders(payload, "1", LibraryEventType.UPDATE);
        kafkaTemplate.send(message).get();
        //when
        CountDownLatch latch = new CountDownLatch(1);
        latch.await(5, TimeUnit.SECONDS);
        //then
        Mockito.doThrow(IllegalArgumentException.class).when(libraryEventConsumerSpy).onMessage(ArgumentMatchers.isA(ConsumerRecord.class));
        Mockito.doThrow(IllegalArgumentException.class).when(serviceSpy).processLibraryEvent(ArgumentMatchers.isA(ConsumerRecord.class));
        com.shra012.library.entity.Book bookFromDB = bookRepository.findById("1").orElse(null);
        Assertions.assertNull(bookFromDB);
    }

    @Test
    void shouldRetryRecoverableException() throws ExecutionException, InterruptedException, JsonProcessingException {
        //given
        Book book = Book.builder().withId("$").withName("recoverable").withAuthor("recoverable author").build();
        String payload = objectMapper.writeValueAsString(book);
        Message<String> message = LibraryHeaders.messageWithStandardHeaders(payload, "$", LibraryEventType.NEW);
        kafkaTemplate.send(message).get();
        //when
        CountDownLatch latch = new CountDownLatch(1);
        latch.await(5, TimeUnit.SECONDS);
        //then
        Mockito.doThrow(RecoverableDataAccessException.class).when(libraryEventConsumerSpy).onMessage(ArgumentMatchers.isA(ConsumerRecord.class));
        Mockito.verify(libraryEventConsumerSpy, Mockito.times(4)).onMessage(ArgumentMatchers.isA(ConsumerRecord.class));
        Mockito.verify(serviceSpy, Mockito.times(4)).processLibraryEvent(ArgumentMatchers.isA(ConsumerRecord.class));
        Mockito.verify(serviceSpy, Mockito.times(1)).handleRecovery(ArgumentMatchers.isA(ConsumerRecord.class));
    }

    @Test
    void shouldConsumeDeleteLibraryEvent() throws ExecutionException, InterruptedException, JsonProcessingException {
        //given
        Book book = Book.builder().withId("1").withName("test book").withAuthor("test author").build();
        String payload = objectMapper.writeValueAsString(book);
        Message<String> message = LibraryHeaders.messageWithStandardHeaders(payload, "1", LibraryEventType.NEW);
        kafkaTemplate.send(message).get();
        Message<String> deleteMessage = LibraryHeaders.messageWithStandardHeaders(payload, "1", LibraryEventType.DELETE);
        kafkaTemplate.send(deleteMessage).get();
        //when
        CountDownLatch latch = new CountDownLatch(1);
        latch.await(3, TimeUnit.SECONDS);
        //then
        Mockito.verify(libraryEventConsumerSpy, Mockito.times(2)).onMessage(ArgumentMatchers.isA(ConsumerRecord.class));
        Mockito.verify(serviceSpy, Mockito.times(2)).processLibraryEvent(ArgumentMatchers.isA(ConsumerRecord.class));
        com.shra012.library.entity.Book bookFromDB = bookRepository.findById("1").orElse(null);
        Assertions.assertNull(bookFromDB);

    }

    @AfterEach
    void tearDown() {
        bookRepository.deleteAll();
    }
}
