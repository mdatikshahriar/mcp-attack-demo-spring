package com.example.client.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class McpChatService {

    private static final Logger logger = LoggerFactory.getLogger(McpChatService.class);

    @Autowired
    private ChatClient.Builder chatClientBuilder;

    @Autowired
    private ToolCallbackProvider toolCallbackProvider;

    public String processWithMcp(String userMessage) {
        try {
            logger.info("Processing message with MCP tools: {}", userMessage);

            String response = chatClientBuilder.build()
                    .prompt(userMessage)
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
