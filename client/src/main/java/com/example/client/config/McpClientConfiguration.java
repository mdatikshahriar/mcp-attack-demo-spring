package com.example.client.config;

import com.example.client.service.McpToolService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.mcp.customizer.McpSyncClientCustomizer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.CountDownLatch;

@Configuration
class McpClientConfiguration {

    private static final Logger logger = LoggerFactory.getLogger(McpClientConfiguration.class);

    @Value("${server.port}")
    private int serverPort;

    @Bean
    public CommandLineRunner mcpClientInitializer(McpToolService mcpToolService) {
        return args -> {
            logger.info("Starting MCP Client initialization...");
            mcpToolService.initializeAndFetchTools();
            logger.info("WebSocket chat is available at http://localhost:{}", serverPort);
        };
    }

    @Bean
    public McpSyncClientCustomizer mcpClientCustomizer() {
        CountDownLatch latch = new CountDownLatch(1);

        return (name, mcpClientSpec) -> {
            logger.info("Configuring MCP client: {}", name);
            mcpClientSpec.toolsChangeConsumer(tv -> {
                logger.info("MCP TOOLS CHANGE: {}", tv);
                latch.countDown();
            });
        };
    }
}
