package com.chatapp.backend.entity.enums;

import lombok.Getter;

// Add an enum to track all possible states
@Getter
public enum MessageDeliveryState {
    SENT(1),
    RECEIVED(2),    // Message received by server
    DELIVERED(3),   // Message delivered to recipient's device
    READ(4);        // Message read by recipient

    private final int order;

    MessageDeliveryState(int order) {
        this.order = order;
    }
}
