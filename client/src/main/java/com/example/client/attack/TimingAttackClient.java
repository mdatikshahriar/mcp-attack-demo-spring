package com.example.client.attack;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
@RequiredArgsConstructor
public class TimingAttackClient {

    private static final Logger logger = LoggerFactory.getLogger(TimingAttackClient.class);
    private final RestTemplate restTemplate = new RestTemplate();
    private final McpConnectionService mcpConnectionService;

    /**
     * PROBLEM 9: Timing Attacks to Infer System Behavior
     */
    public void performTimingAnalysis() {
        logger.info("=== TIMING ATTACK DEMONSTRATION ===");

        // Test response times for different endpoints
        testEndpointTiming("/api/quizzes", "Quiz API");
        testEndpointTiming("/admin", "Admin Panel");
        testEndpointTiming("/health", "Health Check");
        testEndpointTiming("/nonexistent", "Non-existent Endpoint");
    }

    private void testEndpointTiming(String endpoint, String description) {
        try {
            String url = mcpConnectionService.getServer1Url() + endpoint;

            long startTime = System.nanoTime();

            try {
                restTemplate.getForEntity(url, String.class);
            } catch (Exception e) {
                // We're measuring timing, not success
            }

            long endTime = System.nanoTime();
            long responseTime = (endTime - startTime) / 1_000_000; // Convert to milliseconds

            logger.info("🕒 {} response time: {}ms", description, responseTime);

            // Analyze timing patterns
            if (responseTime > 1000) {
                logger.warn(
                        "🚨 SLOW RESPONSE: {} might be processing complex logic or database queries",
                        endpoint);
            } else if (responseTime < 10) {
                logger.warn("🚨 VERY FAST RESPONSE: {} might be cached or immediately rejected",
                        endpoint);
            }

        } catch (Exception e) {
            logger.error("Timing test failed for {}", endpoint);
        }
    }
}

