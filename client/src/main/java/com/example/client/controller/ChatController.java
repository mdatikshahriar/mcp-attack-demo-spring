package com.example.client.controller;

import com.example.client.model.ChatMessage;
import com.example.client.model.FilterResult;
import com.example.client.service.McpChatService;
import com.example.client.service.MessageFilterService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

@Controller
public class ChatController {

    private static final Logger logger = LoggerFactory.getLogger(ChatController.class);

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    private McpChatService mcpChatService;

    @Autowired
    private MessageFilterService messageFilterService;

    // Store chat history per session in memory
    private final ConcurrentHashMap<String, List<String>> chatHistoryMap = new ConcurrentHashMap<>();

    @MessageMapping("/chat.sendMessage")
    @SendTo("/topic/public")
    public ChatMessage sendMessage(@Payload ChatMessage chatMessage, SimpMessageHeaderAccessor headerAccessor) {
        logger.info("Received message from {}: {}", chatMessage.getSender(),
                chatMessage.getContent());

        String sessionId = headerAccessor.getSessionId();
        handleAiCommand(chatMessage, sessionId);

        return chatMessage;
    }

    @MessageMapping("/chat.addUser")
    @SendTo("/topic/public")
    public ChatMessage addUser(@Payload ChatMessage chatMessage,
            SimpMessageHeaderAccessor headerAccessor) {
        // Add username in web socket session
        headerAccessor.getSessionAttributes().put("username", chatMessage.getSender());
        String sessionId = headerAccessor.getSessionId();

        // Initialize chat history for this session
        chatHistoryMap.put(sessionId, new ArrayList<>());

        logger.info("User {} joined the chat with session {}", chatMessage.getSender(), sessionId);
        return chatMessage;
    }

    private void handleAiCommand(ChatMessage originalMessage, String sessionId) {
        String aiPrompt = originalMessage.getContent().trim();

        // Process asynchronously to avoid blocking WebSocket response
        new Thread(() -> {
            try {
                logger.info("Processing AI command: {}", aiPrompt);

                // Get chat history for this session
                List<String> chatHistory = chatHistoryMap.getOrDefault(sessionId, new ArrayList<>());

                // Add user message to history
                chatHistory.add("User: " + aiPrompt);

                // Filter message to determine if it can be handled by MCP tools
                FilterResult filterResult = messageFilterService.filterMessage(aiPrompt);

                String aiResponse;
                String responseSource;

                if (filterResult.canBeHandledByMcp()) {
                    logger.info("Message can be handled by MCP tools: {}", filterResult.toolType());
                    aiResponse = mcpChatService.processWithMcp(filterResult.processedMessage(), chatHistory);
                    responseSource = "MCP (" + filterResult.toolType() + ")";
                } else {
                    logger.info("Message requires LLM processing: {}", filterResult.reason());
                    aiResponse = mcpChatService.processWithMcp(aiPrompt, chatHistory);
                    responseSource = "LLM";
                }

                // Add AI response to history
                chatHistory.add("Assistant: " + aiResponse);

                // Keep only last 20 messages to prevent memory issues
                if (chatHistory.size() > 20) {
                    chatHistory = new ArrayList<>(chatHistory.subList(chatHistory.size() - 20, chatHistory.size()));
                    chatHistoryMap.put(sessionId, chatHistory);
                }

                // Send AI response as a new message
                ChatMessage aiMessage = new ChatMessage();
                aiMessage.setType(ChatMessage.MessageType.CHAT);
                aiMessage.setSender("AI Assistant (" + responseSource + ")");
                aiMessage.setContent(aiResponse);

                messagingTemplate.convertAndSend("/topic/public", aiMessage);

            } catch (Exception e) {
                logger.error("Error processing AI command", e);

                // Send error message
                ChatMessage errorMessage = new ChatMessage();
                errorMessage.setType(ChatMessage.MessageType.CHAT);
                errorMessage.setSender("AI Assistant");
                errorMessage.setContent(
                        "Sorry, I encountered an error processing your request: " + e.getMessage());

                messagingTemplate.convertAndSend("/topic/public", errorMessage);
            }
        }).start();
    }

    // Clean up chat history when user disconnects
    public void cleanupChatHistory(String sessionId) {
        chatHistoryMap.remove(sessionId);
        logger.info("Cleaned up chat history for session: {}", sessionId);
    }
}
