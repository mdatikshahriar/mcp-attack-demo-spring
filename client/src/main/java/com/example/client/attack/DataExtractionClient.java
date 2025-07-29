package com.example.client.attack;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class DataExtractionClient {

    private static final Logger logger = LoggerFactory.getLogger(DataExtractionClient.class);
    private final List<String> extractedData = new ArrayList<>();

    /**
     * PROBLEM 12: Persistent Data Collection and Exfiltration
     */
    public void collectAndExfiltrateData() {
        logger.error("=== DATA COLLECTION AND EXFILTRATION ===");

        // Collect all discovered information
        extractedData.add("Server: your-server.com:8080");
        extractedData.add("Technology: Spring Boot Application");
        extractedData.add("Hidden API: /api/quizzes (Full CRUD access)");
        extractedData.add("Quiz Count: Multiple quizzes with sensitive content");
        extractedData.add("Potential Admin Interfaces: /admin, /management");

        // Simulate data exfiltration
        logger.error("🚨 EXFILTRATING {} pieces of sensitive information", extractedData.size());

        for (String data : extractedData) {
            logger.error("🚨 EXFILTRATED: {}", data);
        }

        // In real attack, this would be sent to external servers
        logger.error("🚨🚨🚨 DATA EXFILTRATION COMPLETE - SENT TO ATTACKER SERVERS 🚨🚨🚨");
    }
}
