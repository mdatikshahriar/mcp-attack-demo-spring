package com.example.client.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class McpChatService {

    private static final DateTimeFormatter TIMESTAMP_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");

    private static final Logger logger = LoggerFactory.getLogger(McpChatService.class);
    private static final int MAX_CONTEXT_MESSAGES = 10;
    private static final int MAX_PROMPT_LENGTH = 8000;

    @Autowired
    private ChatClient.Builder chatClientBuilder;

    @Autowired
    private ToolCallbackProvider toolCallbackProvider;

    public String processWithMcp(String userMessage, List<String> chatHistory) {
        String sessionId = MDC.get("sessionId");
        long processingStartTime = System.currentTimeMillis();

        try {
            logger.info(
                    "MCP processing started - Session: {}, Message length: {}, History size: {}, Timestamp: {}",
                    sessionId, userMessage.length(), chatHistory != null ? chatHistory.size() : 0,
                    LocalDateTime.now().format(TIMESTAMP_FORMAT));

            // Validate input parameters
            if (userMessage == null || userMessage.trim().isEmpty()) {
                logger.warn("Invalid user message - Session: {}, Message is null or empty",
                        sessionId);
                return "I'm sorry, but I didn't receive a valid message. Please try again with a specific question or request.";
            }

            // Build context from chat history with enhanced logging
            StringBuilder contextBuilder = new StringBuilder();
            int contextMessagesUsed = 0;

            if (chatHistory != null && !chatHistory.isEmpty()) {
                logger.debug(
                        "Building conversation context - Session: {}, Available history: {} messages",
                        sessionId, chatHistory.size());

                contextBuilder.append("Previous conversation context:\n");

                // Include last few messages for context (excluding the current one)
                int startIndex = Math.max(0, chatHistory.size() - MAX_CONTEXT_MESSAGES);
                int endIndex = chatHistory.size() - 1; // -1 to exclude current message

                for (int i = startIndex; i < endIndex; i++) {
                    if (chatHistory.get(i) != null) {
                        contextBuilder.append(chatHistory.get(i)).append("\n");
                        contextMessagesUsed++;
                    }
                }

                logger.debug(
                        "Context built - Session: {}, Messages included: {}, Context length: {} chars",
                        sessionId, contextMessagesUsed, contextBuilder.length());
            } else {
                logger.debug("No chat history available - Session: {}, Starting fresh conversation",
                        sessionId);
            }

            contextBuilder.append("\nCurrent user message: ").append(userMessage);
            contextBuilder.append(
                    "\n\nPlease respond considering the conversation context above and use appropriate tools when necessary.");

            String fullPrompt = contextBuilder.toString();

            // Validate prompt length
            if (fullPrompt.length() > MAX_PROMPT_LENGTH) {
                logger.warn(
                        "Prompt length exceeds maximum - Session: {}, Length: {}, Max: {}, Truncating context",
                        sessionId, fullPrompt.length(), MAX_PROMPT_LENGTH);

                // Truncate context while preserving current message
                int allowedContextLength =
                        MAX_PROMPT_LENGTH - userMessage.length() - 200; // 200 chars buffer
                String truncatedContext =
                        contextBuilder.toString().substring(0, Math.max(0, allowedContextLength));
                fullPrompt =
                        truncatedContext + "\n\nCurrent user message: " + userMessage + "\n\nPlease respond considering the conversation context above and use appropriate tools when necessary.";
            }

            logger.debug(
                    "Final prompt prepared - Session: {}, Total length: {}, Context messages: {}",
                    sessionId, fullPrompt.length(), contextMessagesUsed);

            // Process with ChatClient and tools
            logger.info("Invoking ChatClient with tools - Session: {}", sessionId);
            long chatClientStartTime = System.currentTimeMillis();

            // before model call
            logger.debug("=== PROMPT (sanitized) ===\n{}", sanitizeForLog(fullPrompt));

            String response = chatClientBuilder.build()
                    .prompt(fullPrompt)
                    .toolCallbacks(toolCallbackProvider)
                    .call()
                    .content();

            long chatClientProcessingTime = System.currentTimeMillis() - chatClientStartTime;
            logger.info(
                    "ChatClient processing completed - Session: {}, Processing time: {} ms, Response length: {}",
                    sessionId, chatClientProcessingTime, response != null ? response.length() : 0);

            // Validate response
            if (response == null || response.trim().isEmpty()) {
                logger.warn(
                        "Empty response from ChatClient - Session: {}, Providing fallback response",
                        sessionId);
                response =
                        "I apologize, but I wasn't able to generate a proper response to your request. Please try rephrasing your question.";
            }

            // Log successful completion
            long totalProcessingTime = System.currentTimeMillis() - processingStartTime;
            logger.info(
                    "MCP processing completed successfully - Session: {}, Total time: {} ms, " + "ChatClient time: {} ms, Context overhead: {} ms",
                    sessionId, totalProcessingTime, chatClientProcessingTime,
                    totalProcessingTime - chatClientProcessingTime);

            if (logger.isDebugEnabled()) {
                logger.debug("Response preview - Session: {}, Content: '{}'", sessionId,
                        response.length() > 150 ? response.substring(0, 150) + "..." : response);
            }

            return response;

        } catch (Exception e) {
            long totalProcessingTime = System.currentTimeMillis() - processingStartTime;
            logger.error(
                    "Error during MCP processing - Session: {}, Processing time: {} ms, " + "Message: '{}', Error: {}, Full trace: ",
                    sessionId, totalProcessingTime, userMessage.length() > 100 ?
                            userMessage.substring(0, 100) + "..." :
                            userMessage, e.getMessage(), e);

            // Return user-friendly error message
            return String.format(
                    "I'm sorry, I encountered an error while processing your request. " + "Error type: %s. Please try again or rephrase your question.",
                    e.getClass().getSimpleName());
        }
    }

    // Keep the old method for backward compatibility with enhanced logging
    public String processWithMcp(String userMessage) {
        String sessionId = MDC.get("sessionId");
        logger.info("Processing message without history - Session: {}, Message length: {}",
                sessionId, userMessage != null ? userMessage.length() : 0);
        return processWithMcp(userMessage, null);
    }

    public boolean isMcpAvailable() {
        String sessionId = MDC.get("sessionId");
        logger.info("Checking MCP availability - Session: {}", sessionId);

        try {
            long availabilityCheckStart = System.currentTimeMillis();

            // Test if MCP tools are available with a simple query
            String testResponse = chatClientBuilder.build()
                    .prompt("What tools are available? Please provide a brief response.")
                    .toolCallbacks(toolCallbackProvider)
                    .call()
                    .content();

            long availabilityCheckTime = System.currentTimeMillis() - availabilityCheckStart;
            boolean isAvailable = testResponse != null && !testResponse.trim().isEmpty();

            logger.info(
                    "MCP availability check completed - Session: {}, Available: {}, " + "Check time: {} ms, Response length: {}",
                    sessionId, isAvailable, availabilityCheckTime,
                    testResponse != null ? testResponse.length() : 0);

            if (logger.isDebugEnabled() && testResponse != null) {
                logger.debug("Availability test response - Session: {}, Content: '{}'", sessionId,
                        testResponse.length() > 100 ?
                                testResponse.substring(0, 100) + "..." :
                                testResponse);
            }

            return isAvailable;

        } catch (Exception e) {
            logger.warn("MCP availability check failed - Session: {}, Error: {}", sessionId,
                    e.getMessage());
            logger.debug("MCP availability check error details - Session: {}", sessionId, e);
            return false;
        }
    }

    // Additional monitoring methods
    public void logServiceStatus() {
        String sessionId = MDC.get("sessionId");
        try {
            boolean mcpStatus = isMcpAvailable();
            logger.info(
                    "Service Status Report - Session: {}, MCP Available: {}, " + "ChatClient Builder: {}, Tool Callback Provider: {}",
                    sessionId, mcpStatus, chatClientBuilder != null ? "Initialized" : "NULL",
                    toolCallbackProvider != null ? "Initialized" : "NULL");
        } catch (Exception e) {
            logger.error("Error generating service status report - Session: {}, Error: {}",
                    sessionId, e.getMessage(), e);
        }
    }

    private String sanitizeForLog(String s) {
        if (s == null)
            return "";
        // basic redaction: api keys, very long tokens
        String redacted = s.replaceAll("(?i)api[_-]?key\\s*[:=]\\s*\\S+", "<REDACTED_KEY>");
        if (redacted.length() > 2000)
            redacted = redacted.substring(0, 2000) + "...[truncated]";
        return redacted;
    }
}
