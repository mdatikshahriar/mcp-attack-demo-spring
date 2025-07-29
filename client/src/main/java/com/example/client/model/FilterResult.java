package com.example.client.model;

import lombok.Getter;

public record FilterResult(boolean canBeHandledByMcp, @Getter String reason,
                           @Getter String toolType, @Getter String processedMessage) {

    @Override
    public String toString() {
        return String.format("FilterResult{canBeHandledByMcp=%s, reason='%s', toolType='%s'}",
                canBeHandledByMcp, reason, toolType);
    }
}
