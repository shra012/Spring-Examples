package com.shra012.library.exception;

public class LibraryRuntimeException extends RuntimeException {
    public LibraryRuntimeException() {
        super();
    }

    public LibraryRuntimeException(String message) {
        super(message);
    }

    public LibraryRuntimeException(String message, Throwable cause) {
        super(message, cause);
    }

    public LibraryRuntimeException(Throwable cause) {
        super(cause);
    }

    protected LibraryRuntimeException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
