package com.shra012.library.resource;

import com.shra012.library.exception.LibraryEventFailedException;
import com.shra012.library.exception.LibraryRuntimeException;
import com.shra012.library.model.LibraryEvent;
import com.shra012.library.model.LibraryEventResponse;
import com.shra012.library.model.LibraryEventType;
import com.shra012.library.events.producer.LibraryEventProducer;
import com.shra012.library.validation.LibraryEventValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
public class LibraryResource {

    @Autowired
    LibraryEventValidator validator;

    @Autowired
    private LibraryEventProducer libraryEventProducer;

    @PostMapping("/library/record")
    public ResponseEntity<LibraryEventResponse> createLibraryEvent(@RequestBody final LibraryEvent libraryEvent) {
        var event = libraryEvent.toBuilder()
                .withLibraryEventType(LibraryEventType.NEW)
                .withEventId(UUID.randomUUID()).build();
        return sendLibraryEvent(event, HttpStatus.CREATED, LibraryEventResponse.created(event.getEventId()));
    }

    @PutMapping("/library/record")
    public ResponseEntity<LibraryEventResponse> updateLibraryEvent(@RequestBody final LibraryEvent libraryEvent) {
        var event = libraryEvent.toBuilder()
                .withLibraryEventType(LibraryEventType.UPDATE).build();
        return sendLibraryEvent(event, HttpStatus.OK, LibraryEventResponse.updated(event.getEventId()));
    }

    @DeleteMapping("/library/record")
    public ResponseEntity<LibraryEventResponse> deleteLibraryEvent(@RequestBody final LibraryEvent libraryEvent) {
        var event = libraryEvent.toBuilder()
                .withLibraryEventType(LibraryEventType.DELETE).build();
        return sendLibraryEvent(event, HttpStatus.OK, LibraryEventResponse.deleted(event.getEventId()));
    }

    private ResponseEntity<LibraryEventResponse> sendLibraryEvent(final LibraryEvent event,
                                                                  final HttpStatus status,
                                                                  final LibraryEventResponse response) {
        try {
            validator.validate(event);
            libraryEventProducer.sendLibraryEvent(event);
        } catch (LibraryRuntimeException ex) {
            throw new LibraryEventFailedException(event.getEventId(), ex.getMessage());
        }
        return ResponseEntity.status(status).body(response);
    }
}
