package com.shra012.library.validation;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Documented
@Constraint(validatedBy = {LibraryEventIdNotNullValidator.class})
@Target({TYPE, ANNOTATION_TYPE})
@Retention(RUNTIME)
public @interface LibraryEventIdNotNull {
    String message() default "Event Id is mandatory in request for update/delete operations";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}