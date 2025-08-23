package com.example.client.service;

import com.example.client.model.ToolDescription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;

@Service
public class McpToolService {

    private static final Logger logger = LoggerFactory.getLogger(McpToolService.class);
    private static final DateTimeFormatter TIMESTAMP_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
    private static final int MCP_CONNECTION_WAIT_MS = 3000;
    private static final int MAX_INITIALIZATION_ATTEMPTS = 3;
    private static final int RETRY_DELAY_MS = 1000;

    private final ChatClient chatClient;
    private final ToolCallbackProvider toolCallbackProvider;
    private volatile boolean initializationComplete = false;
    private volatile List<ToolDescription> availableTools = Collections.emptyList();

    public McpToolService(ChatClient.Builder chatClientBuilder,
            ToolCallbackProvider toolCallbackProvider) {
        this.chatClient = chatClientBuilder.build();
        this.toolCallbackProvider = toolCallbackProvider;

        logger.info(
                "McpToolService constructed - ChatClient: {}, ToolCallbackProvider: {}, Timestamp: {}",
                this.chatClient != null ? "Initialized" : "NULL",
                this.toolCallbackProvider != null ? "Initialized" : "NULL",
                LocalDateTime.now().format(TIMESTAMP_FORMAT));
    }

    public void initializeAndFetchTools() {
        logger.info("Starting MCP Client initialization - Timestamp: {}",
                LocalDateTime.now().format(TIMESTAMP_FORMAT));
        long initStartTime = System.currentTimeMillis();

        int attempt = 1;
        Exception lastException = null;

        while (attempt <= MAX_INITIALIZATION_ATTEMPTS && !initializationComplete) {
            try {
                logger.info("MCP initialization attempt {} of {} - Start time: {}", attempt,
                        MAX_INITIALIZATION_ATTEMPTS, LocalDateTime.now().format(TIMESTAMP_FORMAT));

                // Wait for MCP connection to establish
                waitForMcpConnection(attempt);

                // Fetch available tools
                List<ToolDescription> tools = fetchAvailableTools(attempt);

                // Store tools and mark as complete
                this.availableTools = tools != null ? tools : Collections.emptyList();
                this.initializationComplete = true;

                // Log available tools
                logAvailableTools(tools);

                long totalInitTime = System.currentTimeMillis() - initStartTime;
                logger.info(
                        "MCP Client initialization completed successfully - Attempt: {}, " + "Total time: {} ms, Tools found: {}",
                        attempt, totalInitTime, this.availableTools.size());
                return;

            } catch (Exception e) {
                lastException = e;
                long attemptTime = System.currentTimeMillis() - initStartTime;

                logger.warn("MCP initialization attempt {} failed - Time: {} ms, Error: {}",
                        attempt, attemptTime, e.getMessage());
                logger.debug("MCP initialization attempt {} error details", attempt, e);

                if (attempt < MAX_INITIALIZATION_ATTEMPTS) {
                    try {
                        logger.info("Retrying MCP initialization in {} ms - Next attempt: {}",
                                RETRY_DELAY_MS, attempt + 1);
                        Thread.sleep(RETRY_DELAY_MS);
                    } catch (InterruptedException ie) {
                        logger.warn("MCP initialization retry interrupted", ie);
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
                attempt++;
            }
        }

        // All attempts failed
        long totalFailedTime = System.currentTimeMillis() - initStartTime;
        logger.error(
                "MCP Client initialization failed after {} attempts - Total time: {} ms, " + "Last error: {}",
                MAX_INITIALIZATION_ATTEMPTS, totalFailedTime,
                lastException != null ? lastException.getMessage() : "Unknown error");

        // Set empty tools list and mark as complete to prevent further attempts
        this.availableTools = Collections.emptyList();
        this.initializationComplete = true;
    }

    private void waitForMcpConnection(int attempt) throws InterruptedException {
        logger.info("Waiting for MCP connection - Attempt: {}, Wait time: {} ms", attempt,
                MCP_CONNECTION_WAIT_MS);

        long waitStartTime = System.currentTimeMillis();
        Thread.sleep(MCP_CONNECTION_WAIT_MS);
        long actualWaitTime = System.currentTimeMillis() - waitStartTime;

        logger.debug("MCP connection wait completed - Attempt: {}, Actual wait time: {} ms",
                attempt, actualWaitTime);
    }

    private List<ToolDescription> fetchAvailableTools(int attempt) {
        logger.info("Fetching available MCP tools - Attempt: {}", attempt);
        long fetchStartTime = System.currentTimeMillis();

        try {
            // Enhanced prompt for better tool detection
            String toolsPrompt =
                    "What tools are available? Please list all available tools with their descriptions. " + "Format the response as a JSON array of objects with 'toolName' and 'toolDescription' fields. " + "Avoid any additional comments or explanations - only provide the JSON format.";

            logger.debug("Sending tools query - Attempt: {}, Prompt length: {}", attempt,
                    toolsPrompt.length());

            List<ToolDescription> tools =
                    chatClient.prompt(toolsPrompt).toolCallbacks(toolCallbackProvider).call()
                            .entity(new ParameterizedTypeReference<List<ToolDescription>>() {
                            });

            long fetchTime = System.currentTimeMillis() - fetchStartTime;
            logger.info("Tools fetch completed - Attempt: {}, Time: {} ms, Tools found: {}",
                    attempt, fetchTime, tools != null ? tools.size() : 0);

            if (tools != null && logger.isDebugEnabled()) {
                logger.debug("Raw tools response - Attempt: {}, Tools: {}", attempt, tools);
            }

            return tools != null ? tools : Collections.emptyList();

        } catch (Exception e) {
            long fetchTime = System.currentTimeMillis() - fetchStartTime;
            logger.error("Error fetching MCP tools - Attempt: {}, Time: {} ms, Error: {}", attempt,
                    fetchTime, e.getMessage());
            logger.debug("Tools fetch error details - Attempt: {}", attempt, e);
            throw e;
        }
    }

    private void logAvailableTools(List<ToolDescription> tools) {
        if (tools == null || tools.isEmpty()) {
            logger.warn("No MCP tools available - Tools list is empty or null");
            return;
        }

        logger.info("Available MCP Tools Summary - Count: {}", tools.size());

        for (int i = 0; i < tools.size(); i++) {
            ToolDescription tool = tools.get(i);
            if (tool != null) {
                logger.info("  Tool {}: {} -> {}", (i + 1), tool.toolName(),
                        tool.toolDescription());
            } else {
                logger.warn("  Tool {}: NULL tool description found", (i + 1));
            }
        }

        // Additional categorization logging for all weather service tools
        long mathTools = tools.stream().filter(tool -> tool != null && tool.toolName() != null)
                .filter(tool -> tool.toolName().toLowerCase().contains("math") || tool.toolName()
                        .toLowerCase().contains("calc") || tool.toolName().toLowerCase()
                        .contains("arithmetic")).count();

        long currentWeatherTools =
                tools.stream().filter(tool -> tool != null && tool.toolName() != null)
                        .filter(tool -> tool.toolName().toLowerCase()
                                .contains("currentweather") || tool.toolName().toLowerCase()
                                .contains("current_weather")).count();

        long forecastTools = tools.stream().filter(tool -> tool != null && tool.toolName() != null)
                .filter(tool -> tool.toolName().toLowerCase()
                        .contains("forecast") || tool.toolName().toLowerCase().contains("detailed"))
                .count();

        long airQualityTools =
                tools.stream().filter(tool -> tool != null && tool.toolName() != null)
                        .filter(tool -> tool.toolName().toLowerCase()
                                .contains("airquality") || tool.toolName().toLowerCase()
                                .contains("air_quality")).count();

        long locationTools = tools.stream().filter(tool -> tool != null && tool.toolName() != null)
                .filter(tool -> tool.toolName().toLowerCase()
                        .contains("location") || tool.toolName().toLowerCase().contains("search"))
                .count();

        long historicalWeatherTools =
                tools.stream().filter(tool -> tool != null && tool.toolName() != null)
                        .filter(tool -> tool.toolName().toLowerCase()
                                .contains("historical") || tool.toolName().toLowerCase()
                                .contains("history")).count();

        long marineWeatherTools =
                tools.stream().filter(tool -> tool != null && tool.toolName() != null)
                        .filter(tool -> tool.toolName().toLowerCase()
                                .contains("marine") || tool.toolName().toLowerCase()
                                .contains("ocean")).count();

        long echoTools = tools.stream().filter(tool -> tool != null && tool.toolName() != null)
                .filter(tool -> tool.toolName().toLowerCase().contains("echo")).count();

        long totalWeatherTools =
                currentWeatherTools + forecastTools + airQualityTools + locationTools + historicalWeatherTools + marineWeatherTools;

        logger.info(
                "Tool Categories - Math: {}, Weather Tools: {} (Current: {}, Forecast: {}, Air Quality: {}, " + "Location: {}, Historical: {}, Marine: {}), Echo: {}, Other: {}",
                mathTools, totalWeatherTools, currentWeatherTools, forecastTools, airQualityTools,
                locationTools, historicalWeatherTools, marineWeatherTools, echoTools,
                tools.size() - mathTools - totalWeatherTools - echoTools);
    }

    // Getter methods with logging
    public List<ToolDescription> getAvailableTools() {
        logger.debug("Retrieving available tools - Initialized: {}, Tools count: {}",
                initializationComplete, availableTools.size());
        return Collections.unmodifiableList(availableTools);
    }

    public boolean isInitializationComplete() {
        logger.debug("Checking initialization status - Complete: {}, Tools available: {}",
                initializationComplete, availableTools.size());
        return initializationComplete;
    }

    public boolean hasTools() {
        boolean hasTools = initializationComplete && !availableTools.isEmpty();
        logger.debug(
                "Checking tools availability - Has tools: {}, Initialized: {}, Tools count: {}",
                hasTools, initializationComplete, availableTools.size());
        return hasTools;
    }

    public boolean hasToolOfType(String toolType) {
        if (!hasTools() || toolType == null) {
            logger.debug("Tool type check failed - Has tools: {}, Tool type: {}", hasTools(),
                    toolType);
            return false;
        }

        String lowerToolType = toolType.toLowerCase();
        boolean hasType =
                availableTools.stream().filter(tool -> tool != null && tool.toolName() != null)
                        .anyMatch(tool -> tool.toolName().toLowerCase().contains(lowerToolType));

        logger.debug("Tool type availability - Type: {}, Available: {}", toolType, hasType);
        return hasType;
    }

    // Enhanced status reporting
    public void logServiceStatus() {
        logger.info("McpToolService Status Report - Timestamp: {}",
                LocalDateTime.now().format(TIMESTAMP_FORMAT));
        logger.info("  Initialization Complete: {}", initializationComplete);
        logger.info("  Available Tools Count: {}", availableTools.size());
        logger.info("  ChatClient Status: {}", chatClient != null ? "Initialized" : "NULL");
        logger.info("  ToolCallbackProvider Status: {}",
                toolCallbackProvider != null ? "Initialized" : "NULL");

        if (hasTools()) {
            logger.info("  Tool Categories Available:");
            logger.info("    Math Tools: {}", hasToolOfType("math") || hasToolOfType("calc"));
            logger.info("    Current Weather Tools: {}",
                    hasToolOfType("currentweather") || hasToolOfType("current_weather"));
            logger.info("    Forecast Tools: {}",
                    hasToolOfType("forecast") || hasToolOfType("detailed"));
            logger.info("    Air Quality Tools: {}",
                    hasToolOfType("airquality") || hasToolOfType("air_quality"));
            logger.info("    Location Search Tools: {}",
                    hasToolOfType("location") || hasToolOfType("search"));
            logger.info("    Historical Weather Tools: {}",
                    hasToolOfType("historical") || hasToolOfType("history"));
            logger.info("    Marine Weather Tools: {}",
                    hasToolOfType("marine") || hasToolOfType("ocean"));
            logger.info("    Echo Tools: {}", hasToolOfType("echo"));
        }

        logger.info("  Service Ready: {}", hasTools());
    }

    // Utility method for force re-initialization if needed
    public void forceReinitialization() {
        logger.warn("Force re-initialization requested - Current status: Initialized={}, Tools={}",
                initializationComplete, availableTools.size());

        this.initializationComplete = false;
        this.availableTools = Collections.emptyList();

        logger.info("Re-initialization triggered");
        initializeAndFetchTools();
    }
}
