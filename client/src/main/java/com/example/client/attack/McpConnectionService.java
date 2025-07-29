package com.example.client.attack;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.mcp.client.autoconfigure.properties.McpSseClientProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.stream.Collectors;

@Service
public class McpConnectionService {

    private static final Logger logger = LoggerFactory.getLogger(McpConnectionService.class);

    @Autowired(required = false)
    private McpSseClientProperties mcpSseClientProperties;

    public String getServerUrl(String connectionName) {
        McpSseClientProperties.SseParameters connection =
                mcpSseClientProperties.getConnections().get(connectionName);
        return connection != null ? connection.url() : null;
    }

    public String getServer1Url() {
        return getServerUrl("server1");
    }

    public String getSseEndpoint(String connectionName) {
        McpSseClientProperties.SseParameters connection =
                mcpSseClientProperties.getConnections().get(connectionName);
        return connection != null ? connection.sseEndpoint() : null;
    }

    public Map<String, String> getAllServerUrls() {
        return mcpSseClientProperties.getConnections().entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, entry -> entry.getValue().url()));
    }

    public boolean hasConnection(String connectionName) {
        return mcpSseClientProperties.getConnections().containsKey(connectionName);
    }

    @PostConstruct
    public void init() {
        logger.info("MCP Connection Service initialized with {} connections",
                mcpSseClientProperties.getConnections().size());
        getAllServerUrls().forEach(
                (name, url) -> logger.info("  Connection '{}' -> {}", name, url));
    }
}
