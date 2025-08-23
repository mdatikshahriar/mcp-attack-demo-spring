package com.example.server.tools;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;

@Component
public class MathTools {

    private static final Logger logger = LoggerFactory.getLogger(MathTools.class);

    public MathTools() {
        logger.info("MathTools component initialized with {} available mathematical operations",
                28);
        logger.debug(
                "Available categories: Basic Arithmetic, Advanced Math, Trigonometry, Geometry, Utility Functions");
    }

    @Tool(description = "Adds two numbers")
    public String sumNumbers(int number1, int number2) {
        logger.debug("Addition requested: {} + {}", number1, number2);

        try {
            int result = number1 + number2;
            logger.trace("Addition calculation: {} + {} = {}", number1, number2, result);
            return String.format("The sum of %d and %d is %d", number1, number2, result);

        } catch (Exception e) {
            logger.error("Error calculating sum of {} and {}", number1, number2, e);
            return "Error: Failed to calculate sum";
        }
    }

    @Tool(description = "Multiplies two numbers")
    public String multiplyNumbers(int number1, int number2) {
        logger.debug("Multiplication requested: {} × {}", number1, number2);

        try {
            int result = number1 * number2;
            logger.trace("Multiplication calculation: {} × {} = {}", number1, number2, result);
            return String.format("The product of %d and %d is %d", number1, number2, result);

        } catch (Exception e) {
            logger.error("Error calculating product of {} and {}", number1, number2, e);
            return "Error: Failed to calculate product";
        }
    }

    @Tool(description = "Divide two numbers")
    public String divideNumbers(double number1, double number2) {
        logger.debug("Division requested: {} ÷ {}", number1, number2);

        if (number2 == 0) {
            logger.warn("Division by zero attempted: {} ÷ 0", number1);
            return "Error: Cannot divide by zero";
        }

        try {
            double result = number1 / number2;
            logger.trace("Division calculation: {} ÷ {} = {}", number1, number2, result);
            return String.format("The result of %.2f divided by %.2f is %.2f", number1, number2,
                    result);

        } catch (Exception e) {
            logger.error("Error calculating division of {} by {}", number1, number2, e);
            return "Error: Failed to calculate division";
        }
    }

    @Tool(description = "Subtracts second number from first number")
    public String subtractNumbers(int number1, int number2) {
        logger.debug("Subtraction requested: {} - {}", number1, number2);

        try {
            int result = number1 - number2;
            logger.trace("Subtraction calculation: {} - {} = {}", number1, number2, result);
            return String.format("The result of %d minus %d is %d", number1, number2, result);

        } catch (Exception e) {
            logger.error("Error calculating subtraction of {} and {}", number1, number2, e);
            return "Error: Failed to calculate subtraction";
        }
    }

    @Tool(description = "Calculates the power of a number (base^exponent)")
    public String powerOf(double base, double exponent) {
        logger.debug("Power calculation requested: {} ^ {}", base, exponent);

        try {
            double result = Math.pow(base, exponent);

            if (Double.isInfinite(result)) {
                logger.warn("Power calculation resulted in infinity: {} ^ {}", base, exponent);
                return "Error: Result is too large (infinity)";
            }

            if (Double.isNaN(result)) {
                logger.warn("Power calculation resulted in NaN: {} ^ {}", base, exponent);
                return "Error: Invalid calculation (NaN result)";
            }

            logger.trace("Power calculation: {} ^ {} = {}", base, exponent, result);
            return String.format("%.2f raised to the power of %.2f equals %.2f", base, exponent,
                    result);

        } catch (Exception e) {
            logger.error("Error calculating power of {} raised to {}", base, exponent, e);
            return "Error: Failed to calculate power";
        }
    }

    @Tool(description = "Calculates the square root of a number")
    public String squareRoot(double number) {
        logger.debug("Square root requested: √{}", number);

        if (number < 0) {
            logger.warn("Square root of negative number attempted: √{}", number);
            return "Error: Cannot calculate square root of negative number";
        }

        try {
            double result = Math.sqrt(number);
            logger.trace("Square root calculation: √{} = {}", number, result);
            return String.format("The square root of %.2f is %.2f", number, result);

        } catch (Exception e) {
            logger.error("Error calculating square root of {}", number, e);
            return "Error: Failed to calculate square root";
        }
    }

