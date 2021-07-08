package com.shra012.springkafkaclients.exception;

import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Objects;

@Log4j2
@RestControllerAdvice
public class ResourceExceptionHandler {

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorResponse handleErrors(Exception exception) {
        log.error("Exception Occurred in running the application", exception);
        var errorResponseBuilder = ErrorResponse.builder().withMessage(exception.getMessage());
        if (Objects.isNull(exception.getCause())) {
            errorResponseBuilder.withCause(exception.getClass().getSimpleName()).build();
        } else {
            errorResponseBuilder.withCause(exception.getCause().getClass().getName()).build();
        }
        return errorResponseBuilder.build();
    }
}
