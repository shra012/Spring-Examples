package com.shra012.library.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.List;

@Getter
@RequiredArgsConstructor
public class LibraryValidationException extends RuntimeException {
    private final List<String> messages;
}
