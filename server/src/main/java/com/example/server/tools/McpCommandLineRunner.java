package com.example.server.tools;

import com.example.server.service.UpdateSignalService;
import io.modelcontextprotocol.server.McpSyncServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

@Component
public class McpCommandLineRunner implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(McpCommandLineRunner.class);
    private static final long STARTUP_TIMEOUT_SECONDS = 30;

    private final McpSyncServer mcpSyncServer;
    private final UpdateSignalService updateSignalService;

    public McpCommandLineRunner(McpSyncServer mcpSyncServer,
            UpdateSignalService updateSignalService) {
        this.mcpSyncServer = mcpSyncServer;
        this.updateSignalService = updateSignalService;
        logger.info("McpCommandLineRunner initialized");
    }

    @Override
    public void run(String... args) throws Exception {
        logger.info("=== MCP Command Line Runner Starting ===");

        try {
            // Log server information
            if (mcpSyncServer != null && mcpSyncServer.getServerInfo() != null) {
                logger.info("MCP Server Info: {}", mcpSyncServer.getServerInfo());
                logger.debug("MCP Server initialized successfully");
            } else {
                logger.warn(
                        "MCP Server or ServerInfo is null - this may indicate initialization issues");
            }

            // Log startup arguments if any
            if (args.length > 0) {
                logger.info("Command line arguments provided: {}", String.join(", ", args));
            } else {
                logger.debug("No command line arguments provided");
            }

            logger.info("Waiting for update signal to complete server initialization...");
            LocalDateTime waitStartTime = LocalDateTime.now();

            // Wait for update signal with timeout
            boolean signalReceived =
                    updateSignalService.awaitUpdate(STARTUP_TIMEOUT_SECONDS, TimeUnit.SECONDS);

            if (signalReceived) {
                LocalDateTime completionTime = LocalDateTime.now();
                logger.info("✅ Update signal received successfully");
                logger.info("Server initialization completed at: {}", completionTime);
                logger.debug("Wait duration: {} seconds",
                        java.time.Duration.between(waitStartTime, completionTime).getSeconds());

                // Log final status
                logServerStatus();

            } else {
                logger.error("❌ Timeout waiting for update signal after {} seconds",
                        STARTUP_TIMEOUT_SECONDS);
                logger.error("Server may not be fully initialized - check for startup issues");

                // Still log what we can about the server status
                logServerStatus();

                // Don't throw exception - let the server continue running
                logger.warn("Continuing with potentially incomplete initialization");
            }

        } catch (InterruptedException e) {
            logger.error("MCP Command Line Runner was interrupted during initialization", e);
            Thread.currentThread().interrupt();
            throw e;

        } catch (Exception e) {
            logger.error("Unexpected error in MCP Command Line Runner", e);
            throw e;
        }

        logger.info("=== MCP Command Line Runner Completed ===");
    }

    private void logServerStatus() {
        try {
            logger.info("📊 Final Server Status:");
            logger.info("   - Update Signal Sent: {}", updateSignalService.isSignalSent());
            logger.info("   - Total Signals: {}", updateSignalService.getSignalCount());
            logger.info("   - Last Signal Time: {}", updateSignalService.getLastSignalTime());
            logger.info("   - Remaining Latch Count: {}",
                    updateSignalService.getRemainingLatchCount());

            if (mcpSyncServer != null) {
                logger.debug("   - MCP Server Instance: Available");
            } else {
                logger.warn("   - MCP Server Instance: NULL");
            }

        } catch (Exception e) {
            logger.error("Error logging server status", e);
        }
    }
}
