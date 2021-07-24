package com.shra012.library.entity;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
@Data
public class Book {
    @Id
    private String id;
    private String name;
    private String author;
}
