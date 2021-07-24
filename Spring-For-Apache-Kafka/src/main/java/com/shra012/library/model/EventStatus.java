package com.shra012.library.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

@Getter
@RequiredArgsConstructor
public enum EventStatus {
    CREATED("created"),UPDATED("updated"),DELETED("deleted"),FAILED("failed");
    private final String value;

    public static EventStatus enumOf(String status){
        return Arrays.stream(values())
                .filter(eventStatus -> eventStatus.getValue().equals(status))
                .findFirst()
                .orElse(null);
    }
}
