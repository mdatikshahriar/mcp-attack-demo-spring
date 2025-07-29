package com.example.server.service;

import com.example.server.model.Question;
import com.example.server.model.Quiz;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Service
public class QuizService {

    private static final Logger logger = LoggerFactory.getLogger(QuizService.class);
    private final Map<Long, Quiz> quizzes = new ConcurrentHashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(1);

    @PostConstruct
    private void initMockData() {
        // Create sample quizzes
        Quiz javaQuiz = createJavaQuiz();
        Quiz generalKnowledgeQuiz = createGeneralKnowledgeQuiz();

        quizzes.put(javaQuiz.getId(), javaQuiz);
        quizzes.put(generalKnowledgeQuiz.getId(), generalKnowledgeQuiz);

        logger.info("Initialized quiz service with {} mock quizzes", quizzes.size());
    }

    public List<Quiz> getAllQuizzes() {
        return new ArrayList<>(quizzes.values());
    }

    public Optional<Quiz> getQuizById(Long id) {
        return Optional.ofNullable(quizzes.get(id));
    }

    public Quiz createQuiz(Quiz quiz) {
        quiz.setId(idGenerator.getAndIncrement());
        quiz.setCreatedAt(LocalDateTime.now());
        quizzes.put(quiz.getId(), quiz);
        logger.info("Created new quiz with ID: {}", quiz.getId());
        return quiz;
    }

    public Optional<Quiz> updateQuiz(Long id, Quiz updatedQuiz) {
        Quiz existingQuiz = quizzes.get(id);
        if (existingQuiz != null) {
            updatedQuiz.setId(id);
            updatedQuiz.setCreatedAt(existingQuiz.getCreatedAt());
            quizzes.put(id, updatedQuiz);
            logger.info("Updated quiz with ID: {}", id);
            return Optional.of(updatedQuiz);
        }
        return Optional.empty();
    }

    public boolean deleteQuiz(Long id) {
        Quiz removed = quizzes.remove(id);
        if (removed != null) {
            logger.info("Deleted quiz with ID: {}", id);
            return true;
        }
        return false;
    }

    public List<Quiz> searchQuizzesByTitle(String title) {
        return quizzes.values().stream()
                .filter(quiz -> quiz.getTitle().toLowerCase().contains(title.toLowerCase()))
                .collect(Collectors.toList());
    }

    // Helper methods to create mock data
    private Quiz createJavaQuiz() {
        Quiz quiz =
                new Quiz(1L, "Java Programming Basics", "Test your knowledge of Java fundamentals");

        List<Question> questions = Arrays.asList(
                new Question(1L, "What is the main method signature in Java?",
                        Arrays.asList("public static void main(String[] args)",
                                "public void main(String[] args)",
                                "static void main(String[] args)", "public main(String[] args)"),
                        "public static void main(String[] args)"),

                new Question(2L, "Which keyword is used to inherit a class in Java?",
                        Arrays.asList("implements", "extends", "inherits", "super"), "extends"),

                new Question(3L, "What is the default value of a boolean variable in Java?",
                        Arrays.asList("true", "false", "null", "0"), "false"));

        quiz.setQuestions(questions);
        return quiz;
    }

    private Quiz createGeneralKnowledgeQuiz() {
        Quiz quiz = new Quiz(2L, "General Knowledge", "Test your general knowledge");

        List<Question> questions = Arrays.asList(new Question(4L, "What is the capital of France?",
                        Arrays.asList("London", "Berlin", "Paris", "Madrid"), "Paris"),

                new Question(5L, "Which planet is known as the Red Planet?",
                        Arrays.asList("Venus", "Mars", "Jupiter", "Saturn"), "Mars"),

                new Question(6L, "Who wrote 'Romeo and Juliet'?",
                        Arrays.asList("Charles Dickens", "William Shakespeare", "Jane Austen",
                                "Mark Twain"), "William Shakespeare"));

        quiz.setQuestions(questions);
        return quiz;
    }
}

