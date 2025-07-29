package com.example.client.service;

import com.example.client.model.FilterResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

@Service
public class MessageFilterService {

    private static final Logger logger = LoggerFactory.getLogger(MessageFilterService.class);

    // Math operation patterns
    private static final List<Pattern> MATH_PATTERNS = Arrays.asList(
            // Basic operations
            Pattern.compile("(?i).*(add|sum|plus|\\+).*\\d+.*\\d+.*", Pattern.CASE_INSENSITIVE),
            Pattern.compile("(?i).*(subtract|minus|\\-).*\\d+.*\\d+.*", Pattern.CASE_INSENSITIVE),
            Pattern.compile("(?i).*(multiply|times|\\*).*\\d+.*\\d+.*", Pattern.CASE_INSENSITIVE),
            Pattern.compile("(?i).*(divide|division|\\/).*\\d+.*\\d+.*", Pattern.CASE_INSENSITIVE),

            // Advanced operations
            Pattern.compile("(?i).*(power|exponent|\\^).*\\d+.*\\d+.*", Pattern.CASE_INSENSITIVE),
            Pattern.compile("(?i).*(square root|sqrt).*\\d+.*", Pattern.CASE_INSENSITIVE),
            Pattern.compile("(?i).*(factorial).*\\d+.*", Pattern.CASE_INSENSITIVE),
            Pattern.compile("(?i).*(absolute|abs).*\\d+.*", Pattern.CASE_INSENSITIVE),
            Pattern.compile("(?i).*(modulo|mod|remainder).*\\d+.*\\d+.*", Pattern.CASE_INSENSITIVE),
            Pattern.compile("(?i).*(log|logarithm).*\\d+.*", Pattern.CASE_INSENSITIVE),

            // Trigonometric functions
            Pattern.compile("(?i).*(sin|sine).*\\d+.*", Pattern.CASE_INSENSITIVE),
            Pattern.compile("(?i).*(cos|cosine).*\\d+.*", Pattern.CASE_INSENSITIVE),
            Pattern.compile("(?i).*(tan|tangent).*\\d+.*", Pattern.CASE_INSENSITIVE),
            Pattern.compile("(?i).*(degrees?\\s+to\\s+radians?|radians?\\s+to\\s+degrees?).*\\d+.*",
                    Pattern.CASE_INSENSITIVE),

            // Geometric calculations
            Pattern.compile("(?i).*(area.*circle|circle.*area).*\\d+.*", Pattern.CASE_INSENSITIVE),
            Pattern.compile("(?i).*(circumference.*circle|circle.*circumference).*\\d+.*",
                    Pattern.CASE_INSENSITIVE),
            Pattern.compile("(?i).*(area.*rectangle|rectangle.*area).*\\d+.*\\d+.*",
                    Pattern.CASE_INSENSITIVE),
            Pattern.compile("(?i).*(area.*triangle|triangle.*area).*\\d+.*\\d+.*",
                    Pattern.CASE_INSENSITIVE),

            // Utility functions
            Pattern.compile("(?i).*(maximum|max|minimum|min).*\\d+.*\\d+.*",
                    Pattern.CASE_INSENSITIVE),
            Pattern.compile("(?i).*(round|ceiling|floor).*\\d+.*", Pattern.CASE_INSENSITIVE),

            // Simple mathematical expressions
            Pattern.compile(".*\\d+\\s*[+\\-*/^%]\\s*\\d+.*", Pattern.CASE_INSENSITIVE),
            Pattern.compile("(?i).*calculate.*\\d+.*", Pattern.CASE_INSENSITIVE),
            Pattern.compile("(?i).*what.*is.*\\d+.*[+\\-*/^%].*\\d+.*", Pattern.CASE_INSENSITIVE));

    // Weather patterns
    private static final List<Pattern> WEATHER_PATTERNS = Arrays.asList(
            Pattern.compile("(?i).*(weather|temperature|temp).*", Pattern.CASE_INSENSITIVE),
            Pattern.compile("(?i).*(how.*hot|how.*cold|how.*warm).*", Pattern.CASE_INSENSITIVE),
            Pattern.compile("(?i).*(forecast|climate).*", Pattern.CASE_INSENSITIVE),
            Pattern.compile("(?i).*(degrees|celsius|fahrenheit).*", Pattern.CASE_INSENSITIVE));

    public FilterResult filterMessage(String message) {
        if (message == null || message.trim().isEmpty()) {
            return new FilterResult(false, "Empty message", "NONE", message);
        }

        String trimmedMessage = message.trim();

        // Check for math operations
        if (isMathQuery(trimmedMessage)) {
            logger.info("Detected math query: {}", trimmedMessage);
            String processedMessage = preprocessMathMessage(trimmedMessage);
            return new FilterResult(true, "Math operation detected", "MATH", processedMessage);
        }

        // Check for weather queries
        if (isWeatherQuery(trimmedMessage)) {
            logger.info("Detected weather query: {}", trimmedMessage);
            String processedMessage = preprocessWeatherMessage(trimmedMessage);
            return new FilterResult(true, "Weather query detected", "WEATHER", processedMessage);
        }

        // Default to LLM processing
        logger.info("No MCP tool match found, routing to LLM: {}", trimmedMessage);
        return new FilterResult(false, "No matching MCP tool found", "LLM", trimmedMessage);
    }

    private boolean isMathQuery(String message) {
        return MATH_PATTERNS.stream().anyMatch(pattern -> pattern.matcher(message).matches());
    }

    private boolean isWeatherQuery(String message) {
        return WEATHER_PATTERNS.stream().anyMatch(pattern -> pattern.matcher(message).matches());
    }

    private String preprocessMathMessage(String message) {
        // Add context to help the LLM understand it should use math tools
        return "Please perform this mathematical calculation using the available math tools: " + message;
    }

    private String preprocessWeatherMessage(String message) {
        // Add context to help the LLM understand it should use weather tools
        if (!message.toLowerCase().contains("latitude") && !message.toLowerCase()
                .contains("longitude")) {
            return "Please get the weather information for the requested location. " + "If coordinates are not provided, please ask the user for latitude and longitude. " + "User request: " + message;
        }
        return "Please get the weather information using the weather tool: " + message;
    }
}
