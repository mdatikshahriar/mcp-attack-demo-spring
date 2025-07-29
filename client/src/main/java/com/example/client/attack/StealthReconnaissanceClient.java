package com.example.client.attack;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Random;

@Component
public class StealthReconnaissanceClient {

    private static final Logger logger = LoggerFactory.getLogger(StealthReconnaissanceClient.class);
    private final RestTemplate restTemplate;

    public StealthReconnaissanceClient() {
        // Configure RestTemplate for stealth
        this.restTemplate = createStealthRestTemplate();
    }

    /**
     * PROBLEM 11: Stealth Reconnaissance to Avoid Detection
     */
    private RestTemplate createStealthRestTemplate() {
        RestTemplate template = new RestTemplate();

        // Add interceptor to randomize user agents and headers
        template.getInterceptors().add((request, body, execution) -> {

            // Rotate user agents to look like legitimate browsers
            String[] userAgents = {"Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36",
                    "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36",
                    "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36"};

            request.getHeaders()
                    .set("User-Agent", userAgents[new Random().nextInt(userAgents.length)]);

            // Add realistic headers
            request.getHeaders().set("Accept",
                    "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
            request.getHeaders().set("Accept-Language", "en-US,en;q=0.5");
            request.getHeaders().set("Accept-Encoding", "gzip, deflate");
            request.getHeaders().set("Connection", "keep-alive");

            // Add random delays to avoid pattern detection
            try {
                Thread.sleep(1000 + new Random().nextInt(3000));
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            return execution.execute(request, body);
        });

        return template;
    }

    /**
     * Perform reconnaissance while trying to avoid detection
     */
    public void performStealthReconnaissance() {
        logger.info("=== STEALTH RECONNAISSANCE MODE ===");
        logger.info("Attempting to avoid detection systems...");

        // Use the stealth-configured RestTemplate for all requests
        // This demonstrates how attackers try to evade monitoring
    }
}
