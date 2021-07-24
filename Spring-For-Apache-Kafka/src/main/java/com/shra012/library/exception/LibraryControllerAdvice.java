package com.shra012.library.exception;

import com.shra012.library.model.LibraryEventResponse;
import org.springframework.http.HttpStatus;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import static java.lang.String.format;

@RestControllerAdvice
public class LibraryControllerAdvice {

    private static final String FIELD_VALIDATION_MESSAGE = "The value '%s' was rejected for the field '%s' with reason '%s'";

    @ExceptionHandler(value = Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ResponseError handleException(Exception exception) {
        return ResponseError.builder()
                .withCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .withMessage(exception.getMessage()).build();
    }

    @ExceptionHandler(value = LibraryEventFailedException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public LibraryEventResponse handleLibraryEventFailedException(LibraryEventFailedException exception) {
        return LibraryEventResponse.failed(exception.getMessage(), exception.getEventId());
    }

    @ExceptionHandler(value = LibraryValidationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseError handleLibraryEventFailedException(LibraryValidationException exception) {
        return ResponseError.builder()
                .withCode(HttpStatus.BAD_REQUEST.value())
                .withMessages(exception.getMessages()).build();
    }

    @ExceptionHandler(value = MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseError handleSpringValidationException(MethodArgumentNotValidException exception) {
        List<FieldError> fieldErrors = exception.getBindingResult().getFieldErrors();
        List<String> messages = fieldErrors.stream()
                .map(fieldError -> format(FIELD_VALIDATION_MESSAGE, fieldError.getRejectedValue(), fieldError.getField(), fieldError.getDefaultMessage()))
                .sorted(Comparator.comparing(String::length))
                .collect(Collectors.toList());
        return ResponseError.builder()
                .withCode(HttpStatus.BAD_REQUEST.value())
                .withMessages(messages)
                .withException(exception.getClass().getSimpleName())
                .build();
    }
}
