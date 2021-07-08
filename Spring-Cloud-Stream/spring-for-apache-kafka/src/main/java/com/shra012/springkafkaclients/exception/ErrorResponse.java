package com.shra012.springkafkaclients.exception;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder(setterPrefix = "with")
public class ErrorResponse {
    private final String message;
    private final String cause;
}
