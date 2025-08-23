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

    public QuizService() {
        logger.info("Initializing QuizService");
    }

    @PostConstruct
    private void initMockData() {
        logger.info("Starting initialization of mock quiz data");

        try {
            // Create sample quizzes
            Quiz javaQuiz = createJavaQuiz();
            Quiz generalKnowledgeQuiz = createGeneralKnowledgeQuiz();

            quizzes.put(javaQuiz.getId(), javaQuiz);
            quizzes.put(generalKnowledgeQuiz.getId(), generalKnowledgeQuiz);

            logger.info("Successfully initialized quiz service with {} mock quizzes",
                    quizzes.size());
            logger.debug("Mock quizzes created: '{}', '{}'", javaQuiz.getTitle(),
                    generalKnowledgeQuiz.getTitle());

        } catch (Exception e) {
            logger.error("Failed to initialize mock quiz data", e);
            throw e;
        }
    }

    public List<Quiz> getAllQuizzes() {
        logger.debug("Retrieving all quizzes, total count: {}", quizzes.size());

        try {
            List<Quiz> allQuizzes = new ArrayList<>(quizzes.values());
            logger.info("Successfully retrieved {} quizzes", allQuizzes.size());
            return allQuizzes;

        } catch (Exception e) {
            logger.error("Error retrieving all quizzes", e);
            throw e;
        }
    }

    public Optional<Quiz> getQuizById(Long id) {
        logger.debug("Looking up quiz with ID: {}", id);

        if (id == null) {
            logger.warn("Attempted to retrieve quiz with null ID");
            return Optional.empty();
        }

        try {
            Optional<Quiz> quiz = Optional.ofNullable(quizzes.get(id));

            if (quiz.isPresent()) {
                logger.info("Successfully found quiz with ID: {} - '{}'", id,
                        quiz.get().getTitle());
                logger.trace("Quiz details - ID: {}, Questions: {}", id,
                        quiz.get().getQuestions().size());
            } else {
                logger.warn("Quiz not found with ID: {}", id);
            }

            return quiz;

        } catch (Exception e) {
            logger.error("Error retrieving quiz with ID: {}", id, e);
            return Optional.empty();
        }
    }

    public Quiz createQuiz(Quiz quiz) {
        logger.info("Creating new quiz: '{}'", quiz != null ? quiz.getTitle() : "null");

        if (quiz == null) {
            logger.error("Attempted to create quiz with null object");
            throw new IllegalArgumentException("Quiz cannot be null");
        }

        if (quiz.getTitle() == null || quiz.getTitle().trim().isEmpty()) {
            logger.error("Attempted to create quiz with empty title");
            throw new IllegalArgumentException("Quiz title cannot be empty");
        }

        try {
            Long newId = idGenerator.getAndIncrement();
            quiz.setId(newId);
            quiz.setCreatedAt(LocalDateTime.now());

            if (quiz.getQuestions() == null) {
                quiz.setQuestions(new ArrayList<>());
            }

            quizzes.put(quiz.getId(), quiz);

            logger.info("Successfully created quiz with ID: {} - '{}'", newId, quiz.getTitle());
            logger.debug("Quiz created with {} questions at {}", quiz.getQuestions().size(),
                    quiz.getCreatedAt());
            logger.trace("Total quizzes in system: {}", quizzes.size());

            return quiz;

        } catch (Exception e) {
            logger.error("Failed to create quiz: '{}'", quiz.getTitle(), e);
            throw e;
        }
    }

    public Optional<Quiz> updateQuiz(Long id, Quiz updatedQuiz) {
        logger.info("Updating quiz with ID: {} - '{}'", id,
                updatedQuiz != null ? updatedQuiz.getTitle() : "null");

        if (id == null) {
            logger.error("Attempted to update quiz with null ID");
            return Optional.empty();
        }

        if (updatedQuiz == null) {
            logger.error("Attempted to update quiz ID: {} with null quiz object", id);
            return Optional.empty();
        }

        try {
            Quiz existingQuiz = quizzes.get(id);

            if (existingQuiz != null) {
                String oldTitle = existingQuiz.getTitle();
                LocalDateTime originalCreatedAt = existingQuiz.getCreatedAt();

                updatedQuiz.setId(id);
                updatedQuiz.setCreatedAt(originalCreatedAt);

                if (updatedQuiz.getQuestions() == null) {
                    updatedQuiz.setQuestions(new ArrayList<>());
                }

                quizzes.put(id, updatedQuiz);

                logger.info("Successfully updated quiz ID: {} - '{}' -> '{}'", id, oldTitle,
                        updatedQuiz.getTitle());
                logger.debug("Updated quiz now has {} questions",
                        updatedQuiz.getQuestions().size());

                return Optional.of(updatedQuiz);

            } else {
                logger.warn("Cannot update - quiz not found with ID: {}", id);
                return Optional.empty();
            }

        } catch (Exception e) {
            logger.error("Failed to update quiz with ID: {}", id, e);
            return Optional.empty();
        }
    }

    public boolean deleteQuiz(Long id) {
        logger.info("Deleting quiz with ID: {}", id);

        if (id == null) {
            logger.error("Attempted to delete quiz with null ID");
            return false;
        }

        try {
            Quiz removed = quizzes.remove(id);

            if (removed != null) {
                logger.info("Successfully deleted quiz ID: {} - '{}'", id, removed.getTitle());
                logger.debug("Quiz had {} questions", removed.getQuestions().size());
                logger.trace("Remaining quizzes in system: {}", quizzes.size());
                return true;

            } else {
                logger.warn("Cannot delete - quiz not found with ID: {}", id);
                return false;
            }

        } catch (Exception e) {
            logger.error("Failed to delete quiz with ID: {}", id, e);
            return false;
        }
    }

    public List<Quiz> searchQuizzesByTitle(String title) {
        logger.info("Searching quizzes by title: '{}'", title);

        if (title == null || title.trim().isEmpty()) {
            logger.warn("Search attempted with empty title, returning all quizzes");
            return getAllQuizzes();
        }

        try {
            String searchTerm = title.toLowerCase().trim();
            List<Quiz> results = quizzes.values().stream()
                    .filter(quiz -> quiz.getTitle().toLowerCase().contains(searchTerm))
                    .collect(Collectors.toList());

            logger.info("Found {} quizzes matching title search: '{}'", results.size(), title);

            if (logger.isDebugEnabled()) {
                results.forEach(quiz -> logger.debug("Match found: ID {} - '{}'", quiz.getId(),
                        quiz.getTitle()));
            }

            return results;

        } catch (Exception e) {
            logger.error("Error searching quizzes by title: '{}'", title, e);
            return new ArrayList<>();
        }
    }

    // Helper methods to create mock data
    private Quiz createJavaQuiz() {
        logger.debug("Creating Java Programming quiz");

        try {
            Quiz quiz = new Quiz(1L, "Java Programming Basics",
                    "Test your knowledge of Java fundamentals");

            List<Question> questions = Arrays.asList(
                    new Question(1L, "What is the main method signature in Java?",
                            Arrays.asList("public static void main(String[] args)",
                                    "public void main(String[] args)",
                                    "static void main(String[] args)",
                                    "public main(String[] args)"),
                            "public static void main(String[] args)"),

                    new Question(2L, "Which keyword is used to inherit a class in Java?",
                            Arrays.asList("implements", "extends", "inherits", "super"), "extends"),

                    new Question(3L, "What is the default value of a boolean variable in Java?",
                            Arrays.asList("true", "false", "null", "0"), "false"));

            quiz.setQuestions(questions);

            logger.debug("Created Java quiz with {} questions", questions.size());
            return quiz;

        } catch (Exception e) {
            logger.error("Failed to create Java Programming quiz", e);
            throw e;
        }
    }

    private Quiz createGeneralKnowledgeQuiz() {
        logger.debug("Creating General Knowledge quiz");

        try {
            Quiz quiz = new Quiz(2L, "General Knowledge", "Test your general knowledge");

            List<Question> questions = Arrays.asList(
                    new Question(4L, "What is the capital of France?",
                            Arrays.asList("London", "Berlin", "Paris", "Madrid"), "Paris"),

                    new Question(5L, "Which planet is known as the Red Planet?",
                            Arrays.asList("Venus", "Mars", "Jupiter", "Saturn"), "Mars"),

                    new Question(6L, "Who wrote 'Romeo and Juliet'?",
                            Arrays.asList("Charles Dickens", "William Shakespeare", "Jane Austen",
                                    "Mark Twain"), "William Shakespeare"));

            quiz.setQuestions(questions);

            logger.debug("Created General Knowledge quiz with {} questions", questions.size());
            return quiz;

        } catch (Exception e) {
            logger.error("Failed to create General Knowledge quiz", e);
            throw e;
        }
    }
}
