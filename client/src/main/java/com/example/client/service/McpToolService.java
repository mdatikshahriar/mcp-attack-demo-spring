package com.example.client.service;

import com.example.client.model.ToolDescription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
public class McpToolService {

    private static final Logger logger = LoggerFactory.getLogger(McpToolService.class);
    private static final int MCP_CONNECTION_WAIT_MS = 3000;

    private final ChatClient chatClient;
    private final ToolCallbackProvider toolCallbackProvider;

    public McpToolService(ChatClient.Builder chatClientBuilder,
            ToolCallbackProvider toolCallbackProvider) {
        this.chatClient = chatClientBuilder.build();
        this.toolCallbackProvider = toolCallbackProvider;
    }

    public void initializeAndFetchTools() {
        try {
            waitForMcpConnection();
            List<ToolDescription> tools = fetchAvailableTools();
            logAvailableTools(tools);
            logger.info("MCP Client initialized successfully");
        } catch (Exception e) {
            logger.warn("MCP Client initialization failed: {}", e.getMessage());
            logger.debug("Full stack trace: ", e);
        }
    }

    private void waitForMcpConnection() throws InterruptedException {
        logger.info("Waiting for MCP connection to establish...");
        Thread.sleep(MCP_CONNECTION_WAIT_MS);
    }

    private List<ToolDescription> fetchAvailableTools() {
        logger.info("Fetching available MCP tools...");

        List<ToolDescription> tools = chatClient.prompt(
                        "What tools are available? Please list them and avoid any additional comments. Only JSON format.")
                .toolCallbacks(toolCallbackProvider).call()
                .entity(new ParameterizedTypeReference<List<ToolDescription>>() {
                });

        return tools != null ? tools : Collections.emptyList();
    }

    private void logAvailableTools(List<ToolDescription> tools) {
        if (tools.isEmpty()) {
            logger.info("No MCP tools available");
            return;
        }

        logger.info("Available MCP Tools:");
        tools.forEach(tool -> logger.info("  {}", tool));
    }
}
