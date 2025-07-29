package com.example.client.attack;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Component
@RequiredArgsConstructor
public class NetworkReconnaissanceClient {

    private static final Logger logger = LoggerFactory.getLogger(NetworkReconnaissanceClient.class);
    private final RestTemplate restTemplate = new RestTemplate();
    private final String targetHost = "your-server.com";
    private final McpConnectionService mcpConnectionService;

    /**
     * PROBLEM 1: Port Scanning from MCP URL Client can discover other services running on the same
     * server
     */
    public void performPortScan() {
        logger.info("=== PORT SCANNING DEMONSTRATION ===");
        logger.info("Original MCP URL: {}/mcp", mcpConnectionService.getServer1Url());

        // Common ports to scan
        int[] commonPorts = {80, 443, 8080, 8081, 8082, 3000, 3001, 5000, 9090, 8888, 8443, 9000};

        ExecutorService executor = Executors.newFixedThreadPool(10);

        for (int port : commonPorts) {
            executor.submit(() -> {
                try {
                    Socket socket = new Socket();
                    socket.connect(new InetSocketAddress(targetHost, port), 1000);
                    socket.close();

                    logger.warn("🚨 DISCOVERED OPEN PORT: {}:{}", targetHost, port);

                    // Try HTTP request to discovered port
                    tryHttpConnection(port);

                } catch (IOException e) {
                    // Port closed - this is normal
                }
            });
        }

        executor.shutdown();
    }

    private void tryHttpConnection(int port) {
        try {
            String testUrl = "http://" + targetHost + ":" + port;
            ResponseEntity<String> response = restTemplate.getForEntity(testUrl, String.class);

            logger.warn("🚨 HTTP SERVICE DISCOVERED: {} - Status: {}", testUrl,
                    response.getStatusCode());
            logger.warn("🚨 Response Headers: {}", response.getHeaders());

            // Analyze response for technology fingerprinting
            analyzeServerFingerprint(response);

        } catch (Exception e) {
            // Service might not be HTTP or might be protected
        }
    }

    /**
     * PROBLEM 2: Server Fingerprinting and Technology Stack Discovery
     */
    private void analyzeServerFingerprint(ResponseEntity<String> response) {
        HttpHeaders headers = response.getHeaders();

        // Extract server information
        String server = headers.getFirst("Server");
        String xPoweredBy = headers.getFirst("X-Powered-By");
        String xFramework = headers.getFirst("X-Framework");

        if (server != null) {
            logger.warn("🚨 SERVER TECHNOLOGY: {}", server);
        }
        if (xPoweredBy != null) {
            logger.warn("🚨 POWERED BY: {}", xPoweredBy);
        }
        if (xFramework != null) {
            logger.warn("🚨 FRAMEWORK: {}", xFramework);
        }

        // Analyze response body for framework signatures
        String body = response.getBody();
        if (body != null) {
            if (body.contains("Spring Boot")) {
                logger.warn("🚨 SPRING BOOT APPLICATION DETECTED");
            }
            if (body.contains("Whitelabel Error Page")) {
                logger.warn("🚨 DEFAULT SPRING ERROR PAGE - INDICATES UNHANDLED ENDPOINTS");
            }
        }
    }
}
