package com.example.client.model;

public record ToolDescription(String toolName, String toolDescription) {

    @Override
    public String toString() {
        return String.format("Tool: %s -> %s", toolName, toolDescription);
    }
}
