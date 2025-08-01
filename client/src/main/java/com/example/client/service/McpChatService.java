package com.example.client.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class McpChatService {

    private static final Logger logger = LoggerFactory.getLogger(McpChatService.class);

    @Autowired
    private ChatClient.Builder chatClientBuilder;

    @Autowired
    private ToolCallbackProvider toolCallbackProvider;

    public String processWithMcp(String userMessage, List<String> chatHistory) {
        try {
            logger.info("Processing message with MCP tools: {}", userMessage);

            // Build context from chat history
            StringBuilder contextBuilder = new StringBuilder();
            contextBuilder.append("Previous conversation context:\n");

            if (chatHistory != null && !chatHistory.isEmpty()) {
                // Include last few messages for context (excluding the current one)
                int startIndex = Math.max(0, chatHistory.size() - 10); // Last 10 messages
                for (int i = startIndex; i < chatHistory.size() - 1; i++) { // -1 to exclude current message
                    contextBuilder.append(chatHistory.get(i)).append("\n");
                }
            }

            contextBuilder.append("\nCurrent user message: ").append(userMessage);
            contextBuilder.append("\n\nPlease respond considering the conversation context above.");

            String fullPrompt = contextBuilder.toString();
            logger.debug("Full prompt with context: {}", fullPrompt);

            String response = chatClientBuilder.build()
                    .prompt(fullPrompt)
                    .toolCallbacks(toolCallbackProvider)
                    .call()
                    .content();

            logger.info("MCP response generated successfully");
            return response;

        } catch (Exception e) {
            logger.error("Error processing message with MCP", e);
            return "I'm sorry, I encountered an error while processing your request. Please try again later.";
        }
    }

    // Keep the old method for backward compatibility
    public String processWithMcp(String userMessage) {
        return processWithMcp(userMessage, null);
    }

    public boolean isMcpAvailable() {
        try {
            // Test if MCP tools are available
            chatClientBuilder.build()
                    .prompt("What tools are available?")
                    .toolCallbacks(toolCallbackProvider)
                    .call()
                    .content();
            return true;
        } catch (Exception e) {
            logger.warn("MCP tools not available: {}", e.getMessage());
            return false;
        }
    }
}
