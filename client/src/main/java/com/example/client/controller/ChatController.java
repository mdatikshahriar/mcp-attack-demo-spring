package com.example.client.controller;

import com.example.client.model.ChatMessage;
import com.example.client.model.FilterResult;
import com.example.client.service.McpChatService;
import com.example.client.service.MessageFilterService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

@Controller
public class ChatController {

    private static final Logger logger = LoggerFactory.getLogger(ChatController.class);
    private static final DateTimeFormatter TIMESTAMP_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
    private static final int MAX_CHAT_HISTORY_SIZE = 20;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    private McpChatService mcpChatService;

    @Autowired
    private MessageFilterService messageFilterService;

    // Store chat history per session in memory with enhanced thread safety
    private final ConcurrentHashMap<String, List<String>> chatHistoryMap =
            new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, String> sessionUserMap = new ConcurrentHashMap<>();

    @MessageMapping("/chat.sendMessage")
    @SendTo("/topic/public")
    public ChatMessage sendMessage(@Payload ChatMessage chatMessage,
            SimpMessageHeaderAccessor headerAccessor) {
        String sessionId = headerAccessor.getSessionId();
        String username = chatMessage.getSender();

        // Set up logging context
        MDC.put("sessionId", sessionId);
        MDC.put("username", username);

        try {
            logger.info("Message received - Session: {}, User: {}, Type: {}, Content length: {}",
                    sessionId, username, chatMessage.getType(),
                    chatMessage.getContent() != null ? chatMessage.getContent().length() : 0);

            if (chatMessage.getContent() != null) {
                logger.debug("Message content preview: '{}'",
                        chatMessage.getContent().length() > 100 ?
                                chatMessage.getContent().substring(0, 100) + "..." :
                                chatMessage.getContent());
            }

            // Store user mapping for session
            sessionUserMap.put(sessionId, username);

            // Process AI command asynchronously
            handleAiCommand(chatMessage, sessionId);

            return chatMessage;

        } catch (Exception e) {
            logger.error("Error processing message - Session: {}, User: {}, Error: {}", sessionId,
                    username, e.getMessage(), e);
            throw e;
        } finally {
            MDC.clear();
        }
    }

    @MessageMapping("/chat.addUser")
    @SendTo("/topic/public")
    public ChatMessage addUser(@Payload ChatMessage chatMessage,
            SimpMessageHeaderAccessor headerAccessor) {

        String sessionId = headerAccessor.getSessionId();
        String username = chatMessage.getSender();

        // Set up logging context
        MDC.put("sessionId", sessionId);
        MDC.put("username", username);

        try {
            // Add username in web socket session
            headerAccessor.getSessionAttributes().put("username", username);

            // Initialize chat history for this session
            chatHistoryMap.put(sessionId, new ArrayList<>());
            sessionUserMap.put(sessionId, username);

            logger.info("User joined chat - Session: {}, Username: {}, Timestamp: {}", sessionId,
                    username, LocalDateTime.now().format(TIMESTAMP_FORMAT));

            logger.debug("Active sessions count: {}, Chat histories count: {}",
                    sessionUserMap.size(), chatHistoryMap.size());

            return chatMessage;

        } catch (Exception e) {
            logger.error("Error adding user - Session: {}, Username: {}, Error: {}", sessionId,
                    username, e.getMessage(), e);
            throw e;
        } finally {
            MDC.clear();
        }
    }

