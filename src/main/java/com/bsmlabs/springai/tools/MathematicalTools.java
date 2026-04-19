package com.bsmlabs.springai.tools;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;

public class MathematicalTools {

    @Tool(description = "Adds two numbers and returns the result.")
    double add(
            @ToolParam(description = "First number") double a,
            @ToolParam(description = "Second number") double b) {
        return a + b;
    }

    @Tool(description = "Subtracts the second number from the first and returns the result.")
    double subtract(
            @ToolParam(description = "Number to subtract from") double a,
            @ToolParam(description = "Number to subtract") double b) {
        return a - b;
    }

    @Tool(description = "Multiplies two numbers and returns the result.")
    double multiply(
            @ToolParam(description = "First number") double a,
            @ToolParam(description = "Second number") double b) {
        return a * b;
    }

    @Tool(description = "Divides the first number by the second. Returns an error if dividing by zero.")
    String divide(
            @ToolParam(description = "Dividend") double a,
            @ToolParam(description = "Divisor") double b) {
        if (b == 0) return "Error: cannot divide by zero.";
        return String.valueOf(a / b);
    }

    @Tool(description = "Returns the remainder when the first number is divided by the second (modulo).")
    String modulo(
            @ToolParam(description = "Dividend") double a,
            @ToolParam(description = "Divisor") double b) {
        if (b == 0) return "Error: cannot divide by zero.";
        return String.valueOf(a % b);
    }

    @Tool(description = "Raises a base number to the power of an exponent.")
    double power(
            @ToolParam(description = "Base number") double base,
            @ToolParam(description = "Exponent") double exponent) {
        return Math.pow(base, exponent);
    }

    @Tool(description = "Returns the square root of a number. Returns an error for negative input.")
    String squareRoot(
            @ToolParam(description = "The number to find the square root of") double number) {
        if (number < 0) return "Error: cannot take square root of a negative number.";
        return String.valueOf(Math.sqrt(number));
    }

    @Tool(description = "Returns the absolute value of a number (removes the negative sign).")
    double absoluteValue(
            @ToolParam(description = "The number") double number) {
        return Math.abs(number);
    }

    @Tool(description = "Rounds a number to a specified number of decimal places.")
    double round(
            @ToolParam(description = "The number to round") double number,
            @ToolParam(description = "Number of decimal places (0 for whole number)") int decimalPlaces) {
        double scale = Math.pow(10, decimalPlaces);
        return Math.round(number * scale) / scale;
    }

    @Tool(description = "Returns the larger of two numbers.")
    double max(
            @ToolParam(description = "First number") double a,
            @ToolParam(description = "Second number") double b) {
        return Math.max(a, b);
    }

    @Tool(description = "Returns the smaller of two numbers.")
    double min(
            @ToolParam(description = "First number") double a,
            @ToolParam(description = "Second number") double b) {
        return Math.min(a, b);
    }

    @Tool(description = "Calculates the percentage of a value. E.g. what is 20% of 150?")
    double percentage(
            @ToolParam(description = "The percentage value (e.g. 20 for 20%)") double percent,
            @ToolParam(description = "The total value") double total) {
        return (percent / 100.0) * total;
    }
}