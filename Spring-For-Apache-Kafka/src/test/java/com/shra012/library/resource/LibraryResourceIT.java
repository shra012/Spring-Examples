package com.shra012.library.resource;

import com.shra012.library.model.Book;
import com.shra012.library.model.LibraryEvent;
import com.shra012.library.model.LibraryEventResponse;
import lombok.extern.log4j.Log4j2;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.test.context.TestPropertySource;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@EmbeddedKafka(topics = "library.events", partitions = 3)
@TestPropertySource(
        properties = {
                "spring.kafka.producer.bootstrap-servers=${spring.embedded.kafka.brokers}",
                "spring.kafka.admin.properties.bootstrap.servers=${spring.embedded.kafka.brokers}"
        }
)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Log4j2
class LibraryResourceIT {

    private static final String LIBRARY_RECORD_URI = "/library/record";
    private static final String LIBRARY_EVENTS_TOPIC = "library.events";
    private static final String EXPECTED_VALUE = "{\"id\":\"123\",\"name\":\"SomeBook\",\"author\":\"Dilip\"}";
    @Autowired
    TestRestTemplate restTemplate;

    @Autowired
    EmbeddedKafkaBroker embeddedKafkaBroker;

    private Consumer<String, String> consumer;

    @BeforeAll
    void setUp(){
        Map<String, Object> consumerProps = KafkaTestUtils.consumerProps("test-group-1", "true", embeddedKafkaBroker);
        consumerProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        Map<String, Object> configs = Collections.unmodifiableMap(consumerProps);
        consumer = new DefaultKafkaConsumerFactory<String,String>(configs).createConsumer();
        embeddedKafkaBroker.consumeFromAllEmbeddedTopics(consumer);
    }

    @Test
    @Timeout(2)
    void postLibraryEvent() {
        //given
        LibraryEvent request = LibraryEvent.builder()
                .withBook(Book.builder().withId("123").withAuthor("Dilip")
                        .withName("SomeBook").build())
                .build();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<LibraryEvent> requestEntity = new HttpEntity<>(request, headers);

        //when
        ResponseEntity<LibraryEventResponse> responseEntity = restTemplate.exchange(LIBRARY_RECORD_URI, HttpMethod.POST,
                requestEntity, LibraryEventResponse.class);

        //then
        Assertions.assertEquals(HttpStatus.CREATED, responseEntity.getStatusCode());
        Assertions.assertNotNull(Objects.requireNonNull(responseEntity.getBody()).getEventUUID());
        UUID responseEventId = responseEntity.getBody().getEventUUID();
        ConsumerRecord<String, String> singleRecord = KafkaTestUtils.getSingleRecord(consumer, LIBRARY_EVENTS_TOPIC);
        String key = singleRecord.key();
        Assertions.assertEquals(responseEventId.toString(), key);
        String value = singleRecord.value();
        Assertions.assertEquals(EXPECTED_VALUE, value);
    }

    @Test
    @Timeout(2)
    void putLibraryEvent() {
        //given
        LibraryEvent request = LibraryEvent.builder()
                .withEventId(UUID.randomUUID())
                .withBook(Book.builder().withId("123").withAuthor("Dilip")
                        .withName("SomeBook").build())
                .build();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<LibraryEvent> requestEntity = new HttpEntity<>(request, headers);

        //when
        ResponseEntity<LibraryEventResponse> responseEntity = restTemplate.exchange(LIBRARY_RECORD_URI, HttpMethod.PUT,
                requestEntity, LibraryEventResponse.class);

        //then
        Assertions.assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        Assertions.assertNotNull(Objects.requireNonNull(responseEntity.getBody()).getEventUUID());
        ConsumerRecord<String, String> singleRecord = KafkaTestUtils.getSingleRecord(consumer, LIBRARY_EVENTS_TOPIC);
        String key = singleRecord.key();
        Assertions.assertEquals(request.getEventId().toString(), key);
        String value = singleRecord.value();
        Assertions.assertEquals(EXPECTED_VALUE, value);
    }

    @AfterAll
    void tearDown(){
        consumer.close();
    }
}
