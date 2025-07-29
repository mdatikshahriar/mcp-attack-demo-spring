package com.example.server.controller;

import com.example.server.model.Question;
import com.example.server.model.Quiz;
import com.example.server.service.QuizService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/quizzes")
public class QuizController {

    private static final Logger logger = LoggerFactory.getLogger(QuizController.class);
    private final QuizService quizService;

    public QuizController(QuizService quizService) {
        this.quizService = quizService;
    }

    @GetMapping
    public ResponseEntity<List<Quiz>> getAllQuizzes() {
        List<Quiz> quizzes = quizService.getAllQuizzes();
        logger.info("Retrieved {} quizzes", quizzes.size());
        return ResponseEntity.ok(quizzes);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Quiz> getQuizById(@PathVariable Long id) {
        Optional<Quiz> quiz = quizService.getQuizById(id);
        if (quiz.isPresent()) {
            logger.info("Retrieved quiz with ID: {}", id);
            return ResponseEntity.ok(quiz.get());
        } else {
            logger.warn("Quiz not found with ID: {}", id);
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping
    public ResponseEntity<Quiz> createQuiz(@RequestBody Quiz quiz) {
        try {
            Quiz createdQuiz = quizService.createQuiz(quiz);
            logger.info("Created quiz: {}", createdQuiz.getTitle());
            return ResponseEntity.status(HttpStatus.CREATED).body(createdQuiz);
        } catch (Exception e) {
            logger.error("Error creating quiz", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<Quiz> updateQuiz(@PathVariable Long id, @RequestBody Quiz quiz) {
        Optional<Quiz> updatedQuiz = quizService.updateQuiz(id, quiz);
        if (updatedQuiz.isPresent()) {
            logger.info("Updated quiz with ID: {}", id);
            return ResponseEntity.ok(updatedQuiz.get());
        } else {
            logger.warn("Quiz not found for update with ID: {}", id);
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteQuiz(@PathVariable Long id) {
        boolean deleted = quizService.deleteQuiz(id);
        if (deleted) {
            logger.info("Deleted quiz with ID: {}", id);
            return ResponseEntity.noContent().build();
        } else {
            logger.warn("Quiz not found for deletion with ID: {}", id);
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/search")
    public ResponseEntity<List<Quiz>> searchQuizzes(@RequestParam String title) {
        List<Quiz> quizzes = quizService.searchQuizzesByTitle(title);
        logger.info("Found {} quizzes matching title: {}", quizzes.size(), title);
        return ResponseEntity.ok(quizzes);
    }

    @GetMapping("/{id}/questions")
    public ResponseEntity<List<Question>> getQuizQuestions(@PathVariable Long id) {
        Optional<Quiz> quiz = quizService.getQuizById(id);
        if (quiz.isPresent()) {
            logger.info("Retrieved questions for quiz ID: {}", id);
            return ResponseEntity.ok(quiz.get().getQuestions());
        } else {
            logger.warn("Quiz not found with ID: {}", id);
            return ResponseEntity.notFound().build();
        }
    }
}
