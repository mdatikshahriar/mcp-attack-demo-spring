package com.example.client.attack;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.net.InetAddress;
import java.net.UnknownHostException;

@Component
@RequiredArgsConstructor
public class SubdomainEnumerationClient {

    private static final Logger logger = LoggerFactory.getLogger(SubdomainEnumerationClient.class);
    private final RestTemplate restTemplate = new RestTemplate();
    private final McpConnectionService mcpConnectionService;

    /**
     * PROBLEM 8: Subdomain Discovery and Lateral Movement
     */
    public void performSubdomainEnumeration() {
        logger.info("=== SUBDOMAIN ENUMERATION DEMONSTRATION ===");

        String baseDomain = mcpConnectionService.getServer1Url(); // Extracted from MCP URL

        // Common subdomains that might exist
        String[] commonSubdomains =
                {"admin", "api", "dashboard", "internal", "management", "monitor", "stats", "db",
                        "database", "cache", "redis", "elasticsearch", "kibana", "grafana",
                        "jenkins", "staging", "dev", "test", "qa"};

        for (String subdomain : commonSubdomains) {
            checkSubdomain(subdomain, baseDomain);
        }
    }

    private void checkSubdomain(String subdomain, String baseDomain) {
        try {
            String subdomainUrl = "http://" + subdomain + "." + baseDomain;

            // Try to resolve DNS first
            InetAddress.getByName(subdomain + "." + baseDomain);

            // DNS resolution successful, try HTTP
            ResponseEntity<String> response = restTemplate.getForEntity(subdomainUrl, String.class);

            logger.warn("🚨 SUBDOMAIN DISCOVERED: {} - Status: {}", subdomainUrl,
                    response.getStatusCode());

            // Check for admin interfaces
            if (subdomain.contains("admin") || subdomain.contains("management")) {
                logger.error("🚨🚨🚨 CRITICAL: ADMINISTRATIVE INTERFACE DISCOVERED: {}", subdomainUrl);
            }

        } catch (UnknownHostException e) {
            // Subdomain doesn't exist - normal
        } catch (Exception e) {
            // Other connection issues
        }
    }
}
