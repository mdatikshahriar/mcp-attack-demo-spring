package com.example.server.tools;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;

@Component
public class MathTools {

    private static final Logger logger = LoggerFactory.getLogger(MathTools.class);

    public MathTools() {
        logger.info("MathTools initialized");
    }

    @Tool(description = "Adds two numbers")
    public String sumNumbers(int number1, int number2) {
        int result = number1 + number2;
        return String.format("The sum of %d and %d is %d", number1, number2, result);
    }

    @Tool(description = "Multiplies two numbers")
    public String multiplyNumbers(int number1, int number2) {
        int result = number1 * number2;
        return String.format("The product of %d and %d is %d", number1, number2, result);
    }

    @Tool(description = "Divide two numbers")
    public String divideNumbers(double number1, double number2) {
        if (number2 == 0) {
            return "Error: Cannot divide by zero";
        }
        double result = number1 / number2;
        return String.format("The result of %.2f divided by %.2f is %.2f", number1, number2,
                result);
    }

    @Tool(description = "Subtracts second number from first number")
    public String subtractNumbers(int number1, int number2) {
        int result = number1 - number2;
        return String.format("The result of %d minus %d is %d", number1, number2, result);
    }

    @Tool(description = "Calculates the power of a number (base^exponent)")
    public String powerOf(double base, double exponent) {
        double result = Math.pow(base, exponent);
        return String.format("%.2f raised to the power of %.2f equals %.2f", base, exponent,
                result);
    }

    @Tool(description = "Calculates the square root of a number")
    public String squareRoot(double number) {
        if (number < 0) {
            return "Error: Cannot calculate square root of negative number";
        }
        double result = Math.sqrt(number);
        return String.format("The square root of %.2f is %.2f", number, result);
    }

    @Tool(description = "Calculates the absolute value of a number")
    public String absoluteValue(double number) {
        double result = Math.abs(number);
        return String.format("The absolute value of %.2f is %.2f", number, result);
    }

    @Tool(description = "Calculates the factorial of a non-negative integer")
    public String factorial(int number) {
        if (number < 0) {
            return "Error: Factorial is not defined for negative numbers";
        }
        if (number > 20) {
            return "Error: Factorial calculation limited to numbers <= 20 to prevent overflow";
        }

        long result = 1;
        for (int i = 2; i <= number; i++) {
            result *= i;
        }
        return String.format("The factorial of %d is %d", number, result);
    }

    @Tool(description = "Calculates the remainder when dividing two numbers")
    public String modulo(double number1, double number2) {
        if (number2 == 0) {
            return "Error: Cannot perform modulo with zero divisor";
        }
        double result = number1 % number2;
        return String.format("%.2f modulo %.2f equals %.2f", number1, number2, result);
    }

    @Tool(description = "Calculates the natural logarithm of a number")
    public String naturalLog(double number) {
        if (number <= 0) {
            return "Error: Logarithm is not defined for non-positive numbers";
        }
        double result = Math.log(number);
        return String.format("The natural logarithm of %.2f is %.4f", number, result);
    }

    @Tool(description = "Calculates the base-10 logarithm of a number")
    public String log10(double number) {
        if (number <= 0) {
            return "Error: Logarithm is not defined for non-positive numbers";
        }
        double result = Math.log10(number);
        return String.format("The base-10 logarithm of %.2f is %.4f", number, result);
    }

    @Tool(description = "Calculates the sine of an angle in radians")
    public String sine(double angleRadians) {
        double result = Math.sin(angleRadians);
        return String.format("The sine of %.4f radians is %.4f", angleRadians, result);
    }

    @Tool(description = "Calculates the cosine of an angle in radians")
    public String cosine(double angleRadians) {
        double result = Math.cos(angleRadians);
        return String.format("The cosine of %.4f radians is %.4f", angleRadians, result);
    }

    @Tool(description = "Calculates the tangent of an angle in radians")
    public String tangent(double angleRadians) {
        double result = Math.tan(angleRadians);
        return String.format("The tangent of %.4f radians is %.4f", angleRadians, result);
    }

    @Tool(description = "Converts degrees to radians")
    public String degreesToRadians(double degrees) {
        double result = Math.toRadians(degrees);
        return String.format("%.2f degrees equals %.4f radians", degrees, result);
    }

    @Tool(description = "Converts radians to degrees")
    public String radiansToDegrees(double radians) {
        double result = Math.toDegrees(radians);
        return String.format("%.4f radians equals %.2f degrees", radians, result);
    }

    @Tool(description = "Finds the maximum of two numbers")
    public String maximum(double number1, double number2) {
        double result = Math.max(number1, number2);
        return String.format("The maximum of %.2f and %.2f is %.2f", number1, number2, result);
    }

    @Tool(description = "Finds the minimum of two numbers")
    public String minimum(double number1, double number2) {
        double result = Math.min(number1, number2);
        return String.format("The minimum of %.2f and %.2f is %.2f", number1, number2, result);
    }

    @Tool(description = "Rounds a number to the nearest integer")
    public String roundNumber(double number) {
        long result = Math.round(number);
        return String.format("%.2f rounded to the nearest integer is %d", number, result);
    }

    @Tool(description = "Rounds a number up to the nearest integer")
    public String ceiling(double number) {
        double result = Math.ceil(number);
        return String.format("%.2f rounded up (ceiling) is %.0f", number, result);
    }

    @Tool(description = "Rounds a number down to the nearest integer")
    public String floor(double number) {
        double result = Math.floor(number);
        return String.format("%.2f rounded down (floor) is %.0f", number, result);
    }

    @Tool(description = "Calculates the area of a circle given its radius")
    public String circleArea(double radius) {
        if (radius < 0) {
            return "Error: Radius cannot be negative";
        }
        double result = Math.PI * radius * radius;
        return String.format("The area of a circle with radius %.2f is %.2f square units", radius,
                result);
    }

    @Tool(description = "Calculates the circumference of a circle given its radius")
    public String circleCircumference(double radius) {
        if (radius < 0) {
            return "Error: Radius cannot be negative";
        }
        double result = 2 * Math.PI * radius;
        return String.format("The circumference of a circle with radius %.2f is %.2f units", radius,
                result);
    }

    @Tool(description = "Calculates the area of a rectangle given width and height")
    public String rectangleArea(double width, double height) {
        if (width < 0 || height < 0) {
            return "Error: Width and height cannot be negative";
        }
        double result = width * height;
        return String.format(
                "The area of a rectangle with width %.2f and height %.2f is %.2f square units",
                width, height, result);
    }

    @Tool(description = "Calculates the area of a triangle given base and height")
    public String triangleArea(double base, double height) {
        if (base < 0 || height < 0) {
            return "Error: Base and height cannot be negative";
        }
        double result = 0.5 * base * height;
        return String.format(
                "The area of a triangle with base %.2f and height %.2f is %.2f square units", base,
                height, result);
    }
}
