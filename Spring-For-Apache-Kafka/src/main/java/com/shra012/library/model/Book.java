package com.shra012.library.model;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Getter
@Builder(setterPrefix = "with", toBuilder = true)
@JsonDeserialize(builder = Book.BookBuilder.class)
@ToString
public class Book {
    @NotNull(message = "id field of a book is mandatory")
    private final String id;
    @NotBlank(message = "name field of a book is mandatory")
    private final String name;
    @NotBlank(message = "author field of a book is mandatory")
    private final String author;
}
