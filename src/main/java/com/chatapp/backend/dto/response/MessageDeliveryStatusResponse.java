package com.chatapp.backend.dto.response;

import com.chatapp.backend.entity.enums.MessageStatus;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;

import java.time.Instant;

@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public record MessageDeliveryStatusResponse(Long messageDeliveryStatusId,
                                            UserResponse recipient,
                                            MessageStatus status,
                                            Instant statusTimestamp) {
}