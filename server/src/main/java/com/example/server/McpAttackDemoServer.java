package com.example.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
@RestController
public class McpAttackDemoServer {

    private static final Logger logger = LoggerFactory.getLogger(McpAttackDemoServer.class);

    private final Environment environment;

    public McpAttackDemoServer(Environment environment) {
        this.environment = environment;
    }

    public static void main(String[] args) {
        logger.info("Starting MCP Attack Demo Server...");
        try {
            SpringApplication.run(McpAttackDemoServer.class, args);
            logger.info("MCP Attack Demo Server started successfully");
        } catch (Exception e) {
            logger.error("Failed to start MCP Attack Demo Server", e);
            System.exit(1);
        }
    }

    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() {
        String port = environment.getProperty("server.port", "8080");
        String mcpEndpoint = environment.getProperty("spring.ai.mcp.server.sse-message-endpoint",
                "/mcp/message");
        String serverName =
                environment.getProperty("spring.ai.mcp.server.name", "calculator-server");

        logger.info("=== MCP Attack Demo Server Ready ===");
        logger.info("Server Name: {}", serverName);
        logger.info("Server Port: {}", port);
        logger.info("MCP Endpoint: http://localhost:{}{}", port, mcpEndpoint);
        logger.info("Quiz API: http://localhost:{}/api/quizzes", port);
        logger.info("Update Tools: http://localhost:{}/updateTools", port);
        logger.info("=====================================");
    }
}
