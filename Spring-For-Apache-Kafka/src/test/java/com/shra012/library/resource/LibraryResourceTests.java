package com.shra012.library.resource;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shra012.library.events.producer.LibraryEventProducer;
import com.shra012.library.model.*;
import com.shra012.library.validation.LibraryEventValidator;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.util.concurrent.SettableListenableFuture;

import javax.validation.Validation;
import javax.validation.Validator;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isA;


@WebMvcTest(LibraryResource.class)
@AutoConfigureMockMvc
@ExtendWith(SpringExtension.class)
class LibraryResourceTests {

    private static final String LIBRARY_RECORD_URI = "/library/record";
    private static final String NO_BOOK_FIELD_VALUES_BAD_REQUEST_RESPONSE = "{\"code\":400,\"messages\":[\"The value 'null' was rejected for the field 'book.id' with reason 'id field of a book is mandatory'\",\"The value 'null' was rejected for the field 'book.name' with reason 'name field of a book is mandatory'\",\"The value 'null' was rejected for the field 'book.author' with reason 'author field of a book is mandatory'\"]}";
    private static final String NULL_EVENT_ID_UPDATE_BAD_REQUEST_RESPONSE = "{\"code\":400,\"messages\":[\"The value 'LibraryEvent(eventId=null, libraryEventType=UPDATE, book=Book(id=123, name=SomeBook, author=Dilip))' was rejected for the field 'eventId' with reason 'Event Id is mandatory in request for update/delete operations'\"]}";
    private LibraryEvent REQUEST = LibraryEvent.builder()
            .withBook(Book.builder().withId("123").withAuthor("Dilip")
                    .withName("SomeBook").build())
            .build();
    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @MockBean
    LibraryEventProducer libraryEventProducer;

    @MockBean
    LibraryEventValidator libraryEventValidator;

    @MockBean
    Validator validator;


    @Test
    void postLibraryEvent() throws Exception {
        //given
        var request = REQUEST.toBuilder().withLibraryEventType(LibraryEventType.NEW).build();
        var requestJson = objectMapper.writeValueAsString(request);
        Mockito.when(libraryEventProducer.sendLibraryEvent(request)).thenReturn(new SettableListenableFuture<>());

        Mockito.doNothing().when(libraryEventValidator).validate(any());
        //when
        var mvcResult = mockMvc.perform(MockMvcRequestBuilders.post(LIBRARY_RECORD_URI)
                .content(requestJson).contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isCreated())
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();
        var response = objectMapper.readValue(mvcResult.getResponse().getContentAsString(), LibraryEventResponse.class);
        //then
        Assertions.assertNotNull(response.getEventUUID());
        Assertions.assertEquals(EventStatus.CREATED, response.getStatus());
        Assertions.assertEquals(LibraryEventResponse.CREATE_EVENT_MESSAGE, response.getStatusMessage());
    }

    @Test
    void postLibraryEvent4xxErrorWhenBookHasNoValues() throws Exception {
        //given
        var request = REQUEST.toBuilder()
                .withLibraryEventType(LibraryEventType.NEW)
                .withBook(Book.builder().build()).build();
        var requestJson = objectMapper.writeValueAsString(request);
        Mockito.when(libraryEventProducer.sendLibraryEvent(request)).thenReturn(new SettableListenableFuture<>());
        Mockito.doCallRealMethod().when(libraryEventValidator).validate(any());
        var result = Validation.buildDefaultValidatorFactory().getValidator().validate(request);
        Mockito.when(validator.validate(isA(LibraryEvent.class))).thenReturn(result);
        ReflectionTestUtils.setField(libraryEventValidator, "validator", validator);
        //when and then
        mockMvc.perform(MockMvcRequestBuilders.post(LIBRARY_RECORD_URI)
                .content(requestJson).contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.content().string(NO_BOOK_FIELD_VALUES_BAD_REQUEST_RESPONSE));
    }

    @Test
    void putLibraryEvent() throws Exception {
        //given
        var request = REQUEST.toBuilder()
                .withEventId(UUID.randomUUID())
                .withLibraryEventType(LibraryEventType.UPDATE)
                .build();
        var requestJson = objectMapper.writeValueAsString(request);
        Mockito.when(libraryEventProducer.sendLibraryEvent(request)).thenReturn(new SettableListenableFuture<>());
        Mockito.doNothing().when(libraryEventValidator).validate(any());
        //when
        var mvcResult = mockMvc.perform(MockMvcRequestBuilders.put(LIBRARY_RECORD_URI)
                .content(requestJson).contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();
        var response = objectMapper.readValue(mvcResult.getResponse().getContentAsString(), LibraryEventResponse.class);
        //then
        Assertions.assertNotNull(response.getEventUUID());
        Assertions.assertEquals(EventStatus.UPDATED, response.getStatus());
        Assertions.assertEquals(LibraryEventResponse.UPDATE_EVENT_MESSAGE, response.getStatusMessage());
    }

    @Test
    void putLibraryEvent4xxErrorWhenEventIdIsNull() throws Exception {
        //given
        var request = REQUEST.toBuilder()
                .withLibraryEventType(LibraryEventType.UPDATE)
                .build();
        var requestJson = objectMapper.writeValueAsString(request);
        Mockito.when(libraryEventProducer.sendLibraryEvent(request)).thenReturn(new SettableListenableFuture<>());
        Mockito.doCallRealMethod().when(libraryEventValidator).validate(any());
        var result = Validation.buildDefaultValidatorFactory().getValidator().validate(request);
        Mockito.when(validator.validate(isA(LibraryEvent.class))).thenReturn(result);
        ReflectionTestUtils.setField(libraryEventValidator, "validator", validator);
        //when and then
        mockMvc.perform(MockMvcRequestBuilders.put(LIBRARY_RECORD_URI)
                .content(requestJson).contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.content().string(NULL_EVENT_ID_UPDATE_BAD_REQUEST_RESPONSE));


    }
}
