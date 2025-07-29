package com.example.server.model;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Setter
@Getter
public class Quiz {
    private Long id;
    private String title;
    private String description;
    private List<Question> questions;
    private LocalDateTime createdAt;

    // Constructors
    public Quiz() {
        this.createdAt = LocalDateTime.now();
        this.questions = new ArrayList<>();
    }

    public Quiz(Long id, String title, String description) {
        this();
        this.id = id;
        this.title = title;
        this.description = description;
    }
}