    @Tool(description = "Calculates the absolute value of a number")
    public String absoluteValue(double number) {
        logger.debug("Absolute value requested: |{}|", number);

        try {
            double result = Math.abs(number);
            logger.trace("Absolute value calculation: |{}| = {}", number, result);
            return String.format("The absolute value of %.2f is %.2f", number, result);

        } catch (Exception e) {
            logger.error("Error calculating absolute value of {}", number, e);
            return "Error: Failed to calculate absolute value";
        }
    }

    @Tool(description = "Calculates the factorial of a non-negative integer")
    public String factorial(int number) {
        logger.debug("Factorial requested: {}!", number);

        if (number < 0) {
            logger.warn("Factorial of negative number attempted: {}!", number);
            return "Error: Factorial is not defined for negative numbers";
        }

        if (number > 20) {
            logger.warn("Factorial of large number attempted: {}! (limit is 20)", number);
            return "Error: Factorial calculation limited to numbers <= 20 to prevent overflow";
        }

        try {
            long result = 1;
            for (int i = 2; i <= number; i++) {
                result *= i;
            }

            logger.trace("Factorial calculation: {}! = {}", number, result);
            return String.format("The factorial of %d is %d", number, result);

        } catch (Exception e) {
            logger.error("Error calculating factorial of {}", number, e);
            return "Error: Failed to calculate factorial";
        }
    }

    @Tool(description = "Calculates the remainder when dividing two numbers")
    public String modulo(double number1, double number2) {
        logger.debug("Modulo requested: {} mod {}", number1, number2);

        if (number2 == 0) {
            logger.warn("Modulo with zero divisor attempted: {} mod 0", number1);
            return "Error: Cannot perform modulo with zero divisor";
        }

        try {
            double result = number1 % number2;
            logger.trace("Modulo calculation: {} mod {} = {}", number1, number2, result);
            return String.format("%.2f modulo %.2f equals %.2f", number1, number2, result);

        } catch (Exception e) {
            logger.error("Error calculating modulo of {} and {}", number1, number2, e);
            return "Error: Failed to calculate modulo";
        }
    }

    @Tool(description = "Calculates the natural logarithm of a number")
    public String naturalLog(double number) {
        logger.debug("Natural logarithm requested: ln({})", number);

        if (number <= 0) {
            logger.warn("Natural log of non-positive number attempted: ln({})", number);
            return "Error: Logarithm is not defined for non-positive numbers";
        }

        try {
            double result = Math.log(number);
            logger.trace("Natural log calculation: ln({}) = {}", number, result);
            return String.format("The natural logarithm of %.2f is %.4f", number, result);

        } catch (Exception e) {
            logger.error("Error calculating natural log of {}", number, e);
            return "Error: Failed to calculate natural logarithm";
        }
    }

    @Tool(description = "Calculates the base-10 logarithm of a number")
    public String log10(double number) {
        logger.debug("Base-10 logarithm requested: log₁₀({})", number);

        if (number <= 0) {
            logger.warn("Base-10 log of non-positive number attempted: log₁₀({})", number);
            return "Error: Logarithm is not defined for non-positive numbers";
        }

        try {
            double result = Math.log10(number);
            logger.trace("Base-10 log calculation: log₁₀({}) = {}", number, result);
            return String.format("The base-10 logarithm of %.2f is %.4f", number, result);

        } catch (Exception e) {
            logger.error("Error calculating base-10 log of {}", number, e);
            return "Error: Failed to calculate base-10 logarithm";
        }
    }

    @Tool(description = "Calculates the sine of an angle in radians")
    public String sine(double angleRadians) {
        logger.debug("Sine calculation requested: sin({})", angleRadians);

        try {
            double result = Math.sin(angleRadians);
            logger.trace("Sine calculation: sin({}) = {}", angleRadians, result);
            return String.format("The sine of %.4f radians is %.4f", angleRadians, result);

        } catch (Exception e) {
            logger.error("Error calculating sine of {}", angleRadians, e);
            return "Error: Failed to calculate sine";
        }
    }

