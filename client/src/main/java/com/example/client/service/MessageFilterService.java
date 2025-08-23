package com.example.client.service;

import com.example.client.model.FilterResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class MessageFilterService {

    private static final Logger logger = LoggerFactory.getLogger(MessageFilterService.class);

    // Enhanced Math operation patterns with more comprehensive coverage
    private static final List<Pattern> MATH_PATTERNS = Arrays.asList(
            // Basic arithmetic operations
            Pattern.compile("(?i).*(add|sum|plus|\\+).*\\d+.*\\d+.*", Pattern.CASE_INSENSITIVE),
            Pattern.compile("(?i).*(subtract|minus|\\-).*\\d+.*\\d+.*", Pattern.CASE_INSENSITIVE),
            Pattern.compile("(?i).*(multiply|times|\\*|product).*\\d+.*\\d+.*",
                    Pattern.CASE_INSENSITIVE),
            Pattern.compile("(?i).*(divide|division|\\/|quotient).*\\d+.*\\d+.*",
                    Pattern.CASE_INSENSITIVE),

            // Advanced mathematical operations
            Pattern.compile("(?i).*(power|exponent|\\^|raised\\s+to).*\\d+.*\\d+.*",
                    Pattern.CASE_INSENSITIVE),
            Pattern.compile("(?i).*(square\\s+root|sqrt|√).*\\d+.*", Pattern.CASE_INSENSITIVE),
            Pattern.compile("(?i).*(cube\\s+root|cbrt|∛).*\\d+.*", Pattern.CASE_INSENSITIVE),
            Pattern.compile("(?i).*(factorial|!).*\\d+.*", Pattern.CASE_INSENSITIVE),
            Pattern.compile("(?i).*(absolute\\s+value|abs|\\|).*\\d+.*", Pattern.CASE_INSENSITIVE),
            Pattern.compile("(?i).*(modulo|mod|remainder|%).*\\d+.*\\d+.*",
                    Pattern.CASE_INSENSITIVE),
            Pattern.compile("(?i).*(logarithm|log|ln|natural\\s+log).*\\d+.*",
                    Pattern.CASE_INSENSITIVE),

            // Trigonometric functions
            Pattern.compile("(?i).*(sin|sine)\\s*\\(?.*\\d+.*\\)?.*", Pattern.CASE_INSENSITIVE),
            Pattern.compile("(?i).*(cos|cosine)\\s*\\(?.*\\d+.*\\)?.*", Pattern.CASE_INSENSITIVE),
            Pattern.compile("(?i).*(tan|tangent)\\s*\\(?.*\\d+.*\\)?.*", Pattern.CASE_INSENSITIVE),
            Pattern.compile("(?i).*(asin|arcsine|inverse\\s+sine)\\s*\\(?.*\\d+.*\\)?.*",
                    Pattern.CASE_INSENSITIVE),
            Pattern.compile("(?i).*(acos|arccosine|inverse\\s+cosine)\\s*\\(?.*\\d+.*\\)?.*",
                    Pattern.CASE_INSENSITIVE),
            Pattern.compile("(?i).*(atan|arctangent|inverse\\s+tangent)\\s*\\(?.*\\d+.*\\)?.*",
                    Pattern.CASE_INSENSITIVE), Pattern.compile(
                    "(?i).*(degrees?\\s+to\\s+radians?|radians?\\s+to\\s+degrees?|deg\\s+to\\s+rad|rad\\s+to\\s+deg).*\\d+.*",
                    Pattern.CASE_INSENSITIVE),

            // Geometric calculations
            Pattern.compile("(?i).*(area.*(circle|circular)|circle.*area).*\\d+.*",
                    Pattern.CASE_INSENSITIVE), Pattern.compile(
                    "(?i).*(circumference.*(circle|circular)|circle.*circumference|perimeter.*circle).*\\d+.*",
                    Pattern.CASE_INSENSITIVE),
            Pattern.compile("(?i).*(area.*(rectangle|rectangular)|rectangle.*area).*\\d+.*\\d+.*",
                    Pattern.CASE_INSENSITIVE),
            Pattern.compile("(?i).*(area.*(triangle|triangular)|triangle.*area).*\\d+.*\\d+.*",
                    Pattern.CASE_INSENSITIVE),
            Pattern.compile("(?i).*(area.*(square)|square.*area|side.*square).*\\d+.*",
                    Pattern.CASE_INSENSITIVE),
            Pattern.compile("(?i).*(volume.*(sphere|spherical)|sphere.*volume).*\\d+.*",
                    Pattern.CASE_INSENSITIVE),
            Pattern.compile("(?i).*(volume.*(cube|cubic)|cube.*volume).*\\d+.*",
                    Pattern.CASE_INSENSITIVE),
            Pattern.compile("(?i).*(volume.*(cylinder|cylindrical)|cylinder.*volume).*\\d+.*\\d+.*",
                    Pattern.CASE_INSENSITIVE),

            // Statistical operations
            Pattern.compile("(?i).*(average|mean).*\\d+.*\\d+.*", Pattern.CASE_INSENSITIVE),
            Pattern.compile("(?i).*(median).*\\d+.*\\d+.*", Pattern.CASE_INSENSITIVE),
            Pattern.compile("(?i).*(standard\\s+deviation|std\\s+dev).*\\d+.*",
                    Pattern.CASE_INSENSITIVE),
            Pattern.compile("(?i).*(variance).*\\d+.*", Pattern.CASE_INSENSITIVE),

            // Utility functions
            Pattern.compile("(?i).*(maximum|max|largest|highest).*\\d+.*\\d+.*",
                    Pattern.CASE_INSENSITIVE),
            Pattern.compile("(?i).*(minimum|min|smallest|lowest).*\\d+.*\\d+.*",
                    Pattern.CASE_INSENSITIVE),
            Pattern.compile("(?i).*(round|rounding).*\\d+.*", Pattern.CASE_INSENSITIVE),
            Pattern.compile("(?i).*(ceiling|ceil|round\\s+up).*\\d+.*", Pattern.CASE_INSENSITIVE),
            Pattern.compile("(?i).*(floor|round\\s+down).*\\d+.*", Pattern.CASE_INSENSITIVE),
            Pattern.compile("(?i).*(percentage|percent|%).*\\d+.*", Pattern.CASE_INSENSITIVE),

            // Mathematical expressions and equations
            Pattern.compile(".*\\d+\\s*[+\\-*/^%]\\s*\\d+.*", Pattern.CASE_INSENSITIVE),
            Pattern.compile("(?i).*calculate.*\\d+.*", Pattern.CASE_INSENSITIVE),
            Pattern.compile("(?i).*compute.*\\d+.*", Pattern.CASE_INSENSITIVE),
            Pattern.compile("(?i).*solve.*\\d+.*[+\\-*/^%=].*", Pattern.CASE_INSENSITIVE),
            Pattern.compile("(?i).*what.*is.*\\d+.*[+\\-*/^%].*\\d+.*", Pattern.CASE_INSENSITIVE),
            Pattern.compile("(?i).*how\\s+much.*\\d+.*[+\\-*/^%].*\\d+.*",
                    Pattern.CASE_INSENSITIVE),
            Pattern.compile("(?i).*(equation|formula).*\\d+.*", Pattern.CASE_INSENSITIVE),

            // Number operations
            Pattern.compile("(?i).*(prime\\s+number|is.*prime).*\\d+.*", Pattern.CASE_INSENSITIVE),
            Pattern.compile("(?i).*(even|odd).*\\d+.*", Pattern.CASE_INSENSITIVE),
            Pattern.compile("(?i).*(fibonacci).*\\d+.*", Pattern.CASE_INSENSITIVE), Pattern.compile(
                    "(?i).*(gcd|greatest\\s+common\\s+divisor|lcm|least\\s+common\\s+multiple).*\\d+.*\\d+.*",
                    Pattern.CASE_INSENSITIVE));

    // Enhanced Weather patterns with more comprehensive coverage for all WeatherService tools
    private static final List<Pattern> WEATHER_PATTERNS = Arrays.asList(
            // Basic weather queries
            Pattern.compile("(?i).*(weather|temperature|temp).*", Pattern.CASE_INSENSITIVE),
            Pattern.compile("(?i).*(forecast|prediction).*", Pattern.CASE_INSENSITIVE),
            Pattern.compile("(?i).*(climate|atmospheric).*", Pattern.CASE_INSENSITIVE),

            // Current weather patterns
            Pattern.compile("(?i).*(current\\s+weather|weather\\s+now|weather\\s+right\\s+now).*",
                    Pattern.CASE_INSENSITIVE),
            Pattern.compile("(?i).*(how.*hot|how.*cold|how.*warm|how.*cool).*",
                    Pattern.CASE_INSENSITIVE),
            Pattern.compile("(?i).*(degrees|celsius|fahrenheit|kelvin|°C|°F|°K).*",
                    Pattern.CASE_INSENSITIVE),
            Pattern.compile("(?i).*(temperature\\s+in|temp\\s+in|weather\\s+in).*",
                    Pattern.CASE_INSENSITIVE),

            // Detailed forecast patterns
            Pattern.compile("(?i).*(weather\\s+forecast|forecast.*weather).*",
                    Pattern.CASE_INSENSITIVE),
            Pattern.compile("(?i).*(detailed\\s+forecast|extended\\s+forecast).*",
                    Pattern.CASE_INSENSITIVE),
            Pattern.compile("(?i).*(hourly\\s+forecast|daily\\s+forecast).*",
                    Pattern.CASE_INSENSITIVE),
            Pattern.compile("(?i).*(\\d+\\s+day.*forecast|forecast.*\\d+\\s+day).*",
                    Pattern.CASE_INSENSITIVE),
            Pattern.compile("(?i).*(next\\s+\\d+\\s+days|weather.*next.*days).*",
                    Pattern.CASE_INSENSITIVE),

            // Air quality patterns
            Pattern.compile("(?i).*(air\\s+quality|aqi|air\\s+pollution).*",
                    Pattern.CASE_INSENSITIVE),
            Pattern.compile("(?i).*(pm2\\.5|pm10|particulate\\s+matter).*",
                    Pattern.CASE_INSENSITIVE), Pattern.compile(
                    "(?i).*(ozone|carbon\\s+monoxide|nitrogen\\s+dioxide|sulphur\\s+dioxide).*",
                    Pattern.CASE_INSENSITIVE),
            Pattern.compile("(?i).*(pollutant|pollution\\s+level|air\\s+index).*",
                    Pattern.CASE_INSENSITIVE),

            // Location search patterns
            Pattern.compile("(?i).*(search\\s+location|find\\s+location|location\\s+search).*",
                    Pattern.CASE_INSENSITIVE),
            Pattern.compile("(?i).*(coordinates\\s+for|lat.*lon.*for|geocode).*",
                    Pattern.CASE_INSENSITIVE),
            Pattern.compile("(?i).*(where\\s+is|location\\s+of).*weather.*",
                    Pattern.CASE_INSENSITIVE),

            // Historical weather patterns
            Pattern.compile("(?i).*(historical\\s+weather|past\\s+weather|weather\\s+history).*",
                    Pattern.CASE_INSENSITIVE),
            Pattern.compile("(?i).*(weather\\s+on|weather\\s+for).*\\d{4}-\\d{2}-\\d{2}.*",
                    Pattern.CASE_INSENSITIVE),
            Pattern.compile("(?i).*(weather\\s+between|weather\\s+from).*\\d{4}.*",
                    Pattern.CASE_INSENSITIVE),
            Pattern.compile("(?i).*(last\\s+month|last\\s+year|previous.*weather).*",
                    Pattern.CASE_INSENSITIVE),

            // Marine weather patterns
            Pattern.compile("(?i).*(marine\\s+weather|ocean\\s+weather|sea\\s+weather).*",
                    Pattern.CASE_INSENSITIVE),
            Pattern.compile("(?i).*(wave\\s+height|wave\\s+conditions|swell).*",
                    Pattern.CASE_INSENSITIVE),
            Pattern.compile("(?i).*(sailing\\s+conditions|boating\\s+weather|maritime).*",
                    Pattern.CASE_INSENSITIVE),
            Pattern.compile("(?i).*(sea\\s+state|ocean\\s+conditions).*", Pattern.CASE_INSENSITIVE),

            // Weather conditions (existing patterns)
            Pattern.compile("(?i).*(rain|raining|rainy|precipitation).*", Pattern.CASE_INSENSITIVE),
            Pattern.compile("(?i).*(snow|snowing|snowy|snowfall).*", Pattern.CASE_INSENSITIVE),
            Pattern.compile("(?i).*(sun|sunny|sunshine|clear).*weather.*",
                    Pattern.CASE_INSENSITIVE),
            Pattern.compile("(?i).*(cloud|cloudy|overcast).*", Pattern.CASE_INSENSITIVE),
            Pattern.compile("(?i).*(wind|windy|breeze|breezy).*", Pattern.CASE_INSENSITIVE),
            Pattern.compile("(?i).*(storm|stormy|thunderstorm|lightning).*",
                    Pattern.CASE_INSENSITIVE),
            Pattern.compile("(?i).*(fog|foggy|mist|misty|haze|hazy).*", Pattern.CASE_INSENSITIVE),
            Pattern.compile("(?i).*(humidity|humid|moisture).*", Pattern.CASE_INSENSITIVE),
            Pattern.compile("(?i).*(pressure|barometric).*", Pattern.CASE_INSENSITIVE),
            Pattern.compile("(?i).*(visibility|visual\\s+range).*", Pattern.CASE_INSENSITIVE),
            Pattern.compile("(?i).*(uv\\s+index|ultraviolet).*", Pattern.CASE_INSENSITIVE),

            // Contextual weather queries
            Pattern.compile("(?i).*weather.*(today|tomorrow|this\\s+week|weekend).*",
                    Pattern.CASE_INSENSITIVE),
            Pattern.compile("(?i).*(weather\\s+report|meteorological).*", Pattern.CASE_INSENSITIVE),
            Pattern.compile("(?i).*(should\\s+I.*umbrella|need.*jacket|dress.*weather).*",
                    Pattern.CASE_INSENSITIVE),

            // Coordinate-based weather queries
            Pattern.compile("(?i).*(latitude|longitude|coordinates?).*weather.*",
                    Pattern.CASE_INSENSITIVE),
            Pattern.compile("(?i).*weather.*(latitude|longitude|coordinates?).*",
                    Pattern.CASE_INSENSITIVE),
            Pattern.compile("(?i).*(\\d+\\.\\d+.*\\d+\\.\\d+|lat.*lon).*weather.*",
                    Pattern.CASE_INSENSITIVE));

    public FilterResult filterMessage(String message) {
        // Input validation with detailed logging
        if (message == null) {
            logger.warn("Received null message for filtering");
            return new FilterResult(false, "Null message received", "NONE", "");
        }

        if (message.trim().isEmpty()) {
            logger.warn("Received empty message for filtering");
            return new FilterResult(false, "Empty message received", "NONE", message);
        }

        String trimmedMessage = message.trim();
        String sessionId = MDC.get("sessionId");

        // Enhanced logging with session context
        logger.info("Starting message filtering - Session: {}, Message length: {}, Preview: '{}'",
                sessionId, trimmedMessage.length(), trimmedMessage.length() > 50 ?
                        trimmedMessage.substring(0, 50) + "..." :
                        trimmedMessage);

        try {
            // Check for math operations with enhanced pattern matching
            FilterResult mathResult = checkMathPatterns(trimmedMessage);
            if (mathResult.canBeHandledByMcp()) {
                logger.info("Math operation detected - Session: {}, Tool: {}, Confidence: HIGH",
                        sessionId, mathResult.toolType());
                return mathResult;
            }

            // Check for weather queries with enhanced pattern matching
            FilterResult weatherResult = checkWeatherPatterns(trimmedMessage);
            if (weatherResult.canBeHandledByMcp()) {
                logger.info("Weather query detected - Session: {}, Tool: {}, Confidence: HIGH",
                        sessionId, weatherResult.toolType());
                return weatherResult;
            }


            // Default to LLM processing
            logger.info("No MCP tool match found, routing to LLM: {}", trimmedMessage);
            return new FilterResult(false, "No matching MCP tool found", "LLM", trimmedMessage);
        } catch (Exception e) {
            logger.error("Error during message filtering - Session: {}, Message: '{}', Error: {}",
                    sessionId, trimmedMessage, e.getMessage(), e);
            return new FilterResult(false, "Filter processing error: " + e.getMessage(), "ERROR",
                    trimmedMessage);
        }
    }

    private FilterResult checkMathPatterns(String message) {
        logger.debug("Checking math patterns for message: '{}'", message);

        for (int i = 0; i < MATH_PATTERNS.size(); i++) {
            Pattern pattern = MATH_PATTERNS.get(i);
            Matcher matcher = pattern.matcher(message);

            if (matcher.matches()) {
                String patternType = determineMathPatternType(i);
                logger.debug("Math pattern match found - Pattern index: {}, Type: {}, Pattern: {}",
                        i, patternType, pattern.pattern());

                String processedMessage = preprocessMathMessage(message, patternType);
                return new FilterResult(true, "Math operation detected: " + patternType, "MATH",
                        processedMessage);
            }
        }

        logger.debug("No math patterns matched for message");
        return new FilterResult(false, "No math patterns matched", "NONE", message);
    }

    private FilterResult checkWeatherPatterns(String message) {
        logger.debug("Checking weather patterns for message: '{}'", message);

        for (int i = 0; i < WEATHER_PATTERNS.size(); i++) {
            Pattern pattern = WEATHER_PATTERNS.get(i);
            Matcher matcher = pattern.matcher(message);

            if (matcher.matches()) {
                String patternType = determineWeatherPatternType(i);
                logger.debug(
                        "Weather pattern match found - Pattern index: {}, Type: {}, Pattern: {}", i,
                        patternType, pattern.pattern());

                String processedMessage = preprocessWeatherMessage(message, patternType);
                return new FilterResult(true, "Weather query detected: " + patternType, "WEATHER",
                        processedMessage);
            }
        }

        logger.debug("No weather patterns matched for message");
        return new FilterResult(false, "No weather patterns matched", "NONE", message);
    }

    private String determineMathPatternType(int patternIndex) {
        if (patternIndex <= 3)
            return "Basic Arithmetic";
        if (patternIndex <= 10)
            return "Advanced Math";
        if (patternIndex <= 16)
            return "Trigonometry";
        if (patternIndex <= 23)
            return "Geometry";
        if (patternIndex <= 27)
            return "Statistics";
        if (patternIndex <= 32)
            return "Utility Functions";
        if (patternIndex <= 40)
            return "Mathematical Expressions";
        return "Number Operations";
    }

    private String determineWeatherPatternType(int patternIndex) {
        if (patternIndex <= 2)
            return "General Weather";
        if (patternIndex <= 6)
            return "Current Weather";
        if (patternIndex <= 11)
            return "Detailed Forecast";
        if (patternIndex <= 15)
            return "Air Quality";
        if (patternIndex <= 17)
            return "Location Search";
        if (patternIndex <= 21)
            return "Historical Weather";
        if (patternIndex <= 25)
            return "Marine Weather";
        if (patternIndex <= 35)
            return "Weather Conditions";
        if (patternIndex <= 38)
            return "Contextual Weather";
        return "Coordinate-based Weather";
    }

    private String preprocessMathMessage(String message, String mathType) {
        logger.debug("Preprocessing math message - Type: {}, Original: '{}'", mathType, message);

        String processedMessage = String.format(
                "Please perform this mathematical calculation using the available math tools. " + "Operation type: %s. User request: %s",
                mathType, message);

        logger.debug("Math message preprocessed: '{}'", processedMessage);
        return processedMessage;
    }

    private String preprocessWeatherMessage(String message, String weatherType) {
        logger.debug("Preprocessing weather message - Type: {}, Original: '{}'", weatherType,
                message);

        String processedMessage;
        boolean hasCoordinates = message.toLowerCase().contains("latitude") || message.toLowerCase()
                .contains("longitude") || message.matches(".*\\d+\\.\\d+.*\\d+\\.\\d+.*");

        boolean hasDateRange = message.matches("(?i).*\\d{4}-\\d{2}-\\d{2}.*");

        switch (weatherType) {
            case "Current Weather" -> {
                if (hasCoordinates) {
                    processedMessage = String.format(
                            "Please get the current weather conditions using getCurrentWeather tool. " + "User request: %s",
                            message);
                } else {
                    processedMessage = String.format(
                            "Please search for the location first using searchLocation tool, then get current weather. " + "User request: %s",
                            message);
                }
            }
            case "Detailed Forecast" -> {
                if (hasCoordinates) {
                    processedMessage = String.format(
                            "Please get the detailed weather forecast using getDetailedForecast tool. " + "User request: %s",
                            message);
                } else {
                    processedMessage = String.format(
                            "Please search for the location first using searchLocation tool, then get detailed forecast. " + "User request: %s",
                            message);
                }
            }
            case "Air Quality" -> {
                if (hasCoordinates) {
                    processedMessage = String.format(
                            "Please get the air quality information using getAirQuality tool. " + "User request: %s",
                            message);
                } else {
                    processedMessage = String.format(
                            "Please search for the location first using searchLocation tool, then get air quality data. " + "User request: %s",
                            message);
                }
            }
            case "Location Search" -> {
                processedMessage = String.format(
                        "Please search for the location using searchLocation tool. " + "User request: %s",
                        message);
            }
            case "Historical Weather" -> {
                if (hasCoordinates && hasDateRange) {
                    processedMessage = String.format(
                            "Please get historical weather data using getHistoricalWeather tool. " + "User request: %s",
                            message);
                } else if (hasCoordinates) {
                    processedMessage = String.format(
                            "Please get historical weather data using getHistoricalWeather tool. " + "Ask user for specific date range if not provided. User request: %s",
                            message);
                } else {
                    processedMessage = String.format(
                            "Please search for the location first using searchLocation tool, then get historical weather data. " + "Ask user for date range if not provided. User request: %s",
                            message);
                }
            }
            case "Marine Weather" -> {
                if (hasCoordinates) {
                    processedMessage = String.format(
                            "Please get marine weather conditions using getMarineWeather tool. " + "User request: %s",
                            message);
                } else {
                    processedMessage = String.format(
                            "Please search for the coastal/marine location first using searchLocation tool, then get marine weather. " + "User request: %s",
                            message);
                }
            }
            default -> {
                // General weather queries
                if (hasCoordinates) {
                    processedMessage = String.format(
                            "Please get the weather information using the appropriate weather tool. " + "Query type: %s. User request: %s",
                            weatherType, message);
                } else {
                    processedMessage = String.format(
                            "Please search for the location first using searchLocation tool, then get weather information. " + "Query type: %s. User request: %s",
                            weatherType, message);
                }
            }
        }

        logger.debug("Weather message preprocessed: '{}'", processedMessage);
        return processedMessage;
    }
}
