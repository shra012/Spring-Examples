package com.shra012.library.model;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
@Builder(setterPrefix = "with")
@JsonDeserialize(builder = LibraryEventResponse.LibraryEventResponseBuilder.class)
public class LibraryEventResponse {
    public static final String CREATE_EVENT_MESSAGE = "Library Event Is Created";
    public static final String UPDATE_EVENT_MESSAGE = "Library Event Is Updated";
    public static final String DELETE_EVENT_MESSAGE = "Library Event Is Deleted";
    private final UUID eventUUID;
    private final EventStatus status;
    private final String statusMessage;

    public static LibraryEventResponse created(UUID eventUUID) {
        return LibraryEventResponse.builder()
                .withStatus(EventStatus.CREATED)
                .withEventUUID(eventUUID)
                .withStatusMessage(CREATE_EVENT_MESSAGE)
                .build();
    }

    public static LibraryEventResponse failed(String reason, UUID eventUUID) {
        return LibraryEventResponse.builder()
                .withStatus(EventStatus.FAILED)
                .withEventUUID(eventUUID)
                .withStatusMessage(reason)
                .build();
    }

    public static LibraryEventResponse updated(UUID eventUUID) {
        return LibraryEventResponse.builder()
                .withStatus(EventStatus.UPDATED)
                .withEventUUID(eventUUID)
                .withStatusMessage(UPDATE_EVENT_MESSAGE)
                .build();
    }

    public static LibraryEventResponse deleted(UUID eventUUID) {
        return LibraryEventResponse.builder()
                .withStatus(EventStatus.DELETED)
                .withEventUUID(eventUUID)
                .withStatusMessage(DELETE_EVENT_MESSAGE)
                .build();
    }
}