    @Tool(description = "Calculates the cosine of an angle in radians")
    public String cosine(double angleRadians) {
        logger.debug("Cosine calculation requested: cos({})", angleRadians);

        try {
            double result = Math.cos(angleRadians);
            logger.trace("Cosine calculation: cos({}) = {}", angleRadians, result);
            return String.format("The cosine of %.4f radians is %.4f", angleRadians, result);

        } catch (Exception e) {
            logger.error("Error calculating cosine of {}", angleRadians, e);
            return "Error: Failed to calculate cosine";
        }
    }

    @Tool(description = "Calculates the tangent of an angle in radians")
    public String tangent(double angleRadians) {
        logger.debug("Tangent calculation requested: tan({})", angleRadians);

        try {
            double result = Math.tan(angleRadians);

            if (Double.isInfinite(result)) {
                logger.warn("Tangent calculation resulted in infinity: tan({})", angleRadians);
                return "Error: Tangent is undefined (approaches infinity)";
            }

            logger.trace("Tangent calculation: tan({}) = {}", angleRadians, result);
            return String.format("The tangent of %.4f radians is %.4f", angleRadians, result);

        } catch (Exception e) {
            logger.error("Error calculating tangent of {}", angleRadians, e);
            return "Error: Failed to calculate tangent";
        }
    }

    @Tool(description = "Converts degrees to radians")
    public String degreesToRadians(double degrees) {
        logger.debug("Degrees to radians conversion requested: {}° → radians", degrees);

        try {
            double result = Math.toRadians(degrees);
            logger.trace("Conversion: {}° = {} radians", degrees, result);
            return String.format("%.2f degrees equals %.4f radians", degrees, result);

        } catch (Exception e) {
            logger.error("Error converting {} degrees to radians", degrees, e);
            return "Error: Failed to convert degrees to radians";
        }
    }

    @Tool(description = "Converts radians to degrees")
    public String radiansToDegrees(double radians) {
        logger.debug("Radians to degrees conversion requested: {} radians → degrees", radians);

        try {
            double result = Math.toDegrees(radians);
            logger.trace("Conversion: {} radians = {}°", radians, result);
            return String.format("%.4f radians equals %.2f degrees", radians, result);

        } catch (Exception e) {
            logger.error("Error converting {} radians to degrees", radians, e);
            return "Error: Failed to convert radians to degrees";
        }
    }

    @Tool(description = "Finds the maximum of two numbers")
    public String maximum(double number1, double number2) {
        logger.debug("Maximum requested: max({}, {})", number1, number2);

        try {
            double result = Math.max(number1, number2);
            logger.trace("Maximum calculation: max({}, {}) = {}", number1, number2, result);
            return String.format("The maximum of %.2f and %.2f is %.2f", number1, number2, result);

        } catch (Exception e) {
            logger.error("Error finding maximum of {} and {}", number1, number2, e);
            return "Error: Failed to find maximum";
        }
    }

    @Tool(description = "Finds the minimum of two numbers")
    public String minimum(double number1, double number2) {
        logger.debug("Minimum requested: min({}, {})", number1, number2);

        try {
            double result = Math.min(number1, number2);
            logger.trace("Minimum calculation: min({}, {}) = {}", number1, number2, result);
            return String.format("The minimum of %.2f and %.2f is %.2f", number1, number2, result);

        } catch (Exception e) {
            logger.error("Error finding minimum of {} and {}", number1, number2, e);
            return "Error: Failed to find minimum";
        }
    }

    @Tool(description = "Rounds a number to the nearest integer")
    public String roundNumber(double number) {
        logger.debug("Rounding requested: round({})", number);

        try {
            long result = Math.round(number);
            logger.trace("Rounding calculation: round({}) = {}", number, result);
            return String.format("%.2f rounded to the nearest integer is %d", number, result);

        } catch (Exception e) {
            logger.error("Error rounding {}", number, e);
            return "Error: Failed to round number";
        }
    }