    private void handleAiCommand(ChatMessage originalMessage, String sessionId) {
        String username = sessionUserMap.get(sessionId);
        MDC.put("sessionId", sessionId);
        MDC.put("username", username);

        String aiPrompt = originalMessage.getContent().trim();
        long processingStartTime = System.currentTimeMillis();

        // Process asynchronously to avoid blocking WebSocket response
        new Thread(() -> {
            try {
                logger.info(
                        "AI processing started - Session: {}, Prompt length: {}, Start time: {}",
                        sessionId, aiPrompt.length(), LocalDateTime.now().format(TIMESTAMP_FORMAT));

                // Get chat history for this session
                List<String> chatHistory =
                        chatHistoryMap.getOrDefault(sessionId, new ArrayList<>());
                logger.debug("Retrieved chat history - Session: {}, History size: {}", sessionId,
                        chatHistory.size());

                // Add user message to history
                String userHistoryEntry = "User: " + aiPrompt;
                chatHistory.add(userHistoryEntry);
                logger.debug("Added user message to history - Session: {}, Entry: '{}'", sessionId,
                        userHistoryEntry.length() > 100 ?
                                userHistoryEntry.substring(0, 100) + "..." :
                                userHistoryEntry);

                // Filter message with detailed logging
                logger.info("Starting message filtering - Session: {}", sessionId);
                FilterResult filterResult = messageFilterService.filterMessage(aiPrompt);

                logger.info(
                        "Filter result - Session: {}, Can use MCP: {}, Tool type: {}, Reason: '{}'",
                        sessionId, filterResult.canBeHandledByMcp(), filterResult.toolType(),
                        filterResult.reason());

                String aiResponse;
                String responseSource;
                long mcpProcessingStartTime = System.currentTimeMillis();

                if (filterResult.canBeHandledByMcp()) {
                    logger.info("Processing with MCP tools - Session: {}, Tool type: {}", sessionId,
                            filterResult.toolType());

                    aiResponse = mcpChatService.processWithMcp(filterResult.processedMessage(),
                            chatHistory);
                    responseSource = "MCP (" + filterResult.toolType() + ")";

                    logger.info(
                            "MCP processing completed - Session: {}, Tool: {}, Processing time: {} ms",
                            sessionId, filterResult.toolType(),
                            System.currentTimeMillis() - mcpProcessingStartTime);
                } else {
                    logger.info("Processing with standard LLM - Session: {}, Reason: {}", sessionId,
                            filterResult.reason());

                    aiResponse = mcpChatService.processWithMcp(aiPrompt, chatHistory);
                    responseSource = "LLM";

                    logger.info("LLM processing completed - Session: {}, Processing time: {} ms",
                            sessionId, System.currentTimeMillis() - mcpProcessingStartTime);
                }

                // Log response details
                logger.info("AI response generated - Session: {}, Response length: {}, Source: {}",
                        sessionId, aiResponse.length(), responseSource);

                if (logger.isDebugEnabled()) {
                    logger.debug("AI response preview - Session: {}, Content: '{}'", sessionId,
                            aiResponse.length() > 200 ?
                                    aiResponse.substring(0, 200) + "..." :
                                    aiResponse);
                }

                // Add AI response to history
                String aiHistoryEntry = "Assistant: " + aiResponse;
                chatHistory.add(aiHistoryEntry);

                // Manage chat history size
                if (chatHistory.size() > MAX_CHAT_HISTORY_SIZE) {
                    int itemsToRemove = chatHistory.size() - MAX_CHAT_HISTORY_SIZE;
                    List<String> trimmedHistory =
                            new ArrayList<>(chatHistory.subList(itemsToRemove, chatHistory.size()));
                    chatHistoryMap.put(sessionId, trimmedHistory);

                    logger.debug(
                            "Chat history trimmed - Session: {}, Removed {} items, New size: {}",
                            sessionId, itemsToRemove, trimmedHistory.size());
                } else {
                    chatHistoryMap.put(sessionId, chatHistory);
                }

                // Send AI response as a new message
                ChatMessage aiMessage = new ChatMessage();
                aiMessage.setType(ChatMessage.MessageType.CHAT);
                aiMessage.setSender("AI Assistant (" + responseSource + ")");
                aiMessage.setContent(aiResponse);

                messagingTemplate.convertAndSend("/topic/public", aiMessage);

                long totalProcessingTime = System.currentTimeMillis() - processingStartTime;
                logger.info(
                        "AI processing completed successfully - Session: {}, Total time: {} ms, " + "Final history size: {}",
                        sessionId, totalProcessingTime, chatHistory.size());

            } catch (Exception e) {
                long totalProcessingTime = System.currentTimeMillis() - processingStartTime;
                logger.error(
                        "Error processing AI command - Session: {}, Processing time: {} ms, " + "Error: {}, Stack trace: ",
                        sessionId, totalProcessingTime, e.getMessage(), e);

                // Send detailed error message
                ChatMessage errorMessage = new ChatMessage();
                errorMessage.setType(ChatMessage.MessageType.CHAT);
                errorMessage.setSender("AI Assistant (Error)");

                String errorResponse = String.format(
                        "Sorry, I encountered an error processing your request. " + "Error details: %s. Please try rephrasing your question or contact support if the issue persists.",
                        e.getMessage());
                errorMessage.setContent(errorResponse);

                messagingTemplate.convertAndSend("/topic/public", errorMessage);

                logger.info("Error message sent to client - Session: {}", sessionId);
            } finally {
                MDC.clear();
            }
        }).start();
    }

    // Enhanced cleanup method with detailed logging
    public void cleanupChatHistory(String sessionId) {
        String username = sessionUserMap.get(sessionId);

        try {
            List<String> removedHistory = chatHistoryMap.remove(sessionId);
            String removedUser = sessionUserMap.remove(sessionId);

            logger.info(
                    "Session cleanup completed - Session: {}, Username: {}, " + "History items removed: {}, User mapping removed: {}",
                    sessionId, username, removedHistory != null ? removedHistory.size() : 0,
                    removedUser != null);

            logger.debug("Remaining active sessions: {}, Remaining chat histories: {}",
                    sessionUserMap.size(), chatHistoryMap.size());

        } catch (Exception e) {
            logger.error("Error during session cleanup - Session: {}, Username: {}, Error: {}",
                    sessionId, username, e.getMessage(), e);
        }
    }

    // Additional utility methods for monitoring and debugging
    public int getActiveSessionsCount() {
        return sessionUserMap.size();
    }

    public int getChatHistoriesCount() {
        return chatHistoryMap.size();
    }

    public void logSystemStatus() {
        logger.info("System Status - Active sessions: {}, Chat histories: {}, Memory usage: {} MB",
                getActiveSessionsCount(), getChatHistoriesCount(),
                (Runtime.getRuntime().totalMemory() - Runtime.getRuntime()
                        .freeMemory()) / 1024 / 1024);
    }
}
