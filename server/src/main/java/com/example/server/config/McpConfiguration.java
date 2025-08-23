package com.example.server.config;

import com.example.server.service.UpdateSignalService;
import com.example.server.service.WeatherService;
import com.example.server.tools.MathTools;
import com.example.server.tools.McpCommandLineRunner;
import io.modelcontextprotocol.server.McpSyncServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class McpConfiguration {

    private static final Logger logger = LoggerFactory.getLogger(McpConfiguration.class);

    public McpConfiguration() {
        logger.info("Initializing MCP Configuration");
    }

    @Bean
    public ToolCallbackProvider allTools(WeatherService weatherService) {
        logger.info("Creating ToolCallbackProvider with WeatherService and MathTools");

        try {
            MathTools mathTools = new MathTools();
            ToolCallbackProvider provider =
                    MethodToolCallbackProvider.builder().toolObjects(weatherService, mathTools)
                            .build();

            logger.info("Successfully created ToolCallbackProvider with {} tool objects", 2);
            logger.debug("Tools registered: WeatherService, MathTools");

            return provider;
        } catch (Exception e) {
            logger.error("Failed to create ToolCallbackProvider", e);
            throw e;
        }
    }

    @Bean
    public CommandLineRunner commandRunner(McpSyncServer mcpSyncServer,
            UpdateSignalService updateSignalService) {
        logger.info("Creating MCP CommandLineRunner");

        try {
            McpCommandLineRunner runner =
                    new McpCommandLineRunner(mcpSyncServer, updateSignalService);
            logger.info("Successfully created MCP CommandLineRunner");
            return runner;
        } catch (Exception e) {
            logger.error("Failed to create MCP CommandLineRunner", e);
            throw e;
        }
    }
}
