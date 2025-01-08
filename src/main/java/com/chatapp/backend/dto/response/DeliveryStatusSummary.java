package com.chatapp.backend.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Builder;
import java.util.Set;

@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public record DeliveryStatusSummary (
     int totalRecipients,
     int readCount,
     int deliveredCount,
     Set<UserResponse> unreadRecipients
){}