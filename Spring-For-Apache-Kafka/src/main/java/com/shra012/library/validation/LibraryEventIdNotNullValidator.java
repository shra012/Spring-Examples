package com.shra012.library.validation;

import com.shra012.library.model.LibraryEvent;
import com.shra012.library.model.LibraryEventType;
import lombok.extern.log4j.Log4j2;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.Objects;

@Log4j2
public class LibraryEventIdNotNullValidator implements ConstraintValidator<LibraryEventIdNotNull, LibraryEvent> {
    @Override
    public void initialize(LibraryEventIdNotNull constraint) {
        // NO OP
    }

    public boolean isValid(LibraryEvent event, ConstraintValidatorContext context) {
        context.disableDefaultConstraintViolation();
        context.buildConstraintViolationWithTemplate(
                context.getDefaultConstraintMessageTemplate())
                .addPropertyNode("eventId")
                .inContainer(LibraryEvent.class, 1)
                .addConstraintViolation();
        return event.getLibraryEventType().equals(LibraryEventType.NEW) || !Objects.isNull(event.getEventId());
    }
}
