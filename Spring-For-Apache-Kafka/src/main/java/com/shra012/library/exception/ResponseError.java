package com.shra012.library.exception;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;
import lombok.Singular;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import java.util.List;

@Getter
@Builder(setterPrefix = "with")
@JsonInclude(JsonInclude.Include.NON_DEFAULT)
public class ResponseError {
    @NonNull
    private final int code;
    @Nullable
    private final String exception;
    @NonNull
    @Singular
    private final List<String> messages;
}
