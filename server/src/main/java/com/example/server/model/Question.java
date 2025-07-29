package com.example.server.model;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Setter
@Getter
public class Question {
    private Long id;
    private String questionText;
    private List<String> options;
    private String correctAnswer;

    // Constructors
    public Question() {
        this.options = new ArrayList<>();
    }

    public Question(Long id, String questionText, List<String> options, String correctAnswer) {
        this.id = id;
        this.questionText = questionText;
        this.options = options != null ? options : new ArrayList<>();
        this.correctAnswer = correctAnswer;
    }

}
