package com.example.server.controller;

import com.example.server.service.UpdateSignalService;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class McpController {

    private static final Logger logger = LoggerFactory.getLogger(McpController.class);

    private final UpdateSignalService updateSignalService;

    public McpController(UpdateSignalService updateSignalService) {
        this.updateSignalService = updateSignalService;
        logger.info("MCP Controller initialized");
    }

    @GetMapping("/updateTools")
    public ResponseEntity<String> updateTools(HttpServletRequest request) {
        String clientIp = getClientIpAddress(request);
        logger.info("Received update tools request from client IP: {}", clientIp);

        try {
            updateSignalService.signalUpdate();
            logger.info("Successfully sent update signal to MCP server");
            logger.debug("Update signal processed for client: {}", clientIp);

            return ResponseEntity.ok("Update signal sent successfully!");

        } catch (Exception e) {
            logger.error("Failed to send update signal from client {}", clientIp, e);
            return ResponseEntity.internalServerError()
                    .body("Failed to send update signal: " + e.getMessage());
        }
    }

    @GetMapping("/health")
    public ResponseEntity<String> healthCheck(HttpServletRequest request) {
        String clientIp = getClientIpAddress(request);
        logger.debug("Health check requested from client IP: {}", clientIp);

        try {
            // Basic health check
            String status = "MCP Server is running";
            logger.trace("Health check successful for client: {}", clientIp);
            return ResponseEntity.ok(status);

        } catch (Exception e) {
            logger.error("Health check failed for client {}", clientIp, e);
            return ResponseEntity.internalServerError()
                    .body("Health check failed: " + e.getMessage());
        }
    }

    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }

        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }

        return request.getRemoteAddr();
    }
}
