package com.shra012.library.validation;

import com.shra012.library.exception.LibraryValidationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Log4j2
@RequiredArgsConstructor
public class LibraryEventValidator {
    private static final String FIELD_VALIDATION_MESSAGE = "The value '%s' was rejected for the field '%s' with reason '%s'";
    private final Validator validator;

    public void validate(Object value) {
        Set<ConstraintViolation<Object>> violations = validator.validate(value);
        if (!violations.isEmpty()) {
            List<String> violationMessages = new ArrayList<>(violations.size());
            for (ConstraintViolation<Object> violation : violations) {
                violationMessages.add(String.format(FIELD_VALIDATION_MESSAGE, violation.getInvalidValue(), violation.getPropertyPath(), violation.getMessage()));
            }
            List<String> messages = violationMessages.stream().sorted(Comparator.comparing(String::length)).collect(Collectors.toList());
            throw new LibraryValidationException(messages);
        }
    }
}