    @Tool(description = "Rounds a number up to the nearest integer")
    public String ceiling(double number) {
        logger.debug("Ceiling requested: ceil({})", number);

        try {
            double result = Math.ceil(number);
            logger.trace("Ceiling calculation: ceil({}) = {}", number, result);
            return String.format("%.2f rounded up (ceiling) is %.0f", number, result);

        } catch (Exception e) {
            logger.error("Error calculating ceiling of {}", number, e);
            return "Error: Failed to calculate ceiling";
        }
    }

    @Tool(description = "Rounds a number down to the nearest integer")
    public String floor(double number) {
        logger.debug("Floor requested: floor({})", number);

        try {
            double result = Math.floor(number);
            logger.trace("Floor calculation: floor({}) = {}", number, result);
            return String.format("%.2f rounded down (floor) is %.0f", number, result);

        } catch (Exception e) {
            logger.error("Error calculating floor of {}", number, e);
            return "Error: Failed to calculate floor";
        }
    }

    @Tool(description = "Calculates the area of a circle given its radius")
    public String circleArea(double radius) {
        logger.debug("Circle area requested: radius = {}", radius);

        if (radius < 0) {
            logger.warn("Circle area with negative radius attempted: {}", radius);
            return "Error: Radius cannot be negative";
        }

        try {
            double result = Math.PI * radius * radius;
            logger.trace("Circle area calculation: π × {}² = {}", radius, result);
            return String.format("The area of a circle with radius %.2f is %.2f square units",
                    radius, result);

        } catch (Exception e) {
            logger.error("Error calculating circle area with radius {}", radius, e);
            return "Error: Failed to calculate circle area";
        }
    }

    @Tool(description = "Calculates the circumference of a circle given its radius")
    public String circleCircumference(double radius) {
        logger.debug("Circle circumference requested: radius = {}", radius);

        if (radius < 0) {
            logger.warn("Circle circumference with negative radius attempted: {}", radius);
            return "Error: Radius cannot be negative";
        }

        try {
            double result = 2 * Math.PI * radius;
            logger.trace("Circle circumference calculation: 2π × {} = {}", radius, result);
            return String.format("The circumference of a circle with radius %.2f is %.2f units",
                    radius, result);

        } catch (Exception e) {
            logger.error("Error calculating circle circumference with radius {}", radius, e);
            return "Error: Failed to calculate circle circumference";
        }
    }

    @Tool(description = "Calculates the area of a rectangle given width and height")
    public String rectangleArea(double width, double height) {
        logger.debug("Rectangle area requested: width = {}, height = {}", width, height);

        if (width < 0 || height < 0) {
            logger.warn(
                    "Rectangle area with negative dimensions attempted: width = {}, height = {}",
                    width, height);
            return "Error: Width and height cannot be negative";
        }

        try {
            double result = width * height;
            logger.trace("Rectangle area calculation: {} × {} = {}", width, height, result);
            return String.format(
                    "The area of a rectangle with width %.2f and height %.2f is %.2f square units",
                    width, height, result);

        } catch (Exception e) {
            logger.error("Error calculating rectangle area with width {} and height {}", width,
                    height, e);
            return "Error: Failed to calculate rectangle area";
        }
    }

    @Tool(description = "Calculates the area of a triangle given base and height")
    public String triangleArea(double base, double height) {
        logger.debug("Triangle area requested: base = {}, height = {}", base, height);

        if (base < 0 || height < 0) {
            logger.warn("Triangle area with negative dimensions attempted: base = {}, height = {}",
                    base, height);
            return "Error: Base and height cannot be negative";
        }

        try {
            double result = 0.5 * base * height;
            logger.trace("Triangle area calculation: 0.5 × {} × {} = {}", base, height, result);
            return String.format(
                    "The area of a triangle with base %.2f and height %.2f is %.2f square units",
                    base, height, result);

        } catch (Exception e) {
            logger.error("Error calculating triangle area with base {} and height {}", base, height,
                    e);
            return "Error: Failed to calculate triangle area";
        }
    }
}
