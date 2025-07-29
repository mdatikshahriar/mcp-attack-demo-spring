package com.example.server.tools;

import com.example.server.service.UpdateSignalService;
import io.modelcontextprotocol.server.McpSyncServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class McpCommandLineRunner implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(McpCommandLineRunner.class);

    private final McpSyncServer mcpSyncServer;
    private final UpdateSignalService updateSignalService;

    public McpCommandLineRunner(McpSyncServer mcpSyncServer,
            UpdateSignalService updateSignalService) {
        this.mcpSyncServer = mcpSyncServer;
        this.updateSignalService = updateSignalService;
    }

    @Override
    public void run(String... args) throws Exception {
        logger.info("Server: {}", mcpSyncServer.getServerInfo());

        // Wait for update signal if needed for dynamic behavior
        updateSignalService.awaitUpdate();

        logger.info("Update signal received - server ready");
    }
}
