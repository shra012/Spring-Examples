package com.shra012.library.model;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.shra012.library.validation.LibraryEventIdNotNull;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.UUID;


@Getter
@Builder(setterPrefix = "with", toBuilder = true)
@JsonDeserialize(builder = LibraryEvent.LibraryEventBuilder.class)
@ToString
@LibraryEventIdNotNull
public class LibraryEvent {
    private final UUID eventId;
    private final LibraryEventType libraryEventType;
    @NotNull(message = "book object is mandatory")
    @Valid
    private final Book book;
}
