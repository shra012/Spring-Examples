package com.shra012.library.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.shra012.library.exception.LibraryRuntimeException;
import lombok.extern.log4j.Log4j2;

import static java.lang.String.format;

@Log4j2
public final class LibraryUtil {

    private static final String JSON_PARSE_EXCEPTION_MESSAGE = "Unable To Convert The Given Object (%s) To Json String";

    private LibraryUtil() throws IllegalAccessException {
        throw new IllegalAccessException("This is a utility class please, cannot create a object. Please use static access.");
    }

    public static String writeObjectAsJsonString(ObjectMapper objectMapper, Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException jsonProcessingException) {
            String message = format(JSON_PARSE_EXCEPTION_MESSAGE, value.getClass().getCanonicalName());
            log.error(message);
            throw new LibraryRuntimeException(message);
        }
    }
}
