package com.chatapp.backend.dto.response;

@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public recor DeliveryStatusSummary (
     int totalRecipients,
     int readCount,
     int deliveredCount,
     Set<UserSummaryResponse> unreadRecipients,
){}