package com.shra012.library.exception;

import lombok.Getter;

import java.util.UUID;

@Getter
public class LibraryEventFailedException extends RuntimeException {
    private final UUID eventId;

    public LibraryEventFailedException(UUID eventId) {
        super();
        this.eventId = eventId;
    }

    public LibraryEventFailedException(UUID eventId, String message) {
        super(message);
        this.eventId = eventId;
    }

    public LibraryEventFailedException(UUID eventId, String message, Throwable cause) {
        super(message, cause);
        this.eventId = eventId;
    }

    public LibraryEventFailedException(UUID eventId, Throwable cause) {
        super(cause);
        this.eventId = eventId;
    }

    protected LibraryEventFailedException(UUID eventId, String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
        this.eventId = eventId;
    }
}
